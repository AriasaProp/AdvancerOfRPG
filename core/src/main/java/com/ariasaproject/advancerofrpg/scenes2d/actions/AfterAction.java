package com.ariasaproject.advancerofrpg.scenes2d.actions;

import com.ariasaproject.advancerofrpg.scenes2d.Action;
import com.ariasaproject.advancerofrpg.scenes2d.Actor;
import com.ariasaproject.advancerofrpg.utils.Array;

public class AfterAction extends DelegateAction {
    private final Array<Action> waitForActions = new Array<Action>(false, 4);

    @Override
    public void setTarget(Actor target) {
        if (target != null)
            waitForActions.addAll(target.getActions());
        super.setTarget(target);
    }

    @Override
    public void restart() {
        super.restart();
        waitForActions.clear();
    }

    @Override
    protected boolean delegate(float delta) {
        Array<Action> currentActions = target.getActions();
        if (currentActions.size == 1)
            waitForActions.clear();
        for (int i = waitForActions.size - 1; i >= 0; i--) {
            Action action = waitForActions.get(i);
            int index = currentActions.indexOf(action, true);
            if (index == -1)
                waitForActions.removeIndex(i);
        }
        if (waitForActions.size > 0)
            return false;
        return action.act(delta);
    }
}
