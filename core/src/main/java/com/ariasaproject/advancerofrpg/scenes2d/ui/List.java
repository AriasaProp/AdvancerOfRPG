package com.ariasaproject.advancerofrpg.scenes2d.ui;

import com.ariasaproject.advancerofrpg.graphics.Color;
import com.ariasaproject.advancerofrpg.graphics.g2d.Batch;
import com.ariasaproject.advancerofrpg.graphics.g2d.BitmapFont;
import com.ariasaproject.advancerofrpg.graphics.g2d.GlyphLayout;
import com.ariasaproject.advancerofrpg.math.Rectangle;
import com.ariasaproject.advancerofrpg.scenes2d.Actor;
import com.ariasaproject.advancerofrpg.scenes2d.InputEvent;
import com.ariasaproject.advancerofrpg.scenes2d.InputListener;
import com.ariasaproject.advancerofrpg.scenes2d.utils.ArraySelection;
import com.ariasaproject.advancerofrpg.scenes2d.utils.Cullable;
import com.ariasaproject.advancerofrpg.scenes2d.utils.Drawable;
import com.ariasaproject.advancerofrpg.utils.Align;
import com.ariasaproject.advancerofrpg.utils.Array;
import com.ariasaproject.advancerofrpg.utils.Null;
import com.ariasaproject.advancerofrpg.utils.ObjectSet;
import com.ariasaproject.advancerofrpg.utils.Pool;
import com.ariasaproject.advancerofrpg.utils.Pools;

public class List<T> extends Widget implements Cullable {
    final Array<T> items = new Array<T>();
    ListStyle style;
    ArraySelection<T> selection = new ArraySelection<T>(items);
    float itemHeight;
    int pressedIndex = -1, overIndex = -1;
    boolean typeToSelect;
    private Rectangle cullingArea;
    private float prefWidth, prefHeight;
    private int alignment = Align.left;

    public List(Skin skin) {
        this(skin.get(ListStyle.class));
    }

    public List(Skin skin, String styleName) {
        this(skin.get(styleName, ListStyle.class));
    }

    public List(ListStyle style) {
        selection.setActor(this);
        selection.setRequired(true);
        setStyle(style);
        setSize(getPrefWidth(), getPrefHeight());
        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (pointer != 0 || button != 0)
                    return true;
                if (selection.isDisabled())
                    return true;
                if (getStage() != null)
                    getStage().setKeyboardFocus(List.this);
                if (items.size == 0)
                    return true;
                int index = getItemIndexAt(y);
                if (index == -1)
                    return true;
                selection.choose(items.get(index));
                pressedIndex = index;
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (pointer != 0 || button != 0)
                    return;
                pressedIndex = -1;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                overIndex = getItemIndexAt(y);
            }

            @Override
            public boolean mouseMoved(InputEvent event, float x, float y) {
                overIndex = getItemIndexAt(y);
                return false;
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == 0)
                    pressedIndex = -1;
                if (pointer == -1)
                    overIndex = -1;
            }
        });
    }

    /**
     * Returns the list's style. Modifying the returned style may not have an effect
     * until {@link #setStyle(ListStyle)} is called.
     */
    public ListStyle getStyle() {
        return style;
    }

    public void setStyle(ListStyle style) {
        if (style == null)
            throw new IllegalArgumentException("style cannot be null.");
        this.style = style;
        invalidateHierarchy();
    }

    @Override
    public void layout() {
        BitmapFont font = style.font;
        Drawable selectedDrawable = style.selection;
        itemHeight = font.getCapHeight() - font.getDescent() * 2;
        itemHeight += selectedDrawable.getTopHeight() + selectedDrawable.getBottomHeight();
        prefWidth = 0;
        Pool<GlyphLayout> layoutPool = Pools.get(GlyphLayout.class);
        GlyphLayout layout = layoutPool.obtain();
        for (int i = 0; i < items.size; i++) {
            layout.setText(font, toString(items.get(i)));
            prefWidth = Math.max(layout.width, prefWidth);
        }
        layoutPool.free(layout);
        prefWidth += selectedDrawable.getLeftWidth() + selectedDrawable.getRightWidth();
        prefHeight = items.size * itemHeight;
        Drawable background = style.background;
        if (background != null) {
            prefWidth = Math.max(prefWidth + background.getLeftWidth() + background.getRightWidth(), background.getMinWidth());
            prefHeight = Math.max(prefHeight + background.getTopHeight() + background.getBottomHeight(), background.getMinHeight());
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        validate();
        drawBackground(batch, parentAlpha);
        BitmapFont font = style.font;
        Drawable selectedDrawable = style.selection;
        Color fontColorSelected = style.fontColorSelected;
        Color fontColorUnselected = style.fontColorUnselected;
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
        float x = getX(), y = getY(), width = getWidth(), height = getHeight();
        float itemY = height;
        Drawable background = style.background;
        if (background != null) {
            float leftWidth = background.getLeftWidth();
            x += leftWidth;
            itemY -= background.getTopHeight();
            width -= leftWidth + background.getRightWidth();
        }
        float textOffsetX = selectedDrawable.getLeftWidth(),
                textWidth = width - textOffsetX - selectedDrawable.getRightWidth();
        float textOffsetY = selectedDrawable.getTopHeight() - font.getDescent();
        font.setColor(fontColorUnselected.r, fontColorUnselected.g, fontColorUnselected.b, fontColorUnselected.a * parentAlpha);
        for (int i = 0; i < items.size; i++) {
            if (cullingArea == null || (itemY - itemHeight <= cullingArea.y + cullingArea.height && itemY >= cullingArea.y)) {
                T item = items.get(i);
                boolean selected = selection.contains(item);
                Drawable drawable = null;
                if (pressedIndex == i && style.down != null)
                    drawable = style.down;
                else if (selected) {
                    drawable = selectedDrawable;
                    font.setColor(fontColorSelected.r, fontColorSelected.g, fontColorSelected.b, fontColorSelected.a * parentAlpha);
                } else if (overIndex == i && style.over != null) //
                    drawable = style.over;
                if (drawable != null)
                    drawable.draw(batch, x, y + itemY - itemHeight, width, itemHeight);
                drawItem(batch, font, i, item, x + textOffsetX, y + itemY - textOffsetY, textWidth);
                if (selected) {
                    font.setColor(fontColorUnselected.r, fontColorUnselected.g, fontColorUnselected.b, fontColorUnselected.a * parentAlpha);
                }
            } else if (itemY < cullingArea.y) {
                break;
            }
            itemY -= itemHeight;
        }
    }

    /**
     * Called to draw the background. Default implementation draws the style
     * background drawable.
     */
    protected void drawBackground(Batch batch, float parentAlpha) {
        if (style.background != null) {
            Color color = getColor();
            batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
            style.background.draw(batch, getX(), getY(), getWidth(), getHeight());
        }
    }

    protected GlyphLayout drawItem(Batch batch, BitmapFont font, int index, T item, float x, float y, float width) {
        String string = toString(item);
        return font.draw(batch, string, x, y, 0, string.length(), width, alignment, false, "...");
    }

    public ArraySelection<T> getSelection() {
        return selection;
    }

    public void setSelection(ArraySelection<T> selection) {
        this.selection = selection;
    }

    /**
     * Returns the first selected item, or null.
     */
    @Null
    public T getSelected() {
        return selection.first();
    }

    /**
     * Sets the selection to only the passed item, if it is a possible choice.
     *
     * @param item May be null.
     */
    public void setSelected(@Null T item) {
        if (items.contains(item, false))
            selection.set(item);
        else if (selection.getRequired() && items.size > 0)
            selection.set(items.first());
        else
            selection.clear();
    }

    /**
     * @return The index of the first selected item. The top item has an index of 0.
     * Nothing selected has an index of -1.
     */
    public int getSelectedIndex() {
        ObjectSet<T> selected = selection.items();
        return selected.size == 0 ? -1 : items.indexOf(selected.first(), false);
    }

    /**
     * Sets the selection to only the selected index.
     *
     * @param index -1 to clear the selection.
     */
    public void setSelectedIndex(int index) {
        if (index < -1 || index >= items.size)
            throw new IllegalArgumentException("index must be >= -1 and < " + items.size + ": " + index);
        if (index == -1) {
            selection.clear();
        } else {
            selection.set(items.get(index));
        }
    }

    /**
     * @return May be null.
     */
    public T getOverItem() {
        return overIndex == -1 ? null : items.get(overIndex);
    }

    /**
     * @return May be null.
     */
    public T getPressedItem() {
        return pressedIndex == -1 ? null : items.get(pressedIndex);
    }

    /**
     * @return null if not over an item.
     */
    @Null
    public T getItemAt(float y) {
        int index = getItemIndexAt(y);
        if (index == -1)
            return null;
        return items.get(index);
    }

    /**
     * @return -1 if not over an item.
     */
    public int getItemIndexAt(float y) {
        float height = getHeight();
        Drawable background = List.this.style.background;
        if (background != null) {
            height -= background.getTopHeight() + background.getBottomHeight();
            y -= background.getBottomHeight();
        }
        int index = (int) ((height - y) / itemHeight);
        if (index < 0 || index >= items.size)
            return -1;
        return index;
    }

    public void clearItems() {
        if (items.size == 0)
            return;
        items.clear();
        overIndex = -1;
        pressedIndex = -1;
        selection.clear();
        invalidateHierarchy();
    }

    /**
     * Returns the internal items array. If modified, {@link #setItems(Array)} must
     * be called to reflect the changes.
     */
    public Array<T> getItems() {
        return items;
    }

    /**
     * Sets the items visible in the list, clearing the selection if it is no longer
     * valid. If a selection is {@link ArraySelection#getRequired()}, the first item
     * is selected. This can safely be called with a (modified) array returned from
     * {@link #getItems()}.
     */
    public void setItems(Array<T> newItems) {
        if (newItems == null)
            throw new IllegalArgumentException("newItems cannot be null.");
        float oldPrefWidth = getPrefWidth(), oldPrefHeight = getPrefHeight();
        if (newItems != items) {
            items.clear();
            items.addAll(newItems);
        }
        overIndex = -1;
        pressedIndex = -1;
        selection.validate();
        invalidate();
        if (oldPrefWidth != getPrefWidth() || oldPrefHeight != getPrefHeight())
            invalidateHierarchy();
    }

    public void setItems(T... newItems) {
        if (newItems == null)
            throw new IllegalArgumentException("newItems cannot be null.");
        float oldPrefWidth = getPrefWidth(), oldPrefHeight = getPrefHeight();
        items.clear();
        items.add(newItems);
        overIndex = -1;
        pressedIndex = -1;
        selection.validate();
        invalidate();
        if (oldPrefWidth != getPrefWidth() || oldPrefHeight != getPrefHeight())
            invalidateHierarchy();
    }

    public float getItemHeight() {
        return itemHeight;
    }

    @Override
    public float getPrefWidth() {
        validate();
        return prefWidth;
    }

    @Override
    public float getPrefHeight() {
        validate();
        return prefHeight;
    }

    public String toString(T object) {
        return object.toString();
    }

    /**
     * @return May be null.
     * @see #setCullingArea(Rectangle)
     */
    public Rectangle getCullingArea() {
        return cullingArea;
    }

    @Override
    public void setCullingArea(@Null Rectangle cullingArea) {
        this.cullingArea = cullingArea;
    }

    /**
     * Sets the horizontal alignment of the list items.
     *
     * @param alignment See {@link Align}.
     */
    public void setAlignment(int alignment) {
        this.alignment = alignment;
    }

    public void setTypeToSelect(boolean typeToSelect) {
        this.typeToSelect = typeToSelect;
    }

    /**
     * The style for a list, see {@link List}.
     *
     * @author mzechner
     * @author Nathan Sweet
     */
    static public class ListStyle {
        public BitmapFont font;
        public Color fontColorSelected = new Color(1, 1, 1, 1);
        public Color fontColorUnselected = new Color(1, 1, 1, 1);
        public Drawable selection;
        /**
         * Optional.
         */
        @Null
        public Drawable down, over, background;

        public ListStyle() {
        }

        public ListStyle(BitmapFont font, Color fontColorSelected, Color fontColorUnselected, Drawable selection) {
            this.font = font;
            this.fontColorSelected.set(fontColorSelected);
            this.fontColorUnselected.set(fontColorUnselected);
            this.selection = selection;
        }

        public ListStyle(ListStyle style) {
            this.font = style.font;
            this.fontColorSelected.set(style.fontColorSelected);
            this.fontColorUnselected.set(style.fontColorUnselected);
            this.selection = style.selection;
            this.down = style.down;
            this.over = style.over;
            this.background = style.background;
        }
    }
}
