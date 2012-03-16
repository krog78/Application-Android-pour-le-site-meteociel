package fr.meteociel.om;

/**
 * Report de visibilité
 * @author ippon
 *
 */
public class Visibilite extends ReportObservation{

	/**
	 * Visibilité en mètres
	 */
	private String longueur;

	public Visibilite(ReportObservation obs, String longueur) {
		super(obs);
		this.longueur = longueur;
	}

	public String getLongueur() {
		return longueur;
	}

	public void setLongueur(String longueur) {
		this.longueur = longueur;
	}

	

	
	
}
