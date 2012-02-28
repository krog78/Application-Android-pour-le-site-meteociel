package fr.meteociel.util;

import fr.meteociel.R;

/**
 * Enumeration des observations possibles
 * 
 * @author SLYFIXE
 * 
 */
public enum ObservationsEnum {

	ENSOLEILLE("Ensoleillé", R.drawable.ensoleille),
	NUAGES_EPARS("Nuages épars", R.drawable.nuages_epars);

	private final String libelle;
	private final int idImage;

	ObservationsEnum(String libelle, int idImage) {
		this.libelle = libelle;
		this.idImage = idImage;
	}

	public String text() {
		return libelle;
	}

	public int image() {
		return idImage;
	}

}
