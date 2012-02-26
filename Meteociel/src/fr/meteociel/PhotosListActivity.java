/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.meteociel;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
import org.ccil.cowan.tagsoup.Parser;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import fr.meteociel.adapter.LazyAdapter;
import fr.meteociel.om.Observation;

/**
 * This activity uses a custom cursor adapter which fetches a XML photo feed and
 * parses the XML to extract the images' URL and their title.
 */
public class PhotosListActivity extends Activity {
	private static final String METEOCIEL_FEED_URL = "http://meteociel.fr/user/day-gallery.php";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		//System.setProperty("http.proxyHost", "80.78.6.10");
		//System.setProperty("http.proxyPort", "8080");

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
			transformer.transform(
					new SAXSource(reader, new InputSource(url.openStream())),
					result);
		} catch (TransformerException e1) {
			throw new RuntimeException(e1);
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}

		XPathFactory xpf = XPathFactory.newInstance();
		XPath xpath = xpf.newXPath();

		List<Observation> listeObservations = new ArrayList<Observation>();
		try {
			NodeList nodeList = (NodeList) xpath.evaluate("//img",
					result.getNode(), XPathConstants.NODESET);
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
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
							StringEscapeUtils.unescapeJavaScript(StringEscapeUtils
									.unescapeHtml(styledText.toString())), src);
					listeObservations.add(o);

				}
			}
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		}

		list = (ListView) findViewById(R.id.list);
		adapter = new LazyAdapter(this,
				listeObservations.toArray(new Observation[listeObservations
						.size()]));
		list.setAdapter(adapter);

	}

	@Override
	public void onDestroy() {
		list.setAdapter(null);
		super.onDestroy();
	}

		
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.liste_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.refresh:
	        	adapter.notifyDataSetChanged();
	            return true;
	        case R.id.report:
	        	Intent intent = new Intent(this, ReportObservationActivity.class);
	            this.startActivity(intent);
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	ListView list;
	LazyAdapter adapter;

}
