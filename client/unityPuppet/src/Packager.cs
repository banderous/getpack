using System;
using System.IO;
using System.Collections.Generic;
using System.Security.Permissions;
using UnityEditor;
using UnityEngine;


[InitializeOnLoad]
internal class Watcher : AssetPostprocessor
{
	const string TaskFolder = "nxt/task";
	const string ImportFolder = "nxt/import";
	const string ExportFolder = "nxt/export";
	static bool initialised = false;
	static Watcher ()
	{
		// This can be called multiple times by Unity.
		if (!initialised) {
			EditorApplication.update += Update;
			Debug.Log ("Watcher " + System.Diagnostics.Process.GetCurrentProcess ().Id);
			if (Application.platform == RuntimePlatform.OSXEditor) {
				Environment.SetEnvironmentVariable ("MONO_MANAGED_WATCHER", "enabled");
			}

			if (!Directory.Exists (TaskFolder)) {
				Directory.CreateDirectory (TaskFolder);
			}

			WatchForExports ();
			WatchForImports ();
			initialised = true;
		}
	}

	private static void WatchForImports ()
	{
		Directory.CreateDirectory (ImportFolder);

		// Create a new FileSystemWatcher and set its properties.
		FileSystemWatcher watcher = new FileSystemWatcher ();
		watcher.Path = ImportFolder;

		watcher.NotifyFilter = NotifyFilters.LastAccess | NotifyFilters.LastWrite
			| NotifyFilters.FileName | NotifyFilters.DirectoryName;

		watcher.Filter = "*.unitypackage";
		watcher.Created += new FileSystemEventHandler (OnImportDetected);
		watcher.Changed += new FileSystemEventHandler (OnChangeDetected);

		bool doImport = false;
		if (Directory.GetFiles (ImportFolder, "*.unitypackage").Length > 0) {
			doImport = true;
		}

		// Begin watching.
		watcher.EnableRaisingEvents = true;

		if (doImport) {
			DoImport ();
		}
	}

	private static void OnChangeDetected (object source, FileSystemEventArgs e)
	{
		DoImport ();
	}

	private static void OnImportDetected (object source, FileSystemEventArgs e)
	{
		DoImport ();
	}

	private static void DoImport ()
	{
		foreach (var file in Directory.GetFiles (ImportFolder, "*.unitypackage")) {
			Debug.Log ("Importing " + file);

			var dest = file + ".importing";
			File.Move (file, dest);
			Debug.Log ("Moved it to " + dest);
			AssetDatabase.ImportPackage (dest, false);
			startedImport = true;
			updatedSinceImport = false;
		}
	}

	static bool updatedSinceImport = false;
	static bool startedImport = false;
	// Unity imports packages on the main thread,
	// so we can figure out when it has finished importing by 
	// waiting for an update after we start the import.
	static void Update ()
	{
		if (startedImport) {
			updatedSinceImport = true;
		}
	}

	private static void WatchForExports ()
	{
		Directory.CreateDirectory (TaskFolder);
		// Create a new FileSystemWatcher and set its properties.
		FileSystemWatcher watcher = new FileSystemWatcher ();
		watcher.Path = TaskFolder;

		watcher.NotifyFilter = NotifyFilters.LastAccess | NotifyFilters.LastWrite
			| NotifyFilters.FileName | NotifyFilters.DirectoryName;

		watcher.Filter = "*.task";
		watcher.Created += new FileSystemEventHandler (OnExportDetected);

		bool doExport = false;
		if (Directory.GetFiles (TaskFolder, "*.task").Length > 0) {
			doExport = true;
		}

		// Begin watching.
		watcher.EnableRaisingEvents = true;

		if (doExport) {
			DoExport ();
		}
	}

	private static void OnExportDetected (object source, FileSystemEventArgs e)
	{
		DoExport ();
	}

	private static void DoExport ()
	{
		foreach (var file in Directory.GetFiles (TaskFolder, "*.task")) {
			
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
			var destination = Path.Combine (ExportFolder, string.Format ("{0}.unitypackage", Path.GetFileNameWithoutExtension(file)));
			AssetDatabase.ExportPackage (files, destination,
										 ExportPackageOptions.Recurse);



			Debug.Log ("Published to " + Path.GetFullPath(destination));

		}
	}

	private static void log (string s)
	{
		Debug.Log (s);
	}

	static void OnPostprocessAllAssets (string [] importedAssets, string [] deletedAssets, string [] movedAssets, string [] movedFromAssetPaths)
	{
		if (updatedSinceImport) {
			foreach (var file in Directory.GetFiles (ImportFolder, "*.importing")) {
				var newFile = Path.ChangeExtension (file, "completed");
				Debug.Log ("Moving to " + newFile);
				File.Move (file, newFile);
				return;
			}
		}

	}
}