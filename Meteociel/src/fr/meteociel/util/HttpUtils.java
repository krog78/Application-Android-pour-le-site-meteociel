package fr.meteociel.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.graphics.Bitmap;
import fr.meteo.meteociel.activity.AbstractMeteocielActivity;
import fr.meteociel.exception.SoumissionFormulaireException;

/**
 * Classe utilitaire pour les accès Http
 * 
 * @author A512568
 * 
 */
public class HttpUtils {
	/**
	 * Timeout de soumission du formulaire à meteociel
	 */
	private static final int TIMEOUT_MS = 20000;

	/**
	 * Taille du buffer de récupération de la réponse après soumission du
	 * formulaire
	 */
	private static final int BUFFER_SIZE = 8096;

	/**
	 * Paramètre site météociel
	 */
	private static final int MAX_FILE_SIZE = 3170000;

	/**
	 * Contenu form-data
	 */
	public static final String MULTIPART_FORM_DATA = "multipart/form-data; boundary=";

	/**
	 * HttpClient utilisé par l'application
	 */
	private static final DefaultHttpClient httpClient = new DefaultHttpClient();

	/**
	 * Contexte de la requête http
	 */
	private static final HttpContext localContext = new BasicHttpContext();

	/**
	 * Boundary délimitant les parties du multipart
	 */
	private static final String BOUNDARY = "--OFzbLE37uDifDeYSl6Ie7yxG431UNaDf5Uv6";

	/**
	 * Charset utilisé dans la requête HTTP
	 */
	private static final String CHARSET = "UTF-8";

	static {
		BasicCookieStore cookieStore = new BasicCookieStore();
		httpClient.setCookieStore(cookieStore);
		httpClient
				.getParams()
				.setParameter(
						CoreProtocolPNames.USER_AGENT,
						"Mozilla/5.0 (X11; Ubuntu; Linux i686; rv:10.0.2) Gecko/20100101 Firefox/10.0.2");

	}

	/**
	 * Poste une requête
	 * 
	 * @param url
	 *            l'url de la requête
	 * @param params
	 *            les paramètres à poster
	 * @param filePath
	 *            chemin du fichier à uploader
	 */
	public static final void uploadImageRequest(String url,
			List<NameValuePair> params, String filePath) {

		File file = new File(filePath);

		MultipartEntity mpEntity = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE, BOUNDARY,
				Charset.forName(CHARSET));
		ContentBody cbFile = new FileBody(file, "image/png");
		ContentBody cbSource;
		try {
			cbSource = new StringBody("disquedur");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		ContentBody maxFileSize;
		try {
			maxFileSize = new StringBody(String.valueOf(MAX_FILE_SIZE));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		ContentBody dMode;
		try {
			dMode = new StringBody("1");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		ContentBody imageUrl;
		try {
			imageUrl = new StringBody("");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		ContentBody envoyer;
		try {
			envoyer = new StringBody("En cours d'envoi ...");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		mpEntity.addPart("envoyer", envoyer);
		mpEntity.addPart("imageurl", imageUrl);
		mpEntity.addPart("dmode", dMode);
		mpEntity.addPart("source", cbSource);
		mpEntity.addPart("MAX_FILE_SIZE", maxFileSize);
		mpEntity.addPart("image", cbFile);
		HttpPost httpPost = new HttpPost(url);
		httpPost.setEntity(mpEntity);
		httpPost.setHeader("Content-Type", MULTIPART_FORM_DATA + BOUNDARY);

		httpPost.setHeader("Host", "images.meteociel.fr");
		httpPost.setHeader("Connection", "keep-alive");
		httpPost.setHeader("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		httpPost.setHeader("Accept-Language",
				"fr,fr-fr;q=0.8,en-us;q=0.5,en;q=0.3");
		httpPost.setHeader("Referer", "http://images.meteociel.fr/");

		HttpConnectionParams.setConnectionTimeout(httpClient.getParams(),
				TIMEOUT_MS);
		HttpConnectionParams.setSoTimeout(httpClient.getParams(), TIMEOUT_MS);

		try {
			HttpResponse response = httpClient.execute(httpPost, localContext);
			InputStreamReader is = new InputStreamReader(
					response.getEntity().getContent());
			BufferedReader br = new BufferedReader(is, BUFFER_SIZE);
			
			try{
				StringBuilder htmlResponse = new StringBuilder();
				String line = "";
				while ((line = br.readLine()) != null) {
					htmlResponse.append(line);
				}
			}finally{
				is.close();
				br.close();				
			}			

		} catch (ClientProtocolException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * Méthode exécutant une requête get
	 * 
	 * @param url
	 *            l'url à appeler
	 */
	public static final HttpResponse getRequest(
			AbstractMeteocielActivity activity, String url) {
		HttpGet httpGet = new HttpGet(url);
		HttpResponse httpResponse = null;
		try {
			httpResponse = httpClient.execute(httpGet);
		} catch (ClientProtocolException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			e.printStackTrace();
			activity.showConnectionError();
		}
		return httpResponse;

	}

	/**
	 * Poste une requête httpPost
	 * 
	 * @param url
	 *            l'url de la requête
	 * @param params
	 *            les paramètres à poster
	 * @param httpPost
	 * @throws SoumissionFormulaireException
	 * 
	 */
	public static final void postRequest(String url, List<NameValuePair> params)
			throws SoumissionFormulaireException {

		// DEBUT Proxy pour chez Atos
		// HttpHost proxy = new HttpHost("80.78.6.10", 8080);
		// httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,proxy);
		// FIN Proxy pour chez Atos

		HttpConnectionParams.setConnectionTimeout(httpClient.getParams(),
				TIMEOUT_MS);
		HttpConnectionParams.setSoTimeout(httpClient.getParams(), TIMEOUT_MS);

		HttpPost httpPost = new HttpPost(url);
		// etc...
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(params));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		try {
			
			HttpResponse response = httpClient.execute(httpPost, localContext);
			traiterRetourRequete(httpResponseToString(response));

		} catch (ClientProtocolException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Méthode traitant le retour de la réponse http
	 * 
	 * @param response
	 *            la réponse http
	 * @throws SoumissionFormulaireException
	 */
	private static final void traiterRetourRequete(String response)
			throws SoumissionFormulaireException {
		
		if (response.toLowerCase().contains("incorrects")
				|| response.toLowerCase().contains("probl")) {

//			try {
//				BufferedWriter out = new BufferedWriter(new FileWriter(
//						"/mnt/sdcard/temp/MeteocielError.html"));
//				out.write(response);
//				out.close();
//			} catch (IOException e) {
//				throw new RuntimeException(e);
//			}
			throw new SoumissionFormulaireException();
		}
	}

	/**
	 * Télécharge un fichier à l'url donnée
	 * 
	 * @param fileUrl
	 *            l'url du fichier
	 * @return l'image au format Bitmap
	 * @throws IOException
	 */
	public static final Bitmap downloadFile(String fileUrl,
			AbstractMeteocielActivity activity) throws IOException {

		ImageLoader img = new ImageLoader(activity.getApplicationContext());
		img.setRequiredSize(200);
		return img.getBitmap(fileUrl);

	}

	/**
	 * Récupère la chaine de la réponse Http
	 * 
	 * @param response
	 *            la réponse http
	 * @return la chaine html correspondante
	 */
	public static final String httpResponseToString(HttpResponse response) {
		BufferedReader br;
		StringBuilder htmlResponse = new StringBuilder();
		try {
			br = new BufferedReader(new InputStreamReader(response.getEntity()
					.getContent()), BUFFER_SIZE);

			String line = "";
			while ((line = br.readLine()) != null) {
				htmlResponse.append(line);
			}
		} catch (IllegalStateException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return htmlResponse.toString();
	}

}
