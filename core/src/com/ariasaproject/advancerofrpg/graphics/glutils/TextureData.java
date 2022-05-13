package com.ariasaproject.advancerofrpg.graphics.glutils;
import com.ariasaproject.advancerofrpg.graphics.Pixmap;
import com.ariasaproject.advancerofrpg.graphics.Pixmap.Format;

public interface TextureData {
	TextureDataType getType();

	boolean isPrepared();

	void prepare();

	Pixmap consumePixmap();

	boolean disposePixmap();

	void consumeCustomData(int target);

	int getWidth();

	int getHeight();

	Format getFormat();

	boolean useMipMaps();

	boolean isManaged();

	enum TextureDataType {
		Pixmap, Custom
		}
}
