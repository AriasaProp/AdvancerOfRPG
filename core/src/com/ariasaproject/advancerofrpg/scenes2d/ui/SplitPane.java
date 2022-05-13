package com.ariasaproject.advancerofrpg.scenes2d.ui;

import com.ariasaproject.advancerofrpg.graphics.Color;
import com.ariasaproject.advancerofrpg.graphics.g2d.Batch;
import com.ariasaproject.advancerofrpg.math.Rectangle;
import com.ariasaproject.advancerofrpg.math.Vector2;
import com.ariasaproject.advancerofrpg.scenes2d.Actor;
import com.ariasaproject.advancerofrpg.scenes2d.InputEvent;
import com.ariasaproject.advancerofrpg.scenes2d.InputListener;
import com.ariasaproject.advancerofrpg.scenes2d.Stage;
import com.ariasaproject.advancerofrpg.scenes2d.utils.Drawable;
import com.ariasaproject.advancerofrpg.scenes2d.utils.Layout;
import com.ariasaproject.advancerofrpg.scenes2d.utils.ScissorStack;
import com.ariasaproject.advancerofrpg.utils.Null;

public class SplitPane extends WidgetGroup {
	final Rectangle handleBounds = new Rectangle();
	private final Rectangle firstWidgetBounds = new Rectangle();
	private final Rectangle secondWidgetBounds = new Rectangle();
	private final Rectangle tempScissors = new Rectangle();
	SplitPaneStyle style;
	boolean vertical;
	float splitAmount = 0.5f, minAmount, maxAmount = 1;
	boolean cursorOverHandle;
	Vector2 lastPoint = new Vector2();
	Vector2 handlePosition = new Vector2();
	@Null
	private Actor firstWidget, secondWidget;

	/**
	 * @param firstWidget  May be null.
	 * @param secondWidget May be null.
	 */
	public SplitPane(@Null Actor firstWidget, @Null Actor secondWidget, boolean vertical, Skin skin) {
		this(firstWidget, secondWidget, vertical, skin, "default-" + (vertical ? "vertical" : "horizontal"));
	}

	/**
	 * @param firstWidget  May be null.
	 * @param secondWidget May be null.
	 */
	public SplitPane(@Null Actor firstWidget, @Null Actor secondWidget, boolean vertical, Skin skin, String styleName) {
		this(firstWidget, secondWidget, vertical, skin.get(styleName, SplitPaneStyle.class));
	}

	/**
	 * @param firstWidget  May be null.
	 * @param secondWidget May be null.
	 */
	public SplitPane(@Null Actor firstWidget, @Null Actor secondWidget, boolean vertical, SplitPaneStyle style) {
		this.vertical = vertical;
		setStyle(style);
		setFirstWidget(firstWidget);
		setSecondWidget(secondWidget);
		setSize(getPrefWidth(), getPrefHeight());
		initialize();
	}

	private void initialize() {
		addListener(new InputListener() {
			int draggingPointer = -1;

			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				if (draggingPointer != -1)
					return false;
				if (pointer == 0 && button != 0)
					return false;
				if (handleBounds.contains(x, y)) {
					draggingPointer = pointer;
					lastPoint.set(x, y);
					handlePosition.set(handleBounds.x, handleBounds.y);
					return true;
				}
				return false;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				if (pointer == draggingPointer)
					draggingPointer = -1;
			}

			@Override
			public void touchDragged(InputEvent event, float x, float y, int pointer) {
				if (pointer != draggingPointer)
					return;
				Drawable handle = style.handle;
				if (!vertical) {
					float delta = x - lastPoint.x;
					float availWidth = getWidth() - handle.getMinWidth();
					float dragX = handlePosition.x + delta;
					handlePosition.x = dragX;
					dragX = Math.max(0, dragX);
					dragX = Math.min(availWidth, dragX);
					splitAmount = dragX / availWidth;
					lastPoint.set(x, y);
				} else {
					float delta = y - lastPoint.y;
					float availHeight = getHeight() - handle.getMinHeight();
					float dragY = handlePosition.y + delta;
					handlePosition.y = dragY;
					dragY = Math.max(0, dragY);
					dragY = Math.min(availHeight, dragY);
					splitAmount = 1 - (dragY / availHeight);
					lastPoint.set(x, y);
				}
				invalidate();
			}

			@Override
			public boolean mouseMoved(InputEvent event, float x, float y) {
				cursorOverHandle = handleBounds.contains(x, y);
				return false;
			}
		});
	}

	/**
	 * Returns the split pane's style. Modifying the returned style may not have an
	 * effect until {@link #setStyle(SplitPaneStyle)} is called.
	 */
	public SplitPaneStyle getStyle() {
		return style;
	}

	public void setStyle(SplitPaneStyle style) {
		this.style = style;
		invalidateHierarchy();
	}

	@Override
	public void layout() {
		clampSplitAmount();
		if (!vertical)
			calculateHorizBoundsAndPositions();
		else
			calculateVertBoundsAndPositions();
		Actor firstWidget = this.firstWidget;
		if (firstWidget != null) {
			Rectangle firstWidgetBounds = this.firstWidgetBounds;
			firstWidget.setBounds(firstWidgetBounds.x, firstWidgetBounds.y, firstWidgetBounds.width,
					firstWidgetBounds.height);
			if (firstWidget instanceof Layout)
				((Layout) firstWidget).validate();
		}
		Actor secondWidget = this.secondWidget;
		if (secondWidget != null) {
			Rectangle secondWidgetBounds = this.secondWidgetBounds;
			secondWidget.setBounds(secondWidgetBounds.x, secondWidgetBounds.y, secondWidgetBounds.width,
					secondWidgetBounds.height);
			if (secondWidget instanceof Layout)
				((Layout) secondWidget).validate();
		}
	}

	@Override
	public float getPrefWidth() {
		float first = firstWidget == null ? 0
				: (firstWidget instanceof Layout ? ((Layout) firstWidget).getPrefWidth() : firstWidget.getWidth());
		float second = secondWidget == null ? 0
				: (secondWidget instanceof Layout ? ((Layout) secondWidget).getPrefWidth() : secondWidget.getWidth());
		if (vertical)
			return Math.max(first, second);
		return first + style.handle.getMinWidth() + second;
	}

	@Override
	public float getPrefHeight() {
		float first = firstWidget == null ? 0
				: (firstWidget instanceof Layout ? ((Layout) firstWidget).getPrefHeight() : firstWidget.getHeight());
		float second = secondWidget == null ? 0
				: (secondWidget instanceof Layout ? ((Layout) secondWidget).getPrefHeight() : secondWidget.getHeight());
		if (!vertical)
			return Math.max(first, second);
		return first + style.handle.getMinHeight() + second;
	}

	@Override
	public float getMinWidth() {
		float first = firstWidget instanceof Layout ? ((Layout) firstWidget).getMinWidth() : 0;
		float second = secondWidget instanceof Layout ? ((Layout) secondWidget).getMinWidth() : 0;
		if (vertical)
			return Math.max(first, second);
		return first + style.handle.getMinWidth() + second;
	}

	@Override
	public float getMinHeight() {
		float first = firstWidget instanceof Layout ? ((Layout) firstWidget).getMinHeight() : 0;
		float second = secondWidget instanceof Layout ? ((Layout) secondWidget).getMinHeight() : 0;
		if (!vertical)
			return Math.max(first, second);
		return first + style.handle.getMinHeight() + second;
	}

	public boolean isVertical() {
		return vertical;
	}

	public void setVertical(boolean vertical) {
		if (this.vertical == vertical)
			return;
		this.vertical = vertical;
		invalidateHierarchy();
	}

	private void calculateHorizBoundsAndPositions() {
		Drawable handle = style.handle;
		float height = getHeight();
		float availWidth = getWidth() - handle.getMinWidth();
		float leftAreaWidth = (int) (availWidth * splitAmount);
		float rightAreaWidth = availWidth - leftAreaWidth;
		float handleWidth = handle.getMinWidth();
		firstWidgetBounds.set(0, 0, leftAreaWidth, height);
		secondWidgetBounds.set(leftAreaWidth + handleWidth, 0, rightAreaWidth, height);
		handleBounds.set(leftAreaWidth, 0, handleWidth, height);
	}

	private void calculateVertBoundsAndPositions() {
		Drawable handle = style.handle;
		float width = getWidth();
		float height = getHeight();
		float availHeight = height - handle.getMinHeight();
		float topAreaHeight = (int) (availHeight * splitAmount);
		float bottomAreaHeight = availHeight - topAreaHeight;
		float handleHeight = handle.getMinHeight();
		firstWidgetBounds.set(0, height - topAreaHeight, width, topAreaHeight);
		secondWidgetBounds.set(0, 0, width, bottomAreaHeight);
		handleBounds.set(0, bottomAreaHeight, width, handleHeight);
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		Stage stage = getStage();
		if (stage == null)
			return;
		validate();
		Color color = getColor();
		float alpha = color.a * parentAlpha;
		applyTransform(batch, computeTransform());
		if (firstWidget != null && firstWidget.isVisible()) {
			batch.flush();
			stage.calculateScissors(firstWidgetBounds, tempScissors);
			if (ScissorStack.pushScissors(tempScissors)) {
				firstWidget.draw(batch, alpha);
				batch.flush();
				ScissorStack.popScissors();
			}
		}
		if (secondWidget != null && secondWidget.isVisible()) {
			batch.flush();
			stage.calculateScissors(secondWidgetBounds, tempScissors);
			if (ScissorStack.pushScissors(tempScissors)) {
				secondWidget.draw(batch, alpha);
				batch.flush();
				ScissorStack.popScissors();
			}
		}
		batch.setColor(color.r, color.g, color.b, alpha);
		style.handle.draw(batch, handleBounds.x, handleBounds.y, handleBounds.width, handleBounds.height);
		resetTransform(batch);
	}

	public float getSplitAmount() {
		return splitAmount;
	}

	/**
	 * @param splitAmount The split amount between the min and max amount. This
	 *                    parameter is clamped during layout. See
	 *                    {@link #clampSplitAmount()}.
	 */
	public void setSplitAmount(float splitAmount) {
		this.splitAmount = splitAmount; // will be clamped during layout
		invalidate();
	}

	/**
	 * Called during layout to clamp the {@link #splitAmount} within the set limits.
	 * By default it imposes the limits of the {@linkplain #getMinSplitAmount() min
	 * amount}, {@linkplain #getMaxSplitAmount() max amount}, and min sizes of the
	 * children. This method is internally called in response to layout, so it
	 * should not call {@link #invalidate()}.
	 */
	protected void clampSplitAmount() {
		float effectiveMinAmount = minAmount, effectiveMaxAmount = maxAmount;
		if (vertical) {
			float availableHeight = getHeight() - style.handle.getMinHeight();
			if (firstWidget instanceof Layout)
				effectiveMinAmount = Math.max(effectiveMinAmount,
						Math.min(((Layout) firstWidget).getMinHeight() / availableHeight, 1));
			if (secondWidget instanceof Layout)
				effectiveMaxAmount = Math.min(effectiveMaxAmount,
						1 - Math.min(((Layout) secondWidget).getMinHeight() / availableHeight, 1));
		} else {
			float availableWidth = getWidth() - style.handle.getMinWidth();
			if (firstWidget instanceof Layout)
				effectiveMinAmount = Math.max(effectiveMinAmount,
						Math.min(((Layout) firstWidget).getMinWidth() / availableWidth, 1));
			if (secondWidget instanceof Layout)
				effectiveMaxAmount = Math.min(effectiveMaxAmount,
						1 - Math.min(((Layout) secondWidget).getMinWidth() / availableWidth, 1));
		}
		if (effectiveMinAmount > effectiveMaxAmount) // Locked handle. Average the position.
			splitAmount = 0.5f * (effectiveMinAmount + effectiveMaxAmount);
		else
			splitAmount = Math.max(Math.min(splitAmount, effectiveMaxAmount), effectiveMinAmount);
	}

	public float getMinSplitAmount() {
		return minAmount;
	}

	public void setMinSplitAmount(float minAmount) {
		if (minAmount < 0 || minAmount > 1)
			throw new RuntimeException("minAmount has to be >= 0 and <= 1");
		this.minAmount = minAmount;
	}

	public float getMaxSplitAmount() {
		return maxAmount;
	}

	public void setMaxSplitAmount(float maxAmount) {
		if (maxAmount < 0 || maxAmount > 1)
			throw new RuntimeException("maxAmount has to be >= 0 and <= 1");
		this.maxAmount = maxAmount;
	}

	/**
	 * @param widget May be null.
	 */
	public void setFirstWidget(@Null Actor widget) {
		if (firstWidget != null)
			super.removeActor(firstWidget);
		firstWidget = widget;
		if (widget != null)
			super.addActor(widget);
		invalidate();
	}

	/**
	 * @param widget May be null.
	 */
	public void setSecondWidget(@Null Actor widget) {
		if (secondWidget != null)
			super.removeActor(secondWidget);
		secondWidget = widget;
		if (widget != null)
			super.addActor(widget);
		invalidate();
	}

	@Override
	public void addActor(Actor actor) {
		throw new UnsupportedOperationException("Use SplitPane#setWidget.");
	}

	@Override
	public void addActorAt(int index, Actor actor) {
		throw new UnsupportedOperationException("Use SplitPane#setWidget.");
	}

	@Override
	public void addActorBefore(Actor actorBefore, Actor actor) {
		throw new UnsupportedOperationException("Use SplitPane#setWidget.");
	}

	@Override
	public boolean removeActor(Actor actor) {
		if (actor == null)
			throw new IllegalArgumentException("actor cannot be null.");
		if (actor == firstWidget) {
			setFirstWidget(null);
			return true;
		}
		if (actor == secondWidget) {
			setSecondWidget(null);
			return true;
		}
		return true;
	}

	@Override
	public boolean removeActor(Actor actor, boolean unfocus) {
		if (actor == null)
			throw new IllegalArgumentException("actor cannot be null.");
		if (actor == firstWidget) {
			super.removeActor(actor, unfocus);
			firstWidget = null;
			invalidate();
			return true;
		}
		if (actor == secondWidget) {
			super.removeActor(actor, unfocus);
			secondWidget = null;
			invalidate();
			return true;
		}
		return false;
	}

	@Override
	public Actor removeActorAt(int index, boolean unfocus) {
		Actor actor = super.removeActorAt(index, unfocus);
		if (actor == firstWidget) {
			super.removeActor(actor, unfocus);
			firstWidget = null;
			invalidate();
		} else if (actor == secondWidget) {
			super.removeActor(actor, unfocus);
			secondWidget = null;
			invalidate();
		}
		return actor;
	}

	public boolean isCursorOverHandle() {
		return cursorOverHandle;
	}

	/**
	 * The style for a splitpane, see {@link SplitPane}.
	 *
	 * @author mzechner
	 * @author Nathan Sweet
	 */
	static public class SplitPaneStyle {
		public Drawable handle;

		public SplitPaneStyle() {
		}

		public SplitPaneStyle(Drawable handle) {
			this.handle = handle;
		}

		public SplitPaneStyle(SplitPaneStyle style) {
			this.handle = style.handle;
		}
	}
}
