package sneer.pulp.exceptionhandling.impl;

import sneer.commons.environments.Environments;
import sneer.pulp.exceptionhandling.ExceptionHandler;
import sneer.pulp.log.Logger;

class ExceptionHandlerImpl implements ExceptionHandler {

	@Override
	public void shield(Runnable runnable) {
		try {
			runnable.run();
		} catch (Throwable t) {
			Environments.my(Logger.class).log(t, "Exception shielded.");
		}
	}

}