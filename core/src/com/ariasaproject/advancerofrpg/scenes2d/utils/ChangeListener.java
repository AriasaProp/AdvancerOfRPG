package com.ariasaproject.advancerofrpg.scenes2d.utils;

import com.ariasaproject.advancerofrpg.scenes2d.Actor;
import com.ariasaproject.advancerofrpg.scenes2d.Event;
import com.ariasaproject.advancerofrpg.scenes2d.EventListener;

/**
 * Listener for {@link ChangeEvent}.
 *
 * @author Nathan Sweet
 */
abstract public class ChangeListener implements EventListener {
	@Override
	public boolean handle(Event event) {
		if (!(event instanceof ChangeEvent))
			return false;
		changed((ChangeEvent) event, event.getTarget());
		return false;
	}

	/**
	 * @param actor The event target, which is the actor that emitted the change
	 *              event.
	 */
	abstract public void changed(ChangeEvent event, Actor actor);

	/**
	 * Fired when something in an actor has changed. This is a generic event,
	 * exactly what changed in an actor will vary.
	 *
	 * @author Nathan Sweet
	 */
	static public class ChangeEvent extends Event {
	}
}
