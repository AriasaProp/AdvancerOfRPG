package com.ariasaproject.advancerofrpg.graphics.g2d;

import com.ariasaproject.advancerofrpg.Files.FileHandle;
import com.ariasaproject.advancerofrpg.assets.AssetContainer;
import com.ariasaproject.advancerofrpg.assets.AssetDescriptor;
import com.ariasaproject.advancerofrpg.assets.AssetLoaderParameters;
import com.ariasaproject.advancerofrpg.assets.loaders.FileHandleResolver;
import com.ariasaproject.advancerofrpg.assets.loaders.SynchronousAssetLoader;
import com.ariasaproject.advancerofrpg.graphics.Texture;
import com.ariasaproject.advancerofrpg.math.collision.BoundingBox;
import com.ariasaproject.advancerofrpg.utils.Array;
import com.ariasaproject.advancerofrpg.utils.Disposable;
import com.ariasaproject.advancerofrpg.utils.ObjectMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;

public class ParticleEffect implements Disposable {
    private final Array<ParticleEmitter> emitters;
    protected float xSizeScale = 1f;
    protected float ySizeScale = 1f;
    protected float motionScale = 1f;
    private BoundingBox bounds;
    private boolean ownsTexture;

    public ParticleEffect() {
        emitters = new Array<ParticleEmitter>(8);
    }

    public ParticleEffect(ParticleEffect effect) {
        emitters = new Array<ParticleEmitter>(true, effect.emitters.size);
        for (int i = 0, n = effect.emitters.size; i < n; i++)
            emitters.add(newEmitter(effect.emitters.get(i)));
    }

    public void start() {
        for (int i = 0, n = emitters.size; i < n; i++)
            emitters.get(i).start();
    }

    public void reset() {
        reset(true);
    }

    public void reset(boolean resetScaling) {
        for (int i = 0, n = emitters.size; i < n; i++)
            emitters.get(i).reset();
        if (resetScaling && (xSizeScale != 1f || ySizeScale != 1f || motionScale != 1f)) {
            scaleEffect(1f / xSizeScale, 1f / ySizeScale, 1f / motionScale);
            xSizeScale = ySizeScale = motionScale = 1f;
        }
    }

    public void update(float delta) {
        for (int i = 0, n = emitters.size; i < n; i++)
            emitters.get(i).update(delta);
    }

    public void draw(Batch spriteBatch) {
        for (int i = 0, n = emitters.size; i < n; i++)
            emitters.get(i).draw(spriteBatch);
    }

    public void draw(Batch spriteBatch, float delta) {
        for (int i = 0, n = emitters.size; i < n; i++)
            emitters.get(i).draw(spriteBatch, delta);
    }

    public void allowCompletion() {
        for (int i = 0, n = emitters.size; i < n; i++)
            emitters.get(i).allowCompletion();
    }

    public boolean isComplete() {
        for (int i = 0, n = emitters.size; i < n; i++) {
            ParticleEmitter emitter = emitters.get(i);
            if (!emitter.isComplete())
                return false;
        }
        return true;
    }

    public void setDuration(int duration) {
        for (int i = 0, n = emitters.size; i < n; i++) {
            ParticleEmitter emitter = emitters.get(i);
            emitter.setContinuous(false);
            emitter.duration = duration;
            emitter.durationTimer = 0;
        }
    }

    public void setPosition(float x, float y) {
        for (int i = 0, n = emitters.size; i < n; i++)
            emitters.get(i).setPosition(x, y);
    }

    public void setFlip(boolean flipX, boolean flipY) {
        for (int i = 0, n = emitters.size; i < n; i++)
            emitters.get(i).setFlip(flipX, flipY);
    }

    public void flipY() {
        for (int i = 0, n = emitters.size; i < n; i++)
            emitters.get(i).flipY();
    }

    public Array<ParticleEmitter> getEmitters() {
        return emitters;
    }

    /**
     * Returns the emitter with the specified name, or null.
     */
    public ParticleEmitter findEmitter(String name) {
        for (int i = 0, n = emitters.size; i < n; i++) {
            ParticleEmitter emitter = emitters.get(i);
            if (emitter.getName().equals(name))
                return emitter;
        }
        return null;
    }

    /**
     * Allocates all emitters particles. See
     * {@link com.ariasaproject.advancerofrpg.graphics.g2d.ParticleEmitter#preAllocateParticles()}
     */
    public void preAllocateParticles() {
        for (ParticleEmitter emitter : emitters) {
            emitter.preAllocateParticles();
        }
    }

    public void save(Writer output) throws IOException {
        int index = 0;
        for (int i = 0, n = emitters.size; i < n; i++) {
            ParticleEmitter emitter = emitters.get(i);
            if (index++ > 0)
                output.write("\n");
            emitter.save(output);
        }
    }

    public void load(FileHandle effectFile, FileHandle imagesDir) {
        loadEmitters(effectFile);
        loadEmitterImages(imagesDir);
    }

    public void load(FileHandle effectFile, TextureAtlas atlas) {
        load(effectFile, atlas, null);
    }

    public void load(FileHandle effectFile, TextureAtlas atlas, String atlasPrefix) {
        loadEmitters(effectFile);
        loadEmitterImages(atlas, atlasPrefix);
    }

    public void loadEmitters(FileHandle effectFile) {
        InputStream input = effectFile.read();
        emitters.clear();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(input), 512);
            while (true) {
                ParticleEmitter emitter = newEmitter(reader);
                emitters.add(emitter);
                if (reader.readLine() == null)
                    break;
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error loading effect: " + effectFile, ex);
        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (Throwable ignored) {
                }
        }
    }

    public void loadEmitterImages(TextureAtlas atlas) {
        loadEmitterImages(atlas, null);
    }

    public void loadEmitterImages(TextureAtlas atlas, String atlasPrefix) {
        for (int i = 0, n = emitters.size; i < n; i++) {
            ParticleEmitter emitter = emitters.get(i);
            if (emitter.getImagePaths().size == 0)
                continue;
            Array<Sprite> sprites = new Array<Sprite>();
            for (String imagePath : emitter.getImagePaths()) {
                String imageName = new File(imagePath.replace('\\', '/')).getName();
                int lastDotIndex = imageName.lastIndexOf('.');
                if (lastDotIndex != -1)
                    imageName = imageName.substring(0, lastDotIndex);
                if (atlasPrefix != null)
                    imageName = atlasPrefix + imageName;
                Sprite sprite = atlas.createSprite(imageName);
                if (sprite == null)
                    throw new IllegalArgumentException("SpriteSheet missing image: " + imageName);
                sprites.add(sprite);
            }
            emitter.setSprites(sprites);
        }
    }

    public void loadEmitterImages(FileHandle imagesDir) {
        ownsTexture = true;
        ObjectMap<String, Sprite> loadedSprites = new ObjectMap<String, Sprite>(emitters.size);
        for (int i = 0, n = emitters.size; i < n; i++) {
            ParticleEmitter emitter = emitters.get(i);
            if (emitter.getImagePaths().size == 0)
                continue;
            Array<Sprite> sprites = new Array<Sprite>();
            for (String imagePath : emitter.getImagePaths()) {
                String imageName = new File(imagePath.replace('\\', '/')).getName();
                Sprite sprite = loadedSprites.get(imageName);
                if (sprite == null) {
                    sprite = new Sprite(loadTexture(imagesDir.child(imageName)));
                    loadedSprites.put(imageName, sprite);
                }
                sprites.add(sprite);
            }
            emitter.setSprites(sprites);
        }
    }

    protected ParticleEmitter newEmitter(BufferedReader reader) throws IOException {
        return new ParticleEmitter(reader);
    }

    protected ParticleEmitter newEmitter(ParticleEmitter emitter) {
        return new ParticleEmitter(emitter);
    }

    protected Texture loadTexture(FileHandle file) {
        return new Texture(file, false);
    }

    @Override
    public void dispose() {
        if (!ownsTexture)
            return;
        for (int i = 0, n = emitters.size; i < n; i++) {
            ParticleEmitter emitter = emitters.get(i);
            for (Sprite sprite : emitter.getSprites()) {
                sprite.getTexture().dispose();
            }
        }
    }

    public BoundingBox getBoundingBox() {
        if (bounds == null)
            bounds = new BoundingBox();
        BoundingBox bounds = this.bounds;
        bounds.inf();
        for (ParticleEmitter emitter : this.emitters)
            bounds.ext(emitter.getBoundingBox());
        return bounds;
    }

    public void scaleEffect(float scaleFactor) {
        scaleEffect(scaleFactor, scaleFactor, scaleFactor);
    }

    public void scaleEffect(float scaleFactor, float motionScaleFactor) {
        scaleEffect(scaleFactor, scaleFactor, motionScaleFactor);
    }

    public void scaleEffect(float xSizeScaleFactor, float ySizeScaleFactor, float motionScaleFactor) {
        xSizeScale *= xSizeScaleFactor;
        ySizeScale *= ySizeScaleFactor;
        motionScale *= motionScaleFactor;
        for (ParticleEmitter particleEmitter : emitters) {
            particleEmitter.scaleSize(xSizeScaleFactor, ySizeScaleFactor);
            particleEmitter.scaleMotion(motionScaleFactor);
        }
    }

    public void setEmittersCleanUpBlendFunction(boolean cleanUpBlendFunction) {
        for (int i = 0, n = emitters.size; i < n; i++) {
            emitters.get(i).setCleansUpBlendFunction(cleanUpBlendFunction);
        }
    }
    static public class ParticleEffectLoader extends SynchronousAssetLoader<ParticleEffect, ParticleEffectParameter> {
        public ParticleEffectLoader(FileHandleResolver resolver) {
            super(resolver);
        }

        @Override
        public ParticleEffect load(AssetContainer am, String fileName, FileHandle file, ParticleEffectParameter param) {
            ParticleEffect effect = new ParticleEffect();
            if (param != null && param.atlasFile != null)
                effect.load(file, am.get(param.atlasFile, TextureAtlas.class), param.atlasPrefix);
            else if (param != null && param.imagesDir != null)
                effect.load(file, param.imagesDir);
            else
                effect.load(file, file.parent());
            return effect;
        }

        @Override
        public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, ParticleEffectParameter param) {
            Array<AssetDescriptor> deps = null;
            if (param != null && param.atlasFile != null) {
                deps = new Array();
                deps.add(new AssetDescriptor<TextureAtlas>(param.atlasFile, TextureAtlas.class));
            }
            return deps;
        }
    }
    public static class ParticleEffectParameter extends AssetLoaderParameters<ParticleEffect> {
        public String atlasFile;
        public String atlasPrefix;
        public FileHandle imagesDir;
    }
}
