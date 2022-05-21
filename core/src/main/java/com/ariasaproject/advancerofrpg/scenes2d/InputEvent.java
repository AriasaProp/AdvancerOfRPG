package com.ariasaproject.advancerofrpg.scenes2d;

import com.ariasaproject.advancerofrpg.input.Input.Buttons;
import com.ariasaproject.advancerofrpg.math.Vector2;
import com.ariasaproject.advancerofrpg.utils.Null;

public class InputEvent extends Event {
    private Type type;
    private float stageX, stageY;
    private int pointer, button, keyCode, scrollAmount;
    private char character;
    @Null
    private Actor relatedActor;

    @Override
    public void reset() {
        super.reset();
        relatedActor = null;
        button = -1;
    }

    /**
     * The stage x coordinate where the event occurred. Valid for: touchDown,
     * touchDragged, touchUp, mouseMoved, enter, and exit.
     */
    public float getStageX() {
        return stageX;
    }

    public void setStageX(float stageX) {
        this.stageX = stageX;
    }

    /**
     * The stage x coordinate where the event occurred. Valid for: touchDown,
     * touchDragged, touchUp, mouseMoved, enter, and exit.
     */
    public float getStageY() {
        return stageY;
    }

    public void setStageY(float stageY) {
        this.stageY = stageY;
    }

    /**
     * The type of input event.
     */
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    /**
     * The pointer index for the event. The first touch is index 0, second touch is
     * index 1, etc. Always -1 on desktop. Valid for: touchDown, touchDragged,
     * touchUp, enter, and exit.
     */
    public int getPointer() {
        return pointer;
    }

    public void setPointer(int pointer) {
        this.pointer = pointer;
    }

    /**
     * The index for the mouse button pressed. Always 0 on Android. Valid for:
     * touchDown and touchUp.
     *
     * @see Buttons
     */
    public int getButton() {
        return button;
    }

    public void setButton(int button) {
        this.button = button;
    }

    /**
     * The key code of the key that was pressed. Valid for: keyDown and keyUp.
     */
    public int getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    /**
     * The character for the key that was type. Valid for: keyTyped.
     */
    public char getCharacter() {
        return character;
    }

    public void setCharacter(char character) {
        this.character = character;
    }

    /**
     * The amount the mouse was scrolled. Valid for: scrolled.
     */
    public int getScrollAmount() {
        return scrollAmount;
    }

    public void setScrollAmount(int scrollAmount) {
        this.scrollAmount = scrollAmount;
    }

    /**
     * The actor related to the event. Valid for: enter and exit. For enter, this is
     * the actor being exited, or null. For exit, this is the actor being entered,
     * or null.
     */
    @Null
    public Actor getRelatedActor() {
        return relatedActor;
    }

    /**
     * @param relatedActor May be null.
     */
    public void setRelatedActor(@Null Actor relatedActor) {
        this.relatedActor = relatedActor;
    }

    /**
     * Sets actorCoords to this event's coordinates relative to the specified actor.
     *
     * @param actorCoords Output for resulting coordinates.
     */
    public Vector2 toCoordinates(Actor actor, Vector2 actorCoords) {
        actorCoords.set(stageX, stageY);
        actor.stageToLocalCoordinates(actorCoords);
        return actorCoords;
    }

    /**
     * Returns true of this event is a touchUp triggered by
     * {@link Stage#cancelTouchFocus()}.
     */
    public boolean isTouchFocusCancel() {
        return stageX == Integer.MIN_VALUE || stageY == Integer.MIN_VALUE;
    }

    @Override
    public String toString() {
        return type.toString();
    }

    /**
     * Types of low-level input events supported by scene2d.
     */
    public enum Type {
        /**
         * A new touch for a pointer on the stage was detected
         */
        touchDown,
        /**
         * A pointer has stopped touching the stage.
         */
        touchUp,
        /**
         * A pointer that is touching the stage has moved.
         */
        touchDragged,
        /**
         * The mouse pointer has moved (without a mouse button being active).
         */
        mouseMoved,
        /**
         * The mouse pointer or an active touch have entered (i.e.,
         * {@link Actor#hit(float, float, boolean) hit}) an actor.
         */
        enter,
        /**
         * The mouse pointer or an active touch have exited an actor.
         */
        exit,
        /**
         * The mouse scroll wheel has changed.
         */
        scrolled,
        /**
         * A keyboard key has been pressed.
         */
        keyDown,
        /**
         * A keyboard key has been released.
         */
        keyUp,
        /**
         * A keyboard key has been pressed and released.
         */
        keyTyped
    }
}
