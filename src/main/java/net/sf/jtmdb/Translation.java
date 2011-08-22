package net.sf.jtmdb;

import java.io.Serializable;

/**
 * This class describes a translation of a Movie.
 * 
 * @author Savvas Dalkitsis
 */
public class Translation implements Serializable {

	private static final long serialVersionUID = -7252457369848314891L;

	/**
	 * The English name of the translation.
	 */
	private String englishName;
	/**
	 * The native name of the translation.
	 */
	private String nativeName;
	/**
	 * The ISO_639_1 code of the translation.
	 */
	private String iso639_1;

	protected Translation(String englishName, String nativeName, String iso639_1) {
		this.englishName = englishName;
		this.nativeName = nativeName;
		this.iso639_1 = iso639_1;
	}

	/**
	 * Gets the English name of the translation.
	 * 
	 * @return The English name of the translation.
	 */
	public String getEnglishName() {
		return englishName;
	}

	/**
	 * Gets the native name of the translation.
	 * 
	 * @return The native name of the translation.
	 */
	public String getNativeName() {
		return nativeName;
	}

	/**
	 * Gets the ISO_639_1 code of the translation.
	 * 
	 * @return The ISO_639_1 code of the translation.
	 */
	public String getIso639_1() {
		return iso639_1;
	}

}
