package sneer.pulp.blinkinglights.impl;

import sneer.kernel.container.Inject;
import sneer.pulp.blinkinglights.BlinkingLights;
import sneer.pulp.blinkinglights.Light;
import sneer.pulp.blinkinglights.LightType;
import sneer.pulp.clock.Clock;
import wheel.io.Logger;
import wheel.lang.exceptions.FriendlyException;
import wheel.reactive.lists.ListSignal;
import wheel.reactive.lists.impl.ListRegisterImpl;

class BlinkingLightsImpl implements BlinkingLights {
	
	@Inject
	static private Clock _clock;
	
	private final ListRegisterImpl<Light> _lights = new ListRegisterImpl<Light>();
	
	@Override
	public Light turnOn(LightType type, String caption, Throwable t, int timeout) {
		Light result = prepare(type);
		turnOnIfNecessary(result, caption, "If this problem doesn't go away on its own, get an expert sovereign friend to help you. ;)", t, timeout);
		return result;
	}
	
	@Override
	public Light turnOn(LightType type, String caption, Throwable t) {
		return turnOn(type, caption, t, LightImpl.NEVER);
	}

	@Override
	public Light turnOn(LightType type, String caption, int timeToLive) {
		return turnOn(type, caption, null, timeToLive);
	}

	@Override
	public Light turnOn(LightType type, String caption) {
		return turnOn(type, caption, null);
	}
	
	@Override
	public ListSignal<Light> lights() {
		return _lights.output();
	}
	
	@Override
	public void turnOffIfNecessary(Light light) {
		if (!light.isOn()) return;
		
		_lights.remove(light);
		Logger.log("Light removed: ", light.caption());
		((LightImpl)light).turnOff();
	}
	
	private void turnOffIn(final Light light, int millisFromNow) {
		_clock.wakeUpInAtLeast(millisFromNow, new Runnable() { @Override public void run() {
			turnOffIfNecessary(light);	
		}});
	}

	
	@Override
	public Light prepare(LightType type) {
		return new LightImpl(type);
	}

	
	@Override
	public void turnOnIfNecessary(Light light, FriendlyException e) {
		turnOnIfNecessary(light, e, LightImpl.NEVER);
	}

	
	@Override
	public void turnOnIfNecessary(Light light, FriendlyException e, int timeout) {
		turnOnIfNecessary(light, e.getMessage(), e.getHelp(), e, timeout);
	}

	@Override
	public void turnOnIfNecessary(Light light, String caption, String helpMessage) {
		turnOnIfNecessary(light, caption, helpMessage, null, LightImpl.NEVER);
	}
	
	@Override
	public void turnOnIfNecessary(Light light, String caption, String helpMessage, Throwable t) {
		turnOnIfNecessary(light, caption, helpMessage, t, LightImpl.NEVER);
	}

	
	@Override
	public void turnOnIfNecessary(Light pLight, String caption, String helpMessage, Throwable t, int timeout) {
		if (!(pLight instanceof LightImpl)) throw new IllegalArgumentException();
		if (pLight.isOn()) return;
		
		final LightImpl light = (LightImpl)pLight;
		light._isOn = true;
		_lights.add(light);
		
		light._caption = caption;
		light._error = t;
		light._helpMessage = helpMessage;
		
		if (timeout != LightImpl.NEVER)
			turnOffIn(light, timeout);
	}

}
