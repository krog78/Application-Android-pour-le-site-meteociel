package fr.meteociel.om;

import fr.meteociel.util.TypeChampSupp;

/**
 * Report de température
 * @author ippon
 *
 */
public class Temperature extends ReportObservation implements IReportObservation{

	/**
	 * Température en degrés
	 */
	private String degres = "0";

	public Temperature(ReportObservation obs, String degres) {
		super(obs);
		this.degres = degres;
	}

	public String getDegres() {
		return degres;
	}

	public void setDegres(String degres) {
		this.degres = degres;
	}

	public TypeChampSupp getTypeChampSupp() {
		return TypeChampSupp.TEMPERATURE;
	}
	
}
