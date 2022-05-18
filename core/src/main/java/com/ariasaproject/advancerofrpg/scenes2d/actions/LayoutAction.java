package com.ariasaproject.advancerofrpg.scenes2d.actions;

import com.ariasaproject.advancerofrpg.scenes2d.Action;
import com.ariasaproject.advancerofrpg.scenes2d.Actor;
import com.ariasaproject.advancerofrpg.scenes2d.utils.Layout;

/**
 * Sets an actor's {@link Layout#setLayoutEnabled(boolean) layout} to enabled or
 * disabled. The actor must implements {@link Layout}.
 *
 * @author Nathan Sweet
 */
public class LayoutAction extends Action {
	private boolean enabled;

	@Override
	public void setTarget(Actor actor) {
		if (actor != null && !(actor instanceof Layout))
			throw new RuntimeException("Actor must implement layout: " + actor);
		super.setTarget(actor);
	}

	@Override
	public boolean act(float delta) {
		((Layout) target).setLayoutEnabled(enabled);
		return true;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setLayoutEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
