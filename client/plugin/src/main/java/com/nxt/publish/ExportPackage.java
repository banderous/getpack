package com.nxt.publish;


import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.nxt.Constants;
import com.nxt.Log;
import com.nxt.TimeoutTimer;
import com.nxt.config.Package;
import com.nxt.config.PackageManifest;
import groovy.util.Node;
import groovy.util.NodeList;
import org.apache.commons.lang3.text.WordUtils;
import org.gradle.api.*;
import org.gradle.api.artifacts.repositories.IvyArtifactRepository;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.FileTree;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.ivy.IvyArtifact;
import org.gradle.api.publish.ivy.IvyPublication;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.bundling.Zip;
import org.yaml.snakeyaml.Yaml;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

/**
 * Created by alex on 02/12/2016.
 */
public class ExportPackage {

  public static void configure(final Project project, final PublishConfig config) {
    project.getConfigurations().create("archives");
    project.getPluginManager().apply("ivy-publish");

    project.getExtensions().configure(PublishingExtension.class, new Action<PublishingExtension>() {
      @Override
      public void execute(PublishingExtension e) {
        for (final String url : config.getRepositories()) {
          e.getRepositories().ivy(new Action<IvyArtifactRepository>() {
            @Override
            public void execute(IvyArtifactRepository ivyArtifactRepository) {
              ivyArtifactRepository.setUrl(url);
            }
          });
        }
      }
    });

    for (Package p : config.getPackages()) {
      configurePackage(project, p);
    }
  }

  private static void configurePackage(Project project, final Package pkg) {
    final String packageId = WordUtils.capitalize(pkg.getGroup())
        + WordUtils.capitalize(pkg.getName());

    final FileTree files = gatherForExport(project, pkg);
    final Zip zip = project.getTasks().create("gpZip" + packageId, Zip.class, new Action<Zip>() {
        @Override
        public void execute(Zip zip) {
          zip.dependsOn("launchUnity");
          zip.from(files);
          zip.setIncludeEmptyDirs(false);
          zip.into("Assets");
          zip.setBaseName(pkg.getName());
          zip.setVersion(pkg.getVersion());
          zip.setDestinationDir(project.getBuildDir());
        }
    });

    final CreateManifest createManifest = project.getTasks().create("gpManifest" + packageId,
        CreateManifest.class);
    createManifest.pack = pkg;
    createManifest.packageFiles = files;
    createManifest.manifest = project.file(getPath(project, PathType.manifest, pkg));

    project.getExtensions().configure(PublishingExtension.class, new Action<PublishingExtension>() {
      @Override
      public void execute(PublishingExtension e) {
        IvyPublication i = e.getPublications().create(packageId, IvyPublication.class);
        i.setOrganisation(pkg.getGroup());
        i.setModule(pkg.getName());
        i.setRevision(pkg.getVersion());

        i.artifact(zip);

        i.artifact(createManifest.manifest, new Action<IvyArtifact>() {
          @Override
          public void execute(IvyArtifact ivyArtifact) {
            ivyArtifact.builtBy(createManifest);
          }
        });

        i.getDescriptor().withXml(new Action<XmlProvider>() {
          @Override
          public void execute(XmlProvider xmlProvider) {
            Node deps = (Node) ((NodeList) xmlProvider.asNode().get("dependencies")).get(0);
            for (String dep : pkg.getDependencies()) {
              Package pack = new Package(dep);
              new Node(deps, "dependency", ImmutableMap.of(
                  "org", pack.getGroup(),
                  "name", pack.getName(),
                  "rev", pack.getVersion()));
            }
          }
        });
      }
    });
  }

  static FileTree gatherForExport(Project project, Package pack) {
    ConfigurableFileTree tree = project.fileTree("Assets");
    tree.exclude("Plugins/gp");

    for (String s : pack.getRoots()) {
      Log.L.info("Including '{}'", s);
      tree.include(s);
    }

    Log.L.info("Total files {}", tree.getFiles().size());
    return tree;
  }

  static File getPath(Project project, PathType type, Package pack) {
    String path = String.format("gp/build/%s/%s.%s.%s", type.path, pack.getGroup(), pack.getName(),
        type.extension);
    return project.file(path);
  }

  enum PathType {
    // Tasks go in the export folder.
    task("task", "export"),
    export("zip"),
    manifest("manifest");

    String extension;
    String path;

    PathType(String extension) {
      this.extension = extension;
      this.path = toString();
    }

    PathType(String extension, String path) {
      this.extension = extension;
      this.path = path;
    }
  }
}
