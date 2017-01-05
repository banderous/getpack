using System;
using System.IO;
using System.Collections.Generic;
using System.Security.Permissions;
using UnityEditor;
using UnityEngine;


[InitializeOnLoad]
internal class Watcher
{
    static bool initialised = false;
    static StreamWriter logger = File.AppendText ("nxt/puppet.log");

    public delegate void Action ();
    private static List<Action> Tasks = new List<Action> ();

    static Watcher ()
    {
        // This can be called multiple times by Unity.
        if (!initialised) {
            log ("Watcher " + System.Diagnostics.Process.GetCurrentProcess ().Id);
            if (Application.platform == RuntimePlatform.OSXEditor) {
                Environment.SetEnvironmentVariable ("MONO_MANAGED_WATCHER", "enabled");
            }

            WatchForJobs (Exporter.TaskFolder, Exporter.TaskExtension, Exporter.DoExport);
            WatchForJobs (Importer.ImportFolder, Importer.ImportExtension, Importer.DoImport);
            EditorApplication.update += Update;
            initialised = true;
        }
    }

    static void Update ()
    {
        lock(Tasks) {
            foreach (var task in Tasks) {
                try {
                    task ();
                } catch (Exception e) {
                    log (e.ToString ());
                }
            }
            Tasks.Clear ();
        }
    }

    public static void AddTask (Action action)
    {
        lock(Tasks) {
            Tasks.Add (action);
        }
    }

    private static void WatchForJobs (string folder, string filter, Action task)
    {
        Directory.CreateDirectory (folder);

        // Create a new FileSystemWatcher and set its properties.
        FileSystemWatcher watcher = new FileSystemWatcher ();
        watcher.Path = folder;

        watcher.NotifyFilter = NotifyFilters.LastAccess | NotifyFilters.LastWrite
            | NotifyFilters.FileName | NotifyFilters.DirectoryName;
        watcher.Filter = filter;

        watcher.Created += new FileSystemEventHandler((object sender, FileSystemEventArgs e) => AddTask(task));
        watcher.Changed += new FileSystemEventHandler ((object sender, FileSystemEventArgs e) => AddTask(task));

        // Is there already work pending?
        if (Directory.GetFiles (folder, filter).Length > 0) {
            AddTask (task);
        }

        // Begin watching.
        watcher.EnableRaisingEvents = true;
    }

    public static void log (string s)
    {
        Debug.Log (s);
        logger.WriteLine (s);
        logger.Flush ();
    }
}