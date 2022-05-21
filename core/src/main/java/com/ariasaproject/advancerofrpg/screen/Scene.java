package com.ariasaproject.advancerofrpg.screen;

import com.ariasaproject.advancerofrpg.ApplicationListener;

public abstract class Scene {
    final ApplicationListener appl;

    public Scene(ApplicationListener appl) {
        this.appl = appl;
    }

    public abstract void show();

    public abstract void render(float delta);

    public abstract void resize(int width, int height);

    public abstract void pause();

    public abstract void resume();

    public abstract void hide();
}
