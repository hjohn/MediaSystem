package net.sf.jtmdb;

import java.util.HashMap;
import java.util.Map;

/**
 * This enumeration provides the responses from the server when using a POST
 * method.
 *
 * @author Savvas Dalkitsis
 */
public enum ServerResponse {

	/**
	 * Success.
	 */
	SUCCESS("Success", 1),
	/**
	 * Invalid service - This service does not exist.
	 */
	INVALID_SERVICE("Invalid service - This service does not exist.", 2),
	/**
	 * Authentication Failed - You do not have permissions to access the
	 * service.
	 */
	AUTHENTICATION_FAILED(
			"Authentication Failed - You do not have permissions to access the service.",
			3),
	/**
	 * Invalid format - This service doesn't exist in that format.
	 */
	INVALID_FORMAT(
			"Invalid format - This service doesn't exist in that format.", 4),
	/**
	 * Invalid parameters - Your request is missing a required parameter.
	 */
	INVALID_PARAMETERS(
			"Invalid parameters - Your request is missing a required parameter.",
			5),
	/**
	 * Invalid pre-requisite id - The pre-requisite id is invalid or not found.
	 */
	INVALID_PREREQUISITE_ID(
			"Invalid pre-requisite id - The pre-requisite id is invalid or not found.",
			6),
	/**
	 * Invalid API key - You must be granted a valid key.
	 */
	INVALID_API_KEY("Invalid API key - You must be granted a valid key.", 7),
	/**
	 * Duplicate entry - The data you tried to submit already exists.
	 */
	DUPLICATE_ENTRY(
			"Duplicate entry - The data you tried to submit already exists.", 8),
	/**
	 * Service Offline - This service is temporarily offline. Try again later.
	 */
	SERVER_OFFLINE(
			"Service Offline - This service is temporarily offline. Try again later.",
			9),
	/**
	 * Suspended API key - Access to your account has been suspended, contact
	 * TMDb.
	 */
	SUSPENDED_API_KEY(
			"Suspended API key - Access to your account has been suspended, contact TMDb.",
			10),
	/**
	 * Internal error - Something went wrong. Contact TMDb.
	 */
	INTERNAL_ERROR("Internal error - Something went wrong. Contact TMDb.", 11),
	/**
	 * The item/record was updated successfully
	 */
	ITEM_RECORD_UPDATED_SUCCESFULLY("The item/record was updated successfully",
			12),
	/**
	 * There was an unidentified error.
	 */
	UNKNOWN_ERROR("There was an unidentified error.", -1);

	/**
	 * This will hold the responses mapped to their code.
	 */
	private static Map<Integer, ServerResponse> pool;

	/**
	 * The message of the response.
	 */
	private String message;
	/**
	 * The code of the response.
	 */
	private int code;

	/**
	 * The code of the response.
	 *
	 * @return The response of the response.
	 */
	public int getCode() {
		return code;
	}

	/**
	 * Sets the code of the response.
	 *
	 * @param code
	 *            The code of the response.
	 */
	public void setCode(int code) {
		this.code = code;
		if (pool == null) {
			pool = new HashMap<Integer, ServerResponse>();
		}
		pool.put(code, this);
	}

	/**
	 * The message of the response.
	 *
	 * @return The message of the response.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Set the message of the response.
	 *
	 * @param message
	 *            The message of the response.
	 */
	private void setMessage(String message) {
		this.message = message;
	}

	private ServerResponse(String message, int code) {
		setMessage(message);
		setCode(code);
	}

	/**
	 * Returns the appropriate response for the specified ID.
	 *
	 * @param id
	 *            The ID of the response.
	 * @return The appropriate response for the specified ID.
	 */
	public static ServerResponse forID(int id) {
		ServerResponse response = pool.get(id);
		if (response == null) {
			response = UNKNOWN_ERROR;
		}
		return response;
	}

}
