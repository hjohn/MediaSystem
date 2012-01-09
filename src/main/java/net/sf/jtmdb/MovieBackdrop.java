package net.sf.jtmdb;

import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.sf.jtmdb.Log.Verbosity;

/**
 * This class represents a Movie Backdrop.
 *
 * @author Savvas Dalkitsis
 */
public class MovieBackdrop implements Serializable {

	private static final long serialVersionUID = 3111805995892191441L;

	/**
	 * This enumeration provides different sizes for the backdrops.
	 *
	 * @author Savvas Dalkitsis
	 */
	public enum Size {
		THUMB, POSTER, W1280, ORIGINAL
	}

	/**
	 * The urls and dimensions for the different sizes of the backdrop.
	 */
	private Map<Size, Pair<Dimension, URL>> info = new HashMap<>();

	/**
	 * The ID of the backdrop.
	 */
	private String ID;

	/**
	 * Constructs a new MovieBackdrop.
	 *
	 * @param ID
	 *            The ID of the backdrop.
	 */
	public MovieBackdrop(String ID) {
		this.ID = ID;
		Log.log("Creating MovieBackdrop object with id: " + ID,
				Verbosity.VERBOSE);
	}

	/**
	 * Constructs a new MovieBackdrop.
	 *
	 * @param info
	 *            The urls and dimensions of the different sizes of the
	 *            backdrop.
	 * @param ID
	 *            The ID of the backdrop.
	 */
	public MovieBackdrop(Map<Size, Pair<Dimension, URL>> info, String ID) {
		this(ID);
		if (info != null) {
			this.info.putAll(info);
		}
	}

	/**
	 * Returns the Url of the backdrop for the specified size if it exists,
	 * otherwise null.
	 *
	 * @param size
	 *            The size of the backdrop.
	 * @return The Url of the backdrop for the specified size if it exists,
	 *         otherwise null.
	 */
	public URL getImage(Size size) {
		return info.get(size).getSecond();
	}

	/**
	 * Returns the dimensions of the backdrop for the specified size if it
	 * exists, otherwise null.
	 *
	 * @param size
	 *            The size of the backdrop.
	 * @return The dimensions of the backdrop for the specified size if it
	 *         exists, otherwise null.
	 */
	public Dimension getImageDimension(Size size) {
		return info.get(size).getFirst();
	}

	/**
	 * Sets the image Url and dimension for the provided size.
	 *
	 * @param size
	 *            The size of the backdrop.
	 * @param info
	 *            The Url and dimension of the backdrop for the specified size.
	 */
	public void setImage(Size size, Pair<Dimension, URL> info) {
		this.info.put(size, info);
	}

	/**
	 * The backdrop ID.
	 *
	 * @return The backdrop ID.
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
		URL url = getImage(MovieBackdrop.Size.THUMB);
		if (url == null)
			url = getImage(MovieBackdrop.Size.POSTER);
		if (url == null)
			url = getImage(MovieBackdrop.Size.W1280);
		if (url == null)
			url = getImage(MovieBackdrop.Size.ORIGINAL);
		return url;
	}

	/**
	 * Returns the Url of the largest available size.
	 *
	 * @return The Url of the largest available size.
	 */
	public URL getLargestImage() {
		URL url = getImage(MovieBackdrop.Size.ORIGINAL);
		if (url == null)
			url = getImage(MovieBackdrop.Size.W1280);
		if (url == null)
			url = getImage(MovieBackdrop.Size.POSTER);
		if (url == null)
			url = getImage(MovieBackdrop.Size.THUMB);
		return url;
	}

	/**
	 * Returns the dimensions of the smallest available size.
	 *
	 * @return The dimensions of the smallest available size.
	 */
	public Dimension getSmallestImageDimension() {
		Dimension dim = getImageDimension(MovieBackdrop.Size.THUMB);
		if (dim == null)
			dim = getImageDimension(MovieBackdrop.Size.POSTER);
		if (dim == null)
			dim = getImageDimension(MovieBackdrop.Size.W1280);
		if (dim == null)
			dim = getImageDimension(MovieBackdrop.Size.ORIGINAL);
		return dim;
	}

	/**
	 * Returns the dimensions of the largest available size.
	 *
	 * @return The dimensions of the largest available size.
	 */
	public Dimension getLargestImageDimension() {
		Dimension dim = getImageDimension(MovieBackdrop.Size.ORIGINAL);
		if (dim == null)
			dim = getImageDimension(MovieBackdrop.Size.W1280);
		if (dim == null)
			dim = getImageDimension(MovieBackdrop.Size.POSTER);
		if (dim == null)
			dim = getImageDimension(MovieBackdrop.Size.THUMB);
		return dim;
	}

}
