package net.sf.jtmdb;

import java.io.Serializable;
import java.net.URL;

import net.sf.jtmdb.Log.Verbosity;

/**
 * This class represents a Country.
 * 
 * @author Savvas Dalkitsis
 */
public class Country implements Serializable {

	private static final long serialVersionUID = 5337925489671786943L;

	/**
	 * The url of the Country.
	 */
	private URL url;
	/**
	 * The name of the Country.
	 */
	private String name;
	/**
	 * The code of the Country;
	 */
	private String code;

	/**
	 * Constructs a Country with the given URL, name and code.
	 * 
	 * @param url
	 *            The URL of the Country.
	 * @param name
	 *            The name of the Country.
	 * @param code
	 *            The code of the Country.
	 */
	public Country(URL url, String name, String code) {
		Log.log("Creating Country object with url: "
				+ ((url == null) ? "NULL" : url.toString()) + ", code: " + code
				+ " and name: " + name, Verbosity.VERBOSE);
		setUrl(url);
		setName(name);
		setCode(code);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof Country) {
			return ((Country) obj).getCode().equals(getCode());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getName().hashCode();
	}

	/**
	 * The URL of the Country.
	 * 
	 * @return The URL of the Country.
	 */
	public URL getUrl() {
		return url;
	}

	/**
	 * Sets the URL of the Country.
	 * 
	 * @param url
	 *            The URL of the Country.
	 */
	public void setUrl(URL url) {
		this.url = url;
	}

	/**
	 * The name of the Country.
	 * 
	 * @return The name of the Country.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the Country.
	 * 
	 * @param name
	 *            The name of the Country.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * The code of the Country.
	 * 
	 * @return The code of the Country.
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Sets the code of the Country.
	 * 
	 * @param code
	 *            The code of the Country.
	 */
	public void setCode(String code) {
		this.code = code;
	}

}
