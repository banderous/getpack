package com.nxt;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.FileTree;
import org.gradle.api.resources.ReadableResource;
import org.gradle.api.resources.ResourceHandler;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.util.PatternFilterable;
import org.gradle.api.tasks.util.PatternSet;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by alex on 15/12/2016.
 */
public class UnityPackageCreator extends Copy {


    public static FileTree MergeArchives(Project project, Map<File, Set<String>> filesAndPaths) {
        FileTree result = null;
        for (Map.Entry<File, Set<String>> entry : filesAndPaths.entrySet()) {
            ReadableResource resource = project.getResources().gzip(entry.getKey());

            PatternSet pattern = new PatternSet();

            for (String s : entry.getValue()) {
                pattern.include(String.format("./%s/**", s));
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
}
