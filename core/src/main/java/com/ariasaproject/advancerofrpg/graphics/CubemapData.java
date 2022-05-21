package com.ariasaproject.advancerofrpg.graphics;

public interface CubemapData {

    /**
     * @return whether the TextureData is prepared or not.
     */
    boolean isPrepared();

    /**
     * Prepares the TextureData for a call to {@link #consumeCubemapData()}. This
     * method can be called from a non OpenGL thread and should thus not interact
     * with OpenGL.
     */
    void prepare();

    /**
     * Uploads the pixel data for the 6 faces of the cube to the OpenGL ES texture.
     * The caller must bind an OpenGL ES texture. A call to {@link #prepare()} must
     * preceed a call to this method. Any internal data structures created in
     * {@link #prepare()} should be disposed of here.
     */
    void consumeCubemapData();

    /**
     * @return the width of the pixel data
     */
    int getWidth();

    /**
     * @return the height of the pixel data
     */
    int getHeight();

    /**
     * @return whether this implementation can cope with a EGL context loss.
     */
    boolean isManaged();

}
