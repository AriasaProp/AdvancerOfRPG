package com.ariasaproject.advancerofrpg;

import com.ariasaproject.advancerofrpg.graphics.Graphics;
import com.ariasaproject.advancerofrpg.graphics.TGF;

import java.util.Random;

public class AppV2 {

    public AppV2() {
    }

    public void create() {
    }

    public void resize(int width, int height) {
    }

    public void resume() {
    }

    public void render(float delta) {
    		if (GraphFunc.app.getInput().justTouched()) {
    				Random r = new Random();
    				GraphFunc.tgf.glClearColorMask(TGF.GL_COLOR_BUFFER_BIT, r.nextFloat(), r.nextFloat(), r.nextFloat(), 1);
    		}
    }

    public void pause() {
    }

    public void destroy() {
    }
}