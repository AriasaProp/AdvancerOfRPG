package com.ariasaproject.advancerofrpg.scenes2d.actions;

import com.ariasaproject.advancerofrpg.scenes2d.Action;

/**
 * Removes an actor from the stage.
 *
 * @author Nathan Sweet
 */
public class RemoveActorAction extends Action {
	private boolean removed;

	@Override
	public boolean act(float delta) {
		if (!removed) {
			removed = true;
			target.remove();
		}
		return true;
	}

	@Override
	public void restart() {
		removed = false;
	}
}
