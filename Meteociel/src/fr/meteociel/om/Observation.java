package fr.meteociel.om;

/**
 * Objet metier representant une observation
 * 
 * @author Krog78
 * 
 */
public class Observation {

	public Observation(String titre, String texte, String url) {
		super();
		this.titre = titre;
		this.texte = texte;
		this.urlImage = url;
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

}
