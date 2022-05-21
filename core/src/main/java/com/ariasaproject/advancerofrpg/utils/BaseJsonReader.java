package com.ariasaproject.advancerofrpg.utils;

import com.ariasaproject.advancerofrpg.Files.FileHandle;

import java.io.InputStream;

public interface BaseJsonReader {
    JsonValue parse(InputStream input);

    JsonValue parse(FileHandle file);
}
