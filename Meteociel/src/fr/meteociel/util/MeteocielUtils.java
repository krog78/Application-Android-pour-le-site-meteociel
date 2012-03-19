package fr.meteociel.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import fr.meteociel.R;
import fr.meteociel.activity.ReportObservationActivity;
import fr.meteociel.om.Gresil;
import fr.meteociel.om.Neige;
import fr.meteociel.om.Pluie;
import fr.meteociel.om.ReportObservation;
import fr.meteociel.om.Temperature;
import fr.meteociel.om.Vent;
import fr.meteociel.om.Visibilite;

/**
 * Classe utilitaire permettant d'effectuer des manipulations sur le site
 * météociel
 * 
 * @author ippon
 * 
 */
public class MeteocielUtils {

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

		switch (reportObservation.getTypeChampSupp()) {
		case VENT:
			params.add(new BasicNameValuePair("VentValue",
					((Vent) reportObservation).getVitesse()));
			params.add(new BasicNameValuePair("VentDir",
					((Vent) reportObservation).getDirection()));
			break;

		case PLUIE_MM:
			params.add(new BasicNameValuePair("PluieValue",
					((Pluie) reportObservation).getHauteur()));
			break;
		case NEIGE_CM:
			params.add(new BasicNameValuePair("NeigeValue",
					((Neige) reportObservation).getHauteur()));
			break;
		case GRESIL_MM:
			params.add(new BasicNameValuePair("GresilValue",
					((Gresil) reportObservation).getHauteur()));
			break;
		case TEMPERATURE:
			params.add(new BasicNameValuePair("TempValue",
					((Temperature) reportObservation).getDegres()));
			break;

		case VISIBILITE:
			params.add(new BasicNameValuePair("VisValue",
					((Visibilite) reportObservation).getLongueur()));
			break;

		}

		// Si une image est renseignée on récupère son id
		if (!reportObservation.getPathImage().isEmpty()) {
			params.add(new BasicNameValuePair("imageid",
					getImageIdMeteociel(activity)));
		}

		HttpUtils.postRequest(activity, url, params);
	}

	/**
	 * Méthode permettant de soumettre une image au site Meteociel
	 * 
	 * @param reportObservation
	 */
	public static final void soumettreImageMeteociel(
			ReportObservation reportObservation) {
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
		String imageid = StringUtils.substringBetween(html,
				"javascript:selectImage(", ",");
		return imageid;
	}

	/**
	 * Méthode de login au site météociel
	 * 
	 * @param reportObservation
	 *            le report de l'observation
	 */
	public static final void loginMeteociel(Activity activity,
			ReportObservation reportObservation) {

		String url = "http://www.meteociel.fr/connexion.php";

		// Ajout des paramètres
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("Login", reportObservation.getUser()));
		params.add(new BasicNameValuePair("Pass", reportObservation
				.getPassword()));
		params.add(new BasicNameValuePair("expire", "on"));

		HttpUtils.postRequest(activity, url, params);
	}

	/**
	 * Méthode de création du spinner de sélection du type d'observation
	 * 
	 * @param reportObservationActivity
	 *            le report d'observation
	 * @return le spinner créé
	 */
	public static final Spinner createSpinner(
			final ReportObservationActivity reportObservationActivity) {

		// Parcours de la liste des types d'observations et des images
		// dans le fichier res/values/array.xml et ajout à la
		// liste passée à l'adapter
		List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();

		String[] listTypeObs = reportObservationActivity.getResources()
				.getStringArray(R.array.observations);
		TypedArray imgs = reportObservationActivity.getResources()
				.obtainTypedArray(R.array.observations_img);
		final String[] listValueObs = reportObservationActivity.getResources()
				.getStringArray(R.array.observations_value);
		final String[] listChampsObs = reportObservationActivity.getResources()
				.getStringArray(R.array.observations_champs);

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
		final Spinner spin = (Spinner) reportObservationActivity
				.findViewById(R.id.selectObservation);

		spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {
				reportObservationActivity.getReportObservation().setValue(
						listValueObs[pos]);

				// Initialisation de la boite de dialogue pour les champs
				// supplémentaires si le champs est renseigné dans le array.xml
				if (reportObservationActivity.isSpinnerInit()
						&& !listChampsObs[pos].isEmpty()) {

					// On instancie notre layout en tant que View
					TypeChampSupp t = TypeChampSupp.valueOf(listChampsObs[pos]);
					switch (t) {
					case VENT:

						final AlertDialog alerte = reportObservationActivity
								.prepareAlert(R.layout.vent);
						final Button button = (Button) alerte
								.findViewById(R.id.ok);
						button.setOnClickListener(new View.OnClickListener() {
							public void onClick(View v) {
								Vent vent = new Vent(reportObservationActivity
										.getReportObservation(),
										((TextView) alerte
												.findViewById(R.id.vitesse))
												.getText().toString(),
										((CharSequence) ((Spinner) alerte
												.findViewById(R.id.direction))
												.getSelectedItem()).toString());
								reportObservationActivity
										.setReportObservation(vent);
								alerte.cancel();
							}
						});
						break;
					case PLUIE_MM:

						final AlertDialog alertPluie = reportObservationActivity
								.prepareAlert(R.layout.texte_libelle);
						TextView libelleView = (TextView) alertPluie
								.findViewById(R.id.libelle);
						libelleView.setText(reportObservationActivity
								.getString(R.string.mm));
						final Button buttonPluie = (Button) alertPluie
								.findViewById(R.id.ok);
						buttonPluie
								.setOnClickListener(new View.OnClickListener() {
									public void onClick(View v) {
										Pluie pluie = new Pluie(
												reportObservationActivity
														.getReportObservation(),
												((TextView) alertPluie
														.findViewById(R.id.texte))
														.getText().toString());
										reportObservationActivity
												.setReportObservation(pluie);
										alertPluie.cancel();
									}
								});
						break;
					case NEIGE_CM:
						final AlertDialog alertNeige = reportObservationActivity
								.prepareAlert(R.layout.texte_libelle);
						TextView libelleViewNeige = (TextView) alertNeige
								.findViewById(R.id.libelle);
						libelleViewNeige.setText(reportObservationActivity
								.getString(R.string.cm));
						final Button buttonNeige = (Button) alertNeige
								.findViewById(R.id.ok);
						buttonNeige
								.setOnClickListener(new View.OnClickListener() {
									public void onClick(View v) {
										Neige neige = new Neige(
												reportObservationActivity
														.getReportObservation(),
												((TextView) alertNeige
														.findViewById(R.id.texte))
														.getText().toString());
										reportObservationActivity
												.setReportObservation(neige);
										alertNeige.cancel();
									}
								});
						break;
					case GRESIL_MM:
						final AlertDialog alertGresil = reportObservationActivity
								.prepareAlert(R.layout.texte_libelle);
						TextView libelleViewGresil = (TextView) alertGresil
								.findViewById(R.id.libelle);
						libelleViewGresil.setText(reportObservationActivity
								.getString(R.string.mm));
						final Button buttonGresil = (Button) alertGresil
								.findViewById(R.id.ok);
						buttonGresil
								.setOnClickListener(new View.OnClickListener() {
									public void onClick(View v) {
										Gresil gresil = new Gresil(
												reportObservationActivity
														.getReportObservation(),
												((TextView) alertGresil
														.findViewById(R.id.texte))
														.getText().toString());
										reportObservationActivity
												.setReportObservation(gresil);
										alertGresil.cancel();
									}
								});
						break;
					case TEMPERATURE:
						final AlertDialog alertTemp = reportObservationActivity
								.prepareAlert(R.layout.texte_libelle);

						TextView libelleViewTemp = (TextView) alertTemp
								.findViewById(R.id.libelle);
						libelleViewTemp.setText(reportObservationActivity
								.getString(R.string.degres));
						final Button buttonTemp = (Button) alertTemp
								.findViewById(R.id.ok);
						buttonTemp
								.setOnClickListener(new View.OnClickListener() {
									public void onClick(View v) {
										Temperature temperature = new Temperature(
												reportObservationActivity
														.getReportObservation(),
												((TextView) alertTemp
														.findViewById(R.id.texte))
														.getText().toString());
										reportObservationActivity
												.setReportObservation(temperature);
										alertTemp.cancel();
									}
								});
						break;
					case VISIBILITE:
						final AlertDialog alertVisi = reportObservationActivity
								.prepareAlert(R.layout.texte_libelle);

						TextView libelleViewVisi = (TextView) alertVisi
								.findViewById(R.id.libelle);
						libelleViewVisi.setText(reportObservationActivity
								.getString(R.string.visibilite));
						final Button buttonVisi = (Button) alertVisi
								.findViewById(R.id.ok);
						buttonVisi
								.setOnClickListener(new View.OnClickListener() {
									public void onClick(View v) {
										Visibilite visibilite = new Visibilite(
												reportObservationActivity
														.getReportObservation(),
												((TextView) alertVisi
														.findViewById(R.id.texte))
														.getText().toString());
										reportObservationActivity
												.setReportObservation(visibilite);
										alertVisi.cancel();
									}
								});
						break;
					}

				} else {
					reportObservationActivity.setSpinnerInit(true);
				}

			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		final class SpinnerObservationAdapter extends SimpleAdapter {

			public SpinnerObservationAdapter(Context context,
					List<? extends Map<String, ?>> data, int resource,
					String[] from, int[] to) {
				super(context, data, resource, from, to);

			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {

				if (convertView == null) {
					convertView = reportObservationActivity.getLayoutInflater()
							.inflate(R.layout.type_observation, null);
				}

				HashMap<String, Object> data = (HashMap<String, Object>) getItem(position);

				String texteObservation = (String) data
						.get(MeteocielUtils.DESC_OBSERVATION);
				int imgId = (Integer) data.get(MeteocielUtils.IMG_OBSERVATION);

				((TextView) convertView.findViewById(R.id.text))
						.setText(texteObservation);
				((ImageView) convertView.findViewById(R.id.image))
						.setImageResource(imgId);

				return convertView;
			}

		}

		SpinnerObservationAdapter adapter = new SpinnerObservationAdapter(
				reportObservationActivity.getApplicationContext(), list,
				R.layout.type_observation, new String[] { DESC_OBSERVATION,
						IMG_OBSERVATION, VALUE_OBSERVATION }, new int[] {
						R.id.text, R.id.image });

		spin.setAdapter(adapter);
		spin.setSelection(19); // Sélection par défaut : Soleil
		return spin;
	}

}
