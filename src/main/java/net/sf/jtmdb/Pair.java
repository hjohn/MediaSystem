package net.sf.jtmdb;

import java.io.Serializable;

/**
 * This class represents a pair of objects.
 * 
 * @author Savvas Dalkitsis
 */
public class Pair<K, V> implements Serializable {

	private static final long serialVersionUID = -3592615574714099889L;

	/**
	 * The first object.
	 */
	private K first = null;
	/**
	 * The second object.
	 */
	private V second = null;

	/**
	 * Constructs a Pair with the provided parameters.
	 * 
	 * @param first
	 *            The first object of the pair.
	 * @param second
	 *            The second object of the pair.
	 */
	public Pair(K first, V second) {
		this.first = first;
		this.second = second;
	}

	/**
	 * Constructs an empty Pair with both the objects set to null.
	 */
	public Pair() {
	}

	/**
	 * Gets the first object of the Pair.
	 * 
	 * @return The first object of the Pair.
	 */
	public K getFirst() {
		return first;
	}

	/**
	 * Sets the first object of the Pair.
	 * 
	 * @param first
	 *            The first object of the Pair.
	 */
	public void setFirst(K first) {
		this.first = first;
	}

	/**
	 * Gets the second object of the Pair.
	 * 
	 * @return The second object of the Pair.
	 */
	public V getSecond() {
		return second;
	}

	/**
	 * Set the second object of the pair.
	 * 
	 * @param second
	 *            The second object of the pair.
	 */
	public void setSecond(V second) {
		this.second = second;
	}

}
