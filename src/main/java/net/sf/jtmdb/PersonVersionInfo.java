package net.sf.jtmdb;

import java.util.Date;

/**
 * This class represents version information for a Person.
 * 
 * @author Savvas Dalkitsis
 */
public class PersonVersionInfo extends VersionInfo {

	/**
	 * @param name
	 *            The name of the Person.
	 * @param ID
	 *            The ID of the Person.
	 * @param version
	 *            The version of the Person.
	 * @param lastModified
	 *            The date of the last modification of the Person.
	 */
	protected PersonVersionInfo(String name, int ID, int version,
			Date lastModified) {
		super(name, ID, version, lastModified);
	}

}
