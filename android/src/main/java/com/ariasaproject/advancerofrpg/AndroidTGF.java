package com.ariasaproject.advancerofrpg;

import com.ariasaproject.advancerofrpg.graphics.TGF;
import com.ariasaproject.advancerofrpg.utils.ArrayMap;

public interface AndroidTGF extends TGF {
    final ArrayMap<Integer, Integer> datas = new ArrayMap<Integer, Integer>();

    //context lost on leave application
    public boolean limitGLESContext();
    //android will lost all resources when pause or forced close
    //resources include Texture (2d, TextureArray, Cubemap), FrameBuffer
    public void validateAll();

}
