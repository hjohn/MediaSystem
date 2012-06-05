package net.sf.jtmdb;

import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.sf.jtmdb.Log.Verbosity;

/**
 * This class represents a Person profile picture.
 *
 * @author Savvas Dalkitsis
 */
public class PersonProfile implements Serializable {

	private static final long serialVersionUID = -5039194937693346970L;

	/**
	 * This enumeration provides different sizes for the profile image.
	 *
	 * @author Savvas Dalkitsis
	 */
	public enum Size {
		THUMB, PROFILE, ORIGINAL
	}

	/**
	 * The urls and dimensions for the different sizes of the profile image.
	 */
	private Map<Size, Pair<Dimension, URL>> info = new HashMap<>();

	/**
	 * The ID of the profile image.
	 */
	private String ID;

	/**
	 * Constructs a new PersonProfile.
	 *
	 * @param ID
	 *            The ID of the profile image.
	 */
	public PersonProfile(String ID) {
		this.ID = ID;
		Log.log("Creating PersonProfile object with id: " + ID,
				Verbosity.VERBOSE);
	}

	/**
	 * Constructs a new PersonProfile.
	 *
	 * @param info
	 *            The urls and dimensions of the different sizes of the profile
	 *            image.
	 * @param ID
	 *            The ID of the profile image.
	 */
	public PersonProfile(Map<Size, Pair<Dimension, URL>> info, String ID) {
		this(ID);
		if (info != null) {
			this.info.putAll(info);
		}
	}

	/**
	 * Returns the Url of the profile image for the specified size if it exists,
	 * otherwise null.
	 *
	 * @param size
	 *            The size of the profile image.
	 * @return The Url of the profile image for the specified size if it exists,
	 *         otherwise null.
	 */
	public URL getImage(Size size) {
		return info.get(size).getSecond();
	}

	/**
	 * Returns the dimensions of the profile image for the specified size if it
	 * exists, otherwise null.
	 *
	 * @param size
	 *            The size of the profile image.
	 * @return The dimensions of the profile image for the specified size if it
	 *         exists, otherwise null.
	 */
	public Dimension getImageDimension(Size size) {
		return info.get(size).getFirst();
	}

	/**
	 * Sets the image Url and dimension for the provided size.
	 *
	 * @param size
	 *            The size of the profile image.
	 * @param info
	 *            The Url and dimension of the profile image for the specified
	 *            size.
	 */
	public void setImage(Size size, Pair<Dimension, URL> info) {
		this.info.put(size, info);
	}

	/**
	 * The profile image ID.
	 *
	 * @return The profile image ID.
	 */
	public String getID() {
		return ID;
	}

	/**
	 * Returns the Url of the smallest available size.
	 *
	 * @return The Url of the smallest available size.
	 */
	public URL getSmallestImage() {
		URL url = getImage(Size.THUMB);
		if (url == null) {
			url = getImage(Size.PROFILE);
		}
		if (url == null) {
			url = getImage(Size.ORIGINAL);
		}
		return url;
	}

	/**
	 * Returns the Url of the largest available size.
	 *
	 * @return The Url of the largest available size.
	 */
	public URL getLargestImage() {
		URL url = getImage(Size.ORIGINAL);
		if (url == null) {
			url = getImage(Size.PROFILE);
		}
		if (url == null) {
			url = getImage(Size.THUMB);
		}
		return url;
	}

	/**
	 * Returns the dimensions of the smallest available size.
	 *
	 * @return The dimensions of the smallest available size.
	 */
	public Dimension getSmallestImageDimension() {
		Dimension dim = getImageDimension(Size.THUMB);
		if (dim == null) {
			dim = getImageDimension(Size.PROFILE);
		}
		if (dim == null) {
			dim = getImageDimension(Size.ORIGINAL);
		}
		return dim;
	}

	/**
	 * Returns the dimensions of the largest available size.
	 *
	 * @return The dimensions of the largest available size.
	 */
	public Dimension getLargestImageDimension() {
		Dimension dim = getImageDimension(Size.ORIGINAL);
		if (dim == null) {
			dim = getImageDimension(Size.PROFILE);
		}
		if (dim == null) {
			dim = getImageDimension(Size.THUMB);
		}
		return dim;
	}

}
