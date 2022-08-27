package com.ariasaproject.advancerofrpg.audio;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.utils.Disposable;

public interface Lwjgl3Audio extends Audio, Disposable {

	void update ();
}
