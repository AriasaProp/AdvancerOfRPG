package com.ariasaproject.advancerofrpg.scenes2d.actions;

import com.ariasaproject.advancerofrpg.scenes2d.Action;
import com.ariasaproject.advancerofrpg.scenes2d.EventListener;

/**
 * Removes a listener from an actor.
 *
 * @author Nathan Sweet
 */
public class RemoveListenerAction extends Action {
    private EventListener listener;
    private boolean capture;

    @Override
    public boolean act(float delta) {
        if (capture)
            target.removeCaptureListener(listener);
        else
            target.removeListener(listener);
        return true;
    }

    public EventListener getListener() {
        return listener;
    }

    public void setListener(EventListener listener) {
        this.listener = listener;
    }

    public boolean getCapture() {
        return capture;
    }

    public void setCapture(boolean capture) {
        this.capture = capture;
    }

    @Override
    public void reset() {
        super.reset();
        listener = null;
    }
}
