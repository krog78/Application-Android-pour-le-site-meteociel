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

import javax.xml.crypto.NodeSetData;
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

import org.ccil.cowan.tagsoup.Parser;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
      	
                      
        XPathFactory xpf = XPathFactory.newInstance();
        XPath xpath = xpf.newXPath();
        
        try {
			NodeList nodeList = (NodeList) xpath.evaluate("//img", result.getNode(), XPathConstants.NODESET);
			for(int i=0; i < nodeList.getLength(); i++){
				Node node = nodeList.item(i);
				node.getAttributes();
			}
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		}
        
        
        setContentView(R.layout.photos_list);
        setListAdapter(Adapters.loadCursorAdapter(this, R.xml.photos,
                "content://xmldocument/?url=" + Uri.encode(METEOCIEL_FEED_URL)));
    }
}
