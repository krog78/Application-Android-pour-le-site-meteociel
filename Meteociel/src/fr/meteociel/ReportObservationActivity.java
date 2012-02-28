package fr.meteociel;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.omg.CORBA.NO_MEMORY;

import fr.meteociel.util.ObservationsEnum;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Activité de report des observations (upload image + sélection observation)
 * @author A512568
 *
 */
public class ReportObservationActivity extends Activity {

	private static final int SELECT_PHOTO = 100;
	
	private static final String DESC_OBSERVATION = "DESC_OBSERVATION";
	
	private static final String IMG_OBSERVATION = "IMG_OBSERVATION";

	private Uri imageUri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.report);
		
		ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> map = new HashMap<String, Object>();
        
        
        map.put(DESC_OBSERVATION, ObservationsEnum.ENSOLEILLE.text());
        map.put(IMG_OBSERVATION, ObservationsEnum.ENSOLEILLE.image());
        list.add(map);

        map = new HashMap<String, Object>();
        map.put(DESC_OBSERVATION, ObservationsEnum.NUAGES_EPARS.text());
        map.put(IMG_OBSERVATION, ObservationsEnum.NUAGES_EPARS.image());
        list.add(map);

        Spinner spin = (Spinner) findViewById(R.id.selectObservation);
        SpinnerObservationAdapter adapter = new SpinnerObservationAdapter(getApplicationContext(), list,
                R.layout.type_observation, new String[] { DESC_OBSERVATION, IMG_OBSERVATION },
                new int[] { R.id.text, R.id.image });

        spin.setAdapter(adapter);
		
		

	}

	/**
	 * Permet d'afficher le contenu de la gallerie d'images du téléphone
	 * @param v
	 */
	public void afficherGallerie(View v) {
		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		photoPickerIntent.setType("image/*");
		startActivityForResult(photoPickerIntent, SELECT_PHOTO);
	}

	/**
	 * Prend une photo et la socke sous forme de fichier
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
		imageUri = getContentResolver().insert(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
		// create new Intent
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
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
				if(imageReturnedIntent == null){ // Ca vient de l'appareil photo
					selectedImage = imageUri;
				}else{ // Ca vient de la gallerie		
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

        public SpinnerObservationAdapter(Context context, List<? extends Map<String, ?>> data,
                int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

        	if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.type_observation,
                        null);
            }
        	
            HashMap<String, Object> data = (HashMap<String, Object>) getItem(position);

            String texteObservation = (String)data.get(DESC_OBSERVATION);
            int imgId = (Integer)data.get(IMG_OBSERVATION);
            
            ((TextView) convertView.findViewById(R.id.text))
                    .setText(texteObservation);
            ((ImageView) convertView.findViewById(R.id.image))
                    .setImageResource(imgId);

            return convertView;
        }

    }

}
