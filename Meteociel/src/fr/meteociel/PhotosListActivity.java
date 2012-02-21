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

import fr.meteociel.R;

import android.app.ListActivity;
import android.net.Uri;
import android.os.Bundle;

/**
 * This activity uses a custom cursor adapter which fetches a XML photo feed and parses the XML to
 * extract the images' URL and their title.
 */
public class PhotosListActivity extends ListActivity {
    private static final String METEOCIEL_FEED_URL =
        "http://picasaweb.google.com/data/feed/api/featured?max-results=50&thumbsize=144c";

   
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.setProperty("http.proxyHost", "80.78.6.10");
        System.setProperty("http.proxyPort", "8080");
        setContentView(R.layout.photos_list);
        setListAdapter(Adapters.loadCursorAdapter(this, R.xml.photos,
                "content://xmldocument/?url=" + Uri.encode(METEOCIEL_FEED_URL)));
    }
}
