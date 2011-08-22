package net.sf.jtmdb;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import net.sf.jtmdb.GeneralSettings.Utilities;
import net.sf.jtmdb.Log.Verbosity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class provides methods that links movie information with media formats
 * such as DVDs.
 * 
 * @author Savvas Dalkitsis
 */
public class Media {

	/**
	 * The constructor is private for now until a stateful Media object makes
	 * sense in the site API.
	 */
	private Media() {

	}

	/**
	 * This enumeration provides the type of Media available for tagging Movies
	 * with.
	 * 
	 * @author Savvas Dalkitsis
	 */
	public static enum MediaType {
		/**
		 * The Media is a DVD.
		 */
		DVD("dvd"),
		/**
		 * The Media is a file.
		 */
		FILE("file");

		private String value;

		private MediaType(String value) {
			this.value = value;
		}

		protected String getValue() {
			return value;
		}
	}

	/**
	 * This method allows you to add a file id to the TMDb database. These file
	 * id's can be useful for identifying movies without having to worry about
	 * their file name.</br></br>The method will calculate the hash of the
	 * supplied file for you.</br</br>Requires an authenticated session.
	 * 
	 * @param movieID
	 *            The ID of the TMDb movie you are tagging with the hash.
	 * @param mediaFile
	 *            The file that is the movie to be tagged.
	 * @param fps
	 *            The frames per second of the media.
	 * @param session
	 *            The session to use. Must be authenticated.
	 * @return The response from the server.
	 * @throws IOException
	 */
	public static ServerResponse addID(int movieID, File mediaFile, float fps,
			Session session) throws IOException {
		String mediaHash = generateFileHash(mediaFile);
		return addID(movieID, mediaHash, MediaType.FILE, mediaFile.length(),
				fps, session);
	}

	/**
	 * This method allows you to add a file id to the TMDb database. These file
	 * id's can be useful for identifying movies without having to worry about
	 * their file name.</br></br>The method will calculate the hash of the
	 * supplied file for you.</br</br>Requires an authenticated session.
	 * 
	 * @param movieID
	 *            The ID of the TMDb movie you are tagging with the hash.
	 * @param mediaFile
	 *            The file that is the movie to be tagged.
	 * @param fps
	 *            The frames per second of the media.
	 * @param volumeLabel
	 *            The label of the media.
	 * @param session
	 *            The session to use. Must be authenticated.
	 * @return The response from the server.
	 * @throws IOException
	 */
	public static ServerResponse addID(int movieID, File mediaFile, float fps,
			String volumeLabel, Session session) throws IOException {
		String mediaHash = generateFileHash(mediaFile);
		return addID(movieID, mediaHash, MediaType.FILE, mediaFile.length(),
				fps, volumeLabel, session);
	}

	/**
	 * This method allows you to add a file or disc id to the TMDb database.
	 * These file id's can be useful for identifying movies without having to
	 * worry about their file name.</br> </br> Currently only 2 kinds of id's
	 * are supported. More about these id's and hashes can be found <a
	 * href="http://api.themoviedb.org/2.1/ids-hashes">here</a>.</br> </br>
	 * Requires an authenticated session.
	 * 
	 * @param movieID
	 *            The ID of the TMDb movie you are tagging with the hash.
	 * @param mediaHash
	 *            This is the computed id (hash) for the Movie.
	 * @param mediaType
	 *            The type of the media.
	 * @param byteSize
	 *            The total size of the media file.
	 * @param fps
	 *            The frames per second of the media.
	 * @param session
	 *            The session to use. Must be authenticated.
	 * @return The response from the server.
	 * @throws IOException
	 */
	public static ServerResponse addID(int movieID, String mediaHash,
			MediaType mediaType, long byteSize, float fps, Session session)
			throws IOException {
		return addID(movieID, mediaHash, mediaType, byteSize, fps, null,
				session);
	}

	/**
	 * This method allows you to add a file or disc id to the TMDb database.
	 * These file id's can be useful for identifying movies without having to
	 * worry about their file name.</br> </br> Currently only 2 kinds of id's
	 * are supported. More about these id's and hashes can be found <a
	 * href="http://api.themoviedb.org/2.1/ids-hashes">here</a>.</br> </br>
	 * Requires an authenticated session.
	 * 
	 * @param movieID
	 *            The ID of the TMDb movie you are tagging with the hash.
	 * @param mediaHash
	 *            This is the computed id (hash) for the Movie.
	 * @param mediaType
	 *            The type of the media.
	 * @param byteSize
	 *            The total size of the media file.
	 * @param fps
	 *            The frames per second of the media.
	 * @param volumeLabel
	 *            The label of the media.
	 * @param session
	 *            The session to use. Must be authenticated.
	 * @return The response from the server.
	 * @throws IOException
	 */
	public static ServerResponse addID(int movieID, String mediaHash,
			MediaType mediaType, long byteSize, float fps, String volumeLabel,
			Session session) throws IOException {
		Log
				.log("Adding Media ID for Movie with ID" + movieID,
						Verbosity.NORMAL);
		if (byteSize < 1) {
			Log.log("Byte size must be a positive number.", Verbosity.ERROR);
			return null;
		}
		if (GeneralSettings.getApiKey() != null
				&& !GeneralSettings.getApiKey().equals("")) {
			if (session != null && session.getSession() != null
					&& !session.getSession().equals("")) {
				try {
					URL call = new URL(GeneralSettings.BASE_URL
							+ GeneralSettings.MEDIA_ADD_ID_URL);
					String jsonString;
					if (volumeLabel == null) {
						jsonString = Utilities.postToUrl(call, "type", "json",
								"api_key", GeneralSettings.getApiKey(),
								"session_key", session.getSession(), "id", ""
										+ movieID, "media_id", mediaHash,
								"media_type", mediaType.getValue(),
								"total_size", "" + byteSize, "fps", "" + fps);
					} else {
						jsonString = Utilities.postToUrl(call, "type", "json",
								"api_key", GeneralSettings.getApiKey(),
								"session_key", session.getSession(), "id", ""
										+ movieID, "media_id", mediaHash,
								"media_type", mediaType.getValue(),
								"total_size", "" + byteSize, "fps", "" + fps,
								"volume_label", volumeLabel);
					}
					try {
						JSONObject responseJson = new JSONObject(jsonString);
						if (responseJson.has("code")) {
							int code = responseJson.getInt("code");
							return ServerResponse.forID(code);
						} else {
							Log
									.log(
											"Unknown error while adding Media ID for movie",
											Verbosity.ERROR);
							return ServerResponse.UNKNOWN_ERROR;
						}
					} catch (JSONException e) {
						Log.log(e, Verbosity.ERROR);
						return ServerResponse.UNKNOWN_ERROR;
					}
				} catch (IOException e) {
					Log.log(e, Verbosity.ERROR);
					throw e;
				}
			} else {
				Log.log("Session was null or empty", Verbosity.ERROR);
			}
		} else {
			Log.log("Error with the API key", Verbosity.ERROR);
		}
		return null;
	}

	/**
	 * Gets the info for the movie that is the media file provided. Returns a
	 * list of Movie objects with the normal form (see class description
	 * {@link Movie} and method {@link #isReduced()}). Will return null if a
	 * valid API key was not supplied to the {@link GeneralSettings} or if the
	 * supplied media file was not correspond to a movie in the database.
	 * 
	 * @param mediaFile
	 *            The file to get the Movie info for.
	 * @return A list of Movie objects with the normal form (see class
	 *         description {@link Movie} and method {@link #isReduced()}). Will
	 *         return null if a valid API key was not supplied to the
	 *         {@link GeneralSettings} or if the supplied media file was not
	 *         correspond to a movie in the database.
	 * @throws IOException
	 * @throws JSONException
	 */
	public static List<Movie> getInfo(File mediaFile) throws IOException,
			JSONException {
		return getInfo(generateFileHash(mediaFile), mediaFile.length());
	}

	/**
	 * Gets the info for a specific Movie with the provided DVD ID. Returns a
	 * Movie object with the normal form (see class description {@link Movie}
	 * and method {@link #isReduced()}). Will return null if a valid API key was
	 * not supplied to the {@link GeneralSettings} or if the supplied DVD ID did
	 * not correspond to a movie.
	 * 
	 * @param dvdID
	 *            The ID of the DVD.
	 * @return A Movie object with the normal form (see class description
	 *         {@link Movie} and method {@link #isReduced()}). Will return null
	 *         if a valid API key was not supplied to the
	 *         {@link GeneralSettings} or if the supplied DVD ID did not
	 *         correspond to a movie.
	 * @throws IOException
	 * @throws JSONException
	 */
	public static Movie getInfo(String dvdID) throws IOException, JSONException {
		Log
				.log("Getting info for movie with DVD ID " + dvdID,
						Verbosity.NORMAL);
		if (GeneralSettings.getApiKey() != null
				&& !GeneralSettings.getApiKey().equals("")) {
			try {
				URL call = new URL(GeneralSettings.BASE_URL
						+ GeneralSettings.MEDIA_GETINFO_URL
						+ GeneralSettings.getAPILocaleFormatted() + "/"
						+ GeneralSettings.API_MODE_URL + "/"
						+ GeneralSettings.getApiKey() + "/" + dvdID);
				String jsonString = Utilities.readUrlResponse(call).trim();
				if ((jsonString.startsWith("[") || jsonString.startsWith("{"))
						&& !jsonString.equals("[\"Nothing found.\"]")) {
					JSONArray jsonArray = new JSONArray(jsonString.toString());
					return new Movie(jsonArray);
				} else {
					Log.log("Getting info for Movie with DVD ID " + dvdID
							+ " returned no results", Verbosity.NORMAL);
				}
			} catch (IOException e) {
				Log.log(e, Verbosity.ERROR);
				throw e;
			} catch (JSONException e) {
				Log.log(e, Verbosity.ERROR);
				throw e;
			}
		} else {
			Log.log("Error with the API key", Verbosity.ERROR);
		}
		return null;
	}

	/**
	 * Gets the info for a specific Movie with the provided hash key and byte
	 * size of the file. Returns a list of Movie objects with the normal form
	 * (see class description {@link Movie} and method {@link #isReduced()}).
	 * Will return null if a valid API key was not supplied to the
	 * {@link GeneralSettings} or if the supplied hash and byte size did not
	 * correspond to a movie.
	 * 
	 * @param hash
	 *            The hash of the file.
	 * @param byteSize
	 *            The byte size of the file.
	 * @return A list of Movie objects with the normal form (see class
	 *         description {@link Movie} and method {@link #isReduced()}). Will
	 *         return null if a valid API key was not supplied to the
	 *         {@link GeneralSettings} or if the supplied hash and byte size did
	 *         not correspond to a movie.
	 * @throws IOException
	 * @throws JSONException
	 */
	public static List<Movie> getInfo(String hash, long byteSize)
			throws IOException, JSONException {
		Log.log("Getting info for movie with hash " + hash + " and bytesize "
				+ byteSize, Verbosity.NORMAL);
		if (GeneralSettings.getApiKey() != null
				&& !GeneralSettings.getApiKey().equals("")) {
			try {
				URL call = new URL(GeneralSettings.BASE_URL
						+ GeneralSettings.MEDIA_GETINFO_URL
						+ GeneralSettings.getAPILocaleFormatted() + "/"
						+ GeneralSettings.API_MODE_URL + "/"
						+ GeneralSettings.getApiKey() + "/" + hash + "/"
						+ byteSize);
				String jsonString = Utilities.readUrlResponse(call).trim();
				if ((jsonString.startsWith("[") || jsonString.startsWith("{"))
						&& !jsonString.equals("[\"Nothing found.\"]")) {
					JSONArray jsonArray = new JSONArray(jsonString.toString());
					List<Movie> movies = new LinkedList<Movie>();
					for (int i = 0; i < jsonArray.length(); i++) {
						movies.add(new Movie(jsonArray.getJSONObject(i)));
					}
					return movies;
				} else {
					Log.log("Getting info for Movie with hash " + hash
							+ " and bytesize " + byteSize
							+ " returned no results", Verbosity.NORMAL);
				}
			} catch (IOException e) {
				Log.log(e, Verbosity.ERROR);
				throw e;
			} catch (JSONException e) {
				Log.log(e, Verbosity.ERROR);
				throw e;
			}
		} else {
			Log.log("Error with the API key", Verbosity.ERROR);
		}
		return null;
	}

	/**
	 * This method calculates the hash of the file based on the size + 64bit
	 * checksum of the first and last 64k.
	 * 
	 * @param f
	 *            The file to calculate the checksum for.
	 * @return The hex value of the checksum.
	 * @throws IOException
	 */
	public static String generateFileHash(File f) throws IOException {
		long byteSize = f.length();

		long index = Math.min(byteSize, 1024 * 64);
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
		long head = 0;
		long tail = 0;
		long read = 0;
		byte[] longBuffer = new byte[8];
		int b = 0;
		while ((b = in.read(longBuffer)) != -1 && read < index) {
			long l = 0;
			for (int i = 7; i >= 8 - b; i--) {
				l = l << 8;
				long newL = longBuffer[i];
				l = l | (newL & 255);
			}
			for (int i = 1; i <= 8 - b; i++) {
				l = l << 8;
			}
			read += b;
			head += l;
		}
		in.close();
		in = new BufferedInputStream(new FileInputStream(f));
		in.skip(byteSize - 1024 * 64);
		b = 0;
		while ((b = in.read(longBuffer)) != -1) {
			long l = 0;
			for (int i = 7; i >= 8 - b; i--) {
				l = l << 8;
				long newL = longBuffer[i];
				l = l | (newL & 255);
			}
			for (int i = 1; i <= 8 - b; i++) {
				l = l << 8;
			}
			tail += l;
		}
		in.close();
		return Long.toHexString(byteSize + head + tail);

	}

}
