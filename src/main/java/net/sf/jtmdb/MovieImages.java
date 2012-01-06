package net.sf.jtmdb;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This class represents the images of a Movie. Contains posters and backdrops.
 *
 * @author Savvas Dalkitsis
 */
public class MovieImages implements Serializable {

	private static final long serialVersionUID = 1087926973625501506L;

	public Set<MoviePoster> posters = new LinkedHashSet<MoviePoster>();
	public Set<MovieBackdrop> backdrops = new LinkedHashSet<MovieBackdrop>();

	private String name;
	private int ID;

	public MovieImages(int ID, String name) {
		this.ID = ID;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}

}
