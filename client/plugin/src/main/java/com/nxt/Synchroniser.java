package com.nxt;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.nxt.config.Asset;
import com.nxt.config.AssetDifference;
import com.nxt.config.AssetMap;
import com.nxt.config.PackageManifest;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.artifacts.repositories.IvyArtifactRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

interface IChangedFileFilter {
    boolean hasLocalModifications(Asset asset);
}

/**
 * Created by alex on 09/12/2016.
 */
class Synchroniser {

    static Logger logger = LoggerFactory.getLogger("nxt");
    static IChangedFileFilter Filter(Project project) {
        return new IChangedFileFilter() {
            @Override
            public boolean hasLocalModifications(Asset asset) {
                File f = project.file(asset.getPath());
                if (!f.exists()) {
                    // If the file is missing we don't count it as modified,
                    // since we won't be overwriting work.
                    return false;
                }
                try {
                    String currentHash = Files.hash(f, Hashing.md5()).toString();
                    return !asset.getMd5().equals(currentHash);
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        };
    }

    static AssetDifference difference(AssetMap old, AssetMap latest, IChangedFileFilter filter) {
        MapDifference<String, Asset> diff =  Maps.difference(old, latest);
        Set<String> remove = Sets.newHashSet();

        Map<String, String> moved = Maps.newHashMap();

        // Don't remove any files with local changes.
        for (Map.Entry<String, Asset> entry : diff.entriesOnlyOnLeft().entrySet()) {
            if (!filter.hasLocalModifications(entry.getValue())) {
                remove.add(entry.getValue().getPath());
            }
        }

        AssetMap add = new AssetMap();
        for (Map.Entry<String, Asset> entry : diff.entriesOnlyOnRight().entrySet()) {
            add.put(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, MapDifference.ValueDifference<Asset>> entry : diff.entriesDiffering().entrySet()) {
            Asset original = entry.getValue().leftValue();
            Asset updated = entry.getValue().rightValue();
            boolean pathChanged = !original.getPath().equals(updated.getPath());
            boolean pkgModified = !original.getMd5().equals(updated.getMd5());

            // TODO - make this modal theirs/ours.
            if (pathChanged && pkgModified) {
                // Out with old in with new.
                if (!filter.hasLocalModifications(original)) {
                    remove.add(original.getPath());
                    add.put(entry.getKey(), updated);
                } else {
                    moved.put(original.getPath(), updated.getPath());
                }
            } else if (pathChanged && !pkgModified) {
                moved.put(original.getPath(), updated.getPath());
            } else if (!pathChanged && pkgModified) {
                if (!filter.hasLocalModifications(original)) {
                    add.put(entry.getKey(), updated);
                }
            }
        }

        return new AssetDifference(remove, add, moved);
    }

    static AssetMap buildAssetMap(Set<PackageManifest> manifests) {
        AssetMap result = new AssetMap();
        for (PackageManifest p : manifests) {
            result.putAll(p.getFiles());
        }
        return result;
    }

    static Set<PackageManifest> gatherManifests(Set<ResolvedDependency> deps) {
        Set<PackageManifest> manifests = Sets.newHashSet();

        for (ResolvedDependency dep : deps) {
            File manifest = null, unitypackage = null;
            for (ResolvedArtifact art : dep.getModuleArtifacts()) {
                if (art.getExtension().equals("manifest")) {
                    manifest = art.getFile();
                } else if (art.getExtension().equals("unitypackage")) {
                    unitypackage = art.getFile();
                }
            }

            if (null != manifest && null != unitypackage) {
                PackageManifest p = PackageManifest.load(manifest);
                p.unityPackage = unitypackage;
                manifests.add(p);
            } else {
                logger.error("Malformed package", manifest, unitypackage);
            }

        }

        return manifests;
    }

    static Set<ResolvedDependency> gatherDependencies(Project project, Set<String> repositories, Set<String> dependencies) {
        for (String r : repositories) {
            project.getRepositories().ivy(new Action<IvyArtifactRepository>() {
                @Override
                public void execute(IvyArtifactRepository ivyArtifactRepository) {
                    ivyArtifactRepository.setUrl(r);
                }
            });
        }

        Configuration conf = project.getConfigurations().create("nxtTmp" + count++);
        for (String id : dependencies) {
            conf.getDependencies().add(project.getDependencies().create(id));
        }

        return gatherDependencies(conf.getResolvedConfiguration().getFirstLevelModuleDependencies());
    }

    private static Set<ResolvedDependency> gatherDependencies(Set<ResolvedDependency> deps) {
        Set<ResolvedDependency> result = Sets.newHashSet(deps);
        for (ResolvedDependency dep : deps) {
            result.addAll(gatherDependencies(dep.getChildren()));
        }
        return result;
    }


    static int count = 1;

    public static File stateFile(Project project) {
        return project.file("nxt/nxt.json.state");
    }
}
