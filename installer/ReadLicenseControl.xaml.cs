// Copyright (c) 2018 Tobias Briones. All rights reserved.

using System.Windows;
using System.Windows.Controls;

namespace installer;

public partial class ReadLicenseControl : UserControl
{
    private readonly string license;

    public ReadLicenseControl(string license)
    {
        this.license = license;

        InitializeComponent();
    }

    private void Control_Loaded(object sender, RoutedEventArgs e)
    {
        MainWindow.FindChild<TextBox>(this, "LicenseTextBox").Text =
            license;
    }

    private void DismissButton_Click(object sender, RoutedEventArgs e)
    {
        Window.GetWindow(this).Close();
    }
}