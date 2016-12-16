package com.nxt;

import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
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


    public static FileTree MergeArchives(Project project, HashMultimap<File, String> filesAndPaths) {
        FileTree result = null;
        for (File file : filesAndPaths.keys()) {
            ReadableResource resource = project.getResources().gzip(file);

            PatternSet pattern = new PatternSet();

            for (String s : filesAndPaths.get(file)) {
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
