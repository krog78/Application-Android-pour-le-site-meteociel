package fr.meteociel.om;

/**
 * Report de gresil
 * @author ippon
 *
 */
public class Gresil extends ReportObservation{

	/**
	 * Hauteur de gresil en mm
	 */
	private String hauteur;

	public Gresil(ReportObservation obs, String hauteur) {
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
