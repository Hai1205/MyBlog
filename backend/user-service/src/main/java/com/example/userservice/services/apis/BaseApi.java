package com.example.userservice.services.apis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.userservice.dtos.response.Response;

public class BaseApi {
    protected final Logger logger = LoggerFactory.getLogger(BaseApi.class);

    protected Response buildErrorResponse(int status, String message) {
        Response response = new Response();
        response.setStatusCode(status);
        response.setMessage(message);
        return response;
    }
}
