package com.nxt;

import com.google.common.base.Splitter;
import com.google.common.collect.*;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.nxt.config.*;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.artifacts.repositories.IvyArtifactRepository;
import org.gradle.api.file.FileTree;
import org.gradle.api.resources.ReadableResource;
import org.gradle.api.tasks.util.PatternSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

/**
 * Created by alex on 09/12/2016.
 */
class Synchroniser {

  static Logger logger = LoggerFactory.getLogger("nxt");
  static int count = 1;

  public static InstallDetails sync(Project project) {
    Set<PackageManifest> currentManifests =
        gatherManifests(gatherDependencies(project, ProjectConfig.loadShadow(project)));
    Set<PackageManifest> targetManifests =
        gatherManifests(gatherDependencies(project, ProjectConfig.load(project)));

    Log.L.info("Current packages: {}, target packages: {}", currentManifests.size(),
        targetManifests.size());
    AssetMap current = buildAssetMap(currentManifests);
    AssetMap target = buildAssetMap(targetManifests);

    AssetDifference difference = Synchroniser.difference(current, target,
        Synchroniser.filter(project));
    Log.L.info("Files added: {}", difference.getAdd().size());
    Log.L.info("Files removed: {}", difference.getRemove().size());
    Log.L.info("Files moved: {}", difference.getMoved().size());

    remove(project, difference.getRemove());
    cleanOldPackageDirs(project.getProjectDir(), difference.getRemove());
    move(project, difference.getMoved());

    return install(project, difference.getAdd(), targetManifests);
  }

  public static InstallDetails install(Project project, ImmutableMap<String, Asset> add,
                                 Set<PackageManifest> targetManifests) {
    Map<String, File> filesByGUID = buildGUIDToUnitypackageMap(targetManifests);
    HashMultimap<File, String> guidsByFile = HashMultimap.create();
    for (Map.Entry<String, Asset> entry : add.entrySet()) {
      Log.L.info("add " + entry.getKey() + " " + entry.getValue().getPath());
      File f = filesByGUID.get(entry.getKey());
      Log.L.info("Putting " + f.getPath() + " " + entry.getKey());
      guidsByFile.put(f, entry.getKey());
    }

    Set<File> completePackages = Sets.newHashSet();
    HashMultimap<File, String> partialPackages = HashMultimap.create();
    for (File file : guidsByFile.keys()) {
      PackageManifest manifest = findManifest(file, targetManifests);
      if (manifest.getFiles().size() == guidsByFile.get(file).size()) {
        Log.L.info("Install all {}", file.getName());
        completePackages.add(file);
      } else {
        Log.L.info("Install partially {}", file.getName());
        partialPackages.putAll(file, guidsByFile.values());
      }
    }

    Log.L.info("Installing {} complete packages, {} partial", completePackages.size(),
        partialPackages.size());
    return new InstallDetails(ImmutableSet.copyOf(completePackages),
        mergeArchives(project, partialPackages));
  }

  private static PackageManifest findManifest(File by, Set<PackageManifest> manifests) {
    for (PackageManifest manifest : manifests) {
      if (manifest.getUnitypackage().equals(by)) {
        return manifest;
      }
    }

    throw new IllegalArgumentException("Cannot find manifest for " + by);
  }

  public static FileTree mergeArchives(Project project, HashMultimap<File, String> filesAndPaths) {
    FileTree result = null;
    for (File file : filesAndPaths.keys()) {
      ReadableResource resource = project.getResources().gzip(file);

      PatternSet pattern = new PatternSet();

      for (String s : filesAndPaths.get(file)) {
        pattern.include(String.format("**/%s/**", s));
      }
      FileTree tree = project.tarTree(resource).matching(pattern);
      if (null == result) {
        result = tree;
      } else {
        result = result.plus(tree);
      }
    }

    return result;
  }

  public static void move(Project project, ImmutableMap<String, String> moved) {
    for (Map.Entry<String, String> entry : moved.entrySet()) {
      File from = project.file(entry.getKey());
      File to = project.file(entry.getValue());
      from.renameTo(to);
    }
  }

  public static void cleanOldPackageDirs(File folder, ImmutableSet<String> pathsRemoved) {
    Log.L.debug("Cleaning old directories: {}", pathsRemoved);
    for (String root : getPackageRoots(pathsRemoved)) {
      removeEmptyFoldersRecursive(new File(folder, root));
    }
  }

  public static void removeEmptyFoldersRecursive(File folder) {
    for (File file : folder.listFiles()) {
      if (file.isDirectory()) {
        removeEmptyFoldersRecursive(file);
      }
    }

    if (folder.isDirectory() && folder.listFiles().length == 0) {
      Log.L.info("Removing empty folder {}", folder);
      folder.delete();
      new File(folder.getParent(), folder.getName() + ".meta").delete();
    }
  }

  public static ImmutableSet<String> getPackageRoots(ImmutableSet<String> packagePaths) {
    Set<String> roots = Sets.newHashSet();
    for (String path : packagePaths) {
      String[] splits = path.split("/");
      if (splits.length >= 2) {
        String potentialRoot = splits[0] + "/" + splits[1];
        roots.add(potentialRoot);
      }
    }

    return ImmutableSet.copyOf(roots);
  }

  private static void remove(Project project, ImmutableSet<String> remove) {
    for (String s : remove) {
      project.file(s).delete();
      project.file(s + ".meta").delete();
    }
  }

  public static IChangedFileFilter filter(final Project project) {
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
    MapDifference<String, Asset> diff = Maps.difference(old, latest);
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

    for (Map.Entry<String, MapDifference.ValueDifference<Asset>> entry :
        diff.entriesDiffering().entrySet()) {
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
      File manifest = null;
      File unitypackage = null;
      for (ResolvedArtifact art : dep.getModuleArtifacts()) {
        if (art.getExtension().equals("manifest")) {
          manifest = art.getFile();
        } else if (art.getExtension().equals("unitypackage")) {
          unitypackage = art.getFile();
        }
      }

      if (null != manifest && null != unitypackage) {
        PackageManifest p = PackageManifest.load(manifest);
        p.setUnityPackage(unitypackage);
        manifests.add(p);
      } else {
        logger.error("Malformed package", manifest, unitypackage);
      }

    }

    return manifests;
  }

  public static Map<String, File> buildGUIDToUnitypackageMap(Set<PackageManifest> manifests) {
    Map<String, File> result = Maps.newHashMap();
    for (PackageManifest manifest : manifests) {
      for (Map.Entry<String, Asset> entry : manifest.getFiles().entrySet()) {
        if (!result.containsKey(entry.getKey())) {
          result.put(entry.getKey(), manifest.getUnitypackage());
        }
      }
    }

    return result;
  }

  static Set<ResolvedDependency> gatherDependencies(Project project, ProjectConfig config) {
    return gatherDependencies(project, config.getRepositories(), config.getDependencies());
  }

  static Set<ResolvedDependency> gatherDependencies(Project project, Set<String> repositories,
                                                    Set<String> dependencies) {
    for (final String r : repositories) {
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
}
