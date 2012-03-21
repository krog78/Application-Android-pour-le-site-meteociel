package fr.meteociel.om;

/**
 * Objet metier representant une observation
 * 
 * @author Krog78
 * 
 */
public class Observation {

	public Observation(String titre, String texte, String url, String urlBigImage) {
		super();
		this.titre = titre;
		this.texte = texte;
		this.urlImage = url;
		this.urlBigImage = urlBigImage;
	}

	/**
	 * Titre de l'observation
	 */
	private String titre;

	/**
	 * Texte de l'observation
	 */
	private String texte;

	/**
	 * Url de l'image
	 */
	private String urlImage;

	/**
	 * Url de l'image big
	 */
	private String urlBigImage;

	public String getTexte() {
		return texte;
	}

	public void setTexte(String texte) {
		this.texte = texte;
	}

	public String getUrlImage() {
		return urlImage;
	}

	public void setUrlImage(String urlImage) {
		this.urlImage = urlImage;
	}

	public String getTitre() {
		return titre;
	}

	public void setTitre(String titre) {
		this.titre = titre;
	}

	public String getUrlBigImage() {
		return urlBigImage;
	}

	public void setUrlBigImage(String urlBigImage) {
		this.urlBigImage = urlBigImage;
	}

}
