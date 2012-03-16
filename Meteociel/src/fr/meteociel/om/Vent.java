package fr.meteociel.om;

/**
 * Report de vent
 * 
 * @author ippon
 * 
 */
public class Vent extends ReportObservation {

	public Vent(ReportObservation obs, String vitesse, String direction) {
		super(obs);
		this.vitesse = vitesse;
		this.direction = direction;
	}

	/**
	 * Vitesse du vent
	 */
	private String vitesse;

	/**
	 * Direction du vent
	 */
	private String direction;

	public String getVitesse() {
		return vitesse;
	}

	public void setVitesse(String vitesse) {
		this.vitesse = vitesse;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}
}
