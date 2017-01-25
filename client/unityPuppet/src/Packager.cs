using System;
using System.IO;
using System.Collections.Generic;
using UnityEditor;
using UnityEngine;


[InitializeOnLoad]
internal class Watcher
{
    static bool initialised;
    static readonly StreamWriter logger = File.AppendText ("gp/puppet.log");

    public delegate void Action ();
    private static List<Action> Tasks = new List<Action> ();

    static Watcher ()
    {
        // This can be called multiple times by Unity.
        if (!initialised) {
            log ("Watcher " + System.Diagnostics.Process.GetCurrentProcess ().Id);

            WatchForJobs (Exporter.ExportFolder, Exporter.TaskExtension, Exporter.DoExport);
            WatchForJobs (Importer.ImportFolder, Importer.ImportExtension, Importer.DoImport);
            EditorApplication.update += Update;
            initialised = true;
        }
    }

    static void Update ()
    {
        if (Tasks.Count > 0) {
            var clone = new List<Action> (Tasks);
            Tasks.Clear ();
            foreach (var task in clone) {
                try {
                    task ();
                } catch (Exception e) {
                    log (e.ToString ());
                }
            }
        }
    }

    public static void AddTask (Action action)
    {
        Tasks.Add (action);
    }

    private static void WatchForJobs (string folder, string filter, Action task)
    {
        Directory.CreateDirectory (folder);

        EditorApplication.update += () => {
            if (Directory.GetFiles (folder, filter).Length > 0) {
                AddTask (task);
            }
        };
    }

    public static void log (string s)
    {
        Debug.Log (s);
        logger.WriteLine (s);
        logger.Flush ();
    }
}
