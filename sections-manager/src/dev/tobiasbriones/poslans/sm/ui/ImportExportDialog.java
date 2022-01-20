/*
 * Copyright (c) 2017 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.sm.ui;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

final class ImportExportDialog extends JDialog implements R,
                                                          Strings,
                                                          ActionListener {
    private static final long serialVersionUID = 53695931389262668L;
    public interface Callback {
        void onImport(
            Sheet classesSheet,
            Sheet professorsSheet,
            Sheet classroomsSheet
        );

        void onExport();
    }
    private final Callback callback;

    ImportExportDialog(CareerDataDialog dialog) {
        super(dialog, IMPORT_EXPORT);
        this.callback = dialog;
        final JPanel panel = new JPanel();
        final JPanel mainPanel = new JPanel();
        final JLabel label = new JLabel(IMPORT_EXPORT_LABEL);
        final JButton importButton = new JButton(IMPORT);
        final JButton exportButton = new JButton(EXPORT);
        importButton.setName(R.IMPORT_EXPORT_DIALOG_IMPORT);
        importButton.addActionListener(this);
        exportButton.setName(R.IMPORT_EXPORT_DIALOG_EXPORT);
        exportButton.addActionListener(this);
        mainPanel.add(importButton);
        mainPanel.add(exportButton);
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 15, 10, 15));
        panel.add(label, BorderLayout.NORTH);
        panel.add(mainPanel, BorderLayout.SOUTH);
        getContentPane().add(panel);
        pack();
        setResizable(false);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setIconImage(Toolkit.getDefaultToolkit()
                            .getImage("icons/ic_import_export.png"));
        setLocationRelativeTo(null);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (((Component) e.getSource()).getName()) {
            case R.IMPORT_EXPORT_DIALOG_IMPORT:
                final JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle(SELECT_EXCEL);
                fileChooser.setFileFilter(new FileFilter() {

                    @Override
                    public boolean accept(File file) {
                        final String filePath = file.getAbsolutePath();
                        final String fileName;
                        if (filePath.lastIndexOf("\\") != -1) {
                            fileName = filePath.substring(filePath.lastIndexOf(
                                "\\"));
                        }
                        else {
                            fileName = filePath;
                        }
                        final int index = fileName.indexOf(".");
                        return index != -1 && fileName.substring(index)
                                                      .equals(".xlsx");
                    }

                    @Override
                    public String getDescription() {
                        return EXCEL_FILES;
                    }
                });
                if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    final File file = fileChooser.getSelectedFile();
                    Sheet classesSheet = null;
                    Sheet professorsSheet = null;
                    Sheet classroomsSheet = null;
                    try {
                        final Workbook workbook = new XSSFWorkbook(file);
                        classesSheet = workbook.getSheetAt(0);
                        professorsSheet = workbook.getSheetAt(1);
                        classroomsSheet = workbook.getSheetAt(2);
                        workbook.close();
                    }
                    catch (Exception ex) {
                        JOptionPane.showMessageDialog(
                            this,
                            ex,
                            ERROR_OPENING,
                            JOptionPane.ERROR_MESSAGE
                        );
                        break;
                    }
                    callback.onImport(
                        classesSheet,
                        professorsSheet,
                        classroomsSheet
                    );
                }
                break;
            case R.IMPORT_EXPORT_DIALOG_EXPORT:
                callback.onExport();
                break;
        }
        dispose();
    }
}
