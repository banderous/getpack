using System.IO;
using UnityEditor;

internal static class Importer
{
    public const string ImportFolder = "upm/import";
    public const string ImportExtension = "*.unitypackage";
    public static void DoImport ()
    {
        foreach (var file in Directory.GetFiles (ImportFolder, ImportExtension)) {
            Watcher.log ("Importing " + file);

            var dest = file + ".importing";
            //File.Copy (file, file + DateTime.Now.Ticks + ".copy");
            File.Move (file, dest);
            Watcher.log ("Moved it to " + dest);
            AssetDatabase.ImportPackage (dest, false);

            // On the next update the import will be complete.
            Watcher.AddTask (() => {
                var newFile = Path.ChangeExtension (dest, "completed");
                Watcher.log ("Moving to " + newFile);
                File.Move (dest, newFile);

                // Schedule another iteration for any further imports.
                Watcher.AddTask (DoImport);
            });
            break;
        }
    }
}
