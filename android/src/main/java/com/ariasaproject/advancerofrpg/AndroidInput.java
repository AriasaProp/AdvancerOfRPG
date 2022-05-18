package com.ariasaproject.advancerofrpg;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnGenericMotionListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.ariasaproject.advancerofrpg.input.Input;
import com.ariasaproject.advancerofrpg.input.InputProcessor;
import com.ariasaproject.advancerofrpg.utils.IntSet;
import com.ariasaproject.advancerofrpg.utils.Pool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AndroidInput implements Input, OnTouchListener, OnKeyListener, OnGenericMotionListener {
    public static final int NUM_TOUCHES = 20;
    public static final int SUPPORTED_KEYS = 260;
    protected final float[] accelerometerValues = new float[3];
    protected final float[] gyroscopeValues = new float[3];
    protected final AndroidTouchHandler touchHandler;
    protected final Vibrator vibrator;
    protected final float[] magneticFieldValues = new float[3];
    protected final float[] rotationVectorValues = new float[3];
    protected final Orientation nativeOrientation;
    final boolean hasMultitouch;
    final AndroidApplication app;
    final float[] R = new float[9];
    final float[] orientation = new float[3];
    private final boolean[] keys = new boolean[SUPPORTED_KEYS];
    private final boolean[] justPressedKeys = new boolean[SUPPORTED_KEYS];
    private final boolean[] justPressedButtons = new boolean[NUM_TOUCHES];
    private final Handler handle;
    private final IntSet keysToCatch = new IntSet();
    private final ArrayList<OnGenericMotionListener> genericMotionListeners = new ArrayList<OnGenericMotionListener>();
    private final View view;
    public boolean accelerometerAvailable = false;
    public boolean gyroscopeAvailable = false;
    Pool<KeyEvent> usedKeyEvents = new Pool<KeyEvent>(16, 1000) {
        @Override
        protected KeyEvent newObject() {
            return new KeyEvent();
        }
    };
    Pool<TouchEvent> usedTouchEvents = new Pool<TouchEvent>(16, 1000) {
        @Override
        protected TouchEvent newObject() {
            return new TouchEvent();
        }
    };
    ArrayList<OnKeyListener> keyListeners = new ArrayList<OnKeyListener>();
    ArrayList<KeyEvent> keyEvents = new ArrayList<KeyEvent>();
    ArrayList<TouchEvent> touchEvents = new ArrayList<TouchEvent>();
    int[] touchX = new int[NUM_TOUCHES];
    int[] touchY = new int[NUM_TOUCHES];
    int[] deltaX = new int[NUM_TOUCHES];
    int[] deltaY = new int[NUM_TOUCHES];
    boolean[] touched = new boolean[NUM_TOUCHES];
    int[] button = new int[NUM_TOUCHES];
    int[] realId = new int[NUM_TOUCHES];
    float[] pressure = new float[NUM_TOUCHES];
    boolean keyboardAvailable;
    boolean requestFocus = true;
    boolean useAccelerometer = false, useGyroscope = false, useRotationVector = false, useCompas = false;
    private int keyCount = 0;
    private boolean keyJustPressed = false;
    private SensorManager manager;
    private boolean compassAvailable = false;
    private boolean rotationVectorAvailable = false;
    private float azimuth = 0;
    private float pitch = 0;
    private float roll = 0;
    private boolean justTouched = false;
    private InputProcessor processor;
    private long currentEventTimeStamp = 0;
    private SensorEventListener accelerometerListener, gyroscopeListener, compassListener, rotationVectorListener;
    private int deltaGX = 0;
    private int deltaGY = 0;

    public AndroidInput(AndroidApplication app, View view) {
        this.app = app;
        this.view = view;
        view.setOnKeyListener(this);
        view.setOnTouchListener(this);
        view.requestFocus();
        view.setOnGenericMotionListener(this);
        for (int i = 0; i < realId.length; i++)
            realId[i] = -1;
        handle = new Handler();
        touchHandler = new AndroidTouchHandler();
        hasMultitouch = touchHandler.supportsMultitouch(app);
        vibrator = (Vibrator) app.getSystemService(Context.VIBRATOR_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        app.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int rotation = getRotation();
        if (((rotation == 0 || rotation == 180) && (metrics.widthPixels >= metrics.heightPixels))
                || ((rotation == 90 || rotation == 270) && (metrics.widthPixels <= metrics.heightPixels))) {
            nativeOrientation = Orientation.Landscape;
        } else {
            nativeOrientation = Orientation.Portrait;
        }
        // this is for backward compatibility: libGDX always caught the circle button,
        // original comment:
        // circle button on Xperia Play shouldn't need catchBack == true
        keysToCatch.add(Keys.BUTTON_CIRCLE);
    }

    @Override
    public float getAccelerometerX() {
        return accelerometerValues[0];
    }

    @Override
    public float getAccelerometerY() {
        return accelerometerValues[1];
    }

    @Override
    public float getAccelerometerZ() {
        return accelerometerValues[2];
    }

    @Override
    public float getGyroscopeX() {
        return gyroscopeValues[0];
    }

    @Override
    public float getGyroscopeY() {
        return gyroscopeValues[1];
    }

    @Override
    public float getGyroscopeZ() {
        return gyroscopeValues[2];
    }

    @Override
    public void getTextInput(final TextInputListener listener, final String title, final String text, final String hint,
                             final OnscreenKeyboardType type) {
        getTextInput(listener, title, text, hint);
    }

    @Override
    public void getTextInput(final TextInputListener listener, final String title, final String text,
                             final String hint) {
        handle.post(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder alert = new AlertDialog.Builder(app);
                alert.setTitle(title);
                final EditText input = new EditText(app);
                input.setHint(hint);
                input.setText(text);
                input.setSingleLine();
                alert.setView(input);
                alert.setPositiveButton(app.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        GraphFunc.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                listener.input(input.getText().toString());
                            }
                        });
                    }
                });
                alert.setNegativeButton(app.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        GraphFunc.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                listener.canceled();
                            }
                        });
                    }
                });
                alert.setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface arg0) {
                        GraphFunc.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                listener.canceled();
                            }
                        });
                    }
                });
                alert.show();
            }
        });
    }

    @Override
    public int getMaxPointers() {
        return NUM_TOUCHES;
    }

    @Override
    public int getX() {
        synchronized (this) {
            return touchX[0];
        }
    }

    @Override
    public int getY() {
        synchronized (this) {
            return touchY[0];
        }
    }

    @Override
    public int getX(int pointer) {
        synchronized (this) {
            return touchX[pointer];
        }
    }

    @Override
    public int getY(int pointer) {
        synchronized (this) {
            return touchY[pointer];
        }
    }

    @Override
    public boolean isTouched(int pointer) {
        synchronized (this) {
            return touched[pointer];
        }
    }

    @Override
    public float getPressure() {
        return getPressure(0);
    }

    @Override
    public float getPressure(int pointer) {
        return pressure[pointer];
    }

    @Override
    public void setKeyboardAvailable(boolean available) {
        this.keyboardAvailable = available;
    }

    @Override
    public synchronized boolean isKeyPressed(int key) {
        if (key == AndroidInput.Keys.ANY_KEY) {
            return keyCount > 0;
        }
        if (key < 0 || key >= SUPPORTED_KEYS) {
            return false;
        }
        return keys[key];
    }

    @Override
    public synchronized boolean isKeyJustPressed(int key) {
        if (key == AndroidInput.Keys.ANY_KEY) {
            return keyJustPressed;
        }
        if (key < 0 || key >= SUPPORTED_KEYS) {
            return false;
        }
        return justPressedKeys[key];
    }

    @Override
    public boolean isTouched() {
        synchronized (this) {
            if (hasMultitouch) {
                for (int pointer = 0; pointer < NUM_TOUCHES; pointer++) {
                    if (touched[pointer]) {
                        return true;
                    }
                }
            }
            return touched[0];
        }
    }

    @Override
    public void processEvents() {
        synchronized (this) {
            if (justTouched) {
                justTouched = false;
                for (int i = 0; i < justPressedButtons.length; i++) {
                    justPressedButtons[i] = false;
                }
            }
            if (keyJustPressed) {
                keyJustPressed = false;
                for (int i = 0; i < justPressedKeys.length; i++) {
                    justPressedKeys[i] = false;
                }
            }
            if (processor != null) {
                final InputProcessor processor = this.processor;
                int len = keyEvents.size();
                for (int i = 0; i < len; i++) {
                    KeyEvent e = keyEvents.get(i);
                    currentEventTimeStamp = e.timeStamp;
                    switch (e.type) {
                        case KeyEvent.KEY_DOWN:
                            processor.keyDown(e.keyCode);
                            keyJustPressed = true;
                            justPressedKeys[e.keyCode] = true;
                            break;
                        case KeyEvent.KEY_UP:
                            processor.keyUp(e.keyCode);
                            break;
                        case KeyEvent.KEY_TYPED:
                            processor.keyTyped(e.keyChar);
                    }
                    usedKeyEvents.free(e);
                }
                len = touchEvents.size();
                for (int i = 0; i < len; i++) {
                    TouchEvent e = touchEvents.get(i);
                    currentEventTimeStamp = e.timeStamp;
                    switch (e.type) {
                        case TouchEvent.TOUCH_DOWN:
                            processor.touchDown(e.x, e.y, e.pointer, e.button);
                            justTouched = true;
                            justPressedButtons[e.button] = true;
                            break;
                        case TouchEvent.TOUCH_UP:
                            processor.touchUp(e.x, e.y, e.pointer, e.button);
                            break;
                        case TouchEvent.TOUCH_DRAGGED:
                            processor.touchDragged(e.x, e.y, e.pointer);
                            break;
                        case TouchEvent.TOUCH_MOVED:
                            processor.mouseMoved(e.x, e.y);
                            break;
                        case TouchEvent.TOUCH_SCROLLED:
                            processor.scrolled(e.scrollAmount);
                    }
                    usedTouchEvents.free(e);
                }
            } else {
                int len = touchEvents.size();
                for (int i = 0; i < len; i++) {
                    TouchEvent e = touchEvents.get(i);
                    if (e.type == TouchEvent.TOUCH_DOWN)
                        justTouched = true;
                    usedTouchEvents.free(e);
                }
                len = keyEvents.size();
                for (int i = 0; i < len; i++) {
                    usedKeyEvents.free(keyEvents.get(i));
                }
            }
            if (touchEvents.isEmpty()) {
                for (int i = 0; i < deltaX.length; i++) {
                    deltaX[0] = 0;
                    deltaY[0] = 0;
                }
            }
            keyEvents.clear();
            touchEvents.clear();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (requestFocus && view != null) {
            view.setFocusableInTouchMode(true);
            view.requestFocus();
            requestFocus = false;
        }
        // synchronized in handler.postTouchEvent()
        touchHandler.onTouch(event, this);
        return true;
    }

    @Override
    public boolean onKey(View v, int keyCode, android.view.KeyEvent e) {
        for (int i = 0, n = keyListeners.size(); i < n; i++)
            if (keyListeners.get(i).onKey(v, keyCode, e))
                return true;
        if (e.getAction() == android.view.KeyEvent.ACTION_DOWN && e.getRepeatCount() > 0)
            return keysToCatch.contains(keyCode);
        synchronized (this) {
            KeyEvent event = null;
            if (e.getKeyCode() == android.view.KeyEvent.KEYCODE_UNKNOWN
                    && e.getAction() == android.view.KeyEvent.ACTION_MULTIPLE) {
                String chars = e.getCharacters();
                for (int i = 0; i < chars.length(); i++) {
                    event = usedKeyEvents.obtain();
                    event.timeStamp = System.nanoTime();
                    event.keyCode = 0;
                    event.keyChar = chars.charAt(i);
                    event.type = KeyEvent.KEY_TYPED;
                    keyEvents.add(event);
                }
                return false;
            }
            char character = (char) e.getUnicodeChar();
            // Android doesn't report a unicode char for back space. hrm...
            if (keyCode == 67)
                character = '\b';
            if (e.getKeyCode() < 0 || e.getKeyCode() >= SUPPORTED_KEYS) {
                return false;
            }
            switch (e.getAction()) {
                case android.view.KeyEvent.ACTION_DOWN:
                    event = usedKeyEvents.obtain();
                    event.timeStamp = System.nanoTime();
                    event.keyChar = 0;
                    event.keyCode = e.getKeyCode();
                    event.type = KeyEvent.KEY_DOWN;
                    // Xperia hack for circle key. gah...
                    if (keyCode == android.view.KeyEvent.KEYCODE_BACK && e.isAltPressed()) {
                        keyCode = Keys.BUTTON_CIRCLE;
                        event.keyCode = keyCode;
                    }
                    keyEvents.add(event);
                    if (!keys[event.keyCode]) {
                        keyCount++;
                        keys[event.keyCode] = true;
                    }
                    break;
                case android.view.KeyEvent.ACTION_UP:
                    long timeStamp = System.nanoTime();
                    event = usedKeyEvents.obtain();
                    event.timeStamp = timeStamp;
                    event.keyChar = 0;
                    event.keyCode = e.getKeyCode();
                    event.type = KeyEvent.KEY_UP;
                    // Xperia hack for circle key. gah...
                    if (keyCode == android.view.KeyEvent.KEYCODE_BACK && e.isAltPressed()) {
                        keyCode = Keys.BUTTON_CIRCLE;
                        event.keyCode = keyCode;
                    }
                    keyEvents.add(event);
                    event = usedKeyEvents.obtain();
                    event.timeStamp = timeStamp;
                    event.keyChar = character;
                    event.keyCode = 0;
                    event.type = KeyEvent.KEY_TYPED;
                    keyEvents.add(event);
                    if (keyCode == Keys.BUTTON_CIRCLE) {
                        if (keys[Keys.BUTTON_CIRCLE]) {
                            keyCount--;
                            keys[Keys.BUTTON_CIRCLE] = false;
                        }
                    } else {
                        if (keys[e.getKeyCode()]) {
                            keyCount--;
                            keys[e.getKeyCode()] = false;
                        }
                    }
            }
        }
        return keysToCatch.contains(keyCode);
    }

    @Override
    public void setOnscreenKeyboardVisible(boolean visible, OnscreenKeyboardType type) {
        setOnscreenKeyboardVisible(visible);
    }

    @Override
    public void setOnscreenKeyboardVisible(final boolean visible) {
        handle.post(new Runnable() {
            @Override
            public void run() {
                InputMethodManager manager = (InputMethodManager) app.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (visible) {
                    view.setFocusable(true);
                    view.setFocusableInTouchMode(true);
                    manager.showSoftInput(view, 0);
                } else {
                    manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });
    }

    @Override
    public void setCatchKey(int keycode, boolean catchKey) {
        if (!catchKey) {
            keysToCatch.remove(keycode);
        } else if (catchKey) {
            keysToCatch.add(keycode);
        }
    }

    @Override
    public boolean isCatchKey(int keycode) {
        return keysToCatch.contains(keyCount);
    }

    @Override
    public void vibrate(int milliseconds) {
        if (Build.VERSION.SDK_INT >= 26) {
            vibrateSDK26(milliseconds);
        } else
            vibrateBase(milliseconds);
    }

    @TargetApi(26)
    private void vibrateSDK26(int milliseconds) {
        vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE));
    }

    @SuppressWarnings("deprecation")
    private void vibrateBase(int milliseconds) {
        vibrator.vibrate(milliseconds);
    }

    @Override
    public void vibrate(long[] pattern, int repeat) {
        if (Build.VERSION.SDK_INT >= 26)
            vibrateSDK26(pattern, repeat);
        else
            vibrateBase(pattern, repeat);
    }

    @TargetApi(26)
    private void vibrateSDK26(long[] pattern, int repeat) {
        vibrator.vibrate(VibrationEffect.createWaveform(pattern, repeat));
    }

    @SuppressWarnings("deprecation")
    private void vibrateBase(long[] pattern, int repeat) {
        vibrator.vibrate(pattern, repeat);
    }

    @Override
    public void cancelVibrate() {
        vibrator.cancel();
    }

    @Override
    public boolean justTouched() {
        return justTouched;
    }

    @Override
    public boolean isButtonPressed(int button) {
        synchronized (this) {
            if (hasMultitouch) {
                for (int pointer = 0; pointer < NUM_TOUCHES; pointer++) {
                    if (touched[pointer] && (this.button[pointer] == button)) {
                        return true;
                    }
                }
            }
            return (touched[0] && (this.button[0] == button));
        }
    }

    @Override
    public boolean isButtonJustPressed(int button) {
        if (button < 0 || button > NUM_TOUCHES)
            return false;
        return justPressedButtons[button];
    }

    private void updateOrientation() {
        if (rotationVectorAvailable) {
            SensorManager.getRotationMatrixFromVector(R, rotationVectorValues);
        } else if (!SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues)) {
            return; // compass + accelerometer in free fall
        }
        SensorManager.getOrientation(R, orientation);
        azimuth = (float) Math.toDegrees(orientation[0]);
        pitch = (float) Math.toDegrees(orientation[1]);
        roll = (float) Math.toDegrees(orientation[2]);
    }

    @Override
    public void getRotationMatrix(float[] matrix) {
        if (rotationVectorAvailable)
            SensorManager.getRotationMatrixFromVector(matrix, rotationVectorValues);
        else // compass + accelerometer
            SensorManager.getRotationMatrix(matrix, null, accelerometerValues, magneticFieldValues);
    }

    @Override
    public float getAzimuth() {
        if (!compassAvailable && !rotationVectorAvailable)
            return 0;
        updateOrientation();
        return azimuth;
    }

    @Override
    public float getPitch() {
        if (!compassAvailable && !rotationVectorAvailable)
            return 0;
        updateOrientation();
        return pitch;
    }

    @Override
    public float getRoll() {
        if (!compassAvailable && !rotationVectorAvailable)
            return 0;
        updateOrientation();
        return roll;
    }

    public void registerSensorListeners() {
        if (useAccelerometer) {
            manager = (SensorManager) app.getSystemService(Context.SENSOR_SERVICE);
            if (manager.getSensorList(Sensor.TYPE_ACCELEROMETER).isEmpty()) {
                accelerometerAvailable = false;
            } else {
                Sensor accelerometer = manager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
                accelerometerListener = new SensorEventListener() {
                    @Override
                    public void onAccuracyChanged(Sensor arg0, int arg1) {
                    }

                    @Override
                    public void onSensorChanged(SensorEvent event) {
                        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
                            return;
                        if (nativeOrientation == Orientation.Portrait) {
                            System.arraycopy(event.values, 0, accelerometerValues, 0, accelerometerValues.length);
                        } else {
                            accelerometerValues[0] = event.values[1];
                            accelerometerValues[1] = -event.values[0];
                            accelerometerValues[2] = event.values[2];
                        }
                    }
                };
                accelerometerAvailable = manager.registerListener(accelerometerListener, accelerometer, 0);
            }
        } else
            accelerometerAvailable = false;
        if (useGyroscope) {
            manager = (SensorManager) app.getSystemService(Context.SENSOR_SERVICE);
            if (manager.getSensorList(Sensor.TYPE_GYROSCOPE).isEmpty()) {
                gyroscopeAvailable = false;
            } else {
                Sensor gyroscope = manager.getSensorList(Sensor.TYPE_GYROSCOPE).get(0);
                gyroscopeListener = new SensorEventListener() {
                    @Override
                    public void onAccuracyChanged(Sensor arg0, int arg1) {
                    }

                    @Override
                    public void onSensorChanged(SensorEvent event) {
                        if (event.sensor.getType() != Sensor.TYPE_GYROSCOPE)
                            return;
                        if (nativeOrientation == Orientation.Portrait) {
                            System.arraycopy(event.values, 0, gyroscopeValues, 0, gyroscopeValues.length);
                        } else {
                            gyroscopeValues[0] = event.values[1];
                            gyroscopeValues[1] = -event.values[0];
                            gyroscopeValues[2] = event.values[2];
                        }
                    }
                };
                gyroscopeAvailable = manager.registerListener(gyroscopeListener, gyroscope, 0);
            }
        } else
            gyroscopeAvailable = false;
        rotationVectorAvailable = false;
        if (useRotationVector) {
            if (manager == null)
                manager = (SensorManager) app.getSystemService(Context.SENSOR_SERVICE);
            List<Sensor> rotationVectorSensors = manager.getSensorList(Sensor.TYPE_ROTATION_VECTOR);
            if (!rotationVectorSensors.isEmpty()) {
                rotationVectorListener = new SensorEventListener() {
                    @Override
                    public void onAccuracyChanged(Sensor arg0, int arg1) {
                    }

                    @Override
                    public void onSensorChanged(SensorEvent event) {
                        if (event.sensor.getType() != Sensor.TYPE_ROTATION_VECTOR)
                            return;
                        if (nativeOrientation == Orientation.Portrait) {
                            System.arraycopy(event.values, 0, rotationVectorValues, 0, rotationVectorValues.length);
                        } else {
                            rotationVectorValues[0] = event.values[1];
                            rotationVectorValues[1] = -event.values[0];
                            rotationVectorValues[2] = event.values[2];
                        }

                    }
                };
                for (Sensor sensor : rotationVectorSensors) { // favor AOSP sensor
                    if (sensor.getVendor().equals("Google Inc.") && sensor.getVersion() == 3) {
                        rotationVectorAvailable = manager.registerListener(rotationVectorListener, sensor, 0);
                        break;
                    }
                }
                if (!rotationVectorAvailable)
                    rotationVectorAvailable = manager.registerListener(rotationVectorListener,
                            rotationVectorSensors.get(0), 0);
            }
        }
        if (useCompas && !rotationVectorAvailable) {
            if (manager == null)
                manager = (SensorManager) app.getSystemService(Context.SENSOR_SERVICE);
            Sensor sensor = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            if (sensor != null) {
                compassAvailable = accelerometerAvailable;
                if (compassAvailable) {
                    compassListener = new SensorEventListener() {
                        @Override
                        public void onAccuracyChanged(Sensor arg0, int arg1) {
                        }

                        @Override
                        public void onSensorChanged(SensorEvent event) {
                            if (event.sensor.getType() != Sensor.TYPE_MAGNETIC_FIELD)
                                return;
                            System.arraycopy(event.values, 0, magneticFieldValues, 0, magneticFieldValues.length);
                        }
                    };
                    compassAvailable = manager.registerListener(compassListener, sensor, 0);
                }
            } else {
                compassAvailable = false;
            }
        } else
            compassAvailable = false;
    }

    public void unregisterSensorListeners() {
        if (manager != null) {
            if (accelerometerListener != null) {
                manager.unregisterListener(accelerometerListener);
                accelerometerListener = null;
            }
            if (gyroscopeListener != null) {
                manager.unregisterListener(gyroscopeListener);
                gyroscopeListener = null;
            }
            if (rotationVectorListener != null) {
                manager.unregisterListener(rotationVectorListener);
                rotationVectorListener = null;
            }
            if (compassListener != null) {
                manager.unregisterListener(compassListener);
                compassListener = null;
            }
            manager = null;
        }
    }

    @Override
    public InputProcessor getInputProcessor() {
        return this.processor;
    }

    @Override
    public void setInputProcessor(InputProcessor processor) {
        synchronized (this) {
            this.processor = processor;
        }
    }

    @Override
    public boolean isPeripheralAvailable(Peripheral peripheral) {
        switch (peripheral) {
            case Accelerometer:
                return accelerometerAvailable;
            case Gyroscope:
                return gyroscopeAvailable;
            case Compass:
                return compassAvailable;
            case HardwareKeyboard:
                return keyboardAvailable;
            case Pressure:
            case OnscreenKeyboard:
                return true;
            case Vibrator:
                return vibrator != null && vibrator.hasVibrator();
            case MultitouchScreen:
                return hasMultitouch;
            case RotationVector:
                return rotationVectorAvailable;
            default:
                return false;
        }
    }

    public int getFreePointerIndex() {
        int len = realId.length;
        for (int i = 0; i < len; i++) {
            if (realId[i] == -1)
                return i;
        }
        pressure = resize(pressure);
        realId = resize(realId);
        touchX = resize(touchX);
        touchY = resize(touchY);
        deltaX = resize(deltaX);
        deltaY = resize(deltaY);
        touched = resize(touched);
        button = resize(button);
        return len;
    }

    private int[] resize(int[] orig) {
        int[] tmp = new int[orig.length + 2];
        System.arraycopy(orig, 0, tmp, 0, orig.length);
        return tmp;
    }

    private boolean[] resize(boolean[] orig) {
        boolean[] tmp = new boolean[orig.length + 2];
        System.arraycopy(orig, 0, tmp, 0, orig.length);
        return tmp;
    }

    private float[] resize(float[] orig) {
        float[] tmp = new float[orig.length + 2];
        System.arraycopy(orig, 0, tmp, 0, orig.length);
        return tmp;
    }

    public int lookUpPointerIndex(int pointerId) {
        int len = realId.length;
        for (int i = 0; i < len; i++) {
            if (realId[i] == pointerId)
                return i;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(i + ":" + realId[i] + " ");
        }
        GraphFunc.app.log("AndroidInput", "Pointer ID lookup failed: " + pointerId + ", " + sb.toString());
        return -1;
    }

    @Override
    public int getRotation() {
        int orientation = app.getWindowManager().getDefaultDisplay().getRotation();
        switch (orientation) {
            default:
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
    }

    @Override
    public Orientation getNativeOrientation() {
        return nativeOrientation;
    }

    @Override
    public int getDeltaX() {
        return deltaX[0];
    }

    @Override
    public int getDeltaX(int pointer) {
        return deltaX[pointer];
    }

    @Override
    public int getDeltaY() {
        return deltaY[0];
    }

    @Override
    public int getDeltaY(int pointer) {
        return deltaY[pointer];
    }

    @Override
    public long getCurrentEventTime() {
        return currentEventTimeStamp;
    }

    public void addKeyListener(OnKeyListener listener) {
        keyListeners.add(listener);
    }

    @Override
    public boolean onGenericMotion(View view, MotionEvent event) {
        if ((event.getSource() & InputDevice.SOURCE_CLASS_POINTER) != 0) {
            final int action = event.getAction() & MotionEvent.ACTION_MASK;
            int x = 0, y = 0;
            long timeStamp = System.nanoTime();
            synchronized (this) {
                switch (action) {
                    case MotionEvent.ACTION_HOVER_MOVE:
                        x = (int) event.getX();
                        y = (int) event.getY();
                        if ((x != deltaGX) || (y != deltaGY)) { // Avoid garbage events
                            TouchEvent te = usedTouchEvents.obtain();
                            te.timeStamp = timeStamp;
                            te.x = deltaGX = x;
                            te.y = deltaGY = y;
                            te.type = TouchEvent.TOUCH_MOVED;
                            te.scrollAmount = 0;
                            touchEvents.add(te);
                        }
                        break;
                    case MotionEvent.ACTION_SCROLL:
                        TouchEvent te = usedTouchEvents.obtain();
                        te.timeStamp = timeStamp;
                        te.x = 0;
                        te.y = 0;
                        te.type = TouchEvent.TOUCH_SCROLLED;
                        te.scrollAmount = (int) -Math.signum(event.getAxisValue(MotionEvent.AXIS_VSCROLL));
                        touchEvents.add(te);

                }
            }
            return true;
        }
        for (int i = 0, n = genericMotionListeners.size(); i < n; i++)
            if (genericMotionListeners.get(i).onGenericMotion(view, event))
                return true;
        return false;
    }

    public void addGenericMotionListener(OnGenericMotionListener listener) {
        genericMotionListeners.add(listener);
    }

    public void onPause() {
        unregisterSensorListeners();
        // erase pointer ids and touched state. this sucks donkeyballs...
        Arrays.fill(realId, -1);
        Arrays.fill(touched, false);
    }

    public void onResume() {
        registerSensorListeners();
    }

    static class KeyEvent {
        static final int KEY_DOWN = 0;
        static final int KEY_UP = 1;
        static final int KEY_TYPED = 2;

        long timeStamp;
        int type;
        int keyCode;
        char keyChar;
    }

    static class TouchEvent {
        static final int TOUCH_DOWN = 0;
        static final int TOUCH_UP = 1;
        static final int TOUCH_DRAGGED = 2;
        static final int TOUCH_SCROLLED = 3;
        static final int TOUCH_MOVED = 4;

        long timeStamp;
        int type;
        int x;
        int y;
        int scrollAmount;
        int button;
        int pointer;
    }
}
