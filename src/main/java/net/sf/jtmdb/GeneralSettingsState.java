package net.sf.jtmdb;

import java.io.PrintStream;
import java.util.Locale;

import net.sf.jtmdb.Log.Verbosity;

/**
 * This interface provides a mechanism with which the state of GeneralSettings
 * can be saved and restored. Implementations of this interface can save and
 * restore the state to mediums such as a file, a network, a database.
 * 
 * @author Savvas Dalkitsis
 */
public interface GeneralSettingsState {

	/**
	 * This method stores the API key.
	 * 
	 * @param apiKey
	 *            The API key.
	 */
	public void storeAPIKey(String apiKey);

	/**
	 * This method stores the log stream.
	 * 
	 * @param logStream
	 *            The log stream.
	 */
	public void storeLogStream(PrintStream logStream);

	/**
	 * This method stores the log enabled flag.
	 * 
	 * @param logEnabled
	 *            The log enabled flag.
	 */
	public void storeLogEnabled(boolean logEnabled);

	/**
	 * This method stores the verbosity of the log.
	 * 
	 * @param logVerbosity
	 *            The verbosity of the log.
	 */
	public void storeLogVerbosity(Verbosity logVerbosity);

	/**
	 * This method stores the locale of the API.
	 * 
	 * @param apiLocale
	 *            The locale of the API.
	 */
	public void storeAPILocale(Locale apiLocale);

	/**
	 * This method restores the API key.
	 * 
	 * @return The API key.
	 */
	public String restoreAPIKey();

	/**
	 * This method restores the log stream.
	 * 
	 * @return The log stream.
	 */
	public PrintStream restoreLogStream();

	/**
	 * This method restores the log enabled flag.
	 * 
	 * @param logEnabled
	 *            The log enabled flag.
	 */
	public boolean restoreLogEnabled();

	/**
	 * This method restores the verbosity of the log.
	 * 
	 * @param logVerbosity
	 *            The verbosity of the log.
	 */
	public Verbosity restoreLogVerbosity();

	/**
	 * This method restores the locale of the API.
	 * 
	 * @param apiLocale
	 *            The locale of the API.
	 */
	public Locale restoreAPILocale();

}
