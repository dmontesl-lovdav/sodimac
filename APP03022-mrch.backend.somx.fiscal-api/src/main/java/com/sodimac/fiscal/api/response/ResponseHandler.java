package com.sodimac.fiscal.api.response;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseHandler {

	private ResponseHandler() {
		// utility class
	}

	public static ResponseEntity<Object> responseBuilder(Object message, HttpStatus httpStatus, Object responseObject, int errorcode,
			Throwable throwable) {
		Map<String, Object> response = new HashMap<>();
		response.put("message", message);
		response.put("httpStatus", httpStatus.value());
		response.put("throwable", throwable);
		response.put("data", responseObject);
		response.put("idError", "");
		response.put("timeStamp", "");
		response.put("errorCode", errorcode);
		response.put("detailError", "");

		return new ResponseEntity<>(response, httpStatus);
	}
	
	public static ResponseEntity<Object> responseBuilderError(Object message, HttpStatus httpStatus, Object responseObject,
			Throwable throwable, String idError) {
		Map<String, Object> response = new HashMap<>();
		response.put("message", message);
		response.put("httpStatus", httpStatus.value());
		response.put("throwable", throwable);
		response.put("data", "");
		response.put("idError", idError);
		response.put("timeStamp", "");
		response.put("errorCode", "");
		response.put("detailError", responseObject);

		return new ResponseEntity<>(response, httpStatus);
	}
}
