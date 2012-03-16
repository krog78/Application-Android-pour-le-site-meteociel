package fr.meteociel.om;

/**
 * Report de neige
 * @author ippon
 *
 */
public class Neige extends ReportObservation{

	/**
	 * Hauteur de neige en cm
	 */
	private String hauteur;

	public Neige(ReportObservation obs, String hauteur) {
		super(obs);
		this.hauteur = hauteur;
	}

	public String getHauteur() {
		return hauteur;
	}

	public void setHauteur(String hauteur) {
		this.hauteur = hauteur;
	}
	
}
