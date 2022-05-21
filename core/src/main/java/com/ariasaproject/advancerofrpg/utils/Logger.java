package com.ariasaproject.advancerofrpg.utils;

import com.ariasaproject.advancerofrpg.GraphFunc;

public class Logger {
    static public final int NONE = 0;
    static public final int ERROR = 1;
    static public final int INFO = 2;
    static public final int DEBUG = 3;

    private final String tag;
    private int level;

    public Logger(String tag) {
        this(tag, ERROR);
    }

    public Logger(String tag, int level) {
        this.tag = tag;
        this.level = level;
    }

    public void debug(String message) {
        if (level >= DEBUG)
            GraphFunc.app.debug(tag, message);
    }

    public void debug(String message, Exception exception) {
        if (level >= DEBUG)
            GraphFunc.app.debug(tag, message, exception);
    }

    public void info(String message) {
        if (level >= INFO)
            GraphFunc.app.log(tag, message);
    }

    public void info(String message, Exception exception) {
        if (level >= INFO)
            GraphFunc.app.log(tag, message, exception);
    }

    public void error(String message) {
        if (level >= ERROR)
            GraphFunc.app.error(tag, message);
    }

    public void error(String message, Throwable exception) {
        if (level >= ERROR)
            GraphFunc.app.error(tag, message, exception);
    }

    public int getLevel() {
        return level;
    }

    /**
     * Sets the log level. {@link #NONE} will mute all log output. {@link #ERROR}
     * will only let error messages through. {@link #INFO} will let all non-debug
     * messages through, and {@link #DEBUG} will let all messages through.
     *
     * @param level {@link #NONE}, {@link #ERROR}, {@link #INFO}, {@link #DEBUG}.
     */
    public void setLevel(int level) {
        this.level = level;
    }
}
