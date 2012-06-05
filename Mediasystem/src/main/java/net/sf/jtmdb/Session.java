package net.sf.jtmdb;

import java.io.Serializable;

/**
 * This class represents a session with Tmdb used in POST methods.
 *
 * @author Savvas Dalkitsis
 */
public class Session implements Serializable {

	private static final long serialVersionUID = -6886965265622351865L;

	/**
	 * The user name.
	 */
	private String userName;
	/**
	 * The session key.
	 */
	private String session;

	/**
	 * Gets the user name.
	 *
	 * @return The user name.
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Gets the session key.
	 *
	 * @return The session key.
	 */
	public String getSession() {
		return session;
	}

	/**
	 * Creates a session with the provided user name and key.
	 *
	 * @param userName
	 *            The user name.
	 * @param session
	 *            The session key.
	 */
	public Session(String userName, String session) {
		this.userName = userName;
		this.session = session;
	}

}
