package fr.meteociel.activity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import fr.meteociel.om.ReportObservation;
import fr.meteociel.util.HttpUtils;

/**
 * Activite de report des observations (upload image + selection observation)
 * 
 * @author A512568
 * 
 */
public class ReportObservationActivity extends Activity {

	private static final int SELECT_PHOTO = 100;

	/**
	 * Description de l'observation
	 */
	private static final String DESC_OBSERVATION = "DESC_OBSERVATION";

	/**
	 * Image de l'observation
	 */
	private static final String IMG_OBSERVATION = "IMG_OBSERVATION";

	/**
	 * Valeur meteociel de l'observation
	 */
	private static final String VALUE_OBSERVATION = "VALUE_OBSERVATION";

	/**
	 * Objet stateful representant le report d'observation
	 */
	private ReportObservation reportObservation = new ReportObservation();

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.report);

		// Parcours de la liste des types d'observations et des images
		// dans le fichier res/values/array.xml et ajout à la
		// liste passée à l'adapter
		List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();

		String[] listTypeObs = getResources().getStringArray(
				R.array.observations);
		TypedArray imgs = getResources().obtainTypedArray(
				R.array.observations_img);
		final String[] listValueObs = getResources().getStringArray(
				R.array.observations_value);

		if (listTypeObs.length != imgs.length()
				|| listTypeObs.length != listValueObs.length) {
			throw new RuntimeException(
					"Problème de configuration du fichier array.xml: "
							+ "les tailles de la liste du texte et des "
							+ "images sont différentes.");
		}

		for (int i = 0; i < listTypeObs.length; i++) {

			HashMap<String, Object> map = new HashMap<String, Object>(2);
			map.put(DESC_OBSERVATION, listTypeObs[i]);
			map.put(IMG_OBSERVATION, imgs.getResourceId(i, -1));
			map.put(VALUE_OBSERVATION, listValueObs[i]);
			list.add(map);

		}

		// Rempli la liste déroulante des types d'observations
		Spinner spin = (Spinner) findViewById(R.id.selectObservation);

		spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {
				reportObservation.setValue(listValueObs[pos]);
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		SpinnerObservationAdapter adapter = new SpinnerObservationAdapter(
				getApplicationContext(), list, R.layout.type_observation,
				new String[] { DESC_OBSERVATION, IMG_OBSERVATION,
						VALUE_OBSERVATION },
				new int[] { R.id.text, R.id.image });

		spin.setAdapter(adapter);

		// Boite de dialogue login
		final Dialog dialog = creerDialogLogin();

		// Gestion du bouton de soumission du formulaire
		Button soumettre = (Button) findViewById(R.id.soumettreObservation);
		soumettre.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AutoCompleteTextView actv = (AutoCompleteTextView) findViewById(R.id.textObservation);
				reportObservation.setTexte(actv.getText().toString());

				// Affichage de la boite de dialogue pour le login / pwd
				dialog.show();

				soumettreFormulaireMeteociel(reportObservation);
			}
		});
	}

	/**
	 * Permet d'afficher le contenu de la gallerie d'images du téléphone
	 * 
	 * @param v
	 */
	public void afficherGallerie(View v) {
		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		photoPickerIntent.setType("image/*");
		startActivityForResult(photoPickerIntent, SELECT_PHOTO);
	}

	/**
	 * Prend une photo et la socke sous forme de fichier
	 * 
	 * @param v
	 */
	public void prendrePhoto(View v) {

		// define the file-name to save photo taken by Camera activity
		String fileName = Calendar.getInstance().getTimeInMillis() + ".jpg";
		// create parameters for Intent with filename
		ContentValues values = new ContentValues();
		values.put(MediaStore.Images.Media.TITLE, fileName);
		values.put(MediaStore.Images.Media.DESCRIPTION,
				"Image capture by camera");
		reportObservation.setImageUri(getContentResolver().insert(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values));
		// create new Intent
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT,
				reportObservation.getImageUri());
		intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
		startActivityForResult(intent, SELECT_PHOTO);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent imageReturnedIntent) {
		super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

		switch (requestCode) {
		case SELECT_PHOTO:
			if (resultCode == RESULT_OK) {
				Uri selectedImage = null;
				if (imageReturnedIntent == null) { // Ca vient de l'appareil
													// photo
					selectedImage = reportObservation.getImageUri();
				} else { // Ca vient de la gallerie
					selectedImage = imageReturnedIntent.getData();
				}

				InputStream imageStream;
				try {
					imageStream = getContentResolver().openInputStream(
							selectedImage);
				} catch (FileNotFoundException e) {
					throw new RuntimeException(e);
				}
				Bitmap yourSelectedImage = BitmapFactory
						.decodeStream(imageStream);
				ImageView v = (ImageView) findViewById(R.id.selectedImage);
				v.setImageBitmap(yourSelectedImage);

			}

		}
	}

	private class SpinnerObservationAdapter extends SimpleAdapter {

		public SpinnerObservationAdapter(Context context,
				List<? extends Map<String, ?>> data, int resource,
				String[] from, int[] to) {
			super(context, data, resource, from, to);

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				convertView = getLayoutInflater().inflate(
						R.layout.type_observation, null);
			}

			HashMap<String, Object> data = (HashMap<String, Object>) getItem(position);

			String texteObservation = (String) data.get(DESC_OBSERVATION);
			int imgId = (Integer) data.get(IMG_OBSERVATION);

			((TextView) convertView.findViewById(R.id.text))
					.setText(texteObservation);
			((ImageView) convertView.findViewById(R.id.image))
					.setImageResource(imgId);

			return convertView;
		}

	}

	/**
	 * Méthode permettant de soumettre un report d'observation au site meteociel
	 * 
	 * @param reportObservation
	 *            le report d'observation à soumettre
	 */
	private void soumettreFormulaireMeteociel(
			ReportObservation reportObservation) {
		String url = "http://meteociel.fr/temps-reel/observation_valide.php";
		
		// Ajout des paramètres
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("Login", reportObservation.getUser()));
		params.add(new BasicNameValuePair("Pass", reportObservation.getPassword()));
		params.add(new BasicNameValuePair("heure", reportObservation.getHeure()));
		params.add(new BasicNameValuePair("RadioGroup2", reportObservation.getLieu()));
		params.add(new BasicNameValuePair("Commentaire", reportObservation.getTexte()));
		params.add(new BasicNameValuePair("RadioGroup", reportObservation.getValue()));
		
		
		HttpUtils.postRequest(url, params);
	}

	/**
	 * Création de la boite de dialog de login
	 * 
	 * @return la boite de dialogue
	 */
	private Dialog creerDialogLogin() {
		final Dialog dialog = new Dialog(this);

		dialog.setContentView(R.layout.login_dialog);
		dialog.setTitle(R.string.authentification);

		// bouton ok
		Button loginOK = (Button) dialog.findViewById(R.id.validation_login);
		loginOK.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				TextView login = (TextView) dialog.findViewById(R.id.login);
				TextView password = (TextView) dialog.findViewById(R.id.password);
				reportObservation.setUser(login.getText().toString());
				reportObservation.setPassword(password.getText().toString());
				
				
				soumettreFormulaireMeteociel(reportObservation);
			}
		});

		// bouton ok
		Button loginCancel = (Button) dialog.findViewById(R.id.cancel_login);
		loginCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.cancel();
			}
		});

		return dialog;
	}

}
