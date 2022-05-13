package com.ariasaproject.advancerofrpg.scenes2d;

import com.ariasaproject.advancerofrpg.utils.Null;
import com.ariasaproject.advancerofrpg.utils.Pool;
import com.ariasaproject.advancerofrpg.utils.Pool.Poolable;

abstract public class Action implements Poolable {
	protected Actor actor;
	protected Actor target;

	@Null
	private Pool pool;

	abstract public boolean act(float delta);

	public void restart() {
	}

	public Actor getActor() {
		return actor;
	}

	public void setActor(Actor actor) {
		this.actor = actor;
		if (target == null)
			setTarget(actor);
		if (actor == null) {
			if (pool != null) {
				pool.free(this);
				pool = null;
			}
		}
	}

	/**
	 * @return null if the action has no target.
	 */
	public Actor getTarget() {
		return target;
	}

	public void setTarget(Actor target) {
		this.target = target;
	}

	@Override
	public void reset() {
		actor = null;
		target = null;
		pool = null;
		restart();
	}

	@Null
	public Pool getPool() {
		return pool;
	}

	public void setPool(@Null Pool pool) {
		this.pool = pool;
	}

	@Override
	public String toString() {
		String name = getClass().getName();
		int dotIndex = name.lastIndexOf('.');
		if (dotIndex != -1)
			name = name.substring(dotIndex + 1);
		if (name.endsWith("Action"))
			name = name.substring(0, name.length() - 6);
		return name;
	}
}
