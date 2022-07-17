package com.ariasaproject.advancerofrpg;

import com.ariasaproject.advancerofrpg.input.Input;
import com.ariasaproject.advancerofrpg.input.InputProcessor;

public class LWJGLInput implements Input {

	@Override
	public float getAccelerometerX() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getAccelerometerY() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getAccelerometerZ() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getGyroscopeX() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getGyroscopeY() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getGyroscopeZ() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMaxPointers() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getX() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getX(int pointer) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getDeltaX() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getDeltaX(int pointer) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getY() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getY(int pointer) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getDeltaY() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getDeltaY(int pointer) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isTouched() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean justTouched() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isTouched(int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public float getPressure() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getPressure(int pointer) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isButtonPressed(int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isButtonJustPressed(int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isKeyPressed(int key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isKeyJustPressed(int key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setKeyboardAvailable(boolean available) {
		// TODO Auto-generated method stub

	}

	@Override
	public void getTextInput(TextInputListener listener, String title, String text, String hint) {
		// TODO Auto-generated method stub

	}

	@Override
	public void getTextInput(TextInputListener listener, String title, String text, String hint, OnscreenKeyboardType type) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setOnscreenKeyboardVisible(boolean visible) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setOnscreenKeyboardVisible(boolean visible, OnscreenKeyboardType type) {
		// TODO Auto-generated method stub

	}

	@Override
	public void vibrate(int milliseconds) {
		// TODO Auto-generated method stub

	}

	@Override
	public void vibrate(long[] pattern, int repeat) {
		// TODO Auto-generated method stub

	}

	@Override
	public void cancelVibrate() {
		// TODO Auto-generated method stub

	}

	@Override
	public float getAzimuth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getPitch() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getRoll() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void getRotationMatrix(float[] matrix) {
		// TODO Auto-generated method stub

	}

	@Override
	public long getCurrentEventTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setCatchKey(int keycode, boolean catchKey) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isCatchKey(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public InputProcessor getInputProcessor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setInputProcessor(InputProcessor processor) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isPeripheralAvailable(Peripheral peripheral) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getRotation() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Orientation getNativeOrientation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void processEvents() {
		// TODO Auto-generated method stub

	}

}
