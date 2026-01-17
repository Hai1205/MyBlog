package com.example.userservice.services.apis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.userservice.dtos.response.Response;
import com.example.securitycommon.utils.TimeFormatter;

public class BaseApi {
    protected static final Logger logger = LoggerFactory.getLogger(BaseApi.class);

    protected Response buildErrorResponse(int status, String message) {
        Response response = new Response();
        response.setStatusCode(status);
        response.setMessage(message);
        return response;
    }

    /**
     * Logs the start of a request with readable timestamp
     */
    protected void logRequestStart(long startTime) {
        logger.info("Starting request at {}", TimeFormatter.formatTimestamp(startTime));
    }

    /**
     * Logs the completion of a request with duration
     */
    protected void logRequestComplete(long startTime) {
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        logger.info("Completed request in {}", TimeFormatter.formatDuration(duration));
    }
}
