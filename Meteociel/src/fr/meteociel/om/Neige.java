package fr.meteociel.om;

import fr.meteociel.util.TypeChampSupp;

/**
 * Report de neige
 * @author ippon
 *
 */
public class Neige extends ReportObservation implements IReportObservation{

	/**
	 * Hauteur de neige en cm
	 */
	private String hauteur = "0";

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
	
	public TypeChampSupp getTypeChampSupp() {
		return TypeChampSupp.NEIGE_CM;
	}
	
}
