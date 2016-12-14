package com.nxt.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapDifference;

import java.util.Map;

/**
 * Created by alex on 14/12/2016.
 */
public class AssetDifference {
    private Map<String, Asset> remove;
    private Map<String, Asset> add;
    private Map<String, MapDifference.ValueDifference<Asset>> changed;


    public AssetDifference(Map<String, Asset> remove, Map<String, Asset> add, Map<String, MapDifference.ValueDifference<Asset>> changed) {
        this.remove = remove;
        this.add = add;
        this.changed = changed;
    }

    public ImmutableMap<String, Asset> getRemove() {
        return ImmutableMap.copyOf(remove);
    }

    public ImmutableMap<String, Asset> getAdd() {
        return ImmutableMap.copyOf(add);
    }

    public ImmutableMap<String, MapDifference.ValueDifference<Asset>> getChanged() {
        return ImmutableMap.copyOf(changed);
    }
}
