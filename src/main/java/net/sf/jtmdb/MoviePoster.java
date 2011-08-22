package net.sf.jtmdb;

import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.sf.jtmdb.Log.Verbosity;

/**
 * This class represents a Movie Poster.
 * 
 * @author Savvas Dalkitsis
 */
public class MoviePoster implements Serializable {

	private static final long serialVersionUID = -484188331871936455L;

	/**
	 * This enumeration provides different sizes for the posters.
	 * 
	 * @author Savvas Dalkitsis
	 */
	public enum Size {
		THUMB, W342, COVER, W154, MID, ORIGINAL
	}

	/**
	 * The urls and dimensions info for the different sizes of the poster.
	 */
	private Map<Size, Pair<Dimension, URL>> info = new HashMap<Size, Pair<Dimension, URL>>();

	/**
	 * The ID of the backdrop.
	 */
	private String ID;

	/**
	 * Constructs a new MoviePoster.
	 * 
	 * @param ID
	 *            The ID of the poster.
	 */
	public MoviePoster(String ID) {
		this.ID = ID;
		Log
				.log("Creating MoviePoster object with id: " + ID,
						Verbosity.VERBOSE);
	}

	/**
	 * Constructs a new MoviePoster.
	 * 
	 * @param info
	 *            The url and dimension infos of the different sizes of the
	 *            poster.
	 * @param ID
	 *            The ID of the poster.
	 */
	public MoviePoster(Map<Size, Pair<Dimension, URL>> info, String ID) {
		this(ID);
		if (info != null) {
			this.info.putAll(info);
		}
	}

	/**
	 * Returns the Url of the poster for the specified size if it exists,
	 * otherwise null.
	 * 
	 * @param size
	 *            The size of the poster.
	 * @return The Url of the poster for the specified size if it exists,
	 *         otherwise null.
	 */
	public URL getImage(Size size) {
		return info.get(size).getSecond();
	}

	/**
	 * Returns the dimensions of the poster for the specified size if it exists,
	 * otherwise null.
	 * 
	 * @param size
	 *            The size of the poster.
	 * @return The dimension of the poster for the specified size if it exists,
	 *         otherwise null.
	 */
	public Dimension getImageDimension(Size size) {
		return info.get(size).getFirst();
	}

	/**
	 * Sets the image Url and dimension for the provided size.
	 * 
	 * @param size
	 *            The size of the poster.
	 * @param info
	 *            The Url and dimension info of the poster for the specified
	 *            size.
	 */
	public void setImage(Size size, Pair<Dimension, URL> info) {
		this.info.put(size, info);
	}

	/**
	 * The poster ID.
	 * 
	 * @return The poster ID.
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
		URL url = getImage(MoviePoster.Size.THUMB);
		if (url == null)
			url = getImage(MoviePoster.Size.W154);
		if (url == null)
			url = getImage(MoviePoster.Size.COVER);
		if (url == null)
			url = getImage(MoviePoster.Size.W342);
		if (url == null)
			url = getImage(MoviePoster.Size.MID);
		if (url == null)
			url = getImage(MoviePoster.Size.ORIGINAL);
		return url;
	}

	/**
	 * Returns the Url of the largest available size.
	 * 
	 * @return The Url of the largest available size.
	 */
	public URL getLargestImage() {
		URL url = getImage(MoviePoster.Size.ORIGINAL);
		if (url == null)
			url = getImage(MoviePoster.Size.MID);
		if (url == null)
			url = getImage(MoviePoster.Size.W342);
		if (url == null)
			url = getImage(MoviePoster.Size.COVER);
		if (url == null)
			url = getImage(MoviePoster.Size.W154);
		if (url == null)
			url = getImage(MoviePoster.Size.THUMB);
		return url;
	}

	/**
	 * Returns the dimensions of the smallest available size.
	 * 
	 * @return The dimensions of the smallest available size.
	 */
	public Dimension getSmallestImageDimension() {
		Dimension dim = getImageDimension(MoviePoster.Size.THUMB);
		if (dim == null)
			dim = getImageDimension(MoviePoster.Size.W154);
		if (dim == null)
			dim = getImageDimension(MoviePoster.Size.COVER);
		if (dim == null)
			dim = getImageDimension(MoviePoster.Size.W342);
		if (dim == null)
			dim = getImageDimension(MoviePoster.Size.MID);
		if (dim == null)
			dim = getImageDimension(MoviePoster.Size.ORIGINAL);
		return dim;
	}

	/**
	 * Returns the dimensions of the largest available size.
	 * 
	 * @return The dimensions of the largest available size.
	 */
	public Dimension getLargestImageDimension() {
		Dimension dim = getImageDimension(MoviePoster.Size.ORIGINAL);
		if (dim == null)
			dim = getImageDimension(MoviePoster.Size.MID);
		if (dim == null)
			dim = getImageDimension(MoviePoster.Size.W342);
		if (dim == null)
			dim = getImageDimension(MoviePoster.Size.COVER);
		if (dim == null)
			dim = getImageDimension(MoviePoster.Size.W154);
		if (dim == null)
			dim = getImageDimension(MoviePoster.Size.THUMB);
		return dim;
	}

}
