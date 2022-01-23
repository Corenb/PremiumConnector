package eu.horyzon.premiumconnector.exceptions;

import eu.horyzon.premiumconnector.hooks.Hook;

public class InitHookException extends Exception {
	private final Hook hook;

	public InitHookException(Hook hook, String message) {
		super(message);
		this.hook = hook;
	}

	public Hook getHook() {
		return hook;
	}
}