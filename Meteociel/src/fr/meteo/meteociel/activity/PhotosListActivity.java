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

package fr.meteo.meteociel.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import fr.meteo.meteociel.R;
import fr.meteociel.adapter.LazyAdapter;
import fr.meteociel.om.Observation;
import fr.meteociel.util.HttpUtils;
import fr.meteociel.util.MeteocielUtils;

/**
 * This activity uses a custom cursor adapter which fetches a XML photo feed and
 * parses the XML to extract the images' URL and their title.
 */
public class PhotosListActivity extends AbstractMeteocielActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		new AfficherObservationsTask().execute();

	}

	@Override
	public void onDestroy() {
		if (list != null) {
			list.setAdapter(null);
		}
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
			new AfficherObservationsTask().execute();
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
	List<Observation> listeObservations = new ArrayList<Observation>();

	private class AfficherObservationsTask extends
			AsyncTask<Object, Integer, Long> {

		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog.show(PhotosListActivity.this, "",
					getString(R.string.chargement), true);
			super.onPreExecute();
		}

		@Override
		protected Long doInBackground(Object... obj) {
			return MeteocielUtils.parseHtmlMeteociel(PhotosListActivity.this);

		}

		@Override
		protected void onPostExecute(Long result) {
			super.onPostExecute(result);
			if (result == 1) {
				showConnectionError();
			}
			list = (ListView) findViewById(R.id.list_reports);
			adapter = new LazyAdapter(PhotosListActivity.this,
					listeObservations.toArray(new Observation[listeObservations
							.size()]));
			list.setAdapter(adapter);

			// Gestion du click sur un item
			list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				public void onItemClick(AdapterView parentView, View childView,
						int position, long id) {

					// Appel de la tache de fond pour charger l'observation
					new RechercherObservationsTask().execute(position);
				}
			});
			dialog.dismiss();
		}
	}

	private class RechercherObservationsTask extends
			AsyncTask<Object, Integer, Long> {

		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog.show(PhotosListActivity.this, "",
					getString(R.string.chargement), true);
			super.onPreExecute();
		}

		@Override
		protected Long doInBackground(final Object... obj) {

			PhotosListActivity.this.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					// Chargement de l'image en grand dans un nouveau dialog
					final Dialog dialog = new Dialog(PhotosListActivity.this);

					int position = (Integer)obj[0];
					
					dialog.setContentView(R.layout.big_image);

					ImageView image = (ImageView) dialog.findViewById(R.id.big_image);
						
					try {
						image.setImageBitmap(HttpUtils.downloadFile(listeObservations.get(
								position).getUrlBigImage(), PhotosListActivity.this));
					} catch (IOException e) {
						showConnectionError();
					}
					
					Button closeButton = (Button) dialog.findViewById(R.id.close);
					closeButton.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							dialog.dismiss();
						}
					});

					// Libellé de l'image
					TextView titre = (TextView) dialog.findViewById(R.id.titre);
					titre.setText(listeObservations.get(position).getTitre());

					TextView description = (TextView) dialog
							.findViewById(R.id.description);
					description.setText(listeObservations.get(position).getTexte());

					dialog.show();
					
				}
			});
			
			

			return new Long(0);

		}

		@Override
		protected void onPostExecute(Long result) {
			super.onPostExecute(result);
			
			dialog.dismiss();
		}
	}

	public List<Observation> getListeObservations() {
		return listeObservations;
	}

	public void setListeObservations(List<Observation> listeObservations) {
		this.listeObservations = listeObservations;
	}

}
