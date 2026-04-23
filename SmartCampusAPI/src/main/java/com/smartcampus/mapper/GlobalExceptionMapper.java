/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.mapper;

import com.smartcampus.model.ErrorMessage;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Global catch-all exception mapper.
 * Maps any unexpected exceptions to HTTP 500 Internal Server Error.
 * Prevents leaking internal stack traces to clients for security.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        int status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        String message = "An unexpected error occurred. Please contact support.";

        // If it's already a JAX-RS exception (like 404 NotFound), use its status
        if (exception instanceof javax.ws.rs.WebApplicationException) {
            status = ((javax.ws.rs.WebApplicationException) exception).getResponse().getStatus();
            message = exception.getMessage();
        }

        ErrorMessage errorMessage = new ErrorMessage(
                message,
                status,
                "https://smartcampus.edu/api/docs/errors"
        );

        // Log the actual error server-side for debugging
        if (status == 500) {
            System.err.println("Global exception caught: " + exception.getMessage());
            exception.printStackTrace();
        }

        return Response.status(status)
                .entity(errorMessage)
                .build();
    }
}
