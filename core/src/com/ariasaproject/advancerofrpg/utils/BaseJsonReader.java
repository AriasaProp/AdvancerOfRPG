package com.ariasaproject.advancerofrpg.utils;

import java.io.InputStream;

import com.ariasaproject.advancerofrpg.files.FileHandle;

public interface BaseJsonReader {
	JsonValue parse(InputStream input);

	JsonValue parse(FileHandle file);
}
