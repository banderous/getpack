using System;
using System.IO;
using System.Security.Permissions;
using UnityEditor;
using UnityEngine;

[InitializeOnLoad]
public class Watcher
{
	const string TaskFolder = "nxt/tasks";
	static Watcher ()
	{
		if (Application.platform == RuntimePlatform.OSXEditor) {
			Environment.SetEnvironmentVariable ("MONO_MANAGED_WATCHER", "enabled");
		}

		if (!Directory.Exists (TaskFolder)) {
			Directory.CreateDirectory (TaskFolder);
		}

		// Create a new FileSystemWatcher and set its properties.
		FileSystemWatcher watcher = new FileSystemWatcher ();
		watcher.Path = TaskFolder;

		watcher.NotifyFilter = NotifyFilters.LastAccess | NotifyFilters.LastWrite
			| NotifyFilters.FileName | NotifyFilters.DirectoryName;

		watcher.Filter = "*.task";
		watcher.Created += new FileSystemEventHandler (OnChanged);

		bool doExport = false;
		if (File.Exists (Path.Combine (TaskFolder, "export.task"))) {
			doExport = true;
		}

		// Begin watching.
		watcher.EnableRaisingEvents = true;

		if (doExport) {
			DoExport ();
		}
	}

	// Define the event handlers.
	private static void OnChanged (object source, FileSystemEventArgs e)
	{
		DoExport ();
	}

	private static void DoExport ()
	{
		AssetDatabase.ExportPackage ("Assets", "nxt/package.unitypackage");
	}

	private static void log (string s)
	{
		Debug.Log (s);
	}
}