package fr.meteociel.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import fr.meteociel.R;

/**
 * Comportement commun aux activités météociel
 * 
 * @author ippon
 * 
 */
public abstract class AbstractMeteocielActivity extends Activity {

	/**
	 * Affiche une alerte sur une erreur de connection
	 * 
	 * @param activity
	 */
	public final void showConnectionError() {
		AbstractMeteocielActivity.this.runOnUiThread(new Runnable() {
		    public void run() {
		    	AlertDialog.Builder builder = new AlertDialog.Builder(AbstractMeteocielActivity.this);
				builder.setMessage(R.string.erreur_connexion)
						.setCancelable(false)
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										AbstractMeteocielActivity.this.finish();
									}
								});
				AlertDialog alertError = builder.create();

				alertError.show();
		    }
		});
			
		
	}

}
