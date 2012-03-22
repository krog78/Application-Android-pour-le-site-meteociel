package fr.meteociel.activity;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import fr.meteociel.R;
import fr.meteociel.exception.SoumissionFormulaireException;
import fr.meteociel.om.ReportObservation;
import fr.meteociel.util.MeteocielUtils;

/**
 * Activite de report des observations (upload image + selection observation)
 * 
 * @author A512568
 * 
 */
public class ReportObservationActivity extends AbstractMeteocielActivity {



	private static final String HEURE_SUFFIX = ":00";

	private static final int SELECT_PHOTO = 100;

	/**
	 * Compteur utilisé pour savoir si on est à l'initialisation ou à la
	 * sélection dans le spinner du type de l'observation
	 */
	private boolean spinnerInit = false;

	/**
	 * Objet stateful representant le report d'observation
	 */
	private ReportObservation reportObservation = new ReportObservation();

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.report);

		// On rempli l'heure par défaut l'heure courante
		CheckedTextView v = (CheckedTextView) findViewById(R.id.heureObservation);
		v.setText(getString(R.string.heure_observation) + " "
				+ Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
				+ HEURE_SUFFIX);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		String action = intent.getAction();

		// if this is from the share menu
		if (Intent.ACTION_SEND.equals(action)) {
			if (extras.containsKey(Intent.EXTRA_STREAM)) {
				// Get resource path from intent callee
				Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
				reportObservation.setImageUri(uri);
				reportObservation.setPathImage(getPath(uri));
				setImageView(uri);
			}
		}

		// Création du spinner
		MeteocielUtils.createSpinner(this);

		// Gestion du bouton de soumission du formulaire
		Button soumettre = (Button) findViewById(R.id.soumettreObservation);
		soumettre.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AutoCompleteTextView actv = (AutoCompleteTextView) findViewById(R.id.textObservation);
				reportObservation.setTexte(actv.getText().toString());

				// Affichage de la boite de dialogue pour le login / pwd
				// Si les préférences ne sont pas déjà renseignées
				SharedPreferences settings = getSharedPreferences(MeteocielUtils.PREFS_NAME, 0);
				String login = settings.getString(MeteocielUtils.PREF_LOGIN, "");
				String password = settings.getString(MeteocielUtils.PREF_PWD, "");

				if (login.isEmpty() || password.isEmpty()) {
					// Boite de dialogue login
					Dialog dialogLogin = creerDialogLogin();
					dialogLogin.show();
				} else {
					reportObservation.setUser(login);
					reportObservation.setPassword(password);

					asyncTask = new EnvoiObservationTask();
					asyncTask.execute(reportObservation);
				}

			}
		});
	}

	/**
	 * Prepare une alerte
	 * 
	 * @return l'alerte correspondante
	 */
	public AlertDialog prepareAlert(int idLayout) {
		// Création de l'AlertDialog
		AlertDialog.Builder adb = new AlertDialog.Builder(
				ReportObservationActivity.this);
		View alertDialogView = getLayoutInflater().inflate(idLayout, null);
		adb.setView(alertDialogView);
		final AlertDialog alert = adb.create();
		alert.show();
		return alert;
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
					reportObservation.setImageUri(selectedImage);
					reportObservation.setPathImage(getPath(selectedImage));
				}

				setImageView(selectedImage);

			}

		}
	}

	/**
	 * Création de la boite de dialog de login
	 * 
	 * @return la boite de dialogue
	 */
	private Dialog creerDialogLogin() {
		final Dialog dialog = new Dialog(ReportObservationActivity.this);

		dialog.setContentView(R.layout.login_dialog);
		dialog.setTitle(R.string.authentification);

		// bouton ok
		Button loginOK = (Button) dialog.findViewById(R.id.validation_login);
		loginOK.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				TextView login = (TextView) dialog.findViewById(R.id.login);
				TextView password = (TextView) dialog
						.findViewById(R.id.password);
				reportObservation.setUser(login.getText().toString());
				reportObservation.setPassword(password.getText().toString());
				
				asyncTask = new EnvoiObservationTask();
				asyncTask.execute(reportObservation);

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

	/**
	 * Convertit un URI en path
	 * 
	 * @param uri
	 *            l'uri du fichier
	 * @return le chemin du fichier
	 */
	private String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		startManagingCursor(cursor);
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	/**
	 * Rempli le champ image de la vue
	 * 
	 * @param selectedImage
	 *            l'image sélectionnée
	 */
	private void setImageView(Uri selectedImage) {
		InputStream imageStream;
		try {
			imageStream = getContentResolver().openInputStream(selectedImage);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		Bitmap yourSelectedImage = BitmapFactory.decodeStream(imageStream);
		ImageView v = (ImageView) findViewById(R.id.selectedImage);
		v.setImageBitmap(yourSelectedImage);
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
		reportObservation
				.setPathImage(getPath(reportObservation.getImageUri()));
		// create new Intent
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT,
				reportObservation.getImageUri());
		intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
		startActivityForResult(intent, SELECT_PHOTO);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.observations_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.change_location: // On change le lieu
			final AlertDialog alerteLieu = prepareAlert(R.layout.lieu);
			final Button okLieu = (Button) alerteLieu.findViewById(R.id.ok);
			okLieu.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					EditText ville = (EditText) alerteLieu
							.findViewById(R.id.ville);
					reportObservation.getLieu().setVille(
							ville.getText().toString());

					EditText altitude = (EditText) alerteLieu
							.findViewById(R.id.altitude);
					reportObservation.getLieu().setAltitude(
							altitude.getText().toString());

					alerteLieu.cancel();
				}
			});
			return true;
		case R.id.change_date: // Modification de l'heure de l'observation
			int heureCourante = Calendar.getInstance()
					.get(Calendar.HOUR_OF_DAY);

			final List<String> timeList = new ArrayList<String>(6);
			for (int i = 0; i < 6; i++) {
				timeList.add(heureCourante - i + HEURE_SUFFIX);
			}

			final CharSequence[] items = timeList
					.toArray(new CharSequence[timeList.size()]);

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.choix_heure);
			builder.setItems(items, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					reportObservation.setHeure(item);
					CheckedTextView v = (CheckedTextView) findViewById(R.id.heureObservation);
					v.setText(getString(R.string.heure_observation) + " "
							+ timeList.get(item));
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Tache permettant d'envoyer le report en arrière plan.
	 * 
	 * @author ippon
	 * 
	 */
	private class EnvoiObservationTask extends
			AsyncTask<ReportObservation, Integer, Long> {

		ProgressDialog dialogProgress;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			ReportObservationActivity.this.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					dialogProgress = ProgressDialog.show(
							ReportObservationActivity.this, "",
							getString(R.string.envoi), true);

				}
			});

		}

		@Override
		protected Long doInBackground(ReportObservation... reportObservations) {
			try {
				MeteocielUtils.soumettreFormulaireMeteociel(
						ReportObservationActivity.this, reportObservations[0]);
			} catch (SoumissionFormulaireException e) {
				return new Long(1);
			}
			return new Long(0);
		}

		@Override
		protected void onPostExecute(Long result) {
			super.onPostExecute(result);
			if (result == 1) {
				showFormError();
				dialogProgress.dismiss();
			} else {
				if (!this.isCancelled()) { // On n'a pas cancellé la tache sur
											// une erreur
					ReportObservationActivity.this
							.runOnUiThread(new Runnable() {

								@Override
								public void run() {
									dialogProgress.dismiss();
									Toast toast = Toast.makeText(
											ReportObservationActivity.this
													.getApplicationContext(),
											R.string.report_effectue, 1);
									toast.setGravity(Gravity.CENTER, 0, 0);
									toast.show();

								}
							});
					ReportObservationActivity.this.finish();
				} else {
					ReportObservationActivity.this
							.runOnUiThread(new Runnable() {

								@Override
								public void run() {
									dialogProgress.dismiss();
								}
							});
				}
			}

		}
	}

	public ReportObservation getReportObservation() {
		return reportObservation;
	}

	public void setReportObservation(ReportObservation reportObservation) {
		this.reportObservation = reportObservation;
	}

	public boolean isSpinnerInit() {
		return spinnerInit;
	}

	public void setSpinnerInit(boolean spinnerInit) {
		this.spinnerInit = spinnerInit;
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

}
