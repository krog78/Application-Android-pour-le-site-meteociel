package fr.meteociel.om;

import android.net.Uri;

/**
 * Classe de report d'une observation
 * @author A512568
 *
 */
public class ReportObservation {
	
	/**
	 * Login utilisateur
	 */
	private String user;
	
	/**
	 * Password utilisateur
	 */
	private String password;
	
	/**
	 * Lieu de l'observation
	 */
	private String lieu = "Lieu0";
	
	/**
	 * Heure de l'observation
	 */
	private int heure = 0;
	
	/**
	 * Texte de l'observation
	 */
	private String texte;
	
	/**
	 * Valeur selection pour meteociel
	 */
	private String value;
		
	/**
	 * Uri de l'image seectionnee
	 */
	private Uri imageUri;
	
	/**
	 * Path de l'image sélectionnée
	 */
	private String pathImage = "";

	public String getPathImage() {
		return pathImage;
	}

	public void setPathImage(String pathImage) {
		this.pathImage = pathImage;
	}

	public String getTexte() {
		return texte;
	}

	public void setTexte(String texte) {
		this.texte = texte;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Uri getImageUri() {
		return imageUri;
	}

	public void setImageUri(Uri imageUri) {
		this.imageUri = imageUri;
	}

	public String getLieu() {
		return lieu;
	}

	public void setLieu(String lieu) {
		this.lieu = lieu;
	}

	public int getHeure() {
		return heure;
	}

	public void setHeure(int heure) {
		this.heure = heure;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	//private String image;
}
