package com.ariasaproject.advancerofrpg.scenes2d.ui;

import com.ariasaproject.advancerofrpg.graphics.Color;
import com.ariasaproject.advancerofrpg.graphics.g2d.Batch;
import com.ariasaproject.advancerofrpg.scenes2d.Actor;
import com.ariasaproject.advancerofrpg.scenes2d.Touchable;
import com.ariasaproject.advancerofrpg.scenes2d.ui.Label.LabelStyle;
import com.ariasaproject.advancerofrpg.scenes2d.ui.Value.Fixed;
import com.ariasaproject.advancerofrpg.scenes2d.utils.Drawable;
import com.ariasaproject.advancerofrpg.scenes2d.utils.Layout;
import com.ariasaproject.advancerofrpg.utils.Align;
import com.ariasaproject.advancerofrpg.utils.Array;
import com.ariasaproject.advancerofrpg.utils.Null;
import com.ariasaproject.advancerofrpg.utils.Pool;

public class Table extends WidgetGroup {
	static final Pool<Cell> cellPool = new Pool<Cell>() {
		@Override
		protected Cell newObject() {
			return new Cell();
		}
	};
	static public Value backgroundTop = new Value() {
		@Override
		public float get(@Null Actor context) {
			Drawable background = ((Table) context).background;
			return background == null ? 0 : background.getTopHeight();
		}
	};
	static public Value backgroundLeft = new Value() {
		@Override
		public float get(@Null Actor context) {
			Drawable background = ((Table) context).background;
			return background == null ? 0 : background.getLeftWidth();
		}
	};
	static public Value backgroundBottom = new Value() {
		@Override
		public float get(@Null Actor context) {
			Drawable background = ((Table) context).background;
			return background == null ? 0 : background.getBottomHeight();
		}
	};
	static public Value backgroundRight = new Value() {
		@Override
		public float get(@Null Actor context) {
			Drawable background = ((Table) context).background;
			return background == null ? 0 : background.getRightWidth();
		}
	};
	static private float[] columnWeightedWidth, rowWeightedHeight;
	private final Array<Cell> cells = new Array<Cell>(4);
	private final Cell cellDefaults;
	private final Array<Cell> columnDefaults = new Array<Cell>(2);
	Value padTop = backgroundTop, padLeft = backgroundLeft, padBottom = backgroundBottom, padRight = backgroundRight;
	int align = Align.center;
	@Null
	Drawable background;
	boolean round = true;
	private int columns, rows;
	private boolean implicitEndRow;
	private Cell rowDefaults;
	private boolean sizeInvalid = true;
	private float[] columnMinWidth, rowMinHeight;
	private float[] columnPrefWidth, rowPrefHeight;
	private float tableMinWidth, tableMinHeight;
	private float tablePrefWidth, tablePrefHeight;
	private float[] columnWidth, rowHeight;
	private float[] expandWidth, expandHeight;
	private boolean clip;
	@Null
	private Skin skin;

	public Table() {
		this(null);
	}

	public Table(@Null Skin skin) {
		this.skin = skin;
		cellDefaults = obtainCell();
		setTransform(false);
		setTouchable(Touchable.childrenOnly);
	}

	private Cell obtainCell() {
		Cell cell = cellPool.obtain();
		cell.setTable(this);
		return cell;
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		validate();
		if (isTransform()) {
			applyTransform(batch, computeTransform());
			drawBackground(batch, parentAlpha, 0, 0);
			if (clip) {
				batch.flush();
				float padLeft = this.padLeft.get(this), padBottom = this.padBottom.get(this);
				if (clipBegin(padLeft, padBottom, getWidth() - padLeft - padRight.get(this), getHeight() - padBottom - padTop.get(this))) {
					drawChildren(batch, parentAlpha);
					batch.flush();
					clipEnd();
				}
			} else
				drawChildren(batch, parentAlpha);
			resetTransform(batch);
		} else {
			drawBackground(batch, parentAlpha, getX(), getY());
			super.draw(batch, parentAlpha);
		}
	}

	protected void drawBackground(Batch batch, float parentAlpha, float x, float y) {
		if (background == null)
			return;
		Color color = getColor();
		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
		background.draw(batch, x, y, getWidth(), getHeight());
	}

	public Table background(@Null Drawable background) {
		setBackground(background);
		return this;
	}

	public Table background(String drawableName) {
		setBackground(drawableName);
		return this;
	}

	@Null
	public Drawable getBackground() {
		return background;
	}

	public void setBackground(@Null Drawable background) {
		if (this.background == background)
			return;
		float padTopOld = getPadTop(), padLeftOld = getPadLeft(), padBottomOld = getPadBottom(),
				padRightOld = getPadRight();
		this.background = background; // The default pad values use the background's padding.
		float padTopNew = getPadTop(), padLeftNew = getPadLeft(), padBottomNew = getPadBottom(),
				padRightNew = getPadRight();
		if (padTopOld + padBottomOld != padTopNew + padBottomNew || padLeftOld + padRightOld != padLeftNew + padRightNew)
			invalidateHierarchy();
		else if (padTopOld != padTopNew || padLeftOld != padLeftNew || padBottomOld != padBottomNew || padRightOld != padRightNew)
			invalidate();
	}

	public void setBackground(String drawableName) {
		if (skin == null)
			throw new IllegalStateException("Table must have a skin set to use this method.");
		setBackground(skin.getDrawable(drawableName));
	}

	@Override
	@Null
	public Actor hit(float x, float y, boolean touchable) {
		if (clip) {
			if (touchable && getTouchable() == Touchable.disabled)
				return null;
			if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight())
				return null;
		}
		return super.hit(x, y, touchable);
	}

	public boolean getClip() {
		return clip;
	}

	public void setClip(boolean enabled) {
		clip = enabled;
		setTransform(enabled);
		invalidate();
	}

	@Override
	public void invalidate() {
		sizeInvalid = true;
		super.invalidate();
	}

	public <T extends Actor> Cell<T> add(@Null T actor) {
		Cell<T> cell = obtainCell();
		cell.actor = actor;
		// The row was ended for layout, not by the user, so revert it.
		if (implicitEndRow) {
			implicitEndRow = false;
			rows--;
			cells.peek().endRow = false;
		}
		Array<Cell> cells = this.cells;
		int cellCount = cells.size;
		if (cellCount > 0) {
			// Set cell column and row.
			Cell lastCell = cells.peek();
			if (!lastCell.endRow) {
				cell.column = lastCell.column + lastCell.colspan;
				cell.row = lastCell.row;
			} else {
				cell.column = 0;
				cell.row = lastCell.row + 1;
			}
			// Set the index of the cell above.
			if (cell.row > 0) {
				outer: for (int i = cellCount - 1; i >= 0; i--) {
					Cell other = cells.get(i);
					for (int column = other.column, nn = column + other.colspan; column < nn; column++) {
						if (column == cell.column) {
							cell.cellAboveIndex = i;
							break outer;
						}
					}
				}
			}
		} else {
			cell.column = 0;
			cell.row = 0;
		}
		cells.add(cell);
		cell.set(cellDefaults);
		if (cell.column < columnDefaults.size) {
			Cell columnCell = columnDefaults.get(cell.column);
			if (columnCell != null)
				cell.merge(columnCell);
		}
		cell.merge(rowDefaults);
		if (actor != null)
			addActor(actor);
		return cell;
	}

	public Table add(Actor... actors) {
		for (int i = 0, n = actors.length; i < n; i++)
			add(actors[i]);
		return this;
	}

	public Cell<Label> add(@Null CharSequence text) {
		if (skin == null)
			throw new IllegalStateException("Table must have a skin set to use this method.");
		return add(new Label(text, skin));
	}

	public Cell<Label> add(@Null CharSequence text, String labelStyleName) {
		if (skin == null)
			throw new IllegalStateException("Table must have a skin set to use this method.");
		return add(new Label(text, skin.get(labelStyleName, LabelStyle.class)));
	}

	public Cell<Label> add(@Null CharSequence text, String fontName, @Null Color color) {
		if (skin == null)
			throw new IllegalStateException("Table must have a skin set to use this method.");
		return add(new Label(text, new LabelStyle(skin.getFont(fontName), color)));
	}

	public Cell<Label> add(@Null CharSequence text, String fontName, String colorName) {
		if (skin == null)
			throw new IllegalStateException("Table must have a skin set to use this method.");
		return add(new Label(text, new LabelStyle(skin.getFont(fontName), skin.getColor(colorName))));
	}

	public Cell add() {
		return add((Actor) null);
	}

	public Cell<Stack> stack(Actor... actors) {
		Stack stack = new Stack();
		if (actors != null) {
			for (int i = 0, n = actors.length; i < n; i++)
				stack.addActor(actors[i]);
		}
		return add(stack);
	}

	@Override
	public boolean removeActor(Actor actor) {
		return removeActor(actor, true);
	}

	@Override
	public boolean removeActor(Actor actor, boolean unfocus) {
		if (!super.removeActor(actor, unfocus))
			return false;
		Cell cell = getCell(actor);
		if (cell != null)
			cell.actor = null;
		return true;
	}

	@Override
	public Actor removeActorAt(int index, boolean unfocus) {
		Actor actor = super.removeActorAt(index, unfocus);
		Cell cell = getCell(actor);
		if (cell != null)
			cell.actor = null;
		return actor;
	}

	@Override
	public void clearChildren() {
		Array<Cell> cells = this.cells;
		for (int i = cells.size - 1; i >= 0; i--) {
			Cell cell = cells.get(i);
			Actor actor = cell.actor;
			if (actor != null)
				actor.remove();
		}
		cellPool.freeAll(cells);
		cells.clear();
		rows = 0;
		columns = 0;
		if (rowDefaults != null)
			cellPool.free(rowDefaults);
		rowDefaults = null;
		implicitEndRow = false;
		super.clearChildren();
	}

	public void reset() {
		clearChildren();
		padTop = backgroundTop;
		padLeft = backgroundLeft;
		padBottom = backgroundBottom;
		padRight = backgroundRight;
		align = Align.center;
		cellDefaults.reset();
		for (int i = 0, n = columnDefaults.size; i < n; i++) {
			Cell columnCell = columnDefaults.get(i);
			if (columnCell != null)
				cellPool.free(columnCell);
		}
		columnDefaults.clear();
	}

	public Cell row() {
		if (cells.size > 0) {
			if (!implicitEndRow) {
				if (cells.peek().endRow)
					return rowDefaults; // Row was already ended.
				endRow();
			}
			invalidate();
		}
		implicitEndRow = false;
		if (rowDefaults != null)
			cellPool.free(rowDefaults);
		rowDefaults = obtainCell();
		rowDefaults.clear();
		return rowDefaults;
	}

	private void endRow() {
		Array<Cell> cells = this.cells;
		int rowColumns = 0;
		for (int i = cells.size - 1; i >= 0; i--) {
			Cell cell = cells.get(i);
			if (cell.endRow)
				break;
			rowColumns += cell.colspan;
		}
		columns = Math.max(columns, rowColumns);
		rows++;
		cells.peek().endRow = true;
	}

	public Cell columnDefaults(int column) {
		Cell cell = columnDefaults.size > column ? columnDefaults.get(column) : null;
		if (cell == null) {
			cell = obtainCell();
			cell.clear();
			if (column >= columnDefaults.size) {
				for (int i = columnDefaults.size; i < column; i++)
					columnDefaults.add((Cell) null);
				columnDefaults.add(cell);
			} else
				columnDefaults.set(column, cell);
		}
		return cell;
	}

	@Null
	public <T extends Actor> Cell<T> getCell(T actor) {
		Array<Cell> cells = this.cells;
		for (int i = 0, n = cells.size; i < n; i++) {
			Cell c = cells.get(i);
			if (c.actor == actor)
				return c;
		}
		return null;
	}

	public Array<Cell> getCells() {
		return cells;
	}

	@Override
	public float getPrefWidth() {
		if (sizeInvalid)
			computeSize();
		float width = tablePrefWidth;
		if (background != null)
			return Math.max(width, background.getMinWidth());
		return width;
	}

	@Override
	public float getPrefHeight() {
		if (sizeInvalid)
			computeSize();
		float height = tablePrefHeight;
		if (background != null)
			return Math.max(height, background.getMinHeight());
		return height;
	}

	@Override
	public float getMinWidth() {
		if (sizeInvalid)
			computeSize();
		return tableMinWidth;
	}

	@Override
	public float getMinHeight() {
		if (sizeInvalid)
			computeSize();
		return tableMinHeight;
	}

	public Cell defaults() {
		return cellDefaults;
	}

	public Table pad(Value pad) {
		if (pad == null)
			throw new IllegalArgumentException("pad cannot be null.");
		padTop = pad;
		padLeft = pad;
		padBottom = pad;
		padRight = pad;
		sizeInvalid = true;
		return this;
	}

	public Table pad(Value top, Value left, Value bottom, Value right) {
		if (top == null)
			throw new IllegalArgumentException("top cannot be null.");
		if (left == null)
			throw new IllegalArgumentException("left cannot be null.");
		if (bottom == null)
			throw new IllegalArgumentException("bottom cannot be null.");
		if (right == null)
			throw new IllegalArgumentException("right cannot be null.");
		padTop = top;
		padLeft = left;
		padBottom = bottom;
		padRight = right;
		sizeInvalid = true;
		return this;
	}

	public Table padTop(Value padTop) {
		if (padTop == null)
			throw new IllegalArgumentException("padTop cannot be null.");
		this.padTop = padTop;
		sizeInvalid = true;
		return this;
	}

	public Table padLeft(Value padLeft) {
		if (padLeft == null)
			throw new IllegalArgumentException("padLeft cannot be null.");
		this.padLeft = padLeft;
		sizeInvalid = true;
		return this;
	}

	public Table padBottom(Value padBottom) {
		if (padBottom == null)
			throw new IllegalArgumentException("padBottom cannot be null.");
		this.padBottom = padBottom;
		sizeInvalid = true;
		return this;
	}

	public Table padRight(Value padRight) {
		if (padRight == null)
			throw new IllegalArgumentException("padRight cannot be null.");
		this.padRight = padRight;
		sizeInvalid = true;
		return this;
	}

	public Table pad(float pad) {
		pad(Fixed.valueOf(pad));
		return this;
	}

	public Table pad(float top, float left, float bottom, float right) {
		padTop = Fixed.valueOf(top);
		padLeft = Fixed.valueOf(left);
		padBottom = Fixed.valueOf(bottom);
		padRight = Fixed.valueOf(right);
		sizeInvalid = true;
		return this;
	}

	public Table padTop(float padTop) {
		this.padTop = Fixed.valueOf(padTop);
		sizeInvalid = true;
		return this;
	}

	public Table padLeft(float padLeft) {
		this.padLeft = Fixed.valueOf(padLeft);
		sizeInvalid = true;
		return this;
	}

	public Table padBottom(float padBottom) {
		this.padBottom = Fixed.valueOf(padBottom);
		sizeInvalid = true;
		return this;
	}

	public Table padRight(float padRight) {
		this.padRight = Fixed.valueOf(padRight);
		sizeInvalid = true;
		return this;
	}

	public Table align(int align) {
		this.align = align;
		return this;
	}

	public Table center() {
		align = Align.center;
		return this;
	}

	public Table top() {
		align |= Align.top;
		align &= ~Align.bottom;
		return this;
	}

	public Table left() {
		align |= Align.left;
		align &= ~Align.right;
		return this;
	}

	public Table bottom() {
		align |= Align.bottom;
		align &= ~Align.top;
		return this;
	}

	public Table right() {
		align |= Align.right;
		align &= ~Align.left;
		return this;
	}

	public Value getPadTopValue() {
		return padTop;
	}

	public float getPadTop() {
		return padTop.get(this);
	}

	public Value getPadLeftValue() {
		return padLeft;
	}

	public float getPadLeft() {
		return padLeft.get(this);
	}

	public Value getPadBottomValue() {
		return padBottom;
	}

	public float getPadBottom() {
		return padBottom.get(this);
	}

	public Value getPadRightValue() {
		return padRight;
	}

	public float getPadRight() {
		return padRight.get(this);
	}

	public float getPadX() {
		return padLeft.get(this) + padRight.get(this);
	}

	public float getPadY() {
		return padTop.get(this) + padBottom.get(this);
	}

	public int getAlign() {
		return align;
	}

	public int getRow(float y) {
		Array<Cell> cells = this.cells;
		int row = 0;
		y += getPadTop();
		int i = 0, n = cells.size;
		if (n == 0)
			return -1;
		while (i < n) {
			Cell c = cells.get(i++);
			if (c.actorY + c.computedPadTop < y)
				return row;
			if (c.endRow)
				row++;
		}
		return -1;
	}

	public void setRound(boolean round) {
		this.round = round;
	}

	public int getRows() {
		return rows;
	}

	public int getColumns() {
		return columns;
	}

	public float getRowHeight(int rowIndex) {
		if (rowHeight == null)
			return 0;
		return rowHeight[rowIndex];
	}

	public float getRowMinHeight(int rowIndex) {
		if (sizeInvalid)
			computeSize();
		return rowMinHeight[rowIndex];
	}

	public float getRowPrefHeight(int rowIndex) {
		if (sizeInvalid)
			computeSize();
		return rowPrefHeight[rowIndex];
	}

	public float getColumnWidth(int columnIndex) {
		if (columnWidth == null)
			return 0;
		return columnWidth[columnIndex];
	}

	public float getColumnMinWidth(int columnIndex) {
		if (sizeInvalid)
			computeSize();
		return columnMinWidth[columnIndex];
	}

	public float getColumnPrefWidth(int columnIndex) {
		if (sizeInvalid)
			computeSize();
		return columnPrefWidth[columnIndex];
	}

	private float[] ensureSize(float[] array, int size) {
		if (array == null || array.length < size)
			return new float[size];
		for (int i = 0, n = array.length; i < n; i++)
			array[i] = 0;
		return array;
	}

	@Override
	public void layout() {
		float width = getWidth();
		float height = getHeight();
		layout(0, 0, width, height);
		Array<Cell> cells = this.cells;
		if (round) {
			for (int i = 0, n = cells.size; i < n; i++) {
				Cell c = cells.get(i);
				float actorWidth = Math.round(c.actorWidth);
				float actorHeight = Math.round(c.actorHeight);
				float actorX = Math.round(c.actorX);
				float actorY = height - Math.round(c.actorY) - actorHeight;
				c.setActorBounds(actorX, actorY, actorWidth, actorHeight);
				Actor actor = c.actor;
				if (actor != null)
					actor.setBounds(actorX, actorY, actorWidth, actorHeight);
			}
		} else {
			for (int i = 0, n = cells.size; i < n; i++) {
				Cell c = cells.get(i);
				float actorHeight = c.actorHeight;
				float actorY = height - c.actorY - actorHeight;
				c.setActorY(actorY);
				Actor actor = c.actor;
				if (actor != null)
					actor.setBounds(c.actorX, actorY, c.actorWidth, actorHeight);
			}
		}
		// Validate children separately from sizing actors to ensure actors without a
		// cell are validated.
		Array<Actor> children = getChildren();
		for (int i = 0, n = children.size; i < n; i++) {
			Actor child = children.get(i);
			if (child instanceof Layout)
				((Layout) child).validate();
		}
	}

	private void computeSize() {
		sizeInvalid = false;
		Array<Cell> cells = this.cells;
		int cellCount = cells.size;
		// Implicitly End the row for layout purposes.
		if (cellCount > 0 && !cells.peek().endRow) {
			endRow();
			implicitEndRow = true;
		}
		int columns = this.columns, rows = this.rows;
		float[] columnMinWidth = this.columnMinWidth = ensureSize(this.columnMinWidth, columns);
		float[] rowMinHeight = this.rowMinHeight = ensureSize(this.rowMinHeight, rows);
		float[] columnPrefWidth = this.columnPrefWidth = ensureSize(this.columnPrefWidth, columns);
		float[] rowPrefHeight = this.rowPrefHeight = ensureSize(this.rowPrefHeight, rows);
		/* float[] columnWidth = */this.columnWidth = ensureSize(this.columnWidth, columns);
		/* float[] rowHeight = */this.rowHeight = ensureSize(this.rowHeight, rows);
		float[] expandWidth = this.expandWidth = ensureSize(this.expandWidth, columns);
		float[] expandHeight = this.expandHeight = ensureSize(this.expandHeight, rows);
		float spaceRightLast = 0;
		for (int i = 0; i < cellCount; i++) {
			Cell c = cells.get(i);
			int column = c.column, row = c.row, colspan = c.colspan;
			Actor a = c.actor;
			// Collect rows that expand and colspan=1 columns that expand.
			if (c.expandY != 0 && expandHeight[row] == 0)
				expandHeight[row] = c.expandY;
			if (colspan == 1 && c.expandX != 0 && expandWidth[column] == 0)
				expandWidth[column] = c.expandX;
			// Compute combined padding/spacing for cells.
			// Spacing between actors isn't additive, the larger is used. Also, no spacing
			// around edges.
			c.computedPadLeft = c.padLeft.get(a) + (column == 0 ? 0 : Math.max(0, c.spaceLeft.get(a) - spaceRightLast));
			c.computedPadTop = c.padTop.get(a);
			if (c.cellAboveIndex != -1) {
				Cell above = cells.get(c.cellAboveIndex);
				c.computedPadTop += Math.max(0, c.spaceTop.get(a) - above.spaceBottom.get(a));
			}
			float spaceRight = c.spaceRight.get(a);
			c.computedPadRight = c.padRight.get(a) + ((column + colspan) == columns ? 0 : spaceRight);
			c.computedPadBottom = c.padBottom.get(a) + (row == rows - 1 ? 0 : c.spaceBottom.get(a));
			spaceRightLast = spaceRight;
			// Determine minimum and preferred cell sizes.
			float prefWidth = c.prefWidth.get(a);
			float prefHeight = c.prefHeight.get(a);
			float minWidth = c.minWidth.get(a);
			float minHeight = c.minHeight.get(a);
			float maxWidth = c.maxWidth.get(a);
			float maxHeight = c.maxHeight.get(a);
			if (prefWidth < minWidth)
				prefWidth = minWidth;
			if (prefHeight < minHeight)
				prefHeight = minHeight;
			if (maxWidth > 0 && prefWidth > maxWidth)
				prefWidth = maxWidth;
			if (maxHeight > 0 && prefHeight > maxHeight)
				prefHeight = maxHeight;
			if (colspan == 1) { // Spanned column min and pref width is added later.
				float hpadding = c.computedPadLeft + c.computedPadRight;
				columnPrefWidth[column] = Math.max(columnPrefWidth[column], prefWidth + hpadding);
				columnMinWidth[column] = Math.max(columnMinWidth[column], minWidth + hpadding);
			}
			float vpadding = c.computedPadTop + c.computedPadBottom;
			rowPrefHeight[row] = Math.max(rowPrefHeight[row], prefHeight + vpadding);
			rowMinHeight[row] = Math.max(rowMinHeight[row], minHeight + vpadding);
		}
		float uniformMinWidth = 0, uniformMinHeight = 0;
		float uniformPrefWidth = 0, uniformPrefHeight = 0;
		for (int i = 0; i < cellCount; i++) {
			Cell c = cells.get(i);
			int column = c.column;
			// Colspan with expand will expand all spanned columns if none of the spanned
			// columns have expand.
			int expandX = c.expandX;
			outer: if (expandX != 0) {
				int nn = column + c.colspan;
				for (int ii = column; ii < nn; ii++)
					if (expandWidth[ii] != 0)
						break outer;
				for (int ii = column; ii < nn; ii++)
					expandWidth[ii] = expandX;
			}
			// Collect uniform sizes.
			if (c.uniformX && c.colspan == 1) {
				float hpadding = c.computedPadLeft + c.computedPadRight;
				uniformMinWidth = Math.max(uniformMinWidth, columnMinWidth[column] - hpadding);
				uniformPrefWidth = Math.max(uniformPrefWidth, columnPrefWidth[column] - hpadding);
			}
			if (c.uniformY) {
				float vpadding = c.computedPadTop + c.computedPadBottom;
				uniformMinHeight = Math.max(uniformMinHeight, rowMinHeight[c.row] - vpadding);
				uniformPrefHeight = Math.max(uniformPrefHeight, rowPrefHeight[c.row] - vpadding);
			}
		}
		// Size uniform cells to the same width/height.
		if (uniformPrefWidth > 0 || uniformPrefHeight > 0) {
			for (int i = 0; i < cellCount; i++) {
				Cell c = cells.get(i);
				if (uniformPrefWidth > 0 && c.uniformX == Boolean.TRUE && c.colspan == 1) {
					float hpadding = c.computedPadLeft + c.computedPadRight;
					columnMinWidth[c.column] = uniformMinWidth + hpadding;
					columnPrefWidth[c.column] = uniformPrefWidth + hpadding;
				}
				if (uniformPrefHeight > 0 && c.uniformY == Boolean.TRUE) {
					float vpadding = c.computedPadTop + c.computedPadBottom;
					rowMinHeight[c.row] = uniformMinHeight + vpadding;
					rowPrefHeight[c.row] = uniformPrefHeight + vpadding;
				}
			}
		}
		// Distribute any additional min and pref width added by colspanned cells to the
		// columns spanned.
		for (int i = 0; i < cellCount; i++) {
			Cell c = cells.get(i);
			int colspan = c.colspan;
			if (colspan == 1)
				continue;
			int column = c.column;
			Actor a = c.actor;
			float minWidth = c.minWidth.get(a);
			float prefWidth = c.prefWidth.get(a);
			float maxWidth = c.maxWidth.get(a);
			if (prefWidth < minWidth)
				prefWidth = minWidth;
			if (maxWidth > 0 && prefWidth > maxWidth)
				prefWidth = maxWidth;
			float spannedMinWidth = -(c.computedPadLeft + c.computedPadRight), spannedPrefWidth = spannedMinWidth;
			float totalExpandWidth = 0;
			for (int ii = column, nn = ii + colspan; ii < nn; ii++) {
				spannedMinWidth += columnMinWidth[ii];
				spannedPrefWidth += columnPrefWidth[ii];
				totalExpandWidth += expandWidth[ii]; // Distribute extra space using expand, if any columns have expand.
			}
			float extraMinWidth = Math.max(0, minWidth - spannedMinWidth);
			float extraPrefWidth = Math.max(0, prefWidth - spannedPrefWidth);
			for (int ii = column, nn = ii + colspan; ii < nn; ii++) {
				float ratio = totalExpandWidth == 0 ? 1f / colspan : expandWidth[ii] / totalExpandWidth;
				columnMinWidth[ii] += extraMinWidth * ratio;
				columnPrefWidth[ii] += extraPrefWidth * ratio;
			}
		}
		// Determine table min and pref size.
		tableMinWidth = 0;
		tableMinHeight = 0;
		tablePrefWidth = 0;
		tablePrefHeight = 0;
		for (int i = 0; i < columns; i++) {
			tableMinWidth += columnMinWidth[i];
			tablePrefWidth += columnPrefWidth[i];
		}
		for (int i = 0; i < rows; i++) {
			tableMinHeight += rowMinHeight[i];
			tablePrefHeight += Math.max(rowMinHeight[i], rowPrefHeight[i]);
		}
		float hpadding = padLeft.get(this) + padRight.get(this);
		float vpadding = padTop.get(this) + padBottom.get(this);
		tableMinWidth = tableMinWidth + hpadding;
		tableMinHeight = tableMinHeight + vpadding;
		tablePrefWidth = Math.max(tablePrefWidth + hpadding, tableMinWidth);
		tablePrefHeight = Math.max(tablePrefHeight + vpadding, tableMinHeight);
	}

	private void layout(float layoutX, float layoutY, float layoutWidth, float layoutHeight) {
		Array<Cell> cells = this.cells;
		int cellCount = cells.size;
		if (sizeInvalid)
			computeSize();
		float padLeft = this.padLeft.get(this);
		float hpadding = padLeft + padRight.get(this);
		float padTop = this.padTop.get(this);
		float vpadding = padTop + padBottom.get(this);
		int columns = this.columns, rows = this.rows;
		float[] expandWidth = this.expandWidth, expandHeight = this.expandHeight;
		float[] columnWidth = this.columnWidth, rowHeight = this.rowHeight;
		float totalExpandWidth = 0, totalExpandHeight = 0;
		for (int i = 0; i < columns; i++)
			totalExpandWidth += expandWidth[i];
		for (int i = 0; i < rows; i++)
			totalExpandHeight += expandHeight[i];
		// Size columns and rows between min and pref size using (preferred - min) size
		// to weight distribution of extra space.
		float[] columnWeightedWidth;
		float totalGrowWidth = tablePrefWidth - tableMinWidth;
		if (totalGrowWidth == 0)
			columnWeightedWidth = columnMinWidth;
		else {
			float extraWidth = Math.min(totalGrowWidth, Math.max(0, layoutWidth - tableMinWidth));
			columnWeightedWidth = Table.columnWeightedWidth = ensureSize(Table.columnWeightedWidth, columns);
			float[] columnMinWidth = this.columnMinWidth, columnPrefWidth = this.columnPrefWidth;
			for (int i = 0; i < columns; i++) {
				float growWidth = columnPrefWidth[i] - columnMinWidth[i];
				float growRatio = growWidth / totalGrowWidth;
				columnWeightedWidth[i] = columnMinWidth[i] + extraWidth * growRatio;
			}
		}
		float[] rowWeightedHeight;
		float totalGrowHeight = tablePrefHeight - tableMinHeight;
		if (totalGrowHeight == 0)
			rowWeightedHeight = rowMinHeight;
		else {
			rowWeightedHeight = Table.rowWeightedHeight = ensureSize(Table.rowWeightedHeight, rows);
			float extraHeight = Math.min(totalGrowHeight, Math.max(0, layoutHeight - tableMinHeight));
			float[] rowMinHeight = this.rowMinHeight, rowPrefHeight = this.rowPrefHeight;
			for (int i = 0; i < rows; i++) {
				float growHeight = rowPrefHeight[i] - rowMinHeight[i];
				float growRatio = growHeight / totalGrowHeight;
				rowWeightedHeight[i] = rowMinHeight[i] + extraHeight * growRatio;
			}
		}
		// Determine actor and cell sizes (before expand or fill).
		for (int i = 0; i < cellCount; i++) {
			Cell c = cells.get(i);
			int column = c.column, row = c.row;
			Actor a = c.actor;
			float spannedWeightedWidth = 0;
			int colspan = c.colspan;
			for (int ii = column, nn = ii + colspan; ii < nn; ii++)
				spannedWeightedWidth += columnWeightedWidth[ii];
			float weightedHeight = rowWeightedHeight[row];
			float prefWidth = c.prefWidth.get(a);
			float prefHeight = c.prefHeight.get(a);
			float minWidth = c.minWidth.get(a);
			float minHeight = c.minHeight.get(a);
			float maxWidth = c.maxWidth.get(a);
			float maxHeight = c.maxHeight.get(a);
			if (prefWidth < minWidth)
				prefWidth = minWidth;
			if (prefHeight < minHeight)
				prefHeight = minHeight;
			if (maxWidth > 0 && prefWidth > maxWidth)
				prefWidth = maxWidth;
			if (maxHeight > 0 && prefHeight > maxHeight)
				prefHeight = maxHeight;
			c.actorWidth = Math.min(spannedWeightedWidth - c.computedPadLeft - c.computedPadRight, prefWidth);
			c.actorHeight = Math.min(weightedHeight - c.computedPadTop - c.computedPadBottom, prefHeight);
			if (colspan == 1)
				columnWidth[column] = Math.max(columnWidth[column], spannedWeightedWidth);
			rowHeight[row] = Math.max(rowHeight[row], weightedHeight);
		}
		// Distribute remaining space to any expanding columns/rows.
		if (totalExpandWidth > 0) {
			float extra = layoutWidth - hpadding;
			for (int i = 0; i < columns; i++)
				extra -= columnWidth[i];
			if (extra > 0) { // layoutWidth < tableMinWidth.
				float used = 0;
				int lastIndex = 0;
				for (int i = 0; i < columns; i++) {
					if (expandWidth[i] == 0)
						continue;
					float amount = extra * expandWidth[i] / totalExpandWidth;
					columnWidth[i] += amount;
					used += amount;
					lastIndex = i;
				}
				columnWidth[lastIndex] += extra - used;
			}
		}
		if (totalExpandHeight > 0) {
			float extra = layoutHeight - vpadding;
			for (int i = 0; i < rows; i++)
				extra -= rowHeight[i];
			if (extra > 0) { // layoutHeight < tableMinHeight.
				float used = 0;
				int lastIndex = 0;
				for (int i = 0; i < rows; i++) {
					if (expandHeight[i] == 0)
						continue;
					float amount = extra * expandHeight[i] / totalExpandHeight;
					rowHeight[i] += amount;
					used += amount;
					lastIndex = i;
				}
				rowHeight[lastIndex] += extra - used;
			}
		}
		// Distribute any additional width added by colspanned cells to the columns
		// spanned.
		for (int i = 0; i < cellCount; i++) {
			Cell c = cells.get(i);
			int colspan = c.colspan;
			if (colspan == 1)
				continue;
			float extraWidth = 0;
			for (int column = c.column, nn = column + colspan; column < nn; column++)
				extraWidth += columnWeightedWidth[column] - columnWidth[column];
			extraWidth -= Math.max(0, c.computedPadLeft + c.computedPadRight);
			extraWidth /= colspan;
			if (extraWidth > 0) {
				for (int column = c.column, nn = column + colspan; column < nn; column++)
					columnWidth[column] += extraWidth;
			}
		}
		// Determine table size.
		float tableWidth = hpadding, tableHeight = vpadding;
		for (int i = 0; i < columns; i++)
			tableWidth += columnWidth[i];
		for (int i = 0; i < rows; i++)
			tableHeight += rowHeight[i];
		// Position table within the container.
		int align = this.align;
		float x = layoutX + padLeft;
		if ((align & Align.right) != 0)
			x += layoutWidth - tableWidth;
		else if ((align & Align.left) == 0) // Center
			x += (layoutWidth - tableWidth) / 2;
		float y = layoutY + padTop;
		if ((align & Align.bottom) != 0)
			y += layoutHeight - tableHeight;
		else if ((align & Align.top) == 0) // Center
			y += (layoutHeight - tableHeight) / 2;
		// Position actors within cells.
		float currentX = x, currentY = y;
		for (int i = 0; i < cellCount; i++) {
			Cell c = cells.get(i);
			float spannedCellWidth = 0;
			for (int column = c.column, nn = column + c.colspan; column < nn; column++)
				spannedCellWidth += columnWidth[column];
			spannedCellWidth -= c.computedPadLeft + c.computedPadRight;
			currentX += c.computedPadLeft;
			float fillX = c.fillX, fillY = c.fillY;
			if (fillX > 0) {
				c.actorWidth = Math.max(spannedCellWidth * fillX, c.minWidth.get(c.actor));
				float maxWidth = c.maxWidth.get(c.actor);
				if (maxWidth > 0)
					c.actorWidth = Math.min(c.actorWidth, maxWidth);
			}
			if (fillY > 0) {
				c.actorHeight = Math.max(rowHeight[c.row] * fillY - c.computedPadTop - c.computedPadBottom, c.minHeight.get(c.actor));
				float maxHeight = c.maxHeight.get(c.actor);
				if (maxHeight > 0)
					c.actorHeight = Math.min(c.actorHeight, maxHeight);
			}
			align = c.align;
			if ((align & Align.left) != 0)
				c.actorX = currentX;
			else if ((align & Align.right) != 0)
				c.actorX = currentX + spannedCellWidth - c.actorWidth;
			else
				c.actorX = currentX + (spannedCellWidth - c.actorWidth) / 2;
			if ((align & Align.top) != 0)
				c.actorY = currentY + c.computedPadTop;
			else if ((align & Align.bottom) != 0)
				c.actorY = currentY + rowHeight[c.row] - c.actorHeight - c.computedPadBottom;
			else
				c.actorY = currentY + (rowHeight[c.row] - c.actorHeight + c.computedPadTop - c.computedPadBottom) / 2;
			if (c.endRow) {
				currentX = x;
				currentY += rowHeight[c.row];
			} else
				currentX += spannedCellWidth + c.computedPadRight;
		}
	}

	@Null
	public Skin getSkin() {
		return skin;
	}

	public void setSkin(@Null Skin skin) {
		this.skin = skin;
	}
}
