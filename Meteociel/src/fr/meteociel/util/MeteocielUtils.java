package fr.meteociel.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;

import fr.meteociel.om.ReportObservation;

/**
 * Classe utilitaire permettant d'effectuer des manipulations sur le site
 * météociel
 * 
 * @author ippon
 * 
 */
public class MeteocielUtils {
	/**
	 * Méthode permettant de soumettre un report d'observation au site meteociel
	 * 
	 * @param reportObservation
	 *            le report d'observation à soumettre
	 */
	public static final void soumettreFormulaireMeteociel(Activity activity,
			ReportObservation reportObservation) {

		loginMeteociel(activity, reportObservation);
		
		soumettreImageMeteociel(reportObservation);

		String url = "http://meteociel.fr/temps-reel/observation_valide.php";

		// Ajout des paramètres
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("Login", reportObservation.getUser()));
		params.add(new BasicNameValuePair("Pass", reportObservation
				.getPassword()));
		params.add(new BasicNameValuePair("heure", String
				.valueOf(reportObservation.getHeure())));
		params.add(new BasicNameValuePair("RadioGroup2", reportObservation
				.getLieu()));
		params.add(new BasicNameValuePair("Commentaire", reportObservation
				.getTexte()));
		params.add(new BasicNameValuePair("RadioGroup", reportObservation
				.getValue()));
		
		// Si une image est renseignée on récupère son id
		if(!reportObservation.getPathImage().isEmpty()){
			params.add(new BasicNameValuePair("imageid", getImageIdMeteociel(activity)));
		}

		HttpUtils.postRequest(activity, url, params);
	}

	/**
	 * Méthode permettant de soumettre une image au site Meteociel
	 * 
	 * @param reportObservation
	 */
	public static final void soumettreImageMeteociel(ReportObservation reportObservation) {
		String url = "http://images.meteociel.fr/image_envoi.php";

		if (!reportObservation.getPathImage().isEmpty()) {

			// Ajout des paramètres
			HttpUtils.uploadImageRequest(url, new ArrayList<NameValuePair>(),
					reportObservation.getPathImage());
		}
	}

	/**
	 * Méthode de récupération du dernier image id posté
	 */
	public static final String getImageIdMeteociel(Activity activity) {
		String url = "http://www.meteociel.fr/temps-reel/selectimage.php";

		HttpResponse response = HttpUtils.getRequest(activity, url);
		String html = HttpUtils.httpResponseToString(response);
		String imageid = StringUtils.substringBetween(html, "javascript:selectImage(", ",");
		return imageid;
	}
	
	/**
	 * Méthode de login au site météociel
	 * 
	 * @param reportObservation
	 *            le report de l'observation
	 */
	public static final void loginMeteociel(Activity activity, ReportObservation reportObservation) {

		String url = "http://www.meteociel.fr/connexion.php";

		// Ajout des paramètres
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("Login", reportObservation.getUser()));
		params.add(new BasicNameValuePair("Pass", reportObservation
				.getPassword()));
		params.add(new BasicNameValuePair("expire", "on"));

		HttpUtils.postRequest(activity, url, params);
	}
}
