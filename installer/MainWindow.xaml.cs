// Copyright (c) 2018 Tobias Briones. All rights reserved.

using System;
using System.IO;
using System.Reflection;
using System.Security.AccessControl;
using System.Security.Principal;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;
using IWshRuntimeLibrary;
using Microsoft.WindowsAPICodePack.Dialogs;
using static System.Environment;
using File = System.IO.File;

namespace installer;

public partial class MainWindow : Window
{
    public MainWindow()
    {
        InitializeComponent();
    }

    private void Window_Loaded(object sender, RoutedEventArgs e)
    {
        var defaultPath = GetFolderPath(SpecialFolder.ProgramFiles);
        var directoryTextBox =
            FindChild<TextBox>(Application.Current.MainWindow,
                "DestinationTextBox");

        directoryTextBox.Text = defaultPath;
    }

    private void DestinationButton_Click(object sender, RoutedEventArgs e)
    {
        var directoryTextBox =
            FindChild<TextBox>(this, "DestinationTextBox");
        var dialog = new CommonOpenFileDialog
        {
            Title = "Installation folder",
            IsFolderPicker = true,
            InitialDirectory = null,

            AddToMostRecentlyUsedList = false,
            AllowNonFileSystemItems = false,
            DefaultDirectory = null,
            EnsureFileExists = true,
            EnsurePathExists = true,
            EnsureReadOnly = false,
            EnsureValidNames = true,
            Multiselect = false,
            ShowPlacesList = true
        };

        if (dialog.ShowDialog() == CommonFileDialogResult.Ok)
            directoryTextBox.Text = dialog.FileName;
    }

    private void ReadLicenseTextBlock_Click(object sender,
        RoutedEventArgs e)
    {
        var license =
            File.ReadAllText(
                Path.Combine("Sections Manager", "LICENSE.txt"));
        var window = new Window
        {
            Title = "License",
            Content = new ReadLicenseControl(license)
        };
        window.Width = 500;
        window.Height = 400;
        window.ResizeMode = ResizeMode.CanMinimize;
        window.WindowStartupLocation = WindowStartupLocation.CenterScreen;
        window.ShowDialog();
    }

    private void AgreeCheckBox_Checked_Unchecked(object sender,
        RoutedEventArgs e)
    {
        var installButton = FindChild<Button>(this, "InstallButton");
        installButton.IsEnabled = !installButton.IsEnabled;
    }

    private void CancelButton_Click(object sender, RoutedEventArgs e)
    {
        var result = MessageBox.Show("Exit wizard?",
            "Cancel installation", MessageBoxButton.YesNo);

        if (result == MessageBoxResult.Yes) Application.Current.Shutdown();
    }

    private void InstallButton_Click(object sender, RoutedEventArgs e)
    {
        var directoryTextBox =
            FindChild<TextBox>(Application.Current.MainWindow,
                "DestinationTextBox");
        var executableLocation =
            Path.GetDirectoryName(Assembly.GetExecutingAssembly().Location);
        var src = Path.Combine(executableLocation, "Sections Manager");
        var destination = Path.Combine(directoryTextBox.Text, "POSLANS",
            "Sections Manager");
        var sid =
            new SecurityIdentifier(WellKnownSidType.AuthenticatedUserSid,
                null);

        try
        {
            // Copy files
            foreach (var dirPath in Directory.GetDirectories(src, "*",
                         SearchOption.AllDirectories))
                Directory.CreateDirectory(
                    dirPath.Replace(src, destination));

            foreach (var newPath in Directory.GetFiles(src, "*.*",
                         SearchOption.AllDirectories))
                File.Copy(newPath, newPath.Replace(src, destination), true);

            // Grant folder permissions
            AddDirectorySecurity(destination, sid,
                FileSystemRights.WriteData, AccessControlType.Allow);
            AddDirectorySecurity(destination, sid,
                FileSystemRights.CreateDirectories,
                AccessControlType.Allow);

            // Add shortcut
            ShortcutToDesktop(destination);
            MessageBox.Show("Successfully installed", "Sections Manager");
        }
        catch (Exception ex)
        {
            MessageBox.Show(ex.Message);
        }

        Application.Current.Shutdown();
    }

    public static T FindChild<T>(DependencyObject parent, string childName)
        where T : DependencyObject
    {
        if (parent == null) return null;
        T foundChild = null;
        var childrenCount = VisualTreeHelper.GetChildrenCount(parent);

        for (var i = 0; i < childrenCount; i++)
        {
            var child = VisualTreeHelper.GetChild(parent, i);
            var childType = child as T;

            if (childType == null)
            {
                foundChild = FindChild<T>(child, childName);

                if (foundChild != null) break;
            }
            else if (!string.IsNullOrEmpty(childName))
            {
                if (child is FrameworkElement frameworkElement &&
                    frameworkElement.Name == childName)
                {
                    foundChild = (T) child;
                    break;
                }
            }
            else
            {
                foundChild = (T) child;
                break;
            }
        }

        return foundChild;
    }

    private static void AddDirectorySecurity(
        string FileName,
        IdentityReference Account,
        FileSystemRights Rights,
        AccessControlType ControlType
    )
    {
        // Create a new DirectoryInfo object.
        var dInfo = new DirectoryInfo(FileName);

        // Get a DirectorySecurity object that represents the 
        // current security settings.
        var dSecurity = dInfo.GetAccessControl();

        // Add the FileSystemAccessRule to the security settings. 
        dSecurity.AddAccessRule(
            new FileSystemAccessRule(Account, Rights, ControlType));

        // Set the new access settings.
        dInfo.SetAccessControl(dSecurity);
    }

    private static void ShortcutToDesktop(string location)
    {
        var startupFolderPath = GetFolderPath(SpecialFolder.Desktop);
        var shell = new WshShell();
        var shortCutLinkFilePath =
            Path.Combine(startupFolderPath, "Sections Manager.lnk");
        var windowsApplicationShortcut =
            (IWshShortcut) shell.CreateShortcut(shortCutLinkFilePath);

        windowsApplicationShortcut.Description = "Sections Manager";
        windowsApplicationShortcut.WorkingDirectory = location;
        windowsApplicationShortcut.TargetPath =
            Path.Combine(location, "Sections Manager.exe");
        windowsApplicationShortcut.Save();
    }
}