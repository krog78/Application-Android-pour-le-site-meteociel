package fr.meteociel.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import fr.meteociel.R;
import fr.meteociel.om.ReportObservation;

/**
 * Comportement commun aux activités météociel
 * 
 * @author ippon
 * 
 */
public abstract class AbstractMeteocielActivity extends Activity {

	/**
	 * Tache de fond d'envoi de l'observation
	 */
	protected AsyncTask<ReportObservation, Integer, Long> asyncTask;
	
	
	@Override
	protected void onStart() {		
		super.onStart();
		// On teste si le réseau est disponible
		if(!isNetworkAvailable()){
			showConnectionError();
		}
	}
	
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
	
	/**
	 * Teste si le réseau est disponible
	 * @return
	 */
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null;
	}

	public AsyncTask<ReportObservation, Integer, Long> getAsyncTask() {
		return asyncTask;
	}

	public void setAsyncTask(AsyncTask<ReportObservation, Integer, Long> asyncTask) {
		this.asyncTask = asyncTask;
	}

}
