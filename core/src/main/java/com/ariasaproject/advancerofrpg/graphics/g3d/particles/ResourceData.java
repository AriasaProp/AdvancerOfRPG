package com.ariasaproject.advancerofrpg.graphics.g3d.particles;

import com.ariasaproject.advancerofrpg.assets.AssetContainer;
import com.ariasaproject.advancerofrpg.assets.AssetDescriptor;
import com.ariasaproject.advancerofrpg.utils.Array;
import com.ariasaproject.advancerofrpg.utils.Disposable;
import com.ariasaproject.advancerofrpg.utils.IntArray;
import com.ariasaproject.advancerofrpg.utils.Json;
import com.ariasaproject.advancerofrpg.utils.JsonValue;
import com.ariasaproject.advancerofrpg.utils.ObjectMap;
import com.ariasaproject.advancerofrpg.utils.ObjectMap.Entry;
import com.ariasaproject.advancerofrpg.utils.reflect.ClassReflection;
import com.ariasaproject.advancerofrpg.utils.reflect.ReflectionException;

public class ResourceData<T extends Disposable> implements Json.Serializable {
    public T resource;
    Array<AssetData> sharedAssets;
    private ObjectMap<String, SaveData> uniqueData;
    private Array<SaveData> data;
    private int currentLoadIndex;

    public ResourceData() {
        uniqueData = new ObjectMap<String, SaveData>();
        data = new Array<SaveData>(true, 3, SaveData.class);
        sharedAssets = new Array<AssetData>();
        currentLoadIndex = 0;
    }

    public ResourceData(T resource) {
        this();
        this.resource = resource;
    }

    <K> int getAssetData(String filename, Class<K> type) {
        int i = 0;
        for (AssetData data : sharedAssets) {
            if (data.filename.equals(filename) && data.type.equals(type)) {
                return i;
            }
            ++i;
        }
        return -1;
    }

    public Array<AssetDescriptor> getAssetDescriptors() {
        Array<AssetDescriptor> descriptors = new Array<AssetDescriptor>();
        for (AssetData data : sharedAssets) {
            descriptors.add(new AssetDescriptor<T>(data.filename, data.type));
        }
        return descriptors;
    }

    public Array<AssetData> getAssets() {
        return sharedAssets;
    }

    /**
     * Creates and adds a new SaveData object to the save data list
     */
    public SaveData createSaveData() {
        SaveData saveData = new SaveData(this);
        data.add(saveData);
        return saveData;
    }

    /**
     * Creates and adds a new and unique SaveData object to the save data map
     */
    public SaveData createSaveData(String key) {
        SaveData saveData = new SaveData(this);
        if (uniqueData.containsKey(key))
            throw new RuntimeException("Key already used, data must be unique, use a different key");
        uniqueData.put(key, saveData);
        return saveData;
    }

    /**
     * @return the next save data in the list
     */
    public SaveData getSaveData() {
        return data.get(currentLoadIndex++);
    }

    /**
     * @return the unique save data in the map
     */
    public SaveData getSaveData(String key) {
        return uniqueData.get(key);
    }

    @Override
    public void write(Json json) {
        json.writeValue("unique", uniqueData, ObjectMap.class);
        json.writeValue("data", data, Array.class, SaveData.class);
        json.writeValue("assets", sharedAssets.toArray(AssetData.class), AssetData[].class);
        json.writeValue("resource", resource, null);
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        uniqueData = json.readValue("unique", ObjectMap.class, jsonData);
        for (Entry<String, SaveData> entry : uniqueData.entries()) {
            entry.value.resources = this;
        }
        data = json.readValue("data", Array.class, SaveData.class, jsonData);
        for (SaveData saveData : data) {
            saveData.resources = this;
        }
        sharedAssets.addAll(json.readValue("assets", Array.class, AssetData.class, jsonData));
        resource = json.readValue("resource", null, jsonData);
    }

    /**
     * This interface must be implemented by any class requiring additional assets
     * to be loaded/saved
     */
    public interface Configurable<T extends Disposable> {
        void save(AssetContainer manager, ResourceData<T> resources);

        void load(AssetContainer manager, ResourceData<T> resources);
    }

    /**
     * Contains all the saved data. {@link #data} is a map which link an asset name
     * to its instance. {@link #assets} is an array of indices addressing a given
     * {@link com.ariasaproject.advancerofrpg.graphics.g3d.particles.ResourceData.AssetData}
     * in the {@link ResourceData}
     */
    public static class SaveData implements Json.Serializable {
        protected ResourceData resources;
        ObjectMap<String, Object> data;
        IntArray assets;
        private int loadIndex;

        public SaveData() {
            data = new ObjectMap<String, Object>();
            assets = new IntArray();
            loadIndex = 0;
        }

        public SaveData(ResourceData resources) {
            data = new ObjectMap<String, Object>();
            assets = new IntArray();
            loadIndex = 0;
            this.resources = resources;
        }

        public <K> void saveAsset(String filename, Class<K> type) {
            int i = resources.getAssetData(filename, type);
            if (i == -1) {
                resources.sharedAssets.add(new AssetData(filename, type));
                i = resources.sharedAssets.size - 1;
            }
            assets.add(i);
        }

        public void save(String key, Object value) {
            data.put(key, value);
        }

        public AssetDescriptor loadAsset() {
            if (loadIndex == assets.size)
                return null;
            AssetData data = (AssetData) resources.sharedAssets.get(assets.get(loadIndex++));
            return new AssetDescriptor(data.filename, data.type);
        }

        public <K> K load(String key) {
            return (K) data.get(key);
        }

        @Override
        public void write(Json json) {
            json.writeValue("data", data, ObjectMap.class);
            json.writeValue("indices", assets.toArray(), int[].class);
        }

        @Override
        public void read(Json json, JsonValue jsonData) {
            data = json.readValue("data", ObjectMap.class, jsonData);
            assets.addAll(json.readValue("indices", int[].class, jsonData));
        }
    }

    /**
     * This class contains all the information related to a given asset
     */
    public static class AssetData<T> implements Json.Serializable {
        public String filename;
        public Class<T> type;

        public AssetData() {
        }

        public AssetData(String filename, Class<T> type) {
            this.filename = filename;
            this.type = type;
        }

        @Override
        public void write(Json json) {
            json.writeValue("filename", filename);
            json.writeValue("type", type.getName());
        }

        @Override
        public void read(Json json, JsonValue jsonData) {
            filename = json.readValue("filename", String.class, jsonData);
            String className = json.readValue("type", String.class, jsonData);
            try {
                type = ClassReflection.forName(className);
            } catch (ReflectionException e) {
                throw new RuntimeException("Class not found: " + className, e);
            }
        }
    }

}
