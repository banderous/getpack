package com.nxt.publish;


import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.nxt.config.*;
import com.nxt.config.Package;
import org.apache.commons.lang3.text.WordUtils;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.IvyArtifactRepository;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.FileTree;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.ivy.IvyArtifact;
import org.gradle.api.publish.ivy.IvyPublication;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
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
public class ExportPackage extends DefaultTask {

    enum PathType {
        task("task"),
        export("unitypackage"),
        manifest("manifest");

        String extension;
        PathType(String extension) {
            this.extension = extension;
        }
    }

    @OutputFile
    File unityPackage;

    @OutputFile
    File manifest;

    Package pack;

    @Inject
    public ExportPackage() {

    }

    public static void Configure(final Project project, final PublishConfig config) {
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
            ConfigurePackage(project, p);
        }
    }

    private static void ConfigurePackage(Project project, final Package pkg) {
        final String packageId = WordUtils.capitalize(pkg.group) + WordUtils.capitalize(pkg.name);
        String taskName = "nxtExport" + packageId;

        final ExportPackage task = project.getTasks().create(taskName, ExportPackage.class);
        task.dependsOn("launchUnity");
        task.unityPackage = getPath(project, PathType.export, pkg);
        task.manifest = getPath(project, PathType.manifest, pkg);
        task.pack = pkg;

        project.getExtensions().configure(PublishingExtension.class, new Action<PublishingExtension>() {
            @Override
            public void execute(PublishingExtension e) {
                IvyPublication i = e.getPublications().create(packageId, IvyPublication.class);
                i.setOrganisation(pkg.group);
                i.setModule(pkg.name);
                i.setRevision(pkg.version);
                i.artifact(task.unityPackage, new Action<IvyArtifact>() {
                    @Override
                    public void execute(IvyArtifact ivyArtifact) {
                        ivyArtifact.builtBy(task);
                    }
                });

                i.artifact(task.manifest, new Action<IvyArtifact>() {
                    @Override
                    public void execute(IvyArtifact ivyArtifact) {
                        ivyArtifact.builtBy(task);
                    }
                });
            }
        });
    }

    void exportPackageJob(Project project, Package pack) throws IOException {
        File exportFile = getPath(project, PathType.task, pack);
        Files.createParentDirs(exportFile);

        Path baseDir = Paths.get(project.getProjectDir().getAbsolutePath());
        Set<String> paths = Sets.newHashSet();
        for (File f : project.fileTree("Assets").getFiles()) {
            String s = baseDir.relativize(f.toPath()).toFile().getPath();
            paths.add(s);
        }

        String json = new Gson().toJson(ImmutableMap.of("task",
                ImmutableMap.of("files", paths)));

        Files.write(json, exportFile, Charsets.UTF_8);
    }

    static FileTree gatherForExport(Project project, Package pack) {
        ConfigurableFileTree tree = project.fileTree("Assets");
        tree.exclude("Plugins/nxt");
        tree.exclude("**/*.meta");

        for (String s : pack.getRoots()) {
            tree.include(s);
        }
        return tree;
    }

    void cleanExistingPackage() {
        if (unityPackage.exists()) {
            unityPackage.delete();
        }
    }

    static File getPath(Project project, PathType type, Package pack) {
        String path = String.format("nxt/%s/%s.%s.%s", type, pack.group, pack.name, type.extension);
        return project.file(path);
    }

    public static PackageManifest GenerateManifest(Project project, Package pack) {
        FileTree tree = gatherForExport(project, pack);
        return GenerateManifest(project, tree, pack);
    }

    public static PackageManifest GenerateManifest(Project project, FileTree tree, Package pack) {
        // Relativize the paths to the project root,
        // so they start 'Assets/...".
        Path baseURL = Paths.get(project.getProjectDir().getPath());

        PackageManifest manifest = new PackageManifest(pack);
        for (File file : tree.getFiles()) {
            String guid = GetGUIDForAsset(file);
            String md5 = generateMD5(file);
            Path path = baseURL.relativize(file.toPath());
            manifest.Add(guid, path, md5);
        }

        return manifest;
    }

    public static String GetGUIDForAsset(File asset) {
        return GetGUID(new File(asset.getPath() + ".meta"));
    }

    public static String GetGUID(File meta) {
        Yaml yaml = new Yaml();
        try {
            Map map = (Map) yaml.load(new FileInputStream(meta));
            return map.get("guid").toString();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static String generateMD5(File f) {
        try {
            HashCode md5 = Files.hash(f, Hashing.md5());
            return md5.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @TaskAction
    public void action() throws IOException, InterruptedException {
        PackageManifest.save(GenerateManifest(getProject(), pack), manifest);

        cleanExistingPackage();
        exportPackageJob(getProject(), pack);
        long startTime = System.currentTimeMillis();
        while(!unityPackage.exists())
        {
            Thread.sleep(100);
            // TODO - sensible timeout
            if (System.currentTimeMillis() - startTime > 5000) {
                throw new GradleException("Timed out waiting for export of " + getProject().getProjectDir().getPath());
            }
        }
    }
}
