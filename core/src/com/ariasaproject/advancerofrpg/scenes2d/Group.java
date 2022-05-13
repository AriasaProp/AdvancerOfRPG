package com.ariasaproject.advancerofrpg.scenes2d;

import com.ariasaproject.advancerofrpg.graphics.g2d.Batch;
import com.ariasaproject.advancerofrpg.math.Affine2;
import com.ariasaproject.advancerofrpg.math.Matrix4;
import com.ariasaproject.advancerofrpg.math.Rectangle;
import com.ariasaproject.advancerofrpg.math.Vector2;
import com.ariasaproject.advancerofrpg.scenes2d.utils.Cullable;
import com.ariasaproject.advancerofrpg.utils.Array;
import com.ariasaproject.advancerofrpg.utils.Null;
import com.ariasaproject.advancerofrpg.utils.SnapshotArray;

public class Group extends Actor implements Cullable {
	static private final Vector2 tmp = new Vector2();
	final SnapshotArray<Actor> children = new SnapshotArray<Actor>(true, 4, Actor.class);
	private final Affine2 worldTransform = new Affine2();
	private final Matrix4 computedTransform = new Matrix4();
	private final Matrix4 oldTransform = new Matrix4();
	boolean transform = true;
	@Null
	private Rectangle cullingArea;

	@Override
	public void act(float delta) {
		super.act(delta);
		Actor[] actors = children.begin();
		for (int i = 0, n = children.size; i < n; i++)
			actors[i].act(delta);
		children.end();
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		if (transform)
			applyTransform(batch, computeTransform());
		drawChildren(batch, parentAlpha);
		if (transform)
			resetTransform(batch);
	}

	protected void drawChildren(Batch batch, float parentAlpha) {
		parentAlpha *= this.color.a;
		SnapshotArray<Actor> children = this.children;
		Actor[] actors = children.begin();
		Rectangle cullingArea = this.cullingArea;
		if (cullingArea != null) {
			// Draw children only if inside culling area.
			float cullLeft = cullingArea.x;
			float cullRight = cullLeft + cullingArea.width;
			float cullBottom = cullingArea.y;
			float cullTop = cullBottom + cullingArea.height;
			if (transform) {
				for (int i = 0, n = children.size; i < n; i++) {
					Actor child = actors[i];
					if (!child.isVisible())
						continue;
					float cx = child.x, cy = child.y;
					if (cx <= cullRight && cy <= cullTop && cx + child.width >= cullLeft
							&& cy + child.height >= cullBottom)
						child.draw(batch, parentAlpha);
				}
			} else {
				// No transform for this group, offset each child.
				float offsetX = x, offsetY = y;
				x = 0;
				y = 0;
				for (int i = 0, n = children.size; i < n; i++) {
					Actor child = actors[i];
					if (!child.isVisible())
						continue;
					float cx = child.x, cy = child.y;
					if (cx <= cullRight && cy <= cullTop && cx + child.width >= cullLeft
							&& cy + child.height >= cullBottom) {
						child.x = cx + offsetX;
						child.y = cy + offsetY;
						child.draw(batch, parentAlpha);
						child.x = cx;
						child.y = cy;
					}
				}
				x = offsetX;
				y = offsetY;
			}
		} else {
			// No culling, draw all children.
			if (transform) {
				for (int i = 0, n = children.size; i < n; i++) {
					Actor child = actors[i];
					if (!child.isVisible())
						continue;
					child.draw(batch, parentAlpha);
				}
			} else {
				// No transform for this group, offset each child.
				float offsetX = x, offsetY = y;
				x = 0;
				y = 0;
				for (int i = 0, n = children.size; i < n; i++) {
					Actor child = actors[i];
					if (!child.isVisible())
						continue;
					float cx = child.x, cy = child.y;
					child.x = cx + offsetX;
					child.y = cy + offsetY;
					child.draw(batch, parentAlpha);
					child.x = cx;
					child.y = cy;
				}
				x = offsetX;
				y = offsetY;
			}
		}
		children.end();
	}

	/**
	 * Returns the transform for this group's coordinate system.
	 */
	protected Matrix4 computeTransform() {
		Affine2 worldTransform = this.worldTransform;
		float originX = this.originX, originY = this.originY;
		worldTransform.setToTrnRotScl(x + originX, y + originY, rotation, scaleX, scaleY);
		if (originX != 0 || originY != 0)
			worldTransform.translate(-originX, -originY);
		// Find the first parent that transforms.
		Group parentGroup = parent;
		while (parentGroup != null) {
			if (parentGroup.transform)
				break;
			parentGroup = parentGroup.parent;
		}
		if (parentGroup != null)
			worldTransform.preMul(parentGroup.worldTransform);
		computedTransform.set(worldTransform);
		return computedTransform;
	}

	/**
	 * Set the batch's transformation matrix, often with the result of
	 * {@link #computeTransform()}. Note this causes the batch to be flushed.
	 * {@link #resetTransform(Batch)} will restore the transform to what it was
	 * before this call.
	 */
	protected void applyTransform(Batch batch, Matrix4 transform) {
		oldTransform.set(batch.getTransformMatrix());
		batch.setTransformMatrix(transform);
	}

	/**
	 * Restores the batch transform to what it was before
	 * {@link #applyTransform(Batch, Matrix4)}. Note this causes the batch to be
	 * flushed.
	 */
	protected void resetTransform(Batch batch) {
		batch.setTransformMatrix(oldTransform);
	}

	/**
	 * @return May be null.
	 * @see #setCullingArea(Rectangle)
	 */
	@Null
	public Rectangle getCullingArea() {
		return cullingArea;
	}

	/**
	 * Children completely outside of this rectangle will not be drawn. This is only
	 * valid for use with unrotated and unscaled actors.
	 *
	 * @param cullingArea May be null.
	 */
	@Override
	public void setCullingArea(@Null Rectangle cullingArea) {
		this.cullingArea = cullingArea;
	}

	@Override
	@Null
	public Actor hit(float x, float y, boolean touchable) {
		if (touchable && getTouchable() == Touchable.disabled)
			return null;
		if (!isVisible())
			return null;
		Vector2 point = tmp;
		Actor[] childrenArray = children.items;
		for (int i = children.size - 1; i >= 0; i--) {
			Actor child = childrenArray[i];
			child.parentToLocalCoordinates(point.set(x, y));
			Actor hit = child.hit(point.x, point.y, touchable);
			if (hit != null)
				return hit;
		}
		return super.hit(x, y, touchable);
	}

	/**
	 * Called when actors are added to or removed from the group.
	 */
	protected void childrenChanged() {
	}

	/**
	 * Adds an actor as a child of this group, removing it from its previous parent.
	 * If the actor is already a child of this group, no changes are made.
	 */
	public void addActor(Actor actor) {
		if (actor.parent != null) {
			if (actor.parent == this)
				return;
			actor.parent.removeActor(actor, false);
		}
		children.add(actor);
		actor.setParent(this);
		actor.setStage(getStage());
		childrenChanged();
	}

	/**
	 * Adds an actor as a child of this group at a specific index, removing it from
	 * its previous parent. If the actor is already a child of this group, no
	 * changes are made.
	 *
	 * @param index May be greater than the number of children.
	 */
	public void addActorAt(int index, Actor actor) {
		if (actor.parent != null) {
			if (actor.parent == this)
				return;
			actor.parent.removeActor(actor, false);
		}
		if (index >= children.size)
			children.add(actor);
		else
			children.insert(index, actor);
		actor.setParent(this);
		actor.setStage(getStage());
		childrenChanged();
	}

	/**
	 * Adds an actor as a child of this group immediately before another child
	 * actor, removing it from its previous parent. If the actor is already a child
	 * of this group, no changes are made.
	 */
	public void addActorBefore(Actor actorBefore, Actor actor) {
		if (actor.parent != null) {
			if (actor.parent == this)
				return;
			actor.parent.removeActor(actor, false);
		}
		int index = children.indexOf(actorBefore, true);
		children.insert(index, actor);
		actor.setParent(this);
		actor.setStage(getStage());
		childrenChanged();
	}

	/**
	 * Adds an actor as a child of this group immediately after another child actor,
	 * removing it from its previous parent. If the actor is already a child of this
	 * group, no changes are made. If <code>actorAfter</code> is not in this group,
	 * the actor is added as the last child.
	 */
	public void addActorAfter(Actor actorAfter, Actor actor) {
		if (actor.parent != null) {
			if (actor.parent == this)
				return;
			actor.parent.removeActor(actor, false);
		}
		int index = children.indexOf(actorAfter, true);
		if (index == children.size || index == -1)
			children.add(actor);
		else
			children.insert(index + 1, actor);
		actor.setParent(this);
		actor.setStage(getStage());
		childrenChanged();
	}

	/**
	 * Removes an actor from this group and unfocuses it. Calls
	 * {@link #removeActor(Actor, boolean)} with true.
	 */
	public boolean removeActor(Actor actor) {
		return removeActor(actor, true);
	}

	/**
	 * Removes an actor from this group. Calls {@link #removeActorAt(int, boolean)}
	 * with the actor's child index.
	 */
	public boolean removeActor(Actor actor, boolean unfocus) {
		int index = children.indexOf(actor, true);
		if (index == -1)
			return false;
		removeActorAt(index, unfocus);
		return true;
	}

	public Actor removeActorAt(int index, boolean unfocus) {
		Actor actor = children.removeIndex(index);
		if (unfocus) {
			Stage stage = getStage();
			if (stage != null)
				stage.unfocus(actor);
		}
		actor.setParent(null);
		actor.setStage(null);
		childrenChanged();
		return actor;
	}

	/**
	 * Removes all actors from this group.
	 */
	public void clearChildren() {
		Actor[] actors = children.begin();
		for (int i = 0, n = children.size; i < n; i++) {
			Actor child = actors[i];
			child.setStage(null);
			child.setParent(null);
		}
		children.end();
		children.clear();
		childrenChanged();
	}

	/**
	 * Removes all children, actions, and listeners from this group.
	 */
	@Override
	public void clear() {
		super.clear();
		clearChildren();
	}

	/**
	 * Returns the first actor found with the specified name. Note this recursively
	 * compares the name of every actor in the group.
	 */
	@Null
	public <T extends Actor> T findActor(String name) {
		Array<Actor> children = this.children;
		for (int i = 0, n = children.size; i < n; i++)
			if (name.equals(children.get(i).getName()))
				return (T) children.get(i);
		for (int i = 0, n = children.size; i < n; i++) {
			Actor child = children.get(i);
			if (child instanceof Group) {
				T actor = ((Group) child).findActor(name);
				if (actor != null)
					return actor;
			}
		}
		return null;
	}

	@Override
	protected void setStage(Stage stage) {
		super.setStage(stage);
		Actor[] childrenArray = children.items;
		for (int i = 0, n = children.size; i < n; i++)
			childrenArray[i].setStage(stage); // StackOverflowError here means the group is its own ancestor.
	}

	/**
	 * Swaps two actors by index. Returns false if the swap did not occur because
	 * the indexes were out of bounds.
	 */
	public boolean swapActor(int first, int second) {
		int maxIndex = children.size;
		if (first < 0 || first >= maxIndex)
			return false;
		if (second < 0 || second >= maxIndex)
			return false;
		children.swap(first, second);
		return true;
	}

	/**
	 * Swaps two actors. Returns false if the swap did not occur because the actors
	 * are not children of this group.
	 */
	public boolean swapActor(Actor first, Actor second) {
		int firstIndex = children.indexOf(first, true);
		int secondIndex = children.indexOf(second, true);
		if (firstIndex == -1 || secondIndex == -1)
			return false;
		children.swap(firstIndex, secondIndex);
		return true;
	}

	/**
	 * Returns the child at the specified index.
	 */
	public Actor getChild(int index) {
		return children.get(index);
	}

	/**
	 * Returns an ordered list of child actors in this group.
	 */
	public SnapshotArray<Actor> getChildren() {
		return children;
	}

	public boolean hasChildren() {
		return children.size > 0;
	}

	public boolean isTransform() {
		return transform;
	}

	public void setTransform(boolean transform) {
		this.transform = transform;
	}

	/**
	 * Converts coordinates for this group to those of a descendant actor. The
	 * descendant does not need to be a direct child.
	 *
	 * @throws IllegalArgumentException if the specified actor is not a descendant
	 *                                  of this group.
	 */
	public Vector2 localToDescendantCoordinates(Actor descendant, Vector2 localCoords) {
		Group parent = descendant.parent;
		if (parent == null)
			throw new IllegalArgumentException("Child is not a descendant: " + descendant);
		// First convert to the actor's parent coordinates.
		if (parent != this)
			localToDescendantCoordinates(parent, localCoords);
		// Then from each parent down to the descendant.
		descendant.parentToLocalCoordinates(localCoords);
		return localCoords;
	}

	/**
	 * Returns a description of the actor hierarchy, recursively.
	 */
	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder(128);
		toString(buffer, 1);
		buffer.setLength(buffer.length() - 1);
		return buffer.toString();
	}

	void toString(StringBuilder buffer, int indent) {
		buffer.append(super.toString());
		buffer.append('\n');
		Actor[] actors = children.begin();
		for (int i = 0, n = children.size; i < n; i++) {
			for (int ii = 0; ii < indent; ii++)
				buffer.append("|  ");
			Actor actor = actors[i];
			if (actor instanceof Group)
				((Group) actor).toString(buffer, indent + 1);
			else {
				buffer.append(actor);
				buffer.append('\n');
			}
		}
		children.end();
	}
}
