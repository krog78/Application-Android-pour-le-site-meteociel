package fr.meteociel.om;

/**
 * Report de pluie
 * @author ippon
 *
 */
public class Pluie extends ReportObservation{

	/**
	 * Hauteur de pluie en mm
	 */
	private String hauteur;

	public Pluie(ReportObservation obs, String hauteur) {
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
