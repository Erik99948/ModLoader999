package com.example.modloader.api;

import java.io.File;

public interface CustomAssetAPI {

    void registerSound(String assetId, String soundFilePath);

    void registerModel(String assetId, String modelFilePath);

    void registerTexture(String assetId, String textureFilePath);

    File getAssetFile(String assetId);
}