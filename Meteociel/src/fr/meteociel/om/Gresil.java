package fr.meteociel.om;

import fr.meteociel.util.TypeChampSupp;

/**
 * Report de gresil
 * @author ippon
 *
 */
public class Gresil extends ReportObservation implements IReportObservation{

	/**
	 * Hauteur de gresil en mm
	 */
	private String hauteur = "0";

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
	
	public TypeChampSupp getTypeChampSupp() {
		return TypeChampSupp.GRESIL_MM;
	}
	
}
