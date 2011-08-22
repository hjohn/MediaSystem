package net.sf.jtmdb;

import java.util.Date;

/**
 * This class represents version information for a Movie.
 * 
 * @author Savvas Dalkitsis
 */
public class MovieVersionInfo extends VersionInfo {

	/**
	 * The imdb ID of the Movie.
	 */
	private String imdbID;

	/**
	 * @param name
	 *            The name of the Movie.
	 * @param ID
	 *            2The ID of the Movie.
	 * @param version
	 *            The version of the Movie.
	 * @param lastModified
	 *            The date of the last modification of the Movie.
	 */
	protected MovieVersionInfo(String name, int ID, int version,
			Date lastModified, String imdbID) {
		super(name, ID, version, lastModified);
		this.imdbID = imdbID;
	}

	/**
	 * The imdb ID of the Movie.
	 * 
	 * @return The imdb ID of the Movie.
	 */
	public String getImdbID() {
		return imdbID;
	}

}
