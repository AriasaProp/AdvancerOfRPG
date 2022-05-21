package com.ariasaproject.advancerofrpg.scenes2d.actions;

import com.ariasaproject.advancerofrpg.scenes2d.Action;
import com.ariasaproject.advancerofrpg.scenes2d.Actor;
import com.ariasaproject.advancerofrpg.utils.Pool;

/**
 * Base class for an action that wraps another action.
 *
 * @author Nathan Sweet
 */
abstract public class DelegateAction extends Action {
    protected Action action;

    public Action getAction() {
        return action;
    }

    /**
     * Sets the wrapped action.
     */
    public void setAction(Action action) {
        this.action = action;
    }

    abstract protected boolean delegate(float delta);

    @Override
    public final boolean act(float delta) {
        Pool pool = getPool();
        setPool(null); // Ensure this action can't be returned to the pool inside the delegate action.
        try {
            return delegate(delta);
        } finally {
            setPool(pool);
        }
    }

    @Override
    public void restart() {
        if (action != null)
            action.restart();
    }

    @Override
    public void reset() {
        super.reset();
        action = null;
    }

    @Override
    public void setActor(Actor actor) {
        if (action != null)
            action.setActor(actor);
        super.setActor(actor);
    }

    @Override
    public void setTarget(Actor target) {
        if (action != null)
            action.setTarget(target);
        super.setTarget(target);
    }

    @Override
    public String toString() {
        return super.toString() + (action == null ? "" : "(" + action + ")");
    }
}
