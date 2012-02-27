package fr.meteociel;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.SimpleExpandableListAdapter;

public class ReportObservationActivity extends Activity {

	private static final int SELECT_PHOTO = 100;

	private Uri imageUri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.report);
		
		ExpandableListView expView = (ExpandableListView) findViewById(R.id.selectObservation);
		
		

	}

	/**
	 * Permet d'afficher le contenu de la gallerie d'images du t�l�phone
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

}
