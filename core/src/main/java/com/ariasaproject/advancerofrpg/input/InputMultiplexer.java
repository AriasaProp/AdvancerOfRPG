package com.ariasaproject.advancerofrpg.input;

import com.ariasaproject.advancerofrpg.utils.Array;
import com.ariasaproject.advancerofrpg.utils.SnapshotArray;

public class InputMultiplexer implements InputProcessor {
    private final SnapshotArray<InputProcessor> processors = new SnapshotArray<InputProcessor>(4);

    public InputMultiplexer() {
    }

    public InputMultiplexer(InputProcessor... processors) {
        this.processors.add(processors);
    }

    public void addProcessor(int index, InputProcessor processor) {
        if (processor == null)
            throw new NullPointerException("processor cannot be null");
        processors.insert(index, processor);
    }

    public void removeProcessor(int index) {
        processors.removeIndex(index);
    }

    public void addProcessor(InputProcessor processor) {
        if (processor == null)
            throw new NullPointerException("processor cannot be null");
        processors.add(processor);
    }

    public void removeProcessor(InputProcessor processor) {
        processors.removeValue(processor, true);
    }

    public int size() {
        return processors.size;
    }

    public void clear() {
        processors.clear();
    }

    public SnapshotArray<InputProcessor> getProcessors() {
        return processors;
    }

    public void setProcessors(Array<InputProcessor> processors) {
        this.processors.clear();
        this.processors.addAll(processors);
    }

    public void setProcessors(InputProcessor... processors) {
        this.processors.clear();
        this.processors.add(processors);
    }

    @Override
    public boolean keyDown(int keycode) {
        InputProcessor[] items = processors.begin();
        try {
            for (int i = 0, n = processors.size; i < n; i++)
                if (items[i].keyDown(keycode))
                    return true;
        } finally {
            processors.end();
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        InputProcessor[] items = processors.begin();
        try {
            for (int i = 0, n = processors.size; i < n; i++)
                if (items[i].keyUp(keycode))
                    return true;
        } finally {
            processors.end();
        }
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        InputProcessor[] items = processors.begin();
        try {
            for (int i = 0, n = processors.size; i < n; i++)
                if (items[i].keyTyped(character))
                    return true;
        } finally {
            processors.end();
        }
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        InputProcessor[] items = processors.begin();
        try {
            for (int i = 0, n = processors.size; i < n; i++)
                if (items[i].touchDown(screenX, screenY, pointer, button))
                    return true;
        } finally {
            processors.end();
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        InputProcessor[] items = processors.begin();
        try {
            for (int i = 0, n = processors.size; i < n; i++)
                if (items[i].touchUp(screenX, screenY, pointer, button))
                    return true;
        } finally {
            processors.end();
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        InputProcessor[] items = processors.begin();
        try {
            for (int i = 0, n = processors.size; i < n; i++)
                if (items[i].touchDragged(screenX, screenY, pointer))
                    return true;
        } finally {
            processors.end();
        }
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        InputProcessor[] items = processors.begin();
        try {
            for (int i = 0, n = processors.size; i < n; i++)
                if (items[i].mouseMoved(screenX, screenY))
                    return true;
        } finally {
            processors.end();
        }
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        InputProcessor[] items = processors.begin();
        try {
            for (int i = 0, n = processors.size; i < n; i++)
                if (items[i].scrolled(amount))
                    return true;
        } finally {
            processors.end();
        }
        return false;
    }
}
