package fr.meteociel.om;

import fr.meteociel.util.TypeChampSupp;

/**
 * Report de pluie
 * @author ippon
 *
 */
public class Pluie extends ReportObservation implements IReportObservation{

	/**
	 * Hauteur de pluie en mm
	 */
	private String hauteur = "0";

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
	
	public TypeChampSupp getTypeChampSupp() {
		return TypeChampSupp.PLUIE_MM;
	}
	
}
