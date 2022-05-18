package com.ariasaproject.advancerofrpg.scenes2d;

import static com.ariasaproject.advancerofrpg.utils.Align.bottom;
import static com.ariasaproject.advancerofrpg.utils.Align.left;
import static com.ariasaproject.advancerofrpg.utils.Align.right;
import static com.ariasaproject.advancerofrpg.utils.Align.top;

import com.ariasaproject.advancerofrpg.graphics.Color;
import com.ariasaproject.advancerofrpg.graphics.g2d.Batch;
import com.ariasaproject.advancerofrpg.math.MathUtils;
import com.ariasaproject.advancerofrpg.math.Rectangle;
import com.ariasaproject.advancerofrpg.math.Vector2;
import com.ariasaproject.advancerofrpg.scenes2d.InputEvent.Type;
import com.ariasaproject.advancerofrpg.scenes2d.utils.ClickListener;
import com.ariasaproject.advancerofrpg.scenes2d.utils.ScissorStack;
import com.ariasaproject.advancerofrpg.utils.Align;
import com.ariasaproject.advancerofrpg.utils.Array;
import com.ariasaproject.advancerofrpg.utils.DelayedRemovalArray;
import com.ariasaproject.advancerofrpg.utils.Null;
import com.ariasaproject.advancerofrpg.utils.Pools;
import com.ariasaproject.advancerofrpg.utils.reflect.ClassReflection;

public class Actor {
	final Color color = new Color(1, 1, 1, 1);
	private final DelayedRemovalArray<EventListener> listeners = new DelayedRemovalArray<EventListener>(0);
	private final DelayedRemovalArray<EventListener> captureListeners = new DelayedRemovalArray<EventListener>(0);
	private final Array<Action> actions = new Array<Action>(0);
	@Null
	Group parent;
	float x, y;
	float width, height;
	float originX, originY;
	float scaleX = 1, scaleY = 1;
	float rotation;
	private Stage stage;
	@Null
	private String name;
	private Touchable touchable = Touchable.enabled;
	private boolean visible = true;

	public void draw(Batch batch, float parentAlpha) {
	}

	public void act(float delta) {
		Array<Action> actions = this.actions;
		if (actions.size == 0)
			return;
		try {
			for (int i = 0; i < actions.size; i++) {
				Action action = actions.get(i);
				if (action.act(delta) && i < actions.size) {
					Action current = actions.get(i);
					int actionIndex = current == action ? i : actions.indexOf(action, true);
					if (actionIndex != -1) {
						actions.removeIndex(actionIndex);
						action.setActor(null);
						i--;
					}
				}
			}
		} catch (RuntimeException ex) {
			String context = toString();
			throw new RuntimeException("Actor: " + context.substring(0, Math.min(context.length(), 128)), ex);
		}
	}

	public boolean fire(Event event) {
		if (event.getStage() == null)
			event.setStage(getStage());
		event.setTarget(this);
		Array<Group> ancestors = Pools.obtain(new Array<Group>().getClass());
		Group parent = this.parent;
		while (parent != null) {
			ancestors.add(parent);
			parent = parent.parent;
		}
		try {
			for (int i = ancestors.size - 1; i >= 0; i--) {
				Group currentTarget = ancestors.get(i);
				currentTarget.notify(event, true);
				if (event.isStopped())
					return event.isCancelled();
			}
			// Notify the target capture listeners.
			notify(event, true);
			if (event.isStopped())
				return event.isCancelled();
			// Notify the target listeners.
			notify(event, false);
			if (!event.getBubbles())
				return event.isCancelled();
			if (event.isStopped())
				return event.isCancelled();
			// Notify all parent listeners, starting at the target. Children may stop an
			// event before ancestors receive it.
			for (int i = 0, n = ancestors.size; i < n; i++) {
				ancestors.get(i).notify(event, false);
				if (event.isStopped())
					return event.isCancelled();
			}
			return event.isCancelled();
		} finally {
			ancestors.clear();
			Pools.free(ancestors);
		}
	}

	/**
	 * Notifies this actor's listeners of the event. The event is not propagated to
	 * any parents. Before notifying the listeners, this actor is set as the
	 * {@link Event#getListenerActor() listener actor}. The event
	 * {@link Event#setTarget(Actor) target} must be set before calling this method.
	 * If this actor is not in the stage, the stage must be set before calling this
	 * method.
	 *
	 * @param capture If true, the capture listeners will be notified instead of the
	 *                regular listeners.
	 * @return true of the event was {@link Event#cancel() cancelled}.
	 */
	public boolean notify(Event event, boolean capture) {
		if (event.getTarget() == null)
			throw new IllegalArgumentException("The event target cannot be null.");
		DelayedRemovalArray<EventListener> listeners = capture ? captureListeners : this.listeners;
		if (listeners.size == 0)
			return event.isCancelled();
		event.setListenerActor(this);
		event.setCapture(capture);
		if (event.getStage() == null)
			event.setStage(stage);
		try {
			listeners.begin();
			for (int i = 0, n = listeners.size; i < n; i++) {
				EventListener listener = listeners.get(i);
				if (listener.handle(event)) {
					event.handle();
					if (event instanceof InputEvent) {
						InputEvent inputEvent = (InputEvent) event;
						if (inputEvent.getType() == Type.touchDown) {
							event.getStage().addTouchFocus(listener, this, inputEvent.getTarget(), inputEvent.getPointer(), inputEvent.getButton());
						}
					}
				}
			}
			listeners.end();
		} catch (RuntimeException ex) {
			String context = toString();
			throw new RuntimeException("Actor: " + context.substring(0, Math.min(context.length(), 128)), ex);
		}
		return event.isCancelled();
	}

	@Null
	public Actor hit(float x, float y, boolean touchable) {
		if (touchable && this.touchable != Touchable.enabled)
			return null;
		if (!isVisible())
			return null;
		return x >= 0 && x < width && y >= 0 && y < height ? this : null;
	}

	/**
	 * Removes this actor from its parent, if it has a parent.
	 *
	 * @see Group#removeActor(Actor)
	 */
	public boolean remove() {
		if (parent != null)
			return parent.removeActor(this, true);
		return false;
	}

	/**
	 * Add a listener to receive events that {@link #hit(float, float, boolean) hit}
	 * this actor. See {@link #fire(Event)}.
	 *
	 * @see InputListener
	 * @see ClickListener
	 */
	public boolean addListener(EventListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("listener cannot be null.");
		if (!listeners.contains(listener, true)) {
			listeners.add(listener);
			return true;
		}
		return false;
	}

	public boolean removeListener(EventListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("listener cannot be null.");
		return listeners.removeValue(listener, true);
	}

	public DelayedRemovalArray<EventListener> getListeners() {
		return listeners;
	}

	/**
	 * Adds a listener that is only notified during the capture phase.
	 *
	 * @see #fire(Event)
	 */
	public boolean addCaptureListener(EventListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("listener cannot be null.");
		if (!captureListeners.contains(listener, true))
			captureListeners.add(listener);
		return true;
	}

	public boolean removeCaptureListener(EventListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("listener cannot be null.");
		return captureListeners.removeValue(listener, true);
	}

	public DelayedRemovalArray<EventListener> getCaptureListeners() {
		return captureListeners;
	}

	public void addAction(Action action) {
		action.setActor(this);
		actions.add(action);
	}

	/**
	 * @param action May be null, in which case nothing is done.
	 */
	public void removeAction(@Null Action action) {
		if (action != null && actions.removeValue(action, true))
			action.setActor(null);
	}

	public Array<Action> getActions() {
		return actions;
	}

	/**
	 * Returns true if the actor has one or more actions.
	 */
	public boolean hasActions() {
		return actions.size > 0;
	}

	/**
	 * Removes all actions on this actor.
	 */
	public void clearActions() {
		for (int i = actions.size - 1; i >= 0; i--)
			actions.get(i).setActor(null);
		actions.clear();
	}

	/**
	 * Removes all listeners on this actor.
	 */
	public void clearListeners() {
		listeners.clear();
		captureListeners.clear();
	}

	/**
	 * Removes all actions and listeners on this actor.
	 */
	public void clear() {
		clearActions();
		clearListeners();
	}

	/**
	 * Returns the stage that this actor is currently in, or null if not in a stage.
	 */
	public Stage getStage() {
		return stage;
	}

	/**
	 * Called by the framework when this actor or any parent is added to a group
	 * that is in the stage.
	 *
	 * @param stage May be null if the actor or any parent is no longer in a stage.
	 */
	protected void setStage(Stage stage) {
		this.stage = stage;
	}

	/**
	 * Returns true if this actor is the same as or is the descendant of the
	 * specified actor.
	 */
	public boolean isDescendantOf(Actor actor) {
		if (actor == null)
			throw new IllegalArgumentException("actor cannot be null.");
		Actor parent = this;
		do {
			if (parent == actor)
				return true;
			parent = parent.parent;
		} while (parent != null);
		return false;
	}

	/**
	 * Returns true if this actor is the same as or is the ascendant of the
	 * specified actor.
	 */
	public boolean isAscendantOf(Actor actor) {
		if (actor == null)
			throw new IllegalArgumentException("actor cannot be null.");
		do {
			if (actor == this)
				return true;
			actor = actor.parent;
		} while (actor != null);
		return false;
	}

	/**
	 * Returns this actor or the first ascendant of this actor that is assignable
	 * with the specified type, or null if none were found.
	 */
	@Null
	public <T extends Actor> T firstAscendant(Class<T> type) {
		if (type == null)
			throw new IllegalArgumentException("actor cannot be null.");
		Actor actor = this;
		do {
			if (ClassReflection.isInstance(type, actor))
				return (T) actor;
			actor = actor.parent;
		} while (actor != null);
		return null;
	}

	/**
	 * Returns true if the actor's parent is not null.
	 */
	public boolean hasParent() {
		return parent != null;
	}

	/**
	 * Returns the parent actor, or null if not in a group.
	 */
	@Null
	public Group getParent() {
		return parent;
	}

	/**
	 * Called by the framework when an actor is added to or removed from a group.
	 *
	 * @param parent May be null if the actor has been removed from the parent.
	 */
	protected void setParent(@Null Group parent) {
		this.parent = parent;
	}

	/**
	 * Returns true if input events are processed by this actor.
	 */
	public boolean isTouchable() {
		return touchable == Touchable.enabled;
	}

	public Touchable getTouchable() {
		return touchable;
	}

	/**
	 * Determines how touch events are distributed to this actor. Default is
	 * {@link Touchable#enabled}.
	 */
	public void setTouchable(Touchable touchable) {
		this.touchable = touchable;
	}

	public boolean isVisible() {
		return visible;
	}

	/**
	 * If false, the actor will not be drawn and will not receive touch events.
	 * Default is true.
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * Returns true if this actor and all ancestors are visible.
	 */
	public boolean ancestorsVisible() {
		Actor actor = this;
		do {
			if (!actor.isVisible())
				return false;
			actor = actor.parent;
		} while (actor != null);
		return true;
	}

	/**
	 * Returns true if this actor is the {@link Stage#getKeyboardFocus() keyboard
	 * focus} actor.
	 */
	public boolean hasKeyboardFocus() {
		Stage stage = getStage();
		return stage != null && stage.getKeyboardFocus() == this;
	}

	/**
	 * Returns true if this actor is the {@link Stage#getScrollFocus() scroll focus}
	 * actor.
	 */
	public boolean hasScrollFocus() {
		Stage stage = getStage();
		return stage != null && stage.getScrollFocus() == this;
	}

	/**
	 * Returns true if this actor is a target actor for touch focus.
	 *
	 * @see Stage#addTouchFocus(EventListener, Actor, Actor, int, int)
	 */
	public boolean isTouchFocusTarget() {
		Stage stage = getStage();
		if (stage == null)
			return false;
		for (int i = 0, n = stage.touchFocuses.size; i < n; i++)
			if (stage.touchFocuses.get(i).target == this)
				return true;
		return false;
	}

	public boolean isTouchFocusListener() {
		Stage stage = getStage();
		if (stage == null)
			return false;
		for (int i = 0, n = stage.touchFocuses.size; i < n; i++)
			if (stage.touchFocuses.get(i).listenerActor == this)
				return true;
		return false;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		if (this.x != x) {
			this.x = x;
			positionChanged();
		}
	}

	/**
	 * Returns the X position of the specified {@link Align alignment}.
	 */
	public float getX(int alignment) {
		float x = this.x;
		if ((alignment & right) != 0)
			x += width;
		else if ((alignment & left) == 0) //
			x += width / 2;
		return x;
	}

	/**
	 * Sets the x position using the specified {@link Align alignment}. Note this
	 * may set the position to non-integer coordinates.
	 */
	public void setX(float x, int alignment) {
		if ((alignment & right) != 0)
			x -= width;
		else if ((alignment & left) == 0) //
			x -= width / 2;
		if (this.x != x) {
			this.x = x;
			positionChanged();
		}
	}

	/**
	 * Returns the Y position of the actor's bottom edge.
	 */
	public float getY() {
		return y;
	}

	public void setY(float y) {
		if (this.y != y) {
			this.y = y;
			positionChanged();
		}
	}

	/**
	 * Sets the y position using the specified {@link Align alignment}. Note this
	 * may set the position to non-integer coordinates.
	 */
	public void setY(float y, int alignment) {
		if ((alignment & top) != 0)
			y -= height;
		else if ((alignment & bottom) == 0) //
			y -= height / 2;
		if (this.y != y) {
			this.y = y;
			positionChanged();
		}
	}

	/**
	 * Returns the Y position of the specified {@link Align alignment}.
	 */
	public float getY(int alignment) {
		float y = this.y;
		if ((alignment & top) != 0)
			y += height;
		else if ((alignment & bottom) == 0) //
			y += height / 2;
		return y;
	}

	/**
	 * Sets the position of the actor's bottom left corner.
	 */
	public void setPosition(float x, float y) {
		if (this.x != x || this.y != y) {
			this.x = x;
			this.y = y;
			positionChanged();
		}
	}

	/**
	 * Sets the position using the specified {@link Align alignment}. Note this may
	 * set the position to non-integer coordinates.
	 */
	public void setPosition(float x, float y, int alignment) {
		if ((alignment & right) != 0)
			x -= width;
		else if ((alignment & left) == 0) //
			x -= width / 2;
		if ((alignment & top) != 0)
			y -= height;
		else if ((alignment & bottom) == 0) //
			y -= height / 2;
		if (this.x != x || this.y != y) {
			this.x = x;
			this.y = y;
			positionChanged();
		}
	}

	/**
	 * Add x and y to current position
	 */
	public void moveBy(float x, float y) {
		if (x != 0 || y != 0) {
			this.x += x;
			this.y += y;
			positionChanged();
		}
	}

	public float getWidth() {
		return width;
	}

	public void setWidth(float width) {
		if (this.width != width) {
			this.width = width;
			sizeChanged();
		}
	}

	public float getHeight() {
		return height;
	}

	public void setHeight(float height) {
		if (this.height != height) {
			this.height = height;
			sizeChanged();
		}
	}

	/**
	 * Returns y plus height.
	 */
	public float getTop() {
		return y + height;
	}

	/**
	 * Returns x plus width.
	 */
	public float getRight() {
		return x + width;
	}

	/**
	 * Called when the actor's position has been changed.
	 */
	protected void positionChanged() {
	}

	/**
	 * Called when the actor's size has been changed.
	 */
	protected void sizeChanged() {
	}

	/**
	 * Called when the actor's rotation has been changed.
	 */
	protected void rotationChanged() {
	}

	/**
	 * Sets the width and height.
	 */
	public void setSize(float width, float height) {
		if (this.width != width || this.height != height) {
			this.width = width;
			this.height = height;
			sizeChanged();
		}
	}

	/**
	 * Adds the specified size to the current size.
	 */
	public void sizeBy(float size) {
		if (size != 0) {
			width += size;
			height += size;
			sizeChanged();
		}
	}

	/**
	 * Adds the specified size to the current size.
	 */
	public void sizeBy(float width, float height) {
		if (width != 0 || height != 0) {
			this.width += width;
			this.height += height;
			sizeChanged();
		}
	}

	/**
	 * Set bounds the x, y, width, and height.
	 */
	public void setBounds(float x, float y, float width, float height) {
		if (this.x != x || this.y != y) {
			this.x = x;
			this.y = y;
			positionChanged();
		}
		if (this.width != width || this.height != height) {
			this.width = width;
			this.height = height;
			sizeChanged();
		}
	}

	public float getOriginX() {
		return originX;
	}

	public void setOriginX(float originX) {
		this.originX = originX;
	}

	public float getOriginY() {
		return originY;
	}

	public void setOriginY(float originY) {
		this.originY = originY;
	}

	/**
	 * Sets the origin position which is relative to the actor's bottom left corner.
	 */
	public void setOrigin(float originX, float originY) {
		this.originX = originX;
		this.originY = originY;
	}

	/**
	 * Sets the origin position to the specified {@link Align alignment}.
	 */
	public void setOrigin(int alignment) {
		if ((alignment & left) != 0)
			originX = 0;
		else if ((alignment & right) != 0)
			originX = width;
		else
			originX = width / 2;
		if ((alignment & bottom) != 0)
			originY = 0;
		else if ((alignment & top) != 0)
			originY = height;
		else
			originY = height / 2;
	}

	public float getScaleX() {
		return scaleX;
	}

	public void setScaleX(float scaleX) {
		this.scaleX = scaleX;
	}

	public float getScaleY() {
		return scaleY;
	}

	public void setScaleY(float scaleY) {
		this.scaleY = scaleY;
	}

	/**
	 * Sets the scale for both X and Y
	 */
	public void setScale(float scaleXY) {
		this.scaleX = scaleXY;
		this.scaleY = scaleXY;
	}

	/**
	 * Sets the scale X and scale Y.
	 */
	public void setScale(float scaleX, float scaleY) {
		this.scaleX = scaleX;
		this.scaleY = scaleY;
	}

	/**
	 * Adds the specified scale to the current scale.
	 */
	public void scaleBy(float scale) {
		scaleX += scale;
		scaleY += scale;
	}

	/**
	 * Adds the specified scale to the current scale.
	 */
	public void scaleBy(float scaleX, float scaleY) {
		this.scaleX += scaleX;
		this.scaleY += scaleY;
	}

	public float getRotation() {
		return rotation;
	}

	public void setRotation(float degrees) {
		if (this.rotation != degrees) {
			this.rotation = degrees;
			rotationChanged();
		}
	}

	/**
	 * Adds the specified rotation to the current rotation.
	 */
	public void rotateBy(float amountInDegrees) {
		if (amountInDegrees != 0) {
			rotation = (rotation + amountInDegrees) % 360;
			rotationChanged();
		}
	}

	public void setColor(float r, float g, float b, float a) {
		color.set(r, g, b, a);
	}

	/**
	 * Returns the color the actor will be tinted when drawn. The returned instance
	 * can be modified to change the color.
	 */
	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color.set(color);
	}

	/**
	 * @return May be null.
	 * @see #setName(String)
	 */
	@Null
	public String getName() {
		return name;
	}

	/**
	 * Set the actor's name, which is used for identification convenience and by
	 * {@link #toString()}.
	 *
	 * @param name May be null.
	 * @see Group#findActor(String)
	 */
	public void setName(@Null String name) {
		this.name = name;
	}

	/**
	 * Changes the z-order for this actor so it is in front of all siblings.
	 */
	public void toFront() {
		setZIndex(Integer.MAX_VALUE);
	}

	/**
	 * Changes the z-order for this actor so it is in back of all siblings.
	 */
	public void toBack() {
		setZIndex(0);
	}

	/**
	 * Sets the z-index of this actor. The z-index is the index into the parent's
	 * {@link Group#getChildren() children}, where a lower index is below a higher
	 * index. Setting a z-index higher than the number of children will move the
	 * child to the front. Setting a z-index less than zero is invalid.
	 *
	 * @return true if the z-index changed.
	 */
	public boolean setZIndex(int index) {
		if (index < 0)
			throw new IllegalArgumentException("ZIndex cannot be < 0.");
		Group parent = this.parent;
		if (parent == null)
			return false;
		Array<Actor> children = parent.children;
		if (children.size == 1)
			return false;
		index = Math.min(index, children.size - 1);
		if (children.get(index) == this)
			return false;
		if (!children.removeValue(this, true))
			return false;
		children.insert(index, this);
		return true;
	}

	/**
	 * Returns the z-index of this actor, or -1 if the actor is not in a group.
	 *
	 * @see #setZIndex(int)
	 */
	public int getZIndex() {
		Group parent = this.parent;
		if (parent == null)
			return -1;
		return parent.children.indexOf(this, true);
	}

	/**
	 * Calls {@link #clipBegin(float, float, float, float)} to clip this actor's
	 * bounds.
	 */
	public boolean clipBegin() {
		return clipBegin(x, y, width, height);
	}

	/**
	 * Clips the specified screen aligned rectangle, specified relative to the
	 * transform matrix of the stage's Batch. The transform matrix and the stage's
	 * camera must not have rotational components. Calling this method must be
	 * followed by a call to {@link #clipEnd()} if true is returned.
	 *
	 * @return false if the clipping area is zero and no drawing should occur.
	 * @see ScissorStack
	 */
	public boolean clipBegin(float x, float y, float width, float height) {
		if (width <= 0 || height <= 0)
			return false;
		if (stage == null)
			return false;
		Rectangle tableBounds = Rectangle.tmp;
		tableBounds.x = x;
		tableBounds.y = y;
		tableBounds.width = width;
		tableBounds.height = height;
		Rectangle scissorBounds = Pools.obtain(Rectangle.class);
		stage.calculateScissors(tableBounds, scissorBounds);
		if (ScissorStack.pushScissors(scissorBounds))
			return true;
		Pools.free(scissorBounds);
		return false;
	}

	/**
	 * Ends clipping begun by {@link #clipBegin(float, float, float, float)}.
	 */
	public void clipEnd() {
		Pools.free(ScissorStack.popScissors());
	}

	/**
	 * Transforms the specified point in screen coordinates to the actor's local
	 * coordinate system.
	 *
	 * @see Stage#screenToStageCoordinates(Vector2)
	 */
	public Vector2 screenToLocalCoordinates(Vector2 screenCoords) {
		if (stage == null)
			return screenCoords;
		return stageToLocalCoordinates(stage.screenToStageCoordinates(screenCoords));
	}

	/**
	 * Transforms the specified point in the stage's coordinates to the actor's
	 * local coordinate system.
	 */
	public Vector2 stageToLocalCoordinates(Vector2 stageCoords) {
		if (parent != null)
			parent.stageToLocalCoordinates(stageCoords);
		parentToLocalCoordinates(stageCoords);
		return stageCoords;
	}

	/**
	 * Converts the coordinates given in the parent's coordinate system to this
	 * actor's coordinate system.
	 */
	public Vector2 parentToLocalCoordinates(Vector2 parentCoords) {
		final float rotation = this.rotation;
		final float scaleX = this.scaleX;
		final float scaleY = this.scaleY;
		final float childX = x;
		final float childY = y;
		if (rotation == 0) {
			if (scaleX == 1 && scaleY == 1) {
				parentCoords.x -= childX;
				parentCoords.y -= childY;
			} else {
				final float originX = this.originX;
				final float originY = this.originY;
				parentCoords.x = (parentCoords.x - childX - originX) / scaleX + originX;
				parentCoords.y = (parentCoords.y - childY - originY) / scaleY + originY;
			}
		} else {
			final float cos = (float) Math.cos(rotation * MathUtils.degreesToRadians);
			final float sin = (float) Math.sin(rotation * MathUtils.degreesToRadians);
			final float originX = this.originX;
			final float originY = this.originY;
			final float tox = parentCoords.x - childX - originX;
			final float toy = parentCoords.y - childY - originY;
			parentCoords.x = (tox * cos + toy * sin) / scaleX + originX;
			parentCoords.y = (tox * -sin + toy * cos) / scaleY + originY;
		}
		return parentCoords;
	}

	/**
	 * Transforms the specified point in the actor's coordinates to be in screen
	 * coordinates.
	 *
	 * @see Stage#stageToScreenCoordinates(Vector2)
	 */
	public Vector2 localToScreenCoordinates(Vector2 localCoords) {
		if (stage == null)
			return localCoords;
		return stage.stageToScreenCoordinates(localToAscendantCoordinates(null, localCoords));
	}

	/**
	 * Transforms the specified point in the actor's coordinates to be in the
	 * stage's coordinates.
	 */
	public Vector2 localToStageCoordinates(Vector2 localCoords) {
		return localToAscendantCoordinates(null, localCoords);
	}

	/**
	 * Transforms the specified point in the actor's coordinates to be in the
	 * parent's coordinates.
	 */
	public Vector2 localToParentCoordinates(Vector2 localCoords) {
		final float rotation = -this.rotation;
		final float scaleX = this.scaleX;
		final float scaleY = this.scaleY;
		final float x = this.x;
		final float y = this.y;
		if (rotation == 0) {
			if (scaleX == 1 && scaleY == 1) {
				localCoords.x += x;
				localCoords.y += y;
			} else {
				final float originX = this.originX;
				final float originY = this.originY;
				localCoords.x = (localCoords.x - originX) * scaleX + originX + x;
				localCoords.y = (localCoords.y - originY) * scaleY + originY + y;
			}
		} else {
			final float cos = (float) Math.cos(rotation * MathUtils.degreesToRadians);
			final float sin = (float) Math.sin(rotation * MathUtils.degreesToRadians);
			final float originX = this.originX;
			final float originY = this.originY;
			final float tox = (localCoords.x - originX) * scaleX;
			final float toy = (localCoords.y - originY) * scaleY;
			localCoords.x = (tox * cos + toy * sin) + originX + x;
			localCoords.y = (tox * -sin + toy * cos) + originY + y;
		}
		return localCoords;
	}

	/**
	 * Converts coordinates for this actor to those of a parent actor. The ascendant
	 * does not need to be a direct parent.
	 */
	public Vector2 localToAscendantCoordinates(@Null Actor ascendant, Vector2 localCoords) {
		Actor actor = this;
		do {
			actor.localToParentCoordinates(localCoords);
			actor = actor.parent;
			if (actor == ascendant)
				break;
		} while (actor != null);
		return localCoords;
	}

	/**
	 * Converts coordinates for this actor to those of another actor, which can be
	 * anywhere in the stage.
	 */
	public Vector2 localToActorCoordinates(Actor actor, Vector2 localCoords) {
		localToStageCoordinates(localCoords);
		return actor.stageToLocalCoordinates(localCoords);
	}

	@Override
	public String toString() {
		String name = this.name;
		if (name == null) {
			name = getClass().getName();
			int dotIndex = name.lastIndexOf('.');
			if (dotIndex != -1)
				name = name.substring(dotIndex + 1);
		}
		return name;
	}
}
