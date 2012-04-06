package fr.meteociel.om;

/**
 * Lieu de l'observation
 * @author ippon
 *
 */
public class Lieu {
	/**
	 * Ville de l'observation
	 */
	private String ville = "";
	
	public Lieu(String ville, String altitude) {
		super();
		this.ville = ville;
		this.altitude = altitude;
	}

	/**
	 * Altitude de l'observation
	 */
	private String altitude = "";

	public String getVille() {
		return ville;
	}

	public void setVille(String ville) {
		this.ville = ville;
	}

	public String getAltitude() {
		return altitude;
	}

	public void setAltitude(String altitude) {
		this.altitude = altitude;
	}
	
	
}
