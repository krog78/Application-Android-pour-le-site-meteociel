package fr.meteociel.om;

import fr.meteociel.util.TypeChampSupp;

/**
 * Report de vent
 * 
 * @author ippon
 * 
 */
public class Vent extends ReportObservation implements IReportObservation{

	public Vent(ReportObservation obs, String vitesse, String direction) {
		super(obs);
		this.vitesse = vitesse;
		this.direction = direction;
	}

	/**
	 * Vitesse du vent
	 */
	private String vitesse = "0";

	/**
	 * Direction du vent
	 */
	private String direction = "";

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
	
	public TypeChampSupp getTypeChampSupp() {
		return TypeChampSupp.VENT;
	}
}
