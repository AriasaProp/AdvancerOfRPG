package com.ariasaproject.advancerofrpg.scenes2d.utils;

import com.ariasaproject.advancerofrpg.math.Interpolation;
import com.ariasaproject.advancerofrpg.math.Vector2;
import com.ariasaproject.advancerofrpg.scenes2d.InputEvent;
import com.ariasaproject.advancerofrpg.scenes2d.ui.ScrollPane;
import com.ariasaproject.advancerofrpg.utils.Timer;
import com.ariasaproject.advancerofrpg.utils.Timer.Task;

/**
 * Causes a scroll pane to scroll when a drag goes outside the bounds of the
 * scroll pane. Attach the listener to the actor which will cause scrolling when
 * dragged, usually the scroll pane or the scroll pane's actor.
 * <p>
 * If {@link ScrollPane#setFlickScroll(boolean)} is true, the scroll pane must
 * have {@link ScrollPane#setCancelTouchFocus(boolean)} false. When a drag
 * starts that should drag rather than flick scroll, cancel the scroll pane's
 * touch focus using <code>stage.cancelTouchFocus(scrollPane);</code>. In this
 * case the drag scroll listener must not be attached to the scroll pane, else
 * it would also lose touch focus. Instead it can be attached to the scroll
 * pane's actor.
 * <p>
 * If using drag and drop, {@link DragAndDrop#setCancelTouchFocus(boolean)} must
 * be false.
 *
 * @author Nathan Sweet
 */
public class DragScrollListener extends DragListener {
	static final Vector2 tmpCoords = new Vector2();

	private final ScrollPane scroll;
	private final Task scrollUp;
	private final Task scrollDown;
	Interpolation interpolation = Interpolation.exp5In;
	float minSpeed = 15, maxSpeed = 75, tickSecs = 0.05f;
	long startTime, rampTime = 1750;
	float padTop, padBottom;

	public DragScrollListener(final ScrollPane scroll) {
		this.scroll = scroll;
		scrollUp = new Task() {
			@Override
			public void run() {
				scroll(scroll.getScrollY() - getScrollPixels());
			}
		};
		scrollDown = new Task() {
			@Override
			public void run() {
				scroll(scroll.getScrollY() + getScrollPixels());
			}
		};
	}

	public void setup(float minSpeedPixels, float maxSpeedPixels, float tickSecs, float rampSecs) {
		this.minSpeed = minSpeedPixels;
		this.maxSpeed = maxSpeedPixels;
		this.tickSecs = tickSecs;
		rampTime = (long) (rampSecs * 1000);
	}

	float getScrollPixels() {
		return interpolation.apply(minSpeed, maxSpeed, Math.min(1, (System.currentTimeMillis() - startTime) / (float) rampTime));
	}

	@Override
	public void drag(InputEvent event, float x, float y, int pointer) {
		event.getListenerActor().localToActorCoordinates(scroll, tmpCoords.set(x, y));
		if (isAbove(tmpCoords.y)) {
			scrollDown.cancel();
			if (!scrollUp.isScheduled()) {
				startTime = System.currentTimeMillis();
				Timer.schedule(scrollUp, tickSecs, tickSecs);
			}
			return;
		} else if (isBelow(tmpCoords.y)) {
			scrollUp.cancel();
			if (!scrollDown.isScheduled()) {
				startTime = System.currentTimeMillis();
				Timer.schedule(scrollDown, tickSecs, tickSecs);
			}
			return;
		}
		scrollUp.cancel();
		scrollDown.cancel();
	}

	@Override
	public void dragStop(InputEvent event, float x, float y, int pointer) {
		scrollUp.cancel();
		scrollDown.cancel();
	}

	protected boolean isAbove(float y) {
		return y >= scroll.getHeight() - padTop;
	}

	protected boolean isBelow(float y) {
		return y < padBottom;
	}

	protected void scroll(float y) {
		scroll.setScrollY(y);
	}

	public void setPadding(float padTop, float padBottom) {
		this.padTop = padTop;
		this.padBottom = padBottom;
	}
}
