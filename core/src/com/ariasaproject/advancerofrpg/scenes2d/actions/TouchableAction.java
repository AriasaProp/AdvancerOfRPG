package com.ariasaproject.advancerofrpg.scenes2d.actions;

import com.ariasaproject.advancerofrpg.scenes2d.Action;
import com.ariasaproject.advancerofrpg.scenes2d.Actor;
import com.ariasaproject.advancerofrpg.scenes2d.Touchable;

/**
 * Sets the actor's {@link Actor#setTouchable(Touchable) touchability}.
 *
 * @author Nathan Sweet
 */
public class TouchableAction extends Action {
	private Touchable touchable;
	
	@Override
	public boolean act(float delta) {
		target.setTouchable(touchable);
		return true;
	}

	public Touchable getTouchable() {
		return touchable;
	}

	public void setTouchable(Touchable touchable) {
		this.touchable = touchable;
	}
}
