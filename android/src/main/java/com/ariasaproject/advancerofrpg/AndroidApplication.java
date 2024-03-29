package com.ariasaproject.advancerofrpg;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.widget.Toast;

import com.ariasaproject.advancerofrpg.graphics.TGF;
import com.ariasaproject.advancerofrpg.GraphFunc;

import com.ariasaproject.advancerofrpg.Files.FileHandle;
import com.ariasaproject.advancerofrpg.Files.FileType;
import com.ariasaproject.advancerofrpg.audio.Audio;
import com.ariasaproject.advancerofrpg.audio.AudioDevice;
import com.ariasaproject.advancerofrpg.audio.AudioRecorder;
import com.ariasaproject.advancerofrpg.audio.Music;
import com.ariasaproject.advancerofrpg.audio.Sound;
import com.ariasaproject.advancerofrpg.graphics.Graphics;
import com.ariasaproject.advancerofrpg.input.Clipboard;
import com.ariasaproject.advancerofrpg.input.Input;
import com.ariasaproject.advancerofrpg.utils.Array;
import com.ariasaproject.advancerofrpg.utils.SnapshotArray;

import com.ariasaproject.advancerofrpg.ApplicationListener;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.Exception;
import java.lang.Throwable;
import java.io.FileOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//AndroidApplication include Graphics, Audio and Application
public class AndroidApplication extends Activity implements Application, Runnable, Graphics, Callback, Audio {
    public static final String TAG = "MainActivity";

    static {
        try {
            System.loadLibrary("ext");
        } catch (Exception e) {
            System.out.println("failed to load library : ext");
            System.exit(0);
        }
    }

    protected final Array<Runnable> runnables = new Array<Runnable>();
    protected final SnapshotArray<LifecycleListener> lifecycleListeners = new SnapshotArray<LifecycleListener>(LifecycleListener.class);
    private final List<AndroidMusic> musics = new ArrayList<AndroidMusic>();
    protected AndroidInput input;
    protected AndroidFiles files;
    protected AndroidNet net;
    protected AndroidClipboard clipboard;
    int majorV, minorV;
    volatile boolean resume = false, pause = false, destroy = false, resize = false, rendered = false, hasFocus = true,
            hasSurface = false, mExited = false;
    volatile boolean restart = false;
    long frameStart = System.currentTimeMillis(), lastFrameTime = System.currentTimeMillis();
    int frames, fps, width = 0, height = 0;
    float deltaTime = 0;
    Thread mainTGFThread;
    // graphics params
    private SurfaceHolder holder;
    private final AndroidTGF tgf = new OpenGLES30();
    //audio params
    private SoundPool soundPool;
    private AudioManager manager;
    private static Context ctx;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        super.onCreate(savedInstanceState);
        final View d = getWindow().getDecorView();
  			final int uiHide = 5382;
        d.setSystemUiVisibility(uiHide);
        d.setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int ui) {
                if (ui != uiHide)
                    d.setSystemUiVisibility(uiHide);
            }
        });
        setContentView(R.layout.main);
        SurfaceView view = findViewById(R.id.root);
        
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        this.majorV = (short) (configurationInfo.reqGlEsVersion >> 16);
        this.minorV = (short) (configurationInfo.reqGlEsVersion & 0x0000ffff);
        
        this.input = new AndroidInput(this, view);
        //audio preparation
        AudioAttributes audioAttrib = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
        soundPool = new SoundPool.Builder().setAudioAttributes(audioAttrib).setMaxStreams(1).build();

        manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        //end audio
        getFilesDir(); // workaround for Android bug #10515463
        this.files = new AndroidFiles(this);
        this.net = new AndroidNet(this);
        this.clipboard = new AndroidClipboard(this);
        addLifecycleListener(files);
        if (getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS)
            input.setKeyboardAvailable(true);
        // for graphics loop
        this.holder = view.getHolder();
        GraphFunc.app = this;
        GraphFunc.tgf = tgf;
        ctx = getApplicationContext();
        mainTGFThread = new Thread(this, "GLThread");
        mainTGFThread.start();
        holder.addCallback(this);
    }

    @Override
    protected synchronized void onResume() {
        super.onResume();
        resume = true;
        notifyAll();
        input.onResume();
    }
    @Override
    public synchronized void restart() {
    	restart = true;
      exit();
    }

    @Override
    public void exit() {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            finish();
          }
        });
    }

    @Override
    public synchronized void onWindowFocusChanged(final boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (this.hasFocus = hasFocus) {
            //audio onResume
            synchronized (musics) {
                for (int i = 0; i < musics.size(); i++) {
                    if (musics.get(i).wasPlaying)
                        musics.get(i).play();
                }
            }
            this.soundPool.autoResume();
        }
    }

    @Override
    protected synchronized void onPause() {
        input.onPause();
        //audio pause
        synchronized (musics) {
            for (AndroidMusic music : musics) {
                if (music.isPlaying()) {
                    music.pause();
                    music.wasPlaying = true;
                } else
                    music.wasPlaying = false;
            }
        }
        soundPool.autoPause();
        // graphics
        if (isFinishing()) {
            destroy = true;
            notifyAll();
            while (!mExited) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
            mExited = false;
        } else {
	        pause = true;
	        rendered = true;
	        notifyAll();
	        while (!mExited && rendered) {
	            try {
	                wait();
	            } catch (InterruptedException ex) {
	                Thread.currentThread().interrupt();
	            }
	        }
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
    
    @Override
    protected synchronized void onDestroy() {
        net.destroy();
        //audio destroy
        synchronized (musics) {
            ArrayList<Music> musicsCopy = new ArrayList<Music>(musics);
            for (Music music : musicsCopy) {
                music.dispose();
            }
        }
        soundPool.release();
        super.onDestroy();
        holder.removeCallback(this);
        if (restart) {
        	restart = false;
          startActivity(getIntent());
        }
    }

    @Override
    public Audio getAudio() {
        return this;
    }

    @Override
    public Input getInput() {
        return input;
    }

    @Override
    public Files getFiles() {
        return files;
    }

    @Override
    public Graphics getGraphics() {
        return this;
    }

    @Override
    public Net getNet() {
        return net;
    }

    @Override
    public Clipboard getClipboard() {
        return clipboard;
    }

    @Override
    public void postRunnable(Runnable runnable) {
        synchronized (runnables) {
            runnables.add(runnable);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        input.setKeyboardAvailable(config.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO);
    }

    @Override
    public void debug(String tag, String message) {
        Log.d(tag, message);
    }

    @Override
    public void debug(String tag, String message, Throwable exception) {
        Log.d(tag, message, exception);
    }

    @Override
    public void log(String tag, String message) {
        Log.i(tag, message);
    }

    @Override
    public void log(String tag, String message, Throwable exception) {
        Log.i(tag, message, exception);
    }

    @Override
    public void error(String tag, String message) {
        Log.e(tag, message);
    }

    @Override
    public void error(String tag, String message, Throwable exception) {
        Log.e(tag, message, exception);

    }

    @Override
    public void addLifecycleListener(LifecycleListener listener) {
        synchronized (lifecycleListeners) {
            lifecycleListeners.add(listener);
        }
    }
    // graphics loop function

    @Override
    public void removeLifecycleListener(LifecycleListener listener) {
        synchronized (lifecycleListeners) {
            lifecycleListeners.removeValue(listener, true);
        }
    }

    @Override
    public float getDeltaTime() {
        return deltaTime;
    }

    @Override
    public int getFramesPerSecond() {
        return fps;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public synchronized void surfaceCreated(SurfaceHolder holder) {
        // fall thru surfaceChanged
        hasSurface = true;
        notifyAll();
    }

    @Override
    public synchronized void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        width = w;
        height = h;
        resize = true;
        notifyAll();
    }

    @Override
    public synchronized void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
        notifyAll();
    }

    // main loop
    @Override
    public void run() {
        EGLDisplay mEglDisplay = null;
        EGLSurface mEglSurface = null;
        EGLConfig mEglConfig = null;
        EGLContext mEglContext = null;
        ApplicationListener appl = new ApplicationListener();
        //AppV2 appl = new AppV2();
        Toast.makeText(getApplicationContext(), "Ending", Toast.LENGTH_SHORT).show();
        try {
            byte eglDestroyRequest = 0;// to destroy egl surface, egl contex, egl display, ?....
            boolean wantRender = false,// indicator
                    created = false, lrunning = true, lresize, lresume = false, lpause = false;// on running state
            while (!destroy) {
                synchronized (this) {
                    // render notify
                    if (wantRender) {
                        rendered = false;
                        wantRender = false;
                        notifyAll();
                    }
                    if (rendered)
                        wantRender = true;
                    // egl destroy request
                    if (mEglSurface != null && (eglDestroyRequest > 0 || !hasSurface)) {
                        EGL14.eglMakeCurrent(mEglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
                        if (!EGL14.eglDestroySurface(mEglDisplay, mEglSurface))
                            throw new RuntimeException("eglDestroySurface failed: " + Integer.toHexString(EGL14.eglGetError()));
                        mEglSurface = null;
                        if (mEglContext != null && (eglDestroyRequest > 1)) {
                            if (!EGL14.eglDestroyContext(mEglDisplay, mEglContext))
                                throw new RuntimeException("eglDestroyContext failed: " + Integer.toHexString(EGL14.eglGetError()));
                            mEglContext = null;
                            if (mEglDisplay != null && (eglDestroyRequest > 3)) {
                                EGL14.eglTerminate(mEglDisplay);
                                mEglDisplay = null;
                            }
                        }
                        eglDestroyRequest = 0;
                    }
                    // end destroy request

                    lresize = resize;
                    if (resize)
                        resize = false;
                    if (lpause)
                        lrunning = false;
                    lpause = pause;
                    if (pause)
                        pause = false;
                    if (resume) {
                        resume = false;
                        lresume = true;
                        lrunning = true;
                    }
                    // Ready to draw?
                    if (!lrunning || !hasSurface) {
                        wait();
                        continue;
                    }
                }

                boolean newContext = mEglContext == null;
                if (mEglDisplay == null || newContext || mEglSurface == null) {
                  final int[] temp = new int[1]; // for chaching value output
	                if (mEglDisplay == null) {
                    mEglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
                    if (mEglDisplay == EGL14.EGL_NO_DISPLAY || mEglDisplay == null) {
                        mEglDisplay = null;
                        throw new RuntimeException("eglGetDisplay failed " + Integer.toHexString(EGL14.eglGetError()));
                    }
                    if (EGL14.eglInitialize(mEglDisplay, null, 0, null, 0))
                        throw new RuntimeException("eglInitialize failed " + Integer.toHexString(EGL14.eglGetError()));
	                }
	                if (mEglConfig == null) {
                    // choose best config
                    final int[] configAttr = new int[]{
									    EGL14.EGL_SURFACE_TYPE, EGL14.EGL_WINDOW_BIT,
									    EGL14.EGL_COLOR_BUFFER_TYPE, EGL14.EGL_RGB_BUFFER,
									    EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
									    EGL14.EGL_CONFORMANT, EGL14.EGL_OPENGL_ES2_BIT,
									    EGL14.EGL_ALPHA_SIZE, 0,
									    EGL14.EGL_NONE
									  };
                    EGL14.eglChooseConfig(mEglDisplay, configAttr, 0, null, 0, 0, temp, 0);
                    if (temp[0] <= 0)
                        throw new IllegalArgumentException("No configs match with configSpec");
                    EGLConfig[] configs = new EGLConfig[temp[0]];
                    EGL14.eglChooseConfig(mEglDisplay, configAttr, 0, configs, 0, temp[0], null, 0);
                    int lastSc = -1, curSc;
                    mEglConfig = configs[0];
                    for (EGLConfig config : configs) {
                        EGL14.eglGetConfigAttrib(mEglDisplay, config, EGL14.EGL_BUFFER_SIZE, temp, 0);
                        curSc = temp[0];
                        EGL14.eglGetConfigAttrib(mEglDisplay, config, EGL14.EGL_DEPTH_SIZE, temp, 0);
                        curSc += temp[0];
                        EGL14.eglGetConfigAttrib(mEglDisplay, config, EGL14.EGL_STENCIL_SIZE, temp, 0);
                        curSc += temp[0];
                        if (curSc > lastSc) {
                            lastSc = curSc;
                            mEglConfig = config;
                        }
                    }
                  }
	                if (newContext) {
	                	final int[] ctxAttr = new int[]{EGL14.EGL_CONTEXT_CLIENT_VERSION, majorV, EGL14.EGL_NONE};
                    mEglContext = EGL14.eglCreateContext(mEglDisplay, mEglConfig, EGL14.EGL_NO_CONTEXT, ctxAttr, 0);
                    if (mEglContext == null || mEglContext == EGL14.EGL_NO_CONTEXT) {
                      mEglContext = null;
                      throw new RuntimeException("createContext failed: " + Integer.toHexString(EGL14.eglGetError()));
                    }
	                }
	                if (mEglSurface == null) {
	                	final int[] srfAttr = new int[]{EGL14.EGL_NONE};
                    mEglSurface = EGL14.eglCreateWindowSurface(mEglDisplay, mEglConfig, holder, srfAttr, 0);
                    if (mEglSurface == null || mEglSurface == EGL14.EGL_NO_SURFACE) {
                        mEglSurface = null;
                        throw new RuntimeException("Create EGL Surface failed: " + Integer.toHexString(EGL14.eglGetError()));
                    }
	                }
                  EGL14.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext);
                }
                if (newContext) {
                  if (created)
                    tgf.validateAll();
                  else {
                    appl.create();
                    created = true;
              		}
                  appl.resize(width, height);
                  lresize = false;
                  lastFrameTime = System.currentTimeMillis();
                }
                if (lresize) {
                    EGL14.eglMakeCurrent(mEglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
                    EGL14.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext);
                    appl.resize(width, height);
                }
                long time = System.currentTimeMillis();
                if (lresume) {
                		lresume = false;
                    synchronized (lifecycleListeners) {
                        LifecycleListener[] listeners = lifecycleListeners.begin();
                        for (int i = 0, n = lifecycleListeners.size; i < n; i++) {
                            listeners[i].resume();
                        }
                        lifecycleListeners.end();
                    }
                    appl.resume();
                    time = frameStart = lastFrameTime = 0;
                }
                if (time - frameStart > 1000l) {
                    fps = frames;
                    frames = 0;
                    frameStart = time;
                }
                deltaTime = (time - lastFrameTime) / 1000f;
                lastFrameTime = time;
                synchronized (runnables) {
                    for (Runnable r : runnables)
                        r.run();
                    runnables.clear();
                }
                input.processEvents();
                appl.render(deltaTime);
                if (lpause) {
                    LifecycleListener[] listeners = lifecycleListeners.begin();
                    for (int i = 0, n = lifecycleListeners.size; i < n; i++) {
                        listeners[i].pause();
                    }
                    lifecycleListeners.end();
                    appl.pause();
                    eglDestroyRequest |= (tgf.limitGLESContext() ? 2 : 1);
                }
                if (!EGL14.eglSwapBuffers(mEglDisplay, mEglSurface)) {
                    int error = EGL14.eglGetError();
                    switch (error) {
                        case EGL14.EGL_BAD_DISPLAY:
                            eglDestroyRequest |= 4;
                            break;
                        case 0x300E:
                        case EGL14.EGL_BAD_CONTEXT:
                            eglDestroyRequest |= 2;
                            break;
                        case EGL14.EGL_BAD_SURFACE:
                            eglDestroyRequest |= 1;
                            break;
                        case EGL14.EGL_BAD_NATIVE_WINDOW:
                            error(TAG, "eglSwapBuffers returned EGL_BAD_NATIVE_WINDOW. tid=" + Thread.currentThread().getId());
                            break;
                        default:
                            error(TAG, "eglSwapBuffers failed: " + Integer.toHexString(error));
                    }
                }
                frames++;
            }
        } catch (Throwable e) { 
        		Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            error(TAG, "error", e);
        }
    		Toast.makeText(getApplicationContext(), "Ending", Toast.LENGTH_SHORT).show();
        // dispose all resources
        appl.destroy();
        tgf.clear();
        LifecycleListener[] listeners = lifecycleListeners.begin();
        for (int i = 0, n = lifecycleListeners.size; i < n; i++) {
            listeners[i].dispose();
        }
        lifecycleListeners.end();
        if (mEglSurface != null) {
            EGL14.eglMakeCurrent(mEglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
            EGL14.eglDestroySurface(mEglDisplay, mEglSurface);
            mEglSurface = null;
        }
        if (mEglContext != null) {
            EGL14.eglDestroyContext(mEglDisplay, mEglContext);
            mEglContext = null;
        }
        if (mEglDisplay != null) {
            EGL14.eglTerminate(mEglDisplay);
            mEglDisplay = null;
        }
        // end thread
        synchronized (this) {
            mExited = true;
            notifyAll();
        }
        if (!destroy) finish();
    }

    @Override
    public AudioDevice newAudioDevice(int samplingRate, boolean isMono) {
        return new AndroidAudioDevice(samplingRate, isMono);
    }

    @Override
    public Music newMusic(FileHandle file) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        if (file.type() == FileType.Internal) {
            try {
                AssetFileDescriptor descriptor = files.descriptor(file);
                mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                descriptor.close();
                mediaPlayer.prepare();
                AndroidMusic music = new AndroidMusic(this, mediaPlayer);
                synchronized (musics) {
                    musics.add(music);
                }
                return music;
            } catch (Exception ex) {
                throw new RuntimeException("Error loading audio file: " + file + ", internal audio files should in the assets directory.", ex);
            }
        } else {
            try {
                mediaPlayer.setDataSource(file.file().getPath());
                mediaPlayer.prepare();
                AndroidMusic music = new AndroidMusic(this, mediaPlayer);
                synchronized (musics) {
                    musics.add(music);
                }
                return music;
            } catch (Exception ex) {
                throw new RuntimeException("Error loading audio file: " + file, ex);
            }
        }
    }

    public Music newMusic(FileDescriptor fd) {
    		MediaPlayer mediaPlayer = new MediaPlayer();
    		try {
  					mediaPlayer.setDataSource(fd);
  					mediaPlayer.prepare();
  					AndroidMusic music = new AndroidMusic(this, mediaPlayer);
  					synchronized (musics) {
  							musics.add(music);
  					}
  					return music;
    		} catch (Exception ex) {
    				throw new RuntimeException("Error loading audio from FileDescriptor", ex);
    		}
    }

    @Override
    public Sound newSound(FileHandle file) {
    		if (file.type() == FileType.Internal) {
    				try {
    						AssetFileDescriptor descriptor = files.descriptor(file);
    						Sound sound = new AndroidSound(soundPool, manager, soundPool.load(descriptor, 1));
    						descriptor.close();
    						return sound;
    				} catch (IOException ex) {
    						throw new RuntimeException("Error loading audio file: " + file + ", Internal audio files should in the assets directory.", ex);
    				}
    		} else {
    				try {
    						return new AndroidSound(soundPool, manager, soundPool.load(file.file().getPath(), 1));
    				} catch (Exception ex) {
    						throw new RuntimeException("Error loading audio file: " + file, ex);
    				}
    		}
    }

    @Override
    public AudioRecorder newAudioRecorder(int samplingRate, boolean isMono) {
    		return new AndroidAudioRecorder(samplingRate, isMono);
    }

    @Override
    public void disposeMusic(Music music) {
    		synchronized (musics) {
    				musics.remove(music);
    		}
    }
}
