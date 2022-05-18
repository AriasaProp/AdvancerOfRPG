package com.ariasaproject.advancerofrpg.scenes2d;

import com.ariasaproject.advancerofrpg.math.Vector2;
import com.ariasaproject.advancerofrpg.utils.Null;

public class InputListener implements EventListener {
	static private final Vector2 tmpCoords = new Vector2();

	@Override
	public boolean handle(Event e) {
		if (!(e instanceof InputEvent))
			return false;
		InputEvent event = (InputEvent) e;
		switch (event.getType()) {
		case keyDown:
			return keyDown(event, event.getKeyCode());
		case keyUp:
			return keyUp(event, event.getKeyCode());
		case keyTyped:
			return keyTyped(event, event.getCharacter());
		default:
			break;
		}
		event.toCoordinates(event.getListenerActor(), tmpCoords);
		switch (event.getType()) {
		case touchDown:
			return touchDown(event, tmpCoords.x, tmpCoords.y, event.getPointer(), event.getButton());
		case touchUp:
			touchUp(event, tmpCoords.x, tmpCoords.y, event.getPointer(), event.getButton());
			return true;
		case touchDragged:
			touchDragged(event, tmpCoords.x, tmpCoords.y, event.getPointer());
			return true;
		case mouseMoved:
			return mouseMoved(event, tmpCoords.x, tmpCoords.y);
		case scrolled:
			return scrolled(event, tmpCoords.x, tmpCoords.y, event.getScrollAmount());
		case enter:
			enter(event, tmpCoords.x, tmpCoords.y, event.getPointer(), event.getRelatedActor());
			return false;
		case exit:
			exit(event, tmpCoords.x, tmpCoords.y, event.getPointer(), event.getRelatedActor());
			return false;
		default:
			break;
		}
		return false;
	}

	public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
		return false;
	}

	public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
	}

	public void touchDragged(InputEvent event, float x, float y, int pointer) {
	}

	public boolean mouseMoved(InputEvent event, float x, float y) {
		return false;
	}

	public void enter(InputEvent event, float x, float y, int pointer, @Null Actor fromActor) {
	}

	public void exit(InputEvent event, float x, float y, int pointer, @Null Actor toActor) {
	}

	/**
	 * Called when the mouse wheel has been scrolled. When true is returned, the
	 * event is {@link Event#handle() handled}.
	 */
	public boolean scrolled(InputEvent event, float x, float y, int amount) {
		return false;
	}

	/**
	 * Called when a key goes down. When true is returned, the event is
	 * {@link Event#handle() handled}.
	 */
	public boolean keyDown(InputEvent event, int keycode) {
		return false;
	}

	/**
	 * Called when a key goes up. When true is returned, the event is
	 * {@link Event#handle() handled}.
	 */
	public boolean keyUp(InputEvent event, int keycode) {
		return false;
	}

	/**
	 * Called when a key is typed. When true is returned, the event is
	 * {@link Event#handle() handled}.
	 *
	 * @param character May be 0 for key typed events that don't map to a character
	 *                  (ctrl, shift, etc).
	 */
	public boolean keyTyped(InputEvent event, char character) {
		return false;
	}
}
