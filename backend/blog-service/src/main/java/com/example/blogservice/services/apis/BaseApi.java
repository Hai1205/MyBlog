package com.example.blogservice.services.apis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.blogservice.dtos.responses.Response;

public class BaseApi {
    protected static final Logger logger = LoggerFactory.getLogger(BaseApi.class);

    protected Response buildErrorResponse(int status, String message) {
        Response response = new Response();
        response.setStatusCode(status);
        response.setMessage(message);
        return response;
    }
}
