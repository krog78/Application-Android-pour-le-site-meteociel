package fr.meteociel.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.ccil.cowan.tagsoup.Parser;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import fr.meteo.meteociel.R;
import fr.meteo.meteociel.activity.AbstractMeteocielActivity;
import fr.meteo.meteociel.activity.PhotosListActivity;
import fr.meteo.meteociel.activity.ReportObservationActivity;
import fr.meteociel.exception.SoumissionFormulaireException;
import fr.meteociel.om.Gresil;
import fr.meteociel.om.Neige;
import fr.meteociel.om.Observation;
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
	 * Préférences de l'appli météociel
	 */
	public static final String PREFS_NAME = "MeteocielPrefs";

	/**
	 * Préférence de login
	 */
	public static final String PREF_LOGIN = "login";

	/**
	 * Préférence de password
	 */
	public static final String PREF_PWD = "password";
	
	/**
	 * URL Report Météociel
	 */
	private static final String METEOCIEL_FEED_URL = "http://www.meteociel.fr/user/day-gallery.php";

	
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
	 * @throws SoumissionFormulaireException 
	 */
	public static final void soumettreFormulaireMeteociel(AbstractMeteocielActivity activity,
			ReportObservation reportObservation) throws SoumissionFormulaireException {

		
		loginMeteociel(activity, reportObservation);

		while(!checkUserLogged(activity, reportObservation)){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		
		soumettreImageMeteociel(reportObservation);

		String url = "http://meteociel.fr/temps-reel/observation_valide.php";

		// Ajout des paramètres
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("Login", reportObservation.getUser()));
		params.add(new BasicNameValuePair("Pass", reportObservation
				.getPassword()));
		params.add(new BasicNameValuePair("heure", String
				.valueOf(reportObservation.getHeure())));
		
		if(reportObservation.getLieu().getVille().isEmpty()){
			params.add(new BasicNameValuePair("RadioGroup2", "Lieu0"));
		}else{
			params.add(new BasicNameValuePair("ville", reportObservation.getLieu().getVille()));
			params.add(new BasicNameValuePair("Altitude", reportObservation.getLieu().getAltitude()));
		}
		
		params.add(new BasicNameValuePair("Commentaire", reportObservation
				.getTexte() + "\n\n"+activity.getString(R.string.rapport_android)));
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

		HttpUtils.postRequest(url, params);
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
	 * Vérification que l'utilisateur est bien loggué
	 */
	private static final boolean checkUserLogged(AbstractMeteocielActivity activity, ReportObservation reportObservation) {
		String url = "http://meteociel.fr/user/control.php";
		
		HttpResponse response = HttpUtils.getRequest(activity, url);
		String html = HttpUtils.httpResponseToString(response);
				
		if(html.contains("Bienvenue sur votre espace personnel")){
			return true;
		}else{
			return false;
		}
	}	
		
	/**
	 * Méthode de récupération du dernier image id posté
	 */
	public static final String getImageIdMeteociel(AbstractMeteocielActivity activity) {
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
	 * @throws SoumissionFormulaireException 
	 */
	public static final void loginMeteociel(AbstractMeteocielActivity activity,
			ReportObservation reportObservation) throws SoumissionFormulaireException {

		String url = "http://www.meteociel.fr/connexion.php";

		// Ajout des paramètres
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("Login", reportObservation.getUser()));
		params.add(new BasicNameValuePair("Pass", reportObservation
				.getPassword()));
		params.add(new BasicNameValuePair("expire", "on"));

		HttpUtils.postRequest(url, params);
		
		// Ajout dans les préférences
		SharedPreferences settings = activity.getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(PREF_LOGIN, reportObservation.getUser());
		editor.putString(PREF_PWD, reportObservation.getPassword());
		// Commit the edits!
		editor.commit();
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
	
	/**
	 * Parse le code de la page html des reports Météociel
	 * 
	 * @return la code retour du parsing
	 */
	public static final long parseHtmlMeteociel(PhotosListActivity activity){
		URL url = null;
		try {
			url = new URL(METEOCIEL_FEED_URL);
		} catch (MalformedURLException e1) {
			throw new RuntimeException(e1);
		}
		XMLReader reader = new Parser();
		try {
			reader.setFeature(Parser.namespacesFeature, false);
		} catch (SAXNotRecognizedException e1) {
			throw new RuntimeException(e1);
		} catch (SAXNotSupportedException e1) {
			throw new RuntimeException(e1);
		}
		try {
			reader.setFeature(Parser.namespacePrefixesFeature, false);
		} catch (SAXNotRecognizedException e1) {
			throw new RuntimeException(e1);
		} catch (SAXNotSupportedException e1) {
			throw new RuntimeException(e1);
		}

		Transformer transformer;
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
		} catch (TransformerConfigurationException e1) {
			throw new RuntimeException(e1);
		} catch (TransformerFactoryConfigurationError e1) {
			throw new RuntimeException(e1);
		}

		DOMResult result = new DOMResult();

		try {
			InputStream is = url.openStream();
			try {
				transformer.transform(new SAXSource(reader,
						new InputSource(is)), result);
			} catch (TransformerException e1) {
				throw new RuntimeException(e1);
			}finally{
				is.close();
			}

		} catch (IOException e1) {
			e1.printStackTrace();
			return new Long(1);
		}

		XPathFactory xpf = XPathFactory.newInstance();
		XPath xpath = xpf.newXPath();

		try {
			NodeList nodeList = (NodeList) xpath.evaluate("//a[@rel='shadowbox']",
					result.getNode(), XPathConstants.NODESET);
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node aNode = nodeList.item(i);
				String urlBigImage = aNode.getAttributes().getNamedItem("href").getNodeValue();
				
				Node node = aNode.getFirstChild();
				NamedNodeMap nodeMap = node.getAttributes();

				// Récupération de l'url de l'image source
				Node nodeSrc = nodeMap.getNamedItem("src");
				String src = nodeSrc.getNodeValue();

				if (src.contains("images.meteociel.fr")) {
					// Récupération du commentaire
					Node nodeCom = nodeMap.getNamedItem("onmouseover");
					String commentaire = nodeCom.getNodeValue();
					String date = commentaire.substring(
							commentaire.indexOf("('") + 2,
							commentaire.indexOf("',"));
					String[] tokens = commentaire.split(",'");
					String strFormat = tokens[1].substring(0,
							tokens[1].length() - 1);

					String titre = strFormat.substring(0,
							strFormat.indexOf("<hr>"));
					String user = strFormat.substring(
							strFormat.indexOf("<hr>"),
							strFormat.indexOf("<br>"));
					String corps = strFormat.substring(
							strFormat.indexOf("<br>"), strFormat.length());

					CharSequence styledTitre = Html.fromHtml(titre + " - "
							+ date + " - " + user);

					CharSequence styledText = Html.fromHtml(corps);

					Observation o = new Observation(
							StringEscapeUtils.unescapeJavaScript(StringEscapeUtils
									.unescapeHtml(styledTitre.toString())),
							StringEscapeUtils
									.unescapeJavaScript(StringEscapeUtils
											.unescapeHtml(styledText
													.toString())), src, urlBigImage);
					activity.getListeObservations().add(o);

				}
			}
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		}
		return new Long(0);
	}

}
