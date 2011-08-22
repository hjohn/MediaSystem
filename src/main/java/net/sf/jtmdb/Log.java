package net.sf.jtmdb;

import java.text.DateFormat;
import java.util.Date;

/**
 * This class provides static methods to print to the log.
 * 
 * @author Savvas Dalkitsis
 */
public class Log {

	/**
	 * This enumeration provides different verbosity levels of log output.
	 * 
	 * @author Savvas Dalkitsis
	 */
	public static enum Verbosity {
		/**
		 * In this mode, the log will contain all possible information.
		 */
		VERBOSE,
		/**
		 * In this mode, the log will contain only general information.
		 */
		NORMAL,
		/**
		 * In this mode, the log will contain only error messages.
		 */
		ERROR;

		/**
		 * This method will test if this verbosity level is greater than the one
		 * provided.
		 * 
		 * @param verbosity
		 *            The level against which to test.
		 * @return True if this level of verbosity is greater than the one
		 *         provided.
		 */
		public boolean moreVerboseThan(Verbosity verbosity) {
			if (verbosity == null) {
				return true;
			}
			switch (this) {
			case VERBOSE:
				return true;

			case NORMAL:
				return verbosity == Verbosity.NORMAL
						|| verbosity == Verbosity.ERROR;

			case ERROR:
				return verbosity == Verbosity.ERROR;
			}
			return false;
		}

	}

	private Log() {

	}

	/**
	 * This is the header of each line in the log.
	 * 
	 * @return The header of each line in the log.
	 */
	private static String header(Verbosity verbosity) {
		Date d = new Date();
		return DateFormat.getDateInstance(DateFormat.SHORT).format(d)
				+ " "
				+ DateFormat.getTimeInstance(DateFormat.LONG).format(d)
				+ " THREAD: "
				+ Thread.currentThread().getName()
				+ ((verbosity == null) ? "" : (" VERBOSITY: " + verbosity
						.toString())) + " : ";
	}

	/**
	 * This method will print the provided string that ends with a newline
	 * character to the log if it is enabled.
	 * 
	 * @param text
	 *            The text to print to the log.
	 */
	public static synchronized void log(String text) {
		log(text, null);
	}

	/**
	 * This method will print the provided string that ends with a newline
	 * character to the log if it is enabled and the Log verbosity level is
	 * equal to or greater than the one provided.
	 * 
	 * @param text
	 *            The text to print to the log.
	 * @param verbosity
	 *            The minimum log verbosity needed to log this text.
	 */
	public static synchronized void log(String text, Verbosity verbosity) {
		if (GeneralSettings.getLogVerbosity().moreVerboseThan(verbosity)
				&& GeneralSettings.isLogEnabled()) {
			GeneralSettings.getLogStream().println(header(verbosity) + text);
		}
	}

	/**
	 * This method will print the stack trace of a throwable to the log if it is
	 * enabled.
	 * 
	 * @param throwable
	 *            The throwable whose stack to print to the log.
	 */
	public static synchronized void log(Throwable throwable) {
		log(throwable, null);
	}

	/**
	 * This method will print the stack trace of a throwable to the log if it is
	 * enabled and the Log verbosity level is equal to or greater than the one
	 * provided.
	 * 
	 * @param throwable
	 *            The throwable whose stack to print to the log.
	 * @param verbosity
	 *            The minimum log verbosity needed to log this throwable.
	 */
	public static synchronized void log(Throwable throwable, Verbosity verbosity) {
		if (GeneralSettings.getLogVerbosity().moreVerboseThan(verbosity)
				&& GeneralSettings.isLogEnabled()) {
			log("", verbosity);
			throwable.printStackTrace(GeneralSettings.getLogStream());
		}
	}

}
