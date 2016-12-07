using System;
using System.IO;
using System.Collections.Generic;
using System.Security.Permissions;
using UnityEditor;
using UnityEngine;

namespace com.nxt
{

	[InitializeOnLoad]
	public class Watcher
	{
		static string ExportTaskPath = Path.Combine (TaskFolder, "export.task");
		const string TaskFolder = "nxt/tasks";
		const string ImportFolder = "nxt/import";
		static Watcher ()
		{
			if (Application.platform == RuntimePlatform.OSXEditor) {
				Environment.SetEnvironmentVariable ("MONO_MANAGED_WATCHER", "enabled");
			}

			if (!Directory.Exists (TaskFolder)) {
				Directory.CreateDirectory (TaskFolder);
			}

			WatchForExports ();
			WatchForImports ();
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

		private static void OnImportDetected (object source, FileSystemEventArgs e)
		{
			DoImport ();
		}

		private static void DoImport ()
		{
			foreach (var file in Directory.GetFiles (ImportFolder, "*.unitypackage")) {
				Debug.Log ("Importing " + file);
				AssetDatabase.ImportPackage (file, false);
				//Debug.Log("EXISTS: " + File.Exists("Assets/
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
			if (File.Exists (Path.Combine (TaskFolder, "export.task"))) {
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
			// Read the list of files to export.
			var json = System.IO.File.ReadAllText (ExportTaskPath);
			Dictionary<string, object> dic = (Dictionary<string, object>) MiniJSON.Deserialize (json);
			var task = (Dictionary<string, object>)dic ["task"];
			var fileList = (List<object>)task["files"];
			var files = new string [fileList.Count];
			var t = 0;
			foreach (var f in fileList) {
				files [t++] = f.ToString ();
			}

			AssetDatabase.ExportPackage (files, "nxt/package.unitypackage",
										 ExportPackageOptions.Recurse);
			Debug.Log ("Published to " + Directory.GetCurrentDirectory ());
		}

		private static void log (string s)
		{
			Debug.Log (s);
		}
	}
}
