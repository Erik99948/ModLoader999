package com.example.modloader.api;

import java.io.File;

public interface CustomAssetAPI {

    void registerSound(String assetId, String soundFilePath);
    void registerSound(String assetId, String soundFilePath, int priority);

    void registerModel(String assetId, String modelFilePath);
    void registerModel(String assetId, String modelFilePath, int priority);

    void registerTexture(String assetId, String textureFilePath);
    void registerTexture(String assetId, String textureFilePath, int priority);

    File getAssetFile(String assetId);

    String getAssetUrl(String assetId);

    AssetBundle createAssetBundle(String bundleId);

    boolean registerAssetBundle(AssetBundle bundle);

    AssetBundle getAssetBundle(String bundleId);
}
