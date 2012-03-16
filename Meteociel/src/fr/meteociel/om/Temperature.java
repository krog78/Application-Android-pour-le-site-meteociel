package fr.meteociel.om;

/**
 * Report de température
 * @author ippon
 *
 */
public class Temperature extends ReportObservation{

	/**
	 * Température en degrés
	 */
	private String degres;

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

	
	
}
