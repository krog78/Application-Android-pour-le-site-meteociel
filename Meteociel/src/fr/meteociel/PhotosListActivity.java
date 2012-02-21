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
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;

import org.ccil.cowan.tagsoup.Parser;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.xml.sax.InputSource;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import android.app.ListActivity;
import android.net.Uri;
import android.os.Bundle;

/**
 * This activity uses a custom cursor adapter which fetches a XML photo feed and parses the XML to
 * extract the images' URL and their title.
 */
public class PhotosListActivity extends ListActivity {
    private static final String METEOCIEL_FEED_URL =
        "http://meteociel.fr/user/day-gallery.php";

   
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        System.setProperty("http.proxyHost", "80.78.6.10");
        System.setProperty("http.proxyPort", "8080");
        
        try {
			Thread.sleep(3);
		} catch (InterruptedException e1) {
			throw new RuntimeException(e1);
		}
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
			transformer.transform(new SAXSource(reader, new InputSource(url.openStream())), 
			                      result);
		} catch (TransformerException e1) {
			throw new RuntimeException(e1);
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}
      	
                      
        org.jdom.Document document = null ;
        try {
            /* On crée une instance de SAXBuilder */
            SAXBuilder sxb = new SAXBuilder();
            document = sxb.build(result.getNode().getTextContent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JDOMException e){
        	throw new RuntimeException(e);
        }
        
        try {
            /* On initialise un nouvel élément avec l'élément racine du
               document. */
            Element racine = document.getRootElement();
            
            /* On va dans un premier temps rechercher l'ensemble des noms 
               des patients de notre hôpital. */
            
            /* Recherche de la liste des patients         */
            XPath xpa = XPath.newInstance("//patient");   
            
            /* On récupère tous les noeuds répondant au chemin //patient */
            List results = xpa.selectNodes(racine) ;
            
            Iterator iter = results.iterator() ;
            
            Element noeudCourant = null;
            String personneId = null ;
            while (iter.hasNext()){
                /* Pour chaque patient nous allons chercher son nom puis l'afficher */
                noeudCourant = (Element) iter.next();
                
                /* On récupère l'identifiant de la personne                
                   Noter le . en début du chemin : on part de la position courante 
                   le @ indique que l'on cherche un attribut               */
                xpa = XPath.newInstance("./@personneId");
                personneId = xpa.valueOf(noeudCourant);
                
                /* A partir de là on récupère les infos dans la balise personne correspondante 
                   On spécifie que l'on recherche une balise en fonction de la valeur 
                   d'un de ses attributs :*/
                xpa = XPath.newInstance("//personne[@id='" + personneId + "']");
                noeudCourant = (Element) xpa.selectSingleNode(noeudCourant);
                
                /* Nous cherchons à présent la valeur de la balise nom :                */
                xpa = XPath.newInstance("./nom");                
            }
        } catch (JDOMException e) {
            throw new RuntimeException(e);            
        } 
        
        
        setContentView(R.layout.photos_list);
        setListAdapter(Adapters.loadCursorAdapter(this, R.xml.photos,
                "content://xmldocument/?url=" + Uri.encode(METEOCIEL_FEED_URL)));
    }
}
