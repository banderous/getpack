package com.nxt.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MapDifference;

import java.util.Map;
import java.util.Set;

/**
 * Created by alex on 14/12/2016.
 */
public class AssetDifference {
    private Set<String> remove;
    private Map<String, Asset> add;
    private Map<String, String> moved;

    public AssetDifference(Set<String> remove, Map<String, Asset> add, Map<String, String> moved) {
        this.remove = remove;
        this.add = add;
        this.moved = moved;
    }

    public ImmutableSet<String> getRemove() {
        return ImmutableSet.copyOf(remove);
    }

    public ImmutableMap<String, Asset> getAdd() {
        return ImmutableMap.copyOf(add);
    }

    public ImmutableMap<String, String> getMoved() {
        return ImmutableMap.copyOf(moved);
    }
}
