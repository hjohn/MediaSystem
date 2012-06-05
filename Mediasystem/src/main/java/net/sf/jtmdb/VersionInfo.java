package net.sf.jtmdb;

import java.util.Date;

/**
 * This class represents version information.
 *
 * @author Savvas Dalkitsis
 */
public abstract class VersionInfo {

	/**
	 * The ID of the object.
	 */
	private int ID;
	/**
	 * The name of the object.
	 */
	private String name;
	/**
	 * The version of the object.
	 */
	private int version;
	/**
	 * The date of the last modification of the object.
	 */
	private Date lastModified;

	/**
	 * Contructs a VersionInfo object.
	 *
	 * @param name
	 *            The name of the object.
	 * @param ID
	 *            The ID of the object.
	 * @param version
	 *            The version of the object.
	 * @param lastModified
	 *            The date of the last modification of the object.
	 */
	protected VersionInfo(String name, int ID, int version, Date lastModified) {
		this.name = name;
		this.ID = ID;
		this.version = version;
		this.lastModified = lastModified;
	}

	/**
	 * The ID of the object.
	 *
	 * @return The ID of the object.
	 */
	public int getID() {
		return ID;
	}

	/**
	 * The name of the object.
	 *
	 * @return The name of the object.
	 */
	public String getName() {
		return name;
	}

	/**
	 * The version of the object.
	 *
	 * @return The version of the object.
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * The date of the last modification of the object.
	 *
	 * @return The date of the last modification of the object.
	 */
	public Date getLastModified() {
		return lastModified;
	}

}
