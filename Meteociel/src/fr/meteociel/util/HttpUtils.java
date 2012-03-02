package fr.meteociel.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;

/**
 * Classe utilitaire pour les accès Http
 * @author A512568
 *
 */
public class HttpUtils {
	
	/**
	 * Timeout de soumission du formulaire à meteociel
	 */
	private static final int TIMEOUT_MS = 10000;

	/**
	 * Taille du buffer de récupération de la réponse après soumission du
	 * formulaire
	 */
	private static final int BUFFER_SIZE = 8096;
	
	public static final void postRequest(String url, List<NameValuePair> params){
		HttpClient httpClient = new DefaultHttpClient();

		// DEBUT Proxy pour chez Atos
		HttpHost proxy = new HttpHost("80.78.6.10", 8080);
		httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
				proxy);
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
			HttpResponse response = httpClient.execute(httpPost);
			BufferedReader br = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()), BUFFER_SIZE);
			StringBuilder htmlResponse = new StringBuilder();
			String line = "";
			while ((line = br.readLine()) != null) {
				htmlResponse.append(line);
			}
			
			try {
				File c = new File("/sdcard/test.html");

				BufferedWriter out = new BufferedWriter(new FileWriter(c));
				out.write(htmlResponse.toString());
				out.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

		} catch (ClientProtocolException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
