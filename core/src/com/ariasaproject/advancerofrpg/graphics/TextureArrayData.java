package com.ariasaproject.advancerofrpg.graphics;

import com.ariasaproject.advancerofrpg.files.FileHandle;
import com.ariasaproject.advancerofrpg.graphics.glutils.FileTextureArrayData;
import com.ariasaproject.advancerofrpg.graphics.Pixmap.Format;


public interface TextureArrayData {
	boolean isPrepared();
	void prepare();
	void consumeTextureArrayData();
	int getWidth();
	int getHeight();
	int getDepth();
	boolean isManaged();
	Format getFormat()
;	class Factory {

		public static TextureArrayData loadFromFiles(Format format, boolean useMipMaps, FileHandle... files) {
			return new FileTextureArrayData(format, useMipMaps, files);
		}

	}

}
