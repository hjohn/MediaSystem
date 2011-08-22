package net.sf.jtmdb;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import net.sf.jtmdb.GeneralSettings.Utilities;
import net.sf.jtmdb.Log.Verbosity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class that handles authorization with tmdb. The authentication procedure is
 * as follows. The user must request a token from the server using the method
 * {@link #getToken()}. Then using the method {@link #authorizeToken(String)}
 * the user must browse to the returned URL and authenticate the token. Once the
 * token is authenticated, the user can create an authenticated session using
 * the method {@link #getSession(String)}
 * 
 * @author Savvas Dalkitsis
 */
public class Auth {

	private Auth() {

	}

	/**
	 * Gets the session for the authenticated token. Will return a pair of
	 * Session and the ServerResponse or null if there was an error with the API
	 * or an IOException occurred.
	 * 
	 * @param token
	 *            The authenticated token.
	 * @return A pair of Session and the ServerResponse or null if there was an
	 *         error with the API or an IOException occurred.
	 */
	public static Pair<Session, ServerResponse> getSession(String token) {
		Log.log("Getting session for token " + token, Verbosity.NORMAL);
		if (GeneralSettings.getApiKey() != null
				&& !GeneralSettings.getApiKey().equals("")) {
			try {
				URL call = new URL(GeneralSettings.BASE_URL
						+ GeneralSettings.AUTH_SESSION_URL
						+ GeneralSettings.API_MODE_URL + "/"
						+ GeneralSettings.getApiKey() + "/" + token);
				String jsonString = Utilities.readUrlResponse(call);
				try {
					JSONObject jsonObject = new JSONObject(jsonString
							.toString());
					if (jsonObject.has("code")) {
						return new Pair<Session, ServerResponse>(null,
								ServerResponse.forID(jsonObject.getInt("code")));
					}
					String userName = jsonObject.getString("username");
					String session = jsonObject.getString("session");
					return new Pair<Session, ServerResponse>(new Session(
							userName, session), ServerResponse.SUCCESS);
				} catch (JSONException e) {
					Log.log(e, Verbosity.NORMAL);
					return new Pair<Session, ServerResponse>(null,
							ServerResponse.UNKNOWN_ERROR);
				}
			} catch (IOException e) {
				Log.log(e, Verbosity.ERROR);
			}
		} else {
			Log.log("Error with the API key", Verbosity.ERROR);
		}
		return null;
	}

	/**
	 * Will return the URL that the user must browse to, in order to
	 * authenticate the provided token.
	 * 
	 * @param token
	 *            The token to authenticate.
	 * @return The URL that the user must browse to, in order to authenticate
	 *         the provided token.
	 */
	public static URL authorizeToken(String token) {
		try {
			return new URL("http://www.themoviedb.org/auth/" + token);
		} catch (MalformedURLException e) {
			Log.log(e, Verbosity.ERROR);
		}
		return null;
	}

	/**
	 * Requests a token from the server that can be used to create an
	 * authenticated session. Will be null if an error occurred.
	 * 
	 * @return A token from the server that can be used to create an
	 *         authenticated session. Will be null if an error occurred.
	 * @throws IOException
	 * @throws JSONException
	 */
	public static String getToken() throws IOException, JSONException {
		Log.log("Getting token for authentication", Verbosity.NORMAL);
		if (GeneralSettings.getApiKey() != null
				&& !GeneralSettings.getApiKey().equals("")) {
			try {
				URL call = new URL(GeneralSettings.BASE_URL
						+ GeneralSettings.AUTH_TOKEN_URL
						+ GeneralSettings.API_MODE_URL + "/"
						+ GeneralSettings.getApiKey());
				String jsonString = Utilities.readUrlResponse(call).trim();
				if ((jsonString.startsWith("[") || jsonString.startsWith("{"))
						&& !jsonString.equals("[\"Nothing found.\"]")) {
					JSONObject jsonObject = new JSONObject(jsonString
							.toString());
					String token = jsonObject.getString("token");
					return token;
				} else {
					Log
							.log(
									"Getting token for authentication returned no results",
									Verbosity.NORMAL);
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
}
