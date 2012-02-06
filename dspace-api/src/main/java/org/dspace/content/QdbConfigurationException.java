package org.dspace.content;

public class QdbConfigurationException extends RuntimeException {

	public QdbConfigurationException(String message){
		super(message);
	}

	public QdbConfigurationException(String message, Throwable cause){
		super(message, cause);
	}
}