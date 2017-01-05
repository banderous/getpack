using System;
using System.IO;
using System.Collections.Generic;
using UnityEditor;

internal class Exporter
{
    public const string TaskFolder = "nxt/task";
    public const string TaskExtension = "*.task";
    public const string ExportFolder = "nxt/export";

    internal static void DoExport ()
    {
        foreach (var file in Directory.GetFiles (TaskFolder, TaskExtension)) {

            // Read the list of files to export.
            var json = System.IO.File.ReadAllText (file);
            Dictionary<string, object> dic = (Dictionary<string, object>)com.nxt.MiniJSON.Deserialize (json);
            var task = (Dictionary<string, object>)dic ["task"];
            var fileList = (List<object>)task ["files"];
            var files = new string [fileList.Count];
            var t = 0;
            foreach (var f in fileList) {
                files [t++] = f.ToString ();
            }

            Directory.CreateDirectory (ExportFolder);
            var destination = Path.Combine (ExportFolder, string.Format ("{0}.unitypackage", Path.GetFileNameWithoutExtension (file)));
            AssetDatabase.ExportPackage (files, destination,
                                         ExportPackageOptions.Recurse);



            Watcher.log ("Published to " + Path.GetFullPath (destination));

        }
    }
}
