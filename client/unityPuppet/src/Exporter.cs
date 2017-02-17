using System.IO;
using System.Collections.Generic;
using UnityEditor;

internal static class Exporter
{
    public const string TaskExtension = "*.task";
    public const string ExportFolder = "gp/build/export";

    internal static void DoExport ()
    {
        foreach (var file in Directory.GetFiles (ExportFolder, TaskExtension)) {

            // Read the list of files to export.
            var json = File.ReadAllText (file);
            var dic = (Dictionary<string, object>)com.nxt.MiniJSON.Deserialize (json);
            var task = (Dictionary<string, object>)dic ["task"];
            var fileList = (List<object>)task ["files"];
            var files = new string [fileList.Count];
            var t = 0;
            foreach (var f in fileList) {
                files [t++] = f.ToString ();
            }

            Directory.CreateDirectory (ExportFolder);
            var destination = Path.ChangeExtension (file, "unitypackage");
            AssetDatabase.ExportPackage (files, destination,
                                         ExportPackageOptions.Recurse);

            File.Delete (file);
            Watcher.log ("Published to " + Path.GetFullPath (destination));
        }
    }
}
