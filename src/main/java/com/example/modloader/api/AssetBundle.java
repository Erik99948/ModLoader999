package com.example.modloader.api;

import java.util.List;

/**
 * Represents a bundle of related assets.
 */
public interface AssetBundle {
    String getId();
    List<String> getAssetIds();
    void addAsset(String assetId);
    void removeAsset(String assetId);
    boolean containsAsset(String assetId);
}
