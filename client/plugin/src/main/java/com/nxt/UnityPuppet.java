package com.nxt;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import java.util.Arrays;
import org.gradle.api.Action;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.FileCopyDetails;
import org.gradle.api.file.RelativePath;

/**
 * Created by alex on 04/01/2017.
 */
public class UnityPuppet {
  public static final String IMPORT_PACKAGE_PATH = "gp/build/import";

  public static void installPackage(Project project, File zip, ImmutableSet<String> includes) {
    Log.L.info("Installing {}", zip);
    project.copy(new Action<CopySpec>() {
      @Override
      public void execute(CopySpec copySpec) {
        copySpec.from(project.zipTree(zip));
        copySpec.include(includes);
        copySpec.setIncludeEmptyDirs(false);
        copySpec.into(project.file("Assets"));
        copySpec.eachFile(new Action<FileCopyDetails>() {
          @Override
          public void execute(FileCopyDetails details) {
            Log.L.info("Including {}", details.getRelativePath());
            String[] segments = details.getRelativePath().getSegments();
            // Chop off the 'Assets' folder.
            if (segments[0].equals("Assets")) {
              String[] tail = Arrays.copyOfRange(segments, 1, segments.length);
              boolean isFile = details.getFile().isFile();
              RelativePath path = new RelativePath(isFile, tail);
              details.setRelativePath(path);
            }
          }
        });
      }
    });
  }
}
