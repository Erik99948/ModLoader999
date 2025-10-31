package com.example.modloader.api;

import java.util.List;

public interface AssetBundle {

    String getId();

    List<String> getAssetIds();

    void addAsset(String assetId);

    void removeAsset(String assetId);

    boolean containsAsset(String assetId);
}
