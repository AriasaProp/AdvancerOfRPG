package com.ariasaproject.advancerofrpg.assets;

import com.ariasaproject.advancerofrpg.GraphFunc;
import com.ariasaproject.advancerofrpg.assets.loaders.AssetLoader;
import com.ariasaproject.advancerofrpg.assets.loaders.BitmapFontLoader;
import com.ariasaproject.advancerofrpg.assets.loaders.CubemapLoader;
import com.ariasaproject.advancerofrpg.assets.loaders.FileHandleResolver;
import com.ariasaproject.advancerofrpg.assets.loaders.I18NBundleLoader;
import com.ariasaproject.advancerofrpg.assets.loaders.MusicLoader;
import com.ariasaproject.advancerofrpg.assets.loaders.ParticleEffectLoader;
import com.ariasaproject.advancerofrpg.assets.loaders.PixmapLoader;
import com.ariasaproject.advancerofrpg.assets.loaders.ShaderProgramLoader;
import com.ariasaproject.advancerofrpg.assets.loaders.SkinLoader;
import com.ariasaproject.advancerofrpg.assets.loaders.SoundLoader;
import com.ariasaproject.advancerofrpg.assets.loaders.TextureAtlasLoader;
import com.ariasaproject.advancerofrpg.assets.loaders.TextureLoader;
import com.ariasaproject.advancerofrpg.audio.Music;
import com.ariasaproject.advancerofrpg.audio.Sound;
import com.ariasaproject.advancerofrpg.graphics.Cubemap;
import com.ariasaproject.advancerofrpg.graphics.Pixmap;
import com.ariasaproject.advancerofrpg.graphics.Texture;
import com.ariasaproject.advancerofrpg.graphics.g2d.BitmapFont;
import com.ariasaproject.advancerofrpg.graphics.g2d.ParticleEffect;
import com.ariasaproject.advancerofrpg.graphics.g2d.TextureAtlas;
import com.ariasaproject.advancerofrpg.graphics.g3d.Model;
import com.ariasaproject.advancerofrpg.graphics.g3d.loader.G3dModelLoader;
import com.ariasaproject.advancerofrpg.graphics.g3d.loader.ObjLoader;
import com.ariasaproject.advancerofrpg.graphics.glutils.ShaderProgram;
import com.ariasaproject.advancerofrpg.scenes2d.ui.Skin;
import com.ariasaproject.advancerofrpg.utils.Array;
import com.ariasaproject.advancerofrpg.utils.Disposable;
import com.ariasaproject.advancerofrpg.utils.I18NBundle;
import com.ariasaproject.advancerofrpg.utils.JsonReader;
import com.ariasaproject.advancerofrpg.utils.ObjectIntMap;
import com.ariasaproject.advancerofrpg.utils.ObjectMap;
import com.ariasaproject.advancerofrpg.utils.ObjectSet;
import com.ariasaproject.advancerofrpg.utils.UBJsonReader;
import com.ariasaproject.advancerofrpg.utils.reflect.ClassReflection;

import java.util.Stack;
import java.util.concurrent.ExecutorService;

public class AssetContainer implements Disposable {
    protected final ObjectMap<Class, ObjectMap<String, AssetLoader>> loaders = new ObjectMap<Class, ObjectMap<String, AssetLoader>>();
    final ObjectMap<Class, ObjectMap<String, RefCountedContainer>> assets = new ObjectMap<Class, ObjectMap<String, RefCountedContainer>>();
    final ObjectMap<String, Class> assetTypes = new ObjectMap<String, Class>();
    final ObjectMap<String, Array<String>> assetDependencies = new ObjectMap<String, Array<String>>();
    final ObjectSet<String> injected = new ObjectSet<String>();
    final Array<AssetDescriptor> loadQueue = new Array<AssetDescriptor>();
    final ExecutorService executor;

    final Stack<AssetLoadingTask> tasks = new Stack<AssetLoadingTask>();
    final FileHandleResolver resolver;
    int loaded = 0;
    int toLoad = 0;
    int peakTasks = 0;

    public AssetContainer(final ExecutorService exec, final FileHandleResolver resolver) {
        this.resolver = resolver;
        this.loaders.put(BitmapFont.class, new ObjectMap<String, AssetLoader>() {
            {
                put("", new BitmapFontLoader(resolver));
            }
        });
        this.loaders.put(Music.class, new ObjectMap<String, AssetLoader>() {
            {
                put("", new MusicLoader(resolver));
            }
        });
        this.loaders.put(Pixmap.class, new ObjectMap<String, AssetLoader>() {
            {
                put("", new PixmapLoader(resolver));
            }
        });
        this.loaders.put(Sound.class, new ObjectMap<String, AssetLoader>() {
            {
                put("", new SoundLoader(resolver));
            }
        });
        this.loaders.put(TextureAtlas.class, new ObjectMap<String, AssetLoader>() {
            {
                put("", new TextureAtlasLoader(resolver));
            }
        });
        this.loaders.put(Texture.class, new ObjectMap<String, AssetLoader>() {
            {
                put("", new TextureLoader(resolver));
            }
        });
        this.loaders.put(Skin.class, new ObjectMap<String, AssetLoader>() {
            {
                put("", new SkinLoader(resolver));
            }
        });
        this.loaders.put(ParticleEffect.class, new ObjectMap<String, AssetLoader>() {
            {
                put("", new ParticleEffectLoader(resolver));
            }
        });
        this.loaders.put(com.ariasaproject.advancerofrpg.graphics.g3d.particles.ParticleEffect.class, new ObjectMap<String, AssetLoader>() {
            {
                put("", new com.ariasaproject.advancerofrpg.graphics.g3d.particles.ParticleEffectLoader(resolver));
            }
        });
        this.loaders.put(I18NBundle.class, new ObjectMap<String, AssetLoader>() {
            {
                put("", new I18NBundleLoader(resolver));
            }
        });
        this.loaders.put(Model.class, new ObjectMap<String, AssetLoader>() {
            {
                put(".g3dj", new G3dModelLoader(new JsonReader(), resolver));
                put(".g3db", new G3dModelLoader(new UBJsonReader(), resolver));
                put(".obj", new ObjLoader(resolver));
            }
        });
        this.loaders.put(ShaderProgram.class, new ObjectMap<String, AssetLoader>() {
            {
                put("", new ShaderProgramLoader(resolver));
            }
        });
        this.loaders.put(Cubemap.class, new ObjectMap<String, AssetLoader>() {
            {
                put("", new CubemapLoader(resolver));
            }
        });
        executor = exec;
    }

    public synchronized <T> T get(String fileName) {
        Class<T> type = assetTypes.get(fileName);
        if (type == null)
            throw new RuntimeException("Asset not loaded: " + fileName);
        ObjectMap<String, RefCountedContainer> assetsByType = assets.get(type);
        if (assetsByType == null)
            throw new RuntimeException("Asset not loaded: " + fileName);
        RefCountedContainer assetContainer = assetsByType.get(fileName);
        if (assetContainer == null)
            throw new RuntimeException("Asset not loaded: " + fileName);
        T asset = type.cast(assetContainer.getObject());
        if (asset == null)
            throw new RuntimeException("Asset not loaded: " + fileName);
        return asset;
    }

    public synchronized <T> T get(String fileName, Class<T> type) {
        ObjectMap<String, RefCountedContainer> assetsByType = assets.get(type);
        if (assetsByType == null)
            throw new RuntimeException("Asset not loaded: " + fileName);
        RefCountedContainer assetContainer = assetsByType.get(fileName);
        if (assetContainer == null)
            throw new RuntimeException("Asset not loaded: " + fileName);
        T asset = type.cast(assetContainer.getObject());
        if (asset == null)
            throw new RuntimeException("Asset not loaded: " + fileName);
        return asset;
    }

    public synchronized <T extends Disposable> Array<T> getAll(Class<T> type, Array<T> out) {
        ObjectMap<String, RefCountedContainer> assetsByType = assets.get(type);
        if (assetsByType != null) {
            for (ObjectMap.Entry<String, RefCountedContainer> asset : assetsByType.entries()) {
                out.add((T) asset.value.getObject());
            }
        }
        return out;
    }

    public synchronized <T extends Disposable> T get(AssetDescriptor<T> assetDescriptor) {
        return get(assetDescriptor.fullPath, assetDescriptor.type);
    }

    public synchronized boolean contains(String fileName) {
        if (tasks.size() > 0 && tasks.firstElement().assetDesc.fullPath.equals(fileName))
            return true;
        for (int i = 0; i < loadQueue.size; i++)
            if (loadQueue.get(i).fullPath.equals(fileName))
                return true;
        return isLoaded(fileName);
    }

    public synchronized boolean contains(String fileName, Class type) {
        if (tasks.size() > 0) {
            AssetDescriptor assetDesc = tasks.firstElement().assetDesc;
            if (assetDesc.type == type && assetDesc.fullPath.equals(fileName))
                return true;
        }
        for (int i = 0; i < loadQueue.size; i++) {
            AssetDescriptor assetDesc = loadQueue.get(i);
            if (assetDesc.type == type && assetDesc.fullPath.equals(fileName))
                return true;
        }
        return isLoaded(fileName, type);
    }

    public synchronized void unload(String fileName) {
        if (tasks.size() > 0) {
            AssetLoadingTask currAsset = tasks.firstElement();
            if (currAsset.assetDesc.fullPath.equals(fileName)) {
                currAsset.cancel = true;
                return;
            }
        }
        // check if it's in the queue
        int foundIndex = -1;
        for (int i = 0; i < loadQueue.size; i++) {
            if (loadQueue.get(i).fullPath.equals(fileName)) {
                foundIndex = i;
                break;
            }
        }
        if (foundIndex != -1) {
            toLoad--;
            loadQueue.removeIndex(foundIndex);
            return;
        }
        // get the asset and its type
        Class type = assetTypes.get(fileName);
        if (type == null)
            throw new RuntimeException("Asset not loaded: " + fileName);
        RefCountedContainer assetRef = assets.get(type).get(fileName);
        // if it is reference counted, decrement ref count and check if we can really
        // get rid of it.
        assetRef.decRefCount();
        if (assetRef.getRefCount() <= 0) {
            // if it is disposable dispose it
            assetRef.getObject().dispose();
            // remove the asset from the manager.
            assetTypes.remove(fileName);
            assets.get(type).remove(fileName);
        }
        // remove any dependencies (or just decrement their ref count).
        Array<String> dependencies = assetDependencies.get(fileName);
        if (dependencies != null) {
            for (String dependency : dependencies) {
                if (isLoaded(dependency))
                    unload(dependency);
            }
        }
        // remove dependencies if ref count < 0
        if (assetRef.getRefCount() <= 0) {
            assetDependencies.remove(fileName);
        }
    }

    /**
     * @param asset the asset
     * @return whether the asset is contained in this manager
     */
    public synchronized <T> boolean containsAsset(T asset) {
        ObjectMap<String, RefCountedContainer> assetsByType = assets.get(asset.getClass());
        if (assetsByType == null)
            return false;
        for (String fileName : assetsByType.keys()) {
            T otherAsset = (T) assetsByType.get(fileName).getObject();
            if (otherAsset == asset || asset.equals(otherAsset))
                return true;
        }
        return false;
    }

    public synchronized <T> String getAssetFileName(T asset) {
        for (Class assetType : assets.keys()) {
            ObjectMap<String, RefCountedContainer> assetsByType = assets.get(assetType);
            for (String fileName : assetsByType.keys()) {
                T otherAsset = (T) assetsByType.get(fileName).getObject();
                if (otherAsset == asset || asset.equals(otherAsset))
                    return fileName;
            }
        }
        return null;
    }

    public synchronized boolean isLoaded(AssetDescriptor assetDesc) {
        return isLoaded(assetDesc.fullPath);
    }

    public synchronized boolean isLoaded(String fileName) {
        if (fileName == null)
            return false;
        return assetTypes.containsKey(fileName);
    }

    public synchronized boolean isLoaded(String fileName, Class type) {
        ObjectMap<String, RefCountedContainer> assetsByType = assets.get(type);
        if (assetsByType == null)
            return false;
        RefCountedContainer assetContainer = assetsByType.get(fileName);
        if (assetContainer == null)
            return false;
        return type.cast(assetContainer.getObject()) != null;
    }

    public <T> AssetLoader getLoader(final AssetDescriptor assetDesc) {
        final ObjectMap<String, AssetLoader> loaders = this.loaders.get(assetDesc.type);
        if (loaders == null || loaders.size < 1)
            return null;
        if (assetDesc.fullPath == null)
            return loaders.get("");
        AssetLoader result = null;
        int l = -1;
        for (ObjectMap.Entry<String, AssetLoader> entry : loaders.entries()) {
            if (entry.key.length() > l && assetDesc.fullPath.endsWith(entry.key)) {
                result = entry.value;
                l = entry.key.length();
            }
        }
        return result;
    }

    public synchronized void load(AssetDescriptor<?>... descs) {
        for (AssetDescriptor<?> desc : descs) {
            AssetLoader loader = getLoader(desc);
            if (loader == null)
                throw new RuntimeException("No loader for type: " + ClassReflection.getSimpleName(desc.type));
            // reset stats
            if (loadQueue.size == 0) {
                loaded = 0;
                toLoad = 0;
                peakTasks = 0;
            }
            for (int i = 0; i < loadQueue.size; i++) {
                final AssetDescriptor pre = loadQueue.get(i);
                if (pre.fullPath.equals(desc.fullPath) && !pre.type.equals(desc.type))
                    throw new RuntimeException("Asset with name '" + desc.fullPath + "' already in preload queue, but has different type (expected: " + ClassReflection.getSimpleName(desc.type) + ", found: " + ClassReflection.getSimpleName(pre.type) + ")");
            }
            for (int i = 0; i < tasks.size(); i++) {
                final AssetDescriptor tas = tasks.get(i).assetDesc;
                if (tas.fullPath.equals(desc.fullPath) && !tas.type.equals(desc.type))
                    throw new RuntimeException("Asset with name '" + desc.fullPath + "' already in task list, but has different type (expected: " + ClassReflection.getSimpleName(desc.type) + ", found: " + ClassReflection.getSimpleName(tas.type) + ")");
            }
            // check loaded assets
            Class otherType = assetTypes.get(desc.fullPath);
            if (otherType != null && !otherType.equals(desc.type))
                throw new RuntimeException("Asset with name '" + desc.fullPath + "' already loaded, but has different type (expected: " + ClassReflection.getSimpleName(desc.type) + ", found: " + ClassReflection.getSimpleName(otherType) + ")");
            toLoad++;
            loadQueue.add(desc);
        }
    }

    public synchronized boolean update() {
        try {
            if (tasks.size() == 0) {
                // loop until we have a new task ready to be processed
                while (loadQueue.size != 0 && tasks.size() == 0) {
                    AssetDescriptor assetDesc = loadQueue.removeIndex(0);
                    // if the asset not meant to be reloaded and is already loaded, increase its
                    // reference count
                    if (isLoaded(assetDesc.fullPath)) {
                        Class type = assetTypes.get(assetDesc.fullPath);
                        RefCountedContainer assetRef = assets.get(type).get(assetDesc.fullPath);
                        assetRef.incRefCount();
                        incrementRefCountedDependencies(assetDesc.fullPath);
                        if (assetDesc.params != null && assetDesc.params.loadedCallback != null) {
                            assetDesc.params.loadedCallback.finishedLoading(this, assetDesc.fullPath, assetDesc.type);
                        }
                        loaded++;
                    } else {
                        // else add a new task for the asset.
                        // addtask to executor
                        AssetLoader loader = getLoader(assetDesc);
                        if (loader == null)
                            throw new RuntimeException("No loader for type: " + ClassReflection.getSimpleName(assetDesc.type));
                        tasks.push(new AssetLoadingTask(this, assetDesc, loader, executor));
                        peakTasks++;
                    }
                }
                // have we not found a task? We are done!
                if (tasks.size() == 0)
                    return true;
            }
            AssetLoadingTask task = tasks.peek();
            boolean complete = true;
            try {
                complete = task.cancel || task.update();
            } catch (RuntimeException ex) {
                task.cancel = true;
                throw new RuntimeException("task error " + ex);
            } finally {
                // if the task has been cancelled or has finished loading
                if (!complete)
                    return false;
            }
            // increase the number of loaded assets and pop the task from the stack
            if (tasks.size() == 1) {
                loaded++;
                peakTasks = 0;
            }
            tasks.pop();
            if (task.cancel)
                return true;

            // add the asset to the filename lookup
            assetTypes.put(task.assetDesc.fullPath, task.assetDesc.type);
            // add the asset to the type lookup
            ObjectMap<String, RefCountedContainer> typeToAssets = assets.get(task.assetDesc.type);
            if (typeToAssets == null) {
                typeToAssets = new ObjectMap<String, RefCountedContainer>();
                assets.put(task.assetDesc.type, typeToAssets);
            }
            typeToAssets.put(task.assetDesc.fullPath, new RefCountedContainer(task.getAsset()));

            // otherwise, if a listener was found in the parameter invoke it
            if (task.assetDesc.params != null && task.assetDesc.params.loadedCallback != null) {
                task.assetDesc.params.loadedCallback.finishedLoading(this, task.assetDesc.fullPath, task.assetDesc.type);
            }
            return loadQueue.size == 0 && tasks.size() == 0;
        } catch (Throwable t) {
            // Handles a runtime/loading error
            if (tasks.isEmpty())
                throw new RuntimeException(t);
            // pop the faulty task from the stack
            AssetLoadingTask task = tasks.pop();
            // AssetDescriptor assetDesc = task.assetDesc;
            // remove all dependencies
            if (task.dependenciesLoaded && task.dependencies != null) {
                for (AssetDescriptor desc : task.dependencies) {
                    unload(desc.fullPath);
                }
            }
            tasks.clear();
            GraphFunc.app.log(ClassReflection.getSimpleName(getClass()), "task failed : " + t);
            return loadQueue.size == 0;
        }
    }

    public synchronized boolean isFinished() {
        return loadQueue.size == 0 && tasks.size() == 0;
    }

    /**
     * Blocks until all assets are loaded.
     */
    public void finishLoading() {
        while (!update())
            Thread.yield();
    }

    public <T> T finishLoadingAsset(AssetDescriptor assetDesc) {
        return finishLoadingAsset(assetDesc.fullPath);
    }

    public <T> T finishLoadingAsset(String fileName) {
        while (true) {
            synchronized (this) {
                Class<T> type = assetTypes.get(fileName);
                if (type != null) {
                    ObjectMap<String, RefCountedContainer> assetsByType = assets.get(type);
                    if (assetsByType != null) {
                        RefCountedContainer assetContainer = assetsByType.get(fileName);
                        if (assetContainer != null) {
                            T asset = type.cast(assetContainer.getObject());
                            if (asset != null) {
                                return asset;
                            }
                        }
                    }
                }
                update();
            }
            Thread.yield();
        }
    }

    synchronized void injectDependencies(String parentAssetFilename, Array<AssetDescriptor> dependendAssetDescs) {
        ObjectSet<String> injected = this.injected;
        for (AssetDescriptor dependendAssetDesc : dependendAssetDescs) {
            if (injected.contains(dependendAssetDesc.fullPath))
                continue; // Ignore subsequent dependencies if there are duplicates.
            injected.add(dependendAssetDesc.fullPath);
            Array<String> dependencies = assetDependencies.get(parentAssetFilename);
            if (dependencies == null) {
                dependencies = new Array<String>();
                assetDependencies.put(parentAssetFilename, dependencies);
            }
            dependencies.add(dependendAssetDesc.fullPath);
            // if the asset is already loaded, increase its reference count.
            if (isLoaded(dependendAssetDesc.fullPath)) {
                Class type = assetTypes.get(dependendAssetDesc.fullPath);
                RefCountedContainer assetRef = assets.get(type).get(dependendAssetDesc.fullPath);
                assetRef.incRefCount();
                incrementRefCountedDependencies(dependendAssetDesc.fullPath);
            } else {
                // add task to executor
                AssetLoader loader = getLoader(dependendAssetDesc);
                if (loader == null)
                    throw new RuntimeException("No loader for type: " + ClassReflection.getSimpleName(dependendAssetDesc.type));
                tasks.push(new AssetLoadingTask(this, dependendAssetDesc, loader, executor));
                peakTasks++;
            }
        }
        injected.clear(32);
    }

    private void incrementRefCountedDependencies(String parent) {
        Array<String> dependencies = assetDependencies.get(parent);
        if (dependencies == null)
            return;
        for (String dependency : dependencies) {
            Class type = assetTypes.get(dependency);
            RefCountedContainer assetRef = assets.get(type).get(dependency);
            assetRef.incRefCount();
            incrementRefCountedDependencies(dependency);
        }
    }

    /**
     * @return the number of loaded assets
     */
    public synchronized int getLoadedAssets() {
        return assetTypes.size;
    }

    /**
     * @return the number of currently queued assets
     */
    public synchronized int getQueuedAssets() {
        return loadQueue.size + tasks.size();
    }

    /**
     * @return the progress in percent of completion.
     */
    public synchronized float getProgress() {
        if (toLoad == 0)
            return 1;
        float fractionalLoaded = loaded;
        if (peakTasks > 0) {
            fractionalLoaded += ((peakTasks - tasks.size()) / (float) peakTasks);
        }
        return Math.min(1, fractionalLoaded / toLoad);
    }

    @Override
    public synchronized void dispose() {
        loadQueue.clear();
        while (!update())
            ;
        ObjectIntMap<String> dependencyCount = new ObjectIntMap<String>();
        while (assetTypes.size > 0) {
            // for each asset, figure out how often it was referenced
            dependencyCount.clear();
            Array<String> assets = assetTypes.keys().toArray();
            for (String asset : assets) {
                dependencyCount.put(asset, 0);
            }
            for (String asset : assets) {
                Array<String> dependencies = assetDependencies.get(asset);
                if (dependencies == null)
                    continue;
                for (String dependency : dependencies) {
                    int count = dependencyCount.get(dependency, 0);
                    count++;
                    dependencyCount.put(dependency, count);
                }
            }
            // only dispose of assets that are root assets (not referenced)
            for (String asset : assets) {
                if (dependencyCount.get(asset, 0) == 0) {
                    unload(asset);
                }
            }
        }
        this.assets.clear();
        this.assetTypes.clear();
        this.assetDependencies.clear();
        this.loaded = 0;
        this.toLoad = 0;
        this.peakTasks = 0;
        this.loadQueue.clear();
        this.tasks.clear();
    }

    public synchronized int getReferenceCount(String fileName) {
        Class type = assetTypes.get(fileName);
        if (type == null)
            throw new RuntimeException("Asset not loaded: " + fileName);
        return assets.get(type).get(fileName).getRefCount();
    }

    public synchronized void setReferenceCount(String fileName, int refCount) {
        Class type = assetTypes.get(fileName);
        if (type == null)
            throw new RuntimeException("Asset not loaded: " + fileName);
        assets.get(type).get(fileName).setRefCount(refCount);
    }

    public synchronized Array<String> getDependencies(String fileName) {
        return assetDependencies.get(fileName);
    }

    public synchronized Class getAssetType(String fileName) {
        return assetTypes.get(fileName);
    }

    public static class RefCountedContainer {
        Disposable object;
        int refCount = 1;

        protected RefCountedContainer(Disposable object) {
            if (object == null)
                throw new IllegalArgumentException("Object must not be null");
            this.object = object;
        }

        public void incRefCount() {
            refCount++;
        }

        public void decRefCount() {
            refCount--;
        }

        public int getRefCount() {
            return refCount;
        }

        public void setRefCount(int refCount) {
            this.refCount = refCount;
        }

        public Disposable getObject() {
            return object;
        }

        public void setObject(Disposable asset) {
            this.object = asset;
        }
    }
}
