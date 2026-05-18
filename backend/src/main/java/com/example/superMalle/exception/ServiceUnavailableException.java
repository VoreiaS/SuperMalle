package com.example.superMalle.exception;

/**
 * Exception thrown when a service is unavailable
 * 
 * @author SuperMalle Team
 * @version 1.0
 */
public class ServiceUnavailableException extends RuntimeException {

    private final String serviceName;

    public ServiceUnavailableException(String message) {
        super(message);
        this.serviceName = null;
    }

    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
        this.serviceName = null;
    }

    public ServiceUnavailableException(String serviceName, String message) {
        super(message);
        this.serviceName = serviceName;
    }

    public ServiceUnavailableException(String serviceName, String message, Throwable cause) {
        super(message, cause);
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
}
