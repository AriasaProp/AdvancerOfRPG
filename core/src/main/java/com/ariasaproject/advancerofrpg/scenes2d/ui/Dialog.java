package com.ariasaproject.advancerofrpg.scenes2d.ui;

import com.ariasaproject.advancerofrpg.GraphFunc;
import com.ariasaproject.advancerofrpg.math.Interpolation;
import com.ariasaproject.advancerofrpg.scenes2d.Action;
import com.ariasaproject.advancerofrpg.scenes2d.Actor;
import com.ariasaproject.advancerofrpg.scenes2d.InputEvent;
import com.ariasaproject.advancerofrpg.scenes2d.InputListener;
import com.ariasaproject.advancerofrpg.scenes2d.Stage;
import com.ariasaproject.advancerofrpg.scenes2d.actions.Actions;
import com.ariasaproject.advancerofrpg.scenes2d.ui.Label.LabelStyle;
import com.ariasaproject.advancerofrpg.scenes2d.ui.TextButton.TextButtonStyle;
import com.ariasaproject.advancerofrpg.scenes2d.utils.ChangeListener;
import com.ariasaproject.advancerofrpg.scenes2d.utils.FocusListener;
import com.ariasaproject.advancerofrpg.utils.Null;
import com.ariasaproject.advancerofrpg.utils.ObjectMap;

import static com.ariasaproject.advancerofrpg.scenes2d.actions.Actions.fadeOut;
import static com.ariasaproject.advancerofrpg.scenes2d.actions.Actions.sequence;

public class Dialog extends Window {
    protected InputListener ignoreTouchDown = new InputListener() {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            event.cancel();
            return false;
        }
    };
    Table contentTable, buttonTable;
    ObjectMap<Actor, Object> values = new ObjectMap();
    boolean cancelHide;
    Actor previousKeyboardFocus, previousScrollFocus;
    FocusListener focusListener;
    @Null
    private Skin skin;

    public Dialog(String title, Skin skin) {
        super(title, skin.get(WindowStyle.class));
        setSkin(skin);
        this.skin = skin;
        initialize();
    }

    public Dialog(String title, Skin skin, String windowStyleName) {
        super(title, skin.get(windowStyleName, WindowStyle.class));
        setSkin(skin);
        this.skin = skin;
        initialize();
    }

    public Dialog(String title, WindowStyle windowStyle) {
        super(title, windowStyle);
        initialize();
    }

    private void initialize() {
        setModal(true);
        defaults().space(6);
        add(contentTable = new Table(skin)).expand().fill();
        row();
        add(buttonTable = new Table(skin)).fillX();
        contentTable.defaults().space(6);
        buttonTable.defaults().space(6);
        buttonTable.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!values.containsKey(actor))
                    return;
                while (actor.getParent() != buttonTable)
                    actor = actor.getParent();
                result(values.get(actor));
                if (!cancelHide)
                    hide();
                cancelHide = false;
            }
        });
        focusListener = new FocusListener() {
            @Override
            public void keyboardFocusChanged(FocusEvent event, Actor actor, boolean focused) {
                if (!focused)
                    focusChanged(event);
            }

            @Override
            public void scrollFocusChanged(FocusEvent event, Actor actor, boolean focused) {
                if (!focused)
                    focusChanged(event);
            }

            private void focusChanged(FocusEvent event) {
                Stage stage = getStage();
                if (isModal && stage != null && stage.getRoot().getChildren().size > 0 && stage.getRoot().getChildren().peek() == Dialog.this) { // Dialog
                    // is
                    // top
                    // most
                    // actor.
                    Actor newFocusedActor = event.getRelatedActor();
                    if (newFocusedActor != null && !newFocusedActor.isDescendantOf(Dialog.this) && !(newFocusedActor.equals(previousKeyboardFocus) || newFocusedActor.equals(previousScrollFocus)))
                        event.cancel();
                }
            }
        };
    }

    @Override
    protected void setStage(Stage stage) {
        if (stage == null)
            addListener(focusListener);
        else
            removeListener(focusListener);
        super.setStage(stage);
    }

    public Table getContentTable() {
        return contentTable;
    }

    public Table getButtonTable() {
        return buttonTable;
    }

    /**
     * Adds a label to the content table. The dialog must have been constructed with
     * a skin to use this method.
     */
    public Dialog text(@Null String text) {
        if (skin == null)
            throw new IllegalStateException("This method may only be used if the dialog was constructed with a Skin.");
        return text(text, skin.get(LabelStyle.class));
    }

    /**
     * Adds a label to the content table.
     */
    public Dialog text(@Null String text, LabelStyle labelStyle) {
        return text(new Label(text, labelStyle));
    }

    /**
     * Adds the given Label to the content table
     */
    public Dialog text(Label label) {
        contentTable.add(label);
        return this;
    }

    /**
     * Adds a text button to the button table. Null will be passed to
     * {@link #result(Object)} if this button is clicked. The dialog must have been
     * constructed with a skin to use this method.
     */
    public Dialog button(@Null String text) {
        return button(text, null);
    }

    /**
     * Adds a text button to the button table. The dialog must have been constructed
     * with a skin to use this method.
     *
     * @param object The object that will be passed to {@link #result(Object)} if
     *               this button is clicked. May be null.
     */
    public Dialog button(@Null String text, @Null Object object) {
        if (skin == null)
            throw new IllegalStateException("This method may only be used if the dialog was constructed with a Skin.");
        return button(text, object, skin.get(TextButtonStyle.class));
    }

    /**
     * Adds a text button to the button table.
     *
     * @param object The object that will be passed to {@link #result(Object)} if
     *               this button is clicked. May be null.
     */
    public Dialog button(@Null String text, @Null Object object, TextButtonStyle buttonStyle) {
        return button(new TextButton(text, buttonStyle), object);
    }

    /**
     * Adds the given button to the button table.
     */
    public Dialog button(Button button) {
        return button(button, null);
    }

    /**
     * Adds the given button to the button table.
     *
     * @param object The object that will be passed to {@link #result(Object)} if
     *               this button is clicked. May be null.
     */
    public Dialog button(Button button, @Null Object object) {
        buttonTable.add(button);
        setObject(button, object);
        return this;
    }

    /**
     * {@link #pack() Packs} the dialog (but doesn't set the position), adds it to
     * the stage, sets it as the keyboard and scroll focus, clears any actions on
     * the dialog, and adds the specified action to it. The previous keyboard and
     * scroll focus are remembered so they can be restored when the dialog is
     * hidden.
     *
     * @param action May be null.
     */
    public Dialog show(Stage stage, @Null Action action) {
        clearActions();
        removeCaptureListener(ignoreTouchDown);
        previousKeyboardFocus = null;
        Actor actor = stage.getKeyboardFocus();
        if (actor != null && !actor.isDescendantOf(this))
            previousKeyboardFocus = actor;
        previousScrollFocus = null;
        actor = stage.getScrollFocus();
        if (actor != null && !actor.isDescendantOf(this))
            previousScrollFocus = actor;
        stage.addActor(this);
        pack();
        stage.cancelTouchFocus();
        stage.setKeyboardFocus(this);
        stage.setScrollFocus(this);
        if (action != null)
            addAction(action);
        return this;
    }

    /**
     * Centers the dialog in the stage and calls {@link #show(Stage, Action)} with a
     * {@link Actions#fadeIn(float, Interpolation)} action.
     */
    public Dialog show(Stage stage) {
        show(stage, sequence(Actions.alpha(0), Actions.fadeIn(0.4f, Interpolation.fade)));
        setPosition(Math.round((stage.getWidth() - getWidth()) / 2), Math.round((stage.getHeight() - getHeight()) / 2));
        return this;
    }

    /**
     * Removes the dialog from the stage, restoring the previous keyboard and scroll
     * focus, and adds the specified action to the dialog.
     *
     * @param action If null, the dialog is removed immediately. Otherwise, the
     *               dialog is removed when the action completes. The dialog will
     *               not respond to touch down events during the action.
     */
    public void hide(@Null Action action) {
        Stage stage = getStage();
        if (stage != null) {
            removeListener(focusListener);
            if (previousKeyboardFocus != null && previousKeyboardFocus.getStage() == null)
                previousKeyboardFocus = null;
            Actor actor = stage.getKeyboardFocus();
            if (actor == null || actor.isDescendantOf(this))
                stage.setKeyboardFocus(previousKeyboardFocus);
            if (previousScrollFocus != null && previousScrollFocus.getStage() == null)
                previousScrollFocus = null;
            actor = stage.getScrollFocus();
            if (actor == null || actor.isDescendantOf(this))
                stage.setScrollFocus(previousScrollFocus);
        }
        if (action != null) {
            addCaptureListener(ignoreTouchDown);
            addAction(sequence(action, Actions.removeListener(ignoreTouchDown, true), Actions.removeActor()));
        } else
            remove();
    }

    /**
     * Hides the dialog. Called automatically when a button is clicked. The default
     * implementation fades out the dialog over 400 milliseconds.
     */
    public void hide() {
        hide(fadeOut(0.4f, Interpolation.fade));
    }

    public void setObject(Actor actor, @Null Object object) {
        values.put(actor, object);
    }

    /**
     * If this key is pressed, {@link #result(Object)} is called with the specified
     * object.
     *
     * @see Keys
     */
    public Dialog key(final int keycode, @Null final Object object) {
        addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode2) {
                if (keycode == keycode2) {
                    // Delay a frame to eat the keyTyped event.
                    GraphFunc.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            result(object);
                            if (!cancelHide)
                                hide();
                            cancelHide = false;
                        }
                    });
                }
                return false;
            }
        });
        return this;
    }

    /**
     * Called when a button is clicked. The dialog will be hidden after this method
     * returns unless {@link #cancel()} is called.
     *
     * @param object The object specified when the button was added.
     */
    protected void result(@Null Object object) {
    }

    public void cancel() {
        cancelHide = true;
    }
}
