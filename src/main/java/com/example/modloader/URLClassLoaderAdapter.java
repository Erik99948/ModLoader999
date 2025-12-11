package com.example.modloader;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.net.URLClassLoader;

public class URLClassLoaderAdapter extends TypeAdapter<URLClassLoader> {
    @Override
    public void write(JsonWriter out, URLClassLoader value) throws IOException {

        out.nullValue();
    }

    @Override
    public URLClassLoader read(JsonReader in) throws IOException {

        in.nextNull();
        return null;
    }
}
