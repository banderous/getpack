using System;
using System.IO;
using System.Security.Permissions;
using UnityEditor;
using UnityEngine;

[InitializeOnLoad]
public class Watcher
{
	public static void Run ()
	{
		// Create a new FileSystemWatcher and set its properties.
		FileSystemWatcher watcher = new FileSystemWatcher ();
		watcher.Path = "nxt";
		/* Watch for changes in LastAccess and LastWrite times, and
           the renaming of files or directories. */
		watcher.NotifyFilter = NotifyFilters.LastAccess | NotifyFilters.LastWrite
			| NotifyFilters.FileName | NotifyFilters.DirectoryName;

		watcher.Filter = "*.*";

		// Add event handlers.
		watcher.Changed += new FileSystemEventHandler (OnChanged);
		watcher.Created += new FileSystemEventHandler (OnChanged);
		watcher.Deleted += new FileSystemEventHandler (OnChanged);
		watcher.Renamed += new RenamedEventHandler (OnRenamed);

		// Begin watching.
		watcher.EnableRaisingEvents = true;
	}

	// Define the event handlers.
	private static void OnChanged (object source, FileSystemEventArgs e)
	{
		// Specify what is done when a file is changed, created, or deleted.
		log ("File: " + e.FullPath + " " + e.ChangeType);
	}

	private static void OnRenamed (object source, RenamedEventArgs e)
	{
		// Specify what is done when a file is renamed.
		log ("File: {0} renamed to {1}" + e.OldFullPath + e.FullPath);
	}

	private static void log (string s)
	{
		Console.WriteLine (s);
	}
}