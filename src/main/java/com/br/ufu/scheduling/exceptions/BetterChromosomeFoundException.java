package com.br.ufu.scheduling.exceptions;

public class BetterChromosomeFoundException extends RuntimeException {
	public BetterChromosomeFoundException() {
		super();
	}

	public BetterChromosomeFoundException(String message) {
		super(message);
	}
}