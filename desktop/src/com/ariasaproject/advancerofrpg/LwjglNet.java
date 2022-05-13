package com.ariasaproject.advancerofrpg;

import java.awt.Desktop;

import com.ariasaproject.advancerofrpg.net.Net;

public class LwjglNet extends Net {

	@Override
	public boolean openURI (String URI) {
		if (!Desktop.isDesktopSupported()) return false;

		Desktop desktop = Desktop.getDesktop();

		if (!desktop.isSupported(Desktop.Action.BROWSE)) return false;

		try {
			desktop.browse(new java.net.URI(URI));
		} catch (Exception e) {
			return false;
		}
		return true;
	}

}
