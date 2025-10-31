package com.example.modloader.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AssetBundleImpl implements AssetBundle {

    private final String id;
    private final List<String> assetIds;

    public AssetBundleImpl(String id) {
        this.id = id;
        this.assetIds = new ArrayList<>();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public List<String> getAssetIds() {
        return Collections.unmodifiableList(assetIds);
    }

    @Override
    public void addAsset(String assetId) {
        if (!assetIds.contains(assetId)) {
            assetIds.add(assetId);
        }
    }

    @Override
    public void removeAsset(String assetId) {
        assetIds.remove(assetId);
    }

    @Override
    public boolean containsAsset(String assetId) {
        return assetIds.contains(assetId);
    }
}
