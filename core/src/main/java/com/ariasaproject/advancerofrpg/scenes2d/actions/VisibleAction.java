package com.ariasaproject.advancerofrpg.scenes2d.actions;

import com.ariasaproject.advancerofrpg.scenes2d.Action;
import com.ariasaproject.advancerofrpg.scenes2d.Actor;

/**
 * Sets the actor's {@link Actor#setVisible(boolean) visibility}.
 *
 * @author Nathan Sweet
 */
public class VisibleAction extends Action {
    private boolean visible;

    @Override
    public boolean act(float delta) {
        target.setVisible(visible);
        return true;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
