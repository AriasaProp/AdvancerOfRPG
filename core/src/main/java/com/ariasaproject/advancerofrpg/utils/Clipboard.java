package com.ariasaproject.advancerofrpg.utils;

/**
 * A very simple clipboard interface for text content.
 *
 * @author mzechner
 */
public interface Clipboard {
	/**
	 * gets the current content of the clipboard if it contains text for WebGL app,
	 * getting the system clipboard is currently not supported. It works only inside
	 * the app
	 *
	 * @return the clipboard content or null
	 */
	String getContents();

	/**
	 * Sets the content of the system clipboard. for WebGL app, clipboard content
	 * might not be set if user denied permission, setting clipboard is not
	 * synchronous so you can't rely on getting same content just after setting it
	 *
	 * @param content the content
	 */
	void setContents(String content);
}
