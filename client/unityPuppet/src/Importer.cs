﻿using System.IO;
using UnityEditor;

internal static class Importer
{
    public const string ImportFolder = "gp/build/import";
    public const string ImportExtension = "*.unitypackage";
    public static void DoImport ()
    {
        foreach (var file in Directory.GetFiles (ImportFolder, ImportExtension)) {
            Watcher.log ("Importing " + file);

            var dest = file + ".importing";
            MoveDestructive (file, dest);
            Watcher.log ("Moved it to " + dest);
            AssetDatabase.ImportPackage (dest, false);

            // On the next update the import will be complete.
            Watcher.AddTask (() => {
                var newFile = Path.ChangeExtension (dest, "completed");
                Watcher.log ("Moving to " + newFile);
                MoveDestructive (dest, newFile);

                // Schedule another iteration for any further imports.
                Watcher.AddTask (DoImport);
            });
            break;
        }
    }

    public static void CleanOldImports ()
    {
        if (Directory.Exists (ImportFolder)) {
            try {
                foreach (var file in Directory.GetFiles (ImportFolder, ".importing")) {
                    var newFile = Path.ChangeExtension (file, "completed");
                    MoveDestructive (file, newFile);
                }
            } catch (IOException i) {
                Watcher.log (i.ToString ());
            }
        }
    }

    private static void MoveDestructive (string source, string to)
    {
        if (File.Exists (to)) {
            File.Delete (to);
        }
        File.Move (source, to);
    }
}
