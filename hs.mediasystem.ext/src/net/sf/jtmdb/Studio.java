package net.sf.jtmdb;

import java.io.Serializable;
import java.net.URL;

import net.sf.jtmdb.Log.Verbosity;

/**
 * This class represents a movie Studio.
 *
 * @author Savvas Dalkitsis
 */
public class Studio implements Serializable {

	private static final long serialVersionUID = -3712697550558073084L;

	/**
	 * The url of the Studio.
	 */
	private URL url;
	/**
	 * The name of the Studio.
	 */
	private String name;

	/**
	 * Constructs a Studio with the given URL and name.
	 *
	 * @param url
	 *            The URL of the Studio.
	 * @param name
	 *            The name of the Studio.
	 */
	public Studio(URL url, String name) {
		Log.log("Creating Studio object with url: "
				+ ((url == null) ? "NULL" : url.toString()) + " and name: "
				+ name, Verbosity.VERBOSE);
		setUrl(url);
		setName(name);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof Studio) {
			return ((Studio) obj).getName().equals(getName());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getName().hashCode();
	}

	/**
	 * The URL of the Studio.
	 *
	 * @return The URL of the Studio.
	 */
	public URL getUrl() {
		return url;
	}

	/**
	 * Sets the URL of the Studio.
	 *
	 * @param url
	 *            The URL of the Studio.
	 */
	public void setUrl(URL url) {
		this.url = url;
	}

	/**
	 * The name of the Studio.
	 *
	 * @return The name of the Studio.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the Studio.
	 *
	 * @param name
	 *            The name of the Studio.
	 */
	public void setName(String name) {
		this.name = name;
	}

}
