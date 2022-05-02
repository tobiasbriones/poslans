/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.mu.ui;

import dev.tobiasbriones.poslans.mu.ui.controller.CancelFlag;
import dev.tobiasbriones.poslans.mu.ui.controller.Item;
import dev.tobiasbriones.poslans.mu.ui.editing.Campus;
import dev.tobiasbriones.poslans.mu.ui.editing.Career;
import dev.tobiasbriones.poslans.mu.ui.editing.EditingItem;
import engineer.mathsoftware.jdesk.App;
import engineer.mathsoftware.jdesk.AppInstance;
import engineer.mathsoftware.jdesk.Window;
import engineer.mathsoftware.jdesk.io.FileFormat;
import engineer.mathsoftware.jdesk.resources.Resources;
import engineer.mathsoftware.jdesk.resources.StringResourceId;
import engineer.mathsoftware.jdesk.resources.StringResources;
import engineer.mathsoftware.jdesk.ui.FileFormatFilter;
import engineer.mathsoftware.jdesk.ui.dialog.ActionPanel;
import engineer.mathsoftware.jdesk.ui.dialog.AppDialog;
import engineer.mathsoftware.jdesk.ui.dialog.Dialog;
import engineer.mathsoftware.jdesk.ui.dialog.TaskDialog;
import engineer.mathsoftware.jdesk.ui.style.Style;
import engineer.mathsoftware.jdesk.ui.style.TextStyle;
import engineer.mathsoftware.jdesk.ui.view.Button;
import engineer.mathsoftware.jdesk.ui.view.Panel;
import engineer.mathsoftware.jdesk.ui.view.*;
import engineer.mathsoftware.jdesk.ui.view.loading.BarLoadingView;
import engineer.mathsoftware.jdesk.work.WorkRunnable;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import static dev.tobiasbriones.poslans.mu.Strings.*;

public final class MainWindow extends Window implements ClickListener,
                                                        ActionListener {

    private static final long serialVersionUID = 7189406897677005096L;

    public interface Callback extends EmailLoginWindow.Callback {
        List<EditingItem> getCampuses() throws SQLException;

        List<EditingItem> getCareers() throws SQLException;

        List<Item> listCampusItems() throws SQLException;

        List<Item> listCareerItems() throws SQLException;

        LinkedList<String> listCurrentTerms() throws SQLException;

        JSONArray listTerms() throws Exception;

        boolean isValidUserPassword(String password);

        void updateCampuses(List<EditingItem> campuses) throws SQLException;

        void updateCareers(List<EditingItem> careers) throws SQLException;

        void registerProfessors(
            Sheet sheet,
            Sheet passwordsSheet,
            TaskDialog<Sheet> td
        ) throws Exception;

        void updateProfessors(
            Sheet sheet,
            Sheet passwordsSheet,
            TaskDialog<Sheet> td
        ) throws Exception;

        void deleteProfessors(
            Sheet sheet,
            TaskDialog<Void> td
        ) throws Exception;

        void registerStudents(
            Sheet sheet,
            Sheet passwordsSheet,
            TaskDialog<Sheet> td
        ) throws Exception;

        void updateStudents(
            Sheet sheet,
            Sheet passwordsSheet,
            TaskDialog<Sheet> td
        ) throws Exception;

        void deleteStudents(
            Sheet sheet,
            TaskDialog<Void> td
        ) throws Exception;

        void sendPasswords(
            Sheet passwordsSheet,
            TaskDialog<Void> td,
            CancelFlag cancelFlag
        ) throws Exception;

        void downloadProfessors(
            File file,
            TaskDialog<Void> td,
            CancelFlag cancelFlag
        ) throws Exception;

        void downloadStudents(
            File file,
            TaskDialog<Void> td,
            CancelFlag cancelFlag
        ) throws Exception;

        void saveLoginKey(File file, String userPassword) throws Exception;

        void startNewTerm(String name, JSONArray targets) throws Exception;

        void finishTerm(String term) throws Exception;

        void downloadTermFile(
            String download,
            File saveDir
        ) throws Exception;

        void deleteTermFile(String delete) throws Exception;

        void logout() throws IOException;
    }

    public static String DELETE_FILE_PERMANENTLY(
        StringResources sr,
        String file
    ) {
        String str = "<html><body style='font-family:Roboto Light;"
                     + "color:#FF3D00'>";
        str += sr.get(DELETE_FILE) + " " + file + " " + sr.get(
            PERMANENTLY_QUESTION);
        str += "</body></html>";
        return str;
    }

    public static String DB_ADD(StringResources sr, int number, String items) {
        return sr.get(ADD) + " " + number + " " + items + "?";
    }

    public static String DB_UPDATE(
        StringResources sr,
        int number,
        String items
    ) {
        return sr.get(UPDATE) + " " + number + " " + items + "?";
    }

    public static String DB_DELETE(
        StringResources sr,
        int number,
        String items
    ) {
        return sr.get(DELETE) + " " + number + " " + items + "?";
    }

    private final App app;
    private final Callback callback;
    private final TextLabel dbLabel;
    private final Button logoutButton;
    private final Button copyFormatsButton;
    private final Button configButton;
    private final Button aboutButton;
    private final Button saveKeyButton;
    private final Button emailInfoButton;
    private final Button currentTermButton;
    private final Button termHistoryButton;
    private final Button professorsButton;
    private final Button studentsButton;
    private final Button campusesEditButton;
    private final Button careersEditButton;

    public MainWindow(App app, Callback callback, String dbName) {
        super((AppInstance) app);
        this.app = app;
        this.callback = callback;
        this.dbLabel = new TextLabel(this, dbName);
        this.logoutButton = new Button(this, Resources.loadIcon("ic_logout"));
        this.configButton = new Button(this, Resources.loadIcon("ic_settings"));
        this.copyFormatsButton = new Button(
            this,
            Resources.loadIcon("ic_book")
        );
        this.aboutButton = new Button(this, Resources.loadIcon("ic_about"));
        this.saveKeyButton = new Button(this, SAVE_KEY);
        this.emailInfoButton = new Button(this, SET_EMAIL_ACCESS);
        this.currentTermButton = new Button(this, CURRENT_TERM);
        this.termHistoryButton = new Button(this, TERM_HISTORY);
        this.professorsButton = new Button(this, PROFESSORS);
        this.studentsButton = new Button(this, STUDENTS);
        this.campusesEditButton = new Button(this, CAMPUSES);
        this.careersEditButton = new Button(this, CAREERS);
        setResizable(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final Component source = (Component) e.getSource();
        if (source == logoutButton) {
            try {
                callback.logout();
            }
            catch (IOException ex) {
                AppDialog.showMessage(
                    this,
                    ex.getMessage(),
                    AppDialog.Type.ERROR
                );
            }
            return;
        }
        if (source == copyFormatsButton) {
            final JFileChooser fileChooser = new JFileChooser();
            AppDialog.showMessage(this, SELECT_FOLDER_SAVE_FORMS);
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                final File sourceDirectory = new File(
                    "formats/POSLANS MU Formats");
                final File destDirectory = new File(
                    fileChooser.getSelectedFile(),
                    "POSLANS MU Formats"
                );
                try {
                    Files.copy(sourceDirectory, destDirectory);
                    Runtime.getRuntime()
                           .exec("explorer.exe /select," + destDirectory);
                }
                catch (IOException ex) {
                    final String msg = ex.toString();
                    AppDialog.showMessage(this, msg, AppDialog.Type.ERROR);
                }
            }
            return;
        }
        if (source == configButton) {
            new SettingsDialog(this);
            return;
        }
        if (source == aboutButton) {
            new AboutDialog(this, getAppInstance());
            return;
        }
    }

    @Override
    public void onClick(Object view, StringResourceId textId) {
        if (textId == SAVE_KEY) {
            final JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                final File file = new File(
                    fileChooser.getSelectedFile(),
                    "POSLANS Admin Access.key"
                );
                final String password = AppDialog.showPasswordInput(
                    this,
                    TYPE_PASSWORD
                );
                if (password != null) {
                    final boolean isValid = callback.isValidUserPassword(
                        password);
                    if (isValid) {
                        saveLoginKey(file);
                    }
                    else {
                        AppDialog.showMessage(
                            this,
                            INVALID_PASSWORD,
                            AppDialog.Type.WARNING
                        );
                    }
                }
            }
        }
        else if (textId == SET_EMAIL_ACCESS) {
            final EmailLoginWindow.Callback cb = callback;
            final EmailLoginWindow elw = new EmailLoginWindow(app, cb);
            app.addWindow(elw);
        }
        else if (textId == CURRENT_TERM) {
            new TermDialog(this, callback);
        }
        else if (textId == TERM_HISTORY) {
            new TermHistoryDialog(this, callback);
        }
        else if (textId == PROFESSORS) {
            new Editor1Dialog(this, PROFESSORS, callback);
        }
        else if (textId == STUDENTS) {
            new Editor1Dialog(this, STUDENTS, callback);
        }
        else if (textId == CAMPUSES) {
            openEditCampuses();
        }
        else if (textId == CAREERS) {
            openEditCareers();
        }
    }

    @Override
    protected void createWindow(Panel panel) {
        final Panel topPanel = new Panel(this);
        final Panel topBarPanel = new Panel(this);
        final Panel centerPanel = new Panel(this);
        final Panel bottomPanel = new Panel(this);
        final Panel mainPanel = new Panel(this);
        final TextLabel poslans = PoslansLabel.newInstance(this);
        final Dimension barButtonSize = new Dimension(24, 24);
        final Dimension bottomButtonSize = new Dimension(150, 40);
        final StringResources sr = getStringResources();
        dbLabel.setTextStyle(TextStyle.BOLD);
        logoutButton.setPreferredSize(barButtonSize);
        logoutButton.setToolTipText(sr.get(LOGOUT));
        logoutButton.setBackgroundColor(getAppStyle().getWindowBackgroundColor());
        logoutButton.setFocusable(false);
        logoutButton.addActionListener(this);
        copyFormatsButton.setPreferredSize(barButtonSize);
        copyFormatsButton.setToolTipText(sr.get(GET_FORMATS));
        copyFormatsButton.setBackgroundColor(getAppStyle().getWindowBackgroundColor());
        copyFormatsButton.setFocusable(false);
        copyFormatsButton.addActionListener(this);
        configButton.setPreferredSize(barButtonSize);
        configButton.setToolTipText(sr.get(CONFIG));
        configButton.setBackgroundColor(getAppStyle().getWindowBackgroundColor());
        configButton.setFocusable(false);
        configButton.addActionListener(this);
        aboutButton.setPreferredSize(barButtonSize);
        aboutButton.setToolTipText(sr.get(ABOUT));
        aboutButton.setBackgroundColor(getAppStyle().getWindowBackgroundColor());
        aboutButton.setFocusable(false);
        aboutButton.addActionListener(this);
        saveKeyButton.setFocusable(false);
        emailInfoButton.setFocusable(false);
        currentTermButton.setFocusable(false);
        termHistoryButton.setFocusable(false);
        professorsButton.setFocusable(false);
        studentsButton.setFocusable(false);
        campusesEditButton.setPreferredSize(bottomButtonSize);
        campusesEditButton.setFocusable(false);
        careersEditButton.setPreferredSize(bottomButtonSize);
        careersEditButton.setFocusable(false);
        poslans.setBorder(new EmptyBorder(5, 0, 0, 1));
        // Top bar panel
        topBarPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        topBarPanel.add(logoutButton);
        topBarPanel.add(copyFormatsButton);
        topBarPanel.add(configButton);
        topBarPanel.add(aboutButton);
        // Top panel
        topPanel.setLayout(new BorderLayout());
        topPanel.add(dbLabel, BorderLayout.WEST);
        topPanel.add(topBarPanel, BorderLayout.EAST);
        // Center panel
        centerPanel.setLayout(new GridLayout(6, 1, 0, 5));
        centerPanel.setPadding(0, 0, 10, 0);
        centerPanel.add(saveKeyButton);
        centerPanel.add(emailInfoButton);
        centerPanel.add(currentTermButton);
        centerPanel.add(termHistoryButton);
        centerPanel.add(professorsButton);
        centerPanel.add(studentsButton);
        // Bottom panel
        bottomPanel.setLayout(new GridLayout(1, 2));
        bottomPanel.add(campusesEditButton);
        bottomPanel.add(careersEditButton);
        // Main panel
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        // Panel
        panel.setLayout(new BorderLayout());
        panel.setPadding(10, 10, 10, 10);
        panel.add(mainPanel, BorderLayout.NORTH);
        panel.add(poslans, BorderLayout.CENTER);
    }

    @Override
    protected void windowCreated() {
        setVisible(true);
    }

    private void saveLoginKey(File file) {
        String keyPassword;
        do {
            keyPassword = AppDialog.showPasswordInput(
                this,
                SET_LOGIN_KEY_PASSWORD
            );
            if (keyPassword == null) {
                break;
            }
            try {
                callback.saveLoginKey(file, keyPassword);
                AppDialog.showMessage(
                    this,
                    LOGIN_KEY_SAVED,
                    AppDialog.Type.INFO
                );
            }
            catch (Exception e) {
                AppDialog.showMessage(
                    this,
                    LOGIN_KEY_NOT_SAVED,
                    AppDialog.Type.ERROR
                );
            }
        }
        while (keyPassword == null);
    }

    private void openEditCampuses() {
        final TaskDialog<List<EditingItem>> td = new TaskDialog<>(
            this,
            CONNECTING,
            OPENING_CAMPUSES,
            false
        );
        final WorkRunnable<List<EditingItem>> runnable = () -> {
            final List<EditingItem> campuses = callback.getCampuses();
            return campuses;
        };
        final TaskDialog.TaskDialogCallback<List<EditingItem>> taskCallback =
            new TaskDialog.TaskDialogCallback<List<EditingItem>>(this) {
                @Override
                public void workFinished(List<EditingItem> campuses) {
                    new Editor2Dialog(
                        MainWindow.this,
                        campuses,
                        Campus.class,
                        callback
                    );
                }

                @Override
                public boolean cancelRequest() {
                    return false;
                }
            };
        td.getBarLoadingView()
          .setSpeed(BarLoadingView.BAR_WIDTH_CHANGE_PER_SECOND_FAST);
        td.setCallback(taskCallback);
        td.execute(runnable);
    }

    private void openEditCareers() {
        final TaskDialog<List<EditingItem>> td = new TaskDialog<>(
            this,
            CONNECTING,
            OPENING_CAREERS,
            false
        );
        final WorkRunnable<List<EditingItem>> runnable = () -> {
            final List<EditingItem> careers = callback.getCareers();
            return careers;
        };
        final TaskDialog.TaskDialogCallback<List<EditingItem>> taskCallback =
            new TaskDialog.TaskDialogCallback<List<EditingItem>>(this) {
                @Override
                public void workFinished(List<EditingItem> careers) {
                    new Editor2Dialog(
                        MainWindow.this,
                        careers,
                        Career.class,
                        callback
                    );
                }

                @Override
                public boolean cancelRequest() {
                    return false;
                }
            };
        td.getBarLoadingView()
          .setSpeed(BarLoadingView.BAR_WIDTH_CHANGE_PER_SECOND_FAST);
        td.setCallback(taskCallback);
        td.execute(runnable);
    }

    // -------------------- NESTED CLASSES -------------------- //
    private static final class NewTermCareerSelectorDialog extends Dialog {
        private static final long serialVersionUID = -3075998044728411411L;

        private interface Callback {

            void onStartNewTerm(
                String termName,
                JSONArray careers
            ) throws Exception;
        }

        private NewTermCareerSelectorDialog(
            MainWindow mw, String termName, List<Item> campuses,
            List<Item> careers, Callback callback
        ) {
            super(mw);
            final Panel panel = new Panel(mw);
            final Panel cancelPanel = new Panel(mw);
            final Panel actionsPanel = new Panel(mw);
            final Panel bottomPanel = new Panel(mw);
            final TableModel tableModel = new TableModel(campuses, careers);
            final JTable table = new JTable(tableModel);
            final JScrollPane scroll = new JScrollPane(table);
            final Button cancelButton = new Button(mw, CANCEL);
            final Button unselectAllButton = new Button(mw, UNSELECT_ALL);
            final Button selectAllButton = new Button(mw, SELECT_ALL);
            final Button startNewTermButton = new Button(mw, START_NEW_TERM);
            final DefaultTableCellRenderer cellRenderer =
                new TableCellRenderer();
            final ActionListener l = e -> {
                final Object src = e.getSource();
                if (src == cancelButton) {
                    dispose();
                    return;
                }
                if (src == unselectAllButton) {
                    table.clearSelection();
                    return;
                }
                if (src == selectAllButton) {
                    table.selectAll();
                    return;
                }
                // Else, startNewTermButton
                final JSONArray array = new JSONArray();
                final int[] rows = table.getSelectedRows();
                final int[] colums = table.getSelectedColumns();
                JSONObject currentItem = null;
                if (rows.length == 0 && colums.length == 0) {
                    AppDialog.showMessage(mw, NO_CAREER_SELECTED);
                    return;
                }
                for (int y : colums) {
                    for (int x : rows) {
                        currentItem = new JSONObject();
                        currentItem.put("campus", campuses.get(y).id);
                        currentItem.put("career", careers.get(x).id);
                        array.put(currentItem);
                    }
                }
                openTerm(mw, callback, termName, array);
            };
            for (int i = 0; i < campuses.size(); i++) {
                table.getColumnModel().getColumn(i).setPreferredWidth(120);
            }
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            table.setCellSelectionEnabled(true);
            table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            table.setSelectionBackground(Color.decode("#0288D1"));
            table.setFocusable(false);
            table.selectAll();
            table.setDefaultRenderer(String.class, cellRenderer);
            table.setFont(mw.getAppStyle().getFont());
            table.setRowHeight(18);
            scroll.setPreferredSize(new Dimension(1024, 600));
            cancelButton.addActionListener(l);
            unselectAllButton.addActionListener(l);
            selectAllButton.addActionListener(l);
            startNewTermButton.addActionListener(l);
            actionsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
            actionsPanel.add(unselectAllButton);
            actionsPanel.add(selectAllButton);
            actionsPanel.add(startNewTermButton);
            cancelPanel.add(cancelButton);
            bottomPanel.setLayout(new BorderLayout());
            bottomPanel.add(cancelPanel, BorderLayout.WEST);
            bottomPanel.add(actionsPanel, BorderLayout.EAST);
            panel.setLayout(new BorderLayout());
            panel.setPadding(10, 10, 5, 10);
            panel.add(scroll, BorderLayout.NORTH);
            panel.add(bottomPanel, BorderLayout.CENTER);
            getContentPane().add(panel);
            pack();
            setLocationRelativeTo(null);
            setTitle(mw.getStringResources().get(CAREERS_SELECTION));
            setVisible(true);
        }

        private void openTerm(
            MainWindow mw,
            Callback callback,
            String termName,
            JSONArray array
        ) {
            final TaskDialog<Void> td = new TaskDialog<>(
                mw,
                CONNECTING,
                OPENING_TERM_ELLIPSIS,
                false
            );
            final WorkRunnable<Void> runnable = () -> {
                callback.onStartNewTerm(termName, array);
                return null;
            };
            final TaskDialog.TaskDialogCallback<Void> tdCallback =
                new TaskDialog.TaskDialogCallback<Void>(mw) {
                    @Override
                    public void workFinished(Void result) {
                        AppDialog.showMessage(
                            mw,
                            TERM_STARTED,
                            AppDialog.Type.INFO
                        );
                        dispose();
                    }

                    @Override
                    public void workFailed(Exception exception) {
                        super.workFailed(exception);
                        dispose();
                    }

                    @Override
                    public boolean cancelRequest() {
                        return false;
                    }
                };
            td.setCallback(tdCallback);
            td.execute(runnable);
        }

        private static final class TableCellRenderer extends DefaultTableCellRenderer {
            private static final long serialVersionUID = -627682909662164482L;

            @Override
            public Component getTableCellRendererComponent(
                JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column
            ) {
                final JLabel c =
                    (JLabel) super.getTableCellRendererComponent(
                        table,
                        value,
                        isSelected,
                        hasFocus,
                        row,
                        column
                    );
                c.setToolTipText(value.toString());
                return c;
            }
        }

        private static final class TableModel extends AbstractTableModel {
            private static final long serialVersionUID = 1152165385066461786L;
            private final List<Item> campuses;
            private final List<Item> careers;

            private TableModel(List<Item> campuses2, List<Item> careers2) {
                this.campuses = campuses2;
                this.careers = careers2;
            }

            @Override
            public int getRowCount() {
                return careers.size();
            }

            @Override
            public int getColumnCount() {
                return campuses.size();
            }

            @Override
            public Object getValueAt(int row, int column) {
                return careers.get(row);
            }

            @Override
            public String getColumnName(int column) {
                return campuses.get(column).name;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }
        }
    }

    private static final class TermDialog extends Dialog implements ActionListener,
                                                                    NewTermCareerSelectorDialog.Callback {
        private static final long serialVersionUID = -7759132204514555454L;
        private final MainWindow mw;
        private final Callback callback;
        private final ListPane<String> list;
        private final Button backButton;
        private final Button finishTermButton;
        private final Button startNewTermButton;

        private TermDialog(MainWindow mw, Callback callback) {
            super(mw);
            this.mw = mw;
            this.callback = callback;
            this.list = new ListPane<>(mw);
            this.backButton = new Button(mw, BACK);
            this.finishTermButton = new Button(mw, FINISH_TERM);
            this.startNewTermButton = new Button(mw, START_NEW_TERM);
            final Panel panel = new Panel(mw);
            final DefaultListModel<String> listModel = new DefaultListModel<>();
            final JScrollPane scroll = new JScrollPane(list);
            final ActionPanel actionsPanel = new ActionPanel(
                mw,
                finishTermButton,
                startNewTermButton,
                backButton
            );
            final TaskDialog<LinkedList<String>> td = new TaskDialog<>(
                mw,
                CONNECTING,
                CONNECTING_ELLIPSIS
            );
            final WorkRunnable<LinkedList<String>> runnable =
                () -> callback.listCurrentTerms();
            final TaskDialog.TaskDialogCallback<LinkedList<String>> tdCallback =
                new TaskDialog.TaskDialogCallback<LinkedList<String>>(mw) {
                    @Override
                    public void workFinished(LinkedList<String> result) {
                        while (!result.isEmpty()) {
                            listModel.addElement(result.pop());
                        }
                        pack();
                        setLocationRelativeTo(null);
                        setVisible(true);
                    }

                    @Override
                    public void workFailed(Exception exception) {
                        super.workFailed(exception);
                        dispose();
                    }

                    @Override
                    public boolean cancelRequest() {
                        td.dispose();
                        dispose();
                        return true;
                    }
                };
            scroll.setPreferredSize(new Dimension(320, 240));
            list.setModel(listModel);
            backButton.addActionListener(this);
            finishTermButton.addActionListener(this);
            startNewTermButton.addActionListener(this);
            panel.setLayout(new BorderLayout());
            panel.setBorder(new EmptyBorder(10, 10, 5, 10));
            panel.add(scroll, BorderLayout.NORTH);
            panel.add(actionsPanel, BorderLayout.CENTER);
            getContentPane().add(panel);
            setTitle(mw.getStringResources().get(TERMS));
            td.setCallback(tdCallback);
            td.execute(runnable);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final Object src = e.getSource();
            final int selection = list.getSelectedIndex();
            if (src == backButton) {
                dispose();
                return;
            }
            try {
                if (src == startNewTermButton) {
                    final String name = AppDialog.showInput(mw, TERM_NAME);
                    if (name == null) {
                        AppDialog.showMessage(mw, ACTION_CANCELED);
                        return;
                    }
                    startNewTerm(name);
                    return;
                }
                if (src == finishTermButton && selection != -1) {
                    final String term = list.getSelectedValue();
                    final AppDialog.ConfirmResult option =
                        AppDialog.showConfirm(
                            mw,
                            FINISH_TERM_QUESTION
                        );
                    if (option == AppDialog.ConfirmResult.RESULT_OK) {
                        finishTerm(term);
                    }
                    return;
                }
            }
            catch (Exception ex) {
                AppDialog.showMessage(mw, ex.getMessage(), AppDialog.Type.FAIL);
            }
        }

        @Override
        public void onStartNewTerm(String termName, JSONArray careers)
            throws Exception {
            callback.startNewTerm(termName, careers);
            AppDialog.showMessage(mw, TERM_STARTED, AppDialog.Type.INFO);
            dispose();
        }

        private void startNewTerm(String name) {
            final class Holder {
                private final List<Item> campuses;
                private final List<Item> careers;

                private Holder(List<Item> campuses, List<Item> careers) {
                    this.campuses = campuses;
                    this.careers = careers;
                }
            }
            final TaskDialog<Holder> td = new TaskDialog<>(
                mw,
                CONNECTING,
                CONNECTING_ELLIPSIS
            );
            final WorkRunnable<Holder> runnable = () -> {
                final List<Item> campuses = callback.listCampusItems();
                final List<Item> careers = callback.listCareerItems();
                return new Holder(campuses, careers);
            };
            final TaskDialog.TaskDialogCallback<Holder> tdCallback =
                new TaskDialog.TaskDialogCallback<Holder>(mw) {
                    @Override
                    public void workFinished(Holder result) {
                        final List<Item> campuses = result.campuses;
                        final List<Item> careers = result.careers;
                        new NewTermCareerSelectorDialog(
                            mw,
                            name,
                            campuses,
                            careers,
                            TermDialog.this
                        );
                    }

                    @Override
                    public boolean cancelRequest() {
                        td.dispose();
                        return true;
                    }
                };
            td.setCallback(tdCallback);
            td.execute(runnable);
        }

        private void finishTerm(String term) {
            final TaskDialog<Void> td = new TaskDialog<>(
                mw,
                CONNECTING,
                FINISHING_TERM_ELLIPSIS,
                false
            );
            final WorkRunnable<Void> runnable = () -> {
                callback.finishTerm(term);
                return null;
            };
            final TaskDialog.TaskDialogCallback<Void> tdCallback =
                new TaskDialog.TaskDialogCallback<Void>(mw) {
                    @Override
                    public void workFinished(Void result) {
                        AppDialog.showMessage(
                            mw,
                            TERM_FINISHED,
                            AppDialog.Type.INFO
                        );
                        dispose();
                    }

                    @Override
                    public boolean cancelRequest() {
                        return false;
                    }
                };
            td.setCallback(tdCallback);
            td.execute(runnable);
        }
    }

    private static final class TermHistoryDialog extends Dialog implements ActionListener {
        private static final long serialVersionUID = 8048676012000895152L;
        private final MainWindow mw;
        private final Callback callback;
        private final DefaultListModel<String> listModel;
        private final ListPane<String> list;
        private final Button downloadAllButton;
        private final Button deleteButton;
        private final Button downloadButton;

        TermHistoryDialog(MainWindow mw, Callback callback) {
            super(mw);
            this.mw = mw;
            this.callback = callback;
            this.listModel = new DefaultListModel<String>();
            this.list = new ListPane<>(mw, listModel);
            this.downloadAllButton = new Button(mw, DOWNLOAD_ALL);
            this.deleteButton = new Button(mw, DELETE);
            this.downloadButton = new Button(mw, DOWNLOAD);
            final Panel panel = new Panel(mw);
            final Panel downloadAllPanel = new Panel(mw);
            final Panel actionsPanel = new Panel(mw);
            final Panel bottomPanel = new Panel(mw);
            final JScrollPane scroll = new JScrollPane(list);
            final TaskDialog<JSONArray> td = new TaskDialog<>(
                mw,
                CONNECTING,
                CONNECTING_ELLIPSIS
            );
            final WorkRunnable<JSONArray> runnable = () -> callback.listTerms();
            final TaskDialog.TaskDialogCallback<JSONArray> tdCallback =
                new TaskDialog.TaskDialogCallback<JSONArray>(mw) {
                    @Override
                    public void workFinished(JSONArray result) {
                        for (int i = 0; i < result.length(); i++) {
                            listModel.addElement(result.getString(i));
                        }
                        pack();
                        setLocationRelativeTo(null);
                        setVisible(true);
                    }

                    @Override
                    public void workFailed(Exception exception) {
                        AppDialog.showMessage(
                            mw,
                            exception.getMessage(),
                            AppDialog.Type.FAIL
                        );
                        dispose();
                    }

                    @Override
                    public boolean cancelRequest() {
                        td.dispose();
                        dispose();
                        return true;
                    }
                };
            downloadAllButton.addActionListener(this);
            deleteButton.addActionListener(this);
            downloadButton.addActionListener(this);
            scroll.setPreferredSize(new Dimension(320, 480));
            actionsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
            actionsPanel.add(deleteButton);
            actionsPanel.add(downloadButton);
            downloadAllPanel.add(downloadAllButton);
            bottomPanel.setLayout(new BorderLayout());
            bottomPanel.add(downloadAllPanel, BorderLayout.WEST);
            bottomPanel.add(actionsPanel, BorderLayout.EAST);
            panel.setLayout(new BorderLayout());
            panel.setBorder(new EmptyBorder(10, 10, 5, 10));
            panel.add(scroll, BorderLayout.NORTH);
            panel.add(bottomPanel, BorderLayout.CENTER);
            getContentPane().add(panel);
            setTitle(mw.getStringResources().get(TERMS_HISTORY));
            td.setCallback(tdCallback);
            td.execute(runnable);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final Object src = e.getSource();
            final int selection = list.getSelectedIndex();
            try {
                if (src == downloadAllButton) {
                    final File savingFile = getSavingFile(this);
                    String currentTermFile = null;
                    if (savingFile == null) {
                        return;
                    }
                    for (int i = 0; i < listModel.size(); i++) {
                        currentTermFile = listModel.getElementAt(i);
                        callback.downloadTermFile(currentTermFile, savingFile);
                    }
                    AppDialog.showMessage(mw, DOWNLOADED, AppDialog.Type.INFO);
                    dispose();
                }
                if (selection == -1) {
                    return;
                }
                if (src == downloadButton) {
                    final File savingFile = getSavingFile(this);
                    if (savingFile == null) {
                        return;
                    }
                    callback.downloadTermFile(
                        list.getSelectedValue(),
                        savingFile
                    );
                    AppDialog.showMessage(mw, DOWNLOADED, AppDialog.Type.INFO);
                }
                else if (src == deleteButton) {
                    final String file = list.getSelectedValue();
                    final String title = mw.getStringResources()
                                           .get(DELETE_FILE);
                    final String msg = DELETE_FILE_PERMANENTLY(
                        mw.getStringResources(),
                        file
                    );
                    final String okAction = mw.getStringResources().get(DELETE);
                    final AppDialog.ConfirmResult answer =
                        AppDialog.showConfirm(
                            mw,
                            title,
                            msg,
                            okAction
                        );
                    if (answer == AppDialog.ConfirmResult.RESULT_OK) {
                        callback.deleteTermFile(list.getSelectedValue());
                        AppDialog.showMessage(mw, DELETED, AppDialog.Type.INFO);
                        dispose();
                    }
                }
            }
            catch (Exception ex) {
                AppDialog.showMessage(mw, ex.getMessage(), AppDialog.Type.FAIL);
            }
        }

        private static File getSavingFile(Component parent) {
            final JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
                return fileChooser.getSelectedFile();
            }
            return null;
        }
    }

    // Professor or Student editor dialog
    private static final class Editor1Dialog extends Dialog implements ActionListener {
        private static final long serialVersionUID = 2229814863664014741L;

        private enum Type { PROFESSOR, STUDENT }

        private final MainWindow mw;
        private final String edit;
        private final Type editType;
        private final Callback callback;
        private final Button addButton;
        private final Button updateButton;
        private final Button deleteButton;
        private final Button passwordsButton;
        private final Button downloadButton;

        Editor1Dialog(MainWindow mw, StringResourceId edit, Callback callback) {
            super(mw);
            this.mw = mw;
            this.edit = mw.getStringResources().get(edit);
            this.editType = (edit == PROFESSORS)
                            ? Type.PROFESSOR
                            : Type.STUDENT;
            this.callback = callback;
            this.addButton = new Button(mw, REGISTER);
            this.updateButton = new Button(mw, UPDATE);
            this.deleteButton = new Button(mw, DELETE);
            this.passwordsButton = new Button(mw, SEND_PASSWORDS);
            this.downloadButton = new Button(mw, DOWNLOAD);
            final Panel panel = new Panel(mw);
            final Color buttonHover = Color.decode("#F0F0F0");
            final Color buttonPressed = Color.decode("#EAEAEA");
            addButton.setFocusable(false);
            addButton.setBackgroundColor(mw.getBackground());
            addButton.setHoverColor(buttonHover);
            addButton.setPressedColor(buttonPressed);
            addButton.setBold();
            addButton.addActionListener(this);
            updateButton.setFocusable(false);
            updateButton.setBackgroundColor(mw.getBackground());
            updateButton.setHoverColor(buttonHover);
            updateButton.setPressedColor(buttonPressed);
            updateButton.setBold();
            updateButton.addActionListener(this);
            deleteButton.setFocusable(false);
            deleteButton.setBackgroundColor(mw.getBackground());
            deleteButton.setHoverColor(buttonHover);
            deleteButton.setPressedColor(buttonPressed);
            deleteButton.setBold();
            deleteButton.addActionListener(this);
            passwordsButton.setFocusable(false);
            passwordsButton.setBackgroundColor(mw.getBackground());
            passwordsButton.setHoverColor(buttonHover);
            passwordsButton.setPressedColor(buttonPressed);
            passwordsButton.setBold();
            passwordsButton.addActionListener(this);
            downloadButton.setFocusable(false);
            downloadButton.setBackgroundColor(mw.getBackground());
            downloadButton.setHoverColor(buttonHover);
            downloadButton.setPressedColor(buttonPressed);
            downloadButton.setBold();
            downloadButton.addActionListener(this);
            panel.setLayout(new GridLayout(2, 3));
            panel.setPreferredSize(new Dimension(560, 120));
            panel.add(addButton);
            panel.add(updateButton);
            panel.add(deleteButton);
            panel.add(passwordsButton);
            panel.add(downloadButton);
            getContentPane().add(panel);
            pack();
            setIconImage(getToolkit().createImage("icons/ic_database.png"));
            setLocationRelativeTo(null);
            setTitle(mw.getStringResources().get(edit));
            setVisible(true);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final Object source = e.getSource();
            final JFileChooser fileChooser = new JFileChooser();
            if (source == downloadButton) {
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    final String name = (editType == Type.PROFESSOR)
                                        ? "Professors.xlsx"
                                        : "Students.xlsx";
                    final File file = new File(
                        fileChooser.getSelectedFile(),
                        name
                    );
                    download(file);
                }
                return;
            }
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setFileFilter(new FileFormatFilter(FileFormat.DOC_EXCEL_XLSX));
            if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            final File file = fileChooser.getSelectedFile();
            try (Workbook workbook = new XSSFWorkbook(file)) {
                final Sheet sheet = workbook.getSheetAt(0);
                trimedSheetSize(sheet);
                final int rows = sheet.getLastRowNum();
                if (rows < 1) {
                    AppDialog.showMessage(mw, NO_DATA);
                    return;
                }
                if (source == addButton) {
                    final String msg = DB_ADD(
                        mw.getStringResources(),
                        rows,
                        edit
                    );
                    final AppDialog.ConfirmResult result =
                        AppDialog.showConfirm(
                            mw,
                            msg
                        );
                    final Workbook passwordsWorkbook = new XSSFWorkbook();
                    if (result == AppDialog.ConfirmResult.RESULT_OK) {
                        registerOrUpdate(sheet, passwordsWorkbook, true);
                    }
                    return;
                }
                if (source == updateButton) {
                    final String msg = DB_UPDATE(
                        mw.getStringResources(),
                        rows,
                        edit
                    );
                    final AppDialog.ConfirmResult result =
                        AppDialog.showConfirm(
                            mw,
                            msg
                        );
                    final Workbook passwordsWorkbook = new XSSFWorkbook();
                    if (result == AppDialog.ConfirmResult.RESULT_OK) {
                        registerOrUpdate(sheet, passwordsWorkbook, false);
                    }
                    return;
                }
                if (source == passwordsButton) {
                    sendPasswords(sheet);
                    return;
                }
                if (source == deleteButton) {
                    final String msg = DB_DELETE(
                        mw.getStringResources(),
                        rows,
                        edit
                    );
                    final AppDialog.ConfirmResult result =
                        AppDialog.showConfirm(
                            mw,
                            msg
                        );
                    if (result == AppDialog.ConfirmResult.RESULT_OK) {
                        delete(sheet);
                    }
                    return;
                }
            }
            catch (Exception ex) {
                AppDialog.showMessage(mw, ex.getMessage(), AppDialog.Type.FAIL);
            }
        }

        private StringResourceId getRegisterOrUpdateMsg(boolean register) {
            if (editType == Type.PROFESSOR) {
                if (register) {
                    return REGISTERING_PROFESSORS;
                }
                return UPDATING_PROFESSORS;
            }
            if (register) {
                return REGISTERING_STUDENTS;
            }
            return UPDATING_STUDENTS;
        }

        private void download(File file) {
            final StringResourceId msgRes = (editType == Type.PROFESSOR)
                                            ? DOWNLOADING_PROFESSORS
                                            : DOWNLOADING_STUDENTS;
            final CancelFlag cancelFlag = new CancelFlag();
            final TaskDialog<Void> td = new TaskDialog<>(mw, DOWNLOAD, msgRes);
            final WorkRunnable<Void> runnable = () -> {
                if (editType == Type.PROFESSOR) {
                    callback.downloadProfessors(file, td, cancelFlag);
                }
                else {
                    callback.downloadStudents(file, td, cancelFlag);
                }
                return null;
            };
            final TaskDialog.TaskDialogCallback<Void> taskCallback =
                new TaskDialog.TaskDialogCallback<Void>(mw) {
                    @Override
                    public void workFinished(Void result) {
                        AppDialog.showMessage(
                            mw,
                            DOWNLOAD_COMPLETED,
                            AppDialog.Type.SUCCESS
                        );
                    }

                    @Override
                    public void workFailed(Exception exception) {
                        AppDialog.showMessage(
                            mw,
                            exception.getMessage(),
                            AppDialog.Type.FAIL
                        );
                    }

                    @Override
                    public boolean cancelRequest() {
                        cancelFlag.set(true);
                        return true;
                    }
                };
            td.getBarLoadingView()
              .setStyle(BarLoadingView.STYLE_INCREASING_BAR_SPEED);
            td.setCallback(taskCallback);
            td.execute(runnable);
        }

        private void registerOrUpdate(
            Sheet sheet,
            Workbook passwordsWorkbook,
            boolean register
        ) {
            final StringResourceId titleRes = (register)
                                              ? REGISTERING
                                              : UPDATING;
            final StringResourceId msgRes = getRegisterOrUpdateMsg(register);
            final int rows = sheet.getLastRowNum();
            final TaskDialog<Sheet> td = new TaskDialog<>(
                mw,
                titleRes,
                msgRes,
                false
            );
            final WorkRunnable<Sheet> runnable = () -> {
                final String sheetName;
                final Sheet passwordsSheet;
                if (editType == Type.PROFESSOR) {
                    sheetName = (register)
                                ? "PROFESSOR PASSWORDS"
                                : "PROFESSOR UPDATED PASSWORDS";
                    passwordsSheet =
                        passwordsWorkbook.createSheet(sheetName);
                    passwordsSheet.setColumnWidth(0, 12000);
                    passwordsSheet.setColumnWidth(1, 12000);
                    if (register) {
                        callback.registerProfessors(
                            sheet,
                            passwordsSheet,
                            td
                        );
                    }
                    else {
                        callback.updateProfessors(
                            sheet,
                            passwordsSheet,
                            td
                        );
                    }
                }
                else {
                    sheetName = (register)
                                ? "STUDENT PASSWORDS"
                                : "STUDENT UPDATED PASSWORDS";
                    passwordsSheet =
                        passwordsWorkbook.createSheet(sheetName);
                    passwordsSheet.setColumnWidth(0, 12000);
                    passwordsSheet.setColumnWidth(1, 12000);
                    if (register) {
                        callback.registerStudents(
                            sheet,
                            passwordsSheet,
                            td
                        );
                    }
                    else {
                        callback.updateStudents(sheet, passwordsSheet, td);
                    }
                }
                return passwordsSheet;
            };
            final TaskDialog.TaskDialogCallback<Sheet> taskCallback =
                new TaskDialog.TaskDialogCallback<Sheet>(mw) {
                    @Override
                    public void workFinished(Sheet passwordsSheet) {
                        if (!register && passwordsSheet.getPhysicalNumberOfRows() == 1) {
                            AppDialog.showMessage(
                                mw,
                                UPDATE_COMPLETED,
                                AppDialog.Type.SUCCESS
                            );
                            return;
                        }
                        final StringResourceId msgRes = (register)
                                                        ? REGISTRATION_FINISHED
                                                        : UPDATE_FINISHED;
                        final String pwdFileName;
                        AppDialog.showMessage(
                            mw,
                            SAVE_PASSWORDS,
                            AppDialog.Type.SUCCESS
                        );
                        if (editType == Type.PROFESSOR) {
                            pwdFileName = (register)
                                          ? "Professor passwords"
                                          : "Professor updated passwords";
                            savePasswords(passwordsWorkbook, pwdFileName);
                        }
                        else {
                            pwdFileName = (register)
                                          ? "Student passwords"
                                          : "Student updated passwords";
                            savePasswords(passwordsWorkbook, pwdFileName);
                        }
                        AppDialog.showMessage(
                            mw,
                            msgRes,
                            AppDialog.Type.SUCCESS
                        );
                    }

                    @Override
                    public void workFailed(Exception exception) {
                        AppDialog.showMessage(
                            mw,
                            exception.getMessage(),
                            AppDialog.Type.FAIL
                        );
                    }

                    @Override
                    public boolean cancelRequest() {
                        return false;
                    }
                };
            td.getBarLoadingView()
              .setStyle(BarLoadingView.STYLE_INCREASING_BAR_SPEED);
            td.setTaskSize(rows);
            td.setCallback(taskCallback);
            td.execute(runnable);
        }

        private void trimedSheetSize(Sheet sheet) {
            int i = sheet.getLastRowNum();
            boolean isEmptyRow;
            Row currentRow;
            do {
                currentRow = sheet.getRow(i);
                isEmptyRow = false;
                for (Cell cell : currentRow) {
                    if (cell == null || cell.getCellType() == CellType.BLANK) {
                        isEmptyRow = true;
                    }
                    else {
                        isEmptyRow = false;
                        break;
                    }
                }
                if (isEmptyRow) {
                    sheet.removeRow(currentRow);
                }
                i--;
            }
            while (isEmptyRow && i >= 0);
        }

        private void sendPasswords(Sheet sheet) {
            final StringResourceId msgRes = (editType == Type.PROFESSOR)
                                            ? SENDING_PWD_PROFESSORS
                                            : SENDING_PWD_STUDENTS;
            final int size = sheet.getPhysicalNumberOfRows() - 1;
            final CancelFlag cancelFlag = new CancelFlag();
            final TaskDialog<Void> td = new TaskDialog<>(
                mw,
                SENDING_PWD,
                msgRes
            );
            final WorkRunnable<Void> runnable = () -> {
                callback.sendPasswords(sheet, td, cancelFlag);
                return null;
            };
            final TaskDialog.TaskDialogCallback<Void> taskCallback =
                new TaskDialog.TaskDialogCallback<Void>(mw) {
                    @Override
                    public void workFinished(Void result) {
                        AppDialog.showMessage(
                            mw,
                            PASSWORDS_SENT,
                            AppDialog.Type.SUCCESS
                        );
                    }

                    @Override
                    public boolean cancelRequest() {
                        final AppDialog.ConfirmResult result =
                            AppDialog.showConfirm(
                                mw,
                                CANCEL,
                                CANCEL_SENDING_PWD,
                                STOP
                            );
                        if (result == AppDialog.ConfirmResult.RESULT_OK) {
                            td.setMessage(mw.getStringResources()
                                            .get(CANCELING));
                            cancelFlag.set(true);
                            return true;
                        }
                        return false;
                    }
                };
            td.getBarLoadingView()
              .setSpeed(BarLoadingView.BAR_WIDTH_CHANGE_PER_SECOND_SLOW);
            td.setTaskSize(size);
            td.setCallback(taskCallback);
            td.execute(runnable);
        }

        private void delete(Sheet sheet) {
            final StringResourceId msgRes = (editType == Type.PROFESSOR)
                                            ? DELETING_PROFESSORS
                                            : DELETING_STUDENTS;
            final int size = sheet.getPhysicalNumberOfRows() - 1;
            final TaskDialog<Void> td = new TaskDialog<>(
                mw,
                DELETE,
                msgRes,
                false
            );
            final WorkRunnable<Void> runnable = () -> {
                if (editType == Type.PROFESSOR) {
                    callback.deleteProfessors(sheet, td);
                }
                else {
                    callback.deleteStudents(sheet, td);
                }
                return null;
            };
            final TaskDialog.TaskDialogCallback<Void> taskCallback =
                new TaskDialog.TaskDialogCallback<Void>(mw) {
                    @Override
                    public void workFinished(Void result) {
                        AppDialog.showMessage(
                            mw,
                            DELETE_COMPLETED,
                            AppDialog.Type.SUCCESS
                        );
                    }

                    @Override
                    public void workFailed(Exception exception) {
                        AppDialog.showMessage(
                            mw,
                            exception.getMessage(),
                            AppDialog.Type.FAIL
                        );
                    }

                    @Override
                    public boolean cancelRequest() {
                        return false;
                    }
                };
            td.getBarLoadingView()
              .setStyle(BarLoadingView.STYLE_INCREASING_BAR_SPEED);
            td.setTaskSize(size);
            td.setCallback(taskCallback);
            td.execute(runnable);
        }

        private void savePasswords(Workbook workbook, String name) {
            final JFileChooser fileChooser = new JFileChooser();
            final LocalDateTime ldt = LocalDateTime.now();
            final StringBuilder dtBuilder = new StringBuilder();
            dtBuilder.append(" ");
            dtBuilder.append(ldt.getDayOfMonth());
            dtBuilder.append("-");
            dtBuilder.append(ldt.getMonthValue());
            dtBuilder.append("-");
            dtBuilder.append(ldt.getYear());
            dtBuilder.append("-");
            dtBuilder.append(ldt.getHour());
            dtBuilder.append("_");
            dtBuilder.append(ldt.getMinute());
            dtBuilder.append("_");
            dtBuilder.append(ldt.getSecond());
            dtBuilder.append(".xlsx");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                final String fileName = name + dtBuilder;
                final File file = new File(
                    fileChooser.getSelectedFile(),
                    fileName
                );
                try (final FileOutputStream out = new FileOutputStream(file)) {
                    workbook.write(out);
                }
                catch (IOException e) {
                    AppDialog.showMessage(
                        mw,
                        e.getMessage(),
                        AppDialog.Type.ERROR
                    );
                    savePasswords(workbook, name);
                }
            }
        }
    }

    // Campus or Career editor dialog
    private static final class Editor2Dialog extends Dialog implements ActionListener {
        private static final long serialVersionUID = 7169178428662745473L;
        private final MainWindow mw;
        private final List<EditingItem> items;
        private final Class<? extends EditingItem> type;
        private final Callback callback;
        private final DefaultListModel<EditingItem> listModel;
        private final ListPane<EditingItem> list;
        private final TextLabel editTitleLabel;
        private final InputText nameIT;
        private final InputText passwordIT;
        private final Button deleteButton;
        private final Button updateButton;
        private final Button createButton;
        private final Button saveChangesButton;
        private final Button cancelButton;

        Editor2Dialog(
            MainWindow mw,
            List<EditingItem> items,
            Class<? extends EditingItem> type,
            Callback callback
        ) {
            super(mw);
            this.mw = mw;
            this.items = items;
            this.type = type;
            this.callback = callback;
            this.listModel = new DefaultListModel<>();
            this.list = new ListPane<>(mw, listModel);
            this.editTitleLabel = new TextLabel(mw);
            this.nameIT = new InputText(mw, 20);
            this.passwordIT = new InputText(mw, 20);
            this.deleteButton = new Button(mw, DELETE);
            this.updateButton = new Button(mw, UPDATE);
            this.createButton = new Button(mw, CREATE);
            this.saveChangesButton = new Button(mw, SAVE);
            this.cancelButton = new Button(mw, CANCEL);
            final StringResources sr = mw.getStringResources();
            final Panel panel = new Panel(mw);
            final Panel editPanel = new Panel(mw);
            final TextLabel l1 = new TextLabel(mw, NAME);
            final TextLabel l2 = new TextLabel(mw, DB_NEW_PASSWORD);
            final JScrollPane scroll = new JScrollPane(list);
            final GridBagConstraints gbc = new GridBagConstraints();
            list.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    selectItem();
                }
            });
            updateList();
            scroll.setPreferredSize(new Dimension(240, 160));
            editTitleLabel.setText(" ");
            nameIT.setEnabled(false);
            nameIT.addActionListener(this);
            passwordIT.setEnabled(false);
            passwordIT.addActionListener(this);
            deleteButton.setEnabled(false);
            deleteButton.addActionListener(this);
            updateButton.setEnabled(false);
            updateButton.addActionListener(this);
            createButton.addActionListener(this);
            saveChangesButton.addActionListener(this);
            cancelButton.addActionListener(this);
            editPanel.setLayout(new GridBagLayout());
            editPanel.setBorder(new EmptyBorder(0, 15, 0, 0));
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            gbc.weightx = 2;
            gbc.insets.bottom = 5;
            editPanel.add(editTitleLabel, gbc);
            gbc.gridx = 0;
            gbc.gridy = 1;
            editPanel.add(l1, gbc);
            gbc.gridx = 0;
            gbc.gridy = 2;
            editPanel.add(nameIT, gbc);
            gbc.gridx = 0;
            gbc.gridy = 3;
            editPanel.add(l2, gbc);
            gbc.gridx = 0;
            gbc.gridy = 4;
            editPanel.add(passwordIT, gbc);
            gbc.gridx = 0;
            gbc.gridy = 5;
            gbc.gridwidth = 1;
            gbc.weightx = 1;
            editPanel.add(deleteButton, gbc);
            gbc.gridx = 1;
            gbc.gridy = 5;
            editPanel.add(updateButton, gbc);
            gbc.gridx = 0;
            gbc.gridy = 6;
            gbc.gridwidth = 2;
            gbc.weightx = 2;
            editPanel.add(createButton, gbc);
            gbc.gridx = 0;
            gbc.gridy = 7;
            gbc.gridwidth = 1;
            gbc.weightx = 1;
            editPanel.add(cancelButton, gbc);
            gbc.gridx = 1;
            gbc.gridy = 7;
            editPanel.add(saveChangesButton, gbc);
            panel.setLayout(new BorderLayout());
            panel.setBorder(new EmptyBorder(10, 10, 10, 10));
            panel.add(scroll, BorderLayout.WEST);
            panel.add(editPanel, BorderLayout.CENTER);
            getContentPane().add(panel);
            pack();
            setIconImage(getToolkit().createImage("icons/ic_database.png"));
            setLocationRelativeTo(null);
            setTitle((type == Campus.class)
                     ? sr.get(CAMPUSES)
                     : sr.get(CAREERS));
            setVisible(true);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final Component source = (Component) e.getSource();
            final EditingItem selection = list.getSelectedValue();
            if (source == nameIT) {
                passwordIT.requestFocus();
                return;
            }
            if (source == passwordIT) {
                updateButton.doClick();
                return;
            }
            if (source == cancelButton) {
                dispose();
                return;
            }
            if (source == createButton) {
                final EditingItem newItem = newItem();
                items.add(newItem);
                listModel.addElement(newItem);
                list.setSelectedValue(newItem, true);
                openEditPanel();
                nameIT.setText("");
                return;
            }
            if (source == saveChangesButton) {
                // Data validation
                for (EditingItem item : items) {
                    if (!validateItem(
                        item.isNew(),
                        item.getName(),
                        item.getPassword()
                    )) {
                        return;
                    }
                }
                // Ask for password before saving
                final String password = AppDialog.showPasswordInput(
                    mw,
                    TYPE_PASSWORD
                );
                if (password != null) {
                    final boolean isValid = callback.isValidUserPassword(
                        password);
                    if (isValid) {
                        save();
                    }
                    else {
                        AppDialog.showMessage(
                            mw,
                            INVALID_PASSWORD,
                            AppDialog.Type.WARNING
                        );
                    }
                }
                return;
            }
            if (selection == null) {
                return;
            }
            if (source == deleteButton) {
                selection.setDeleted();
                listModel.removeElementAt(list.getSelectedIndex());
                // If It's new, don't let it stay in the list since is not in
                // the DB
                if (selection.isNew()) {
                    items.remove(selection);
                }
            }
            else if (source == updateButton) {
                // Data validation
                // Null value will not update the password
                final String itemPassword = (passwordIT.getText().isEmpty())
                                            ? null
                                            : passwordIT.getText();
                if (!validateItem(
                    selection.isNew(),
                    nameIT.getText(),
                    itemPassword
                )) {
                    return;
                }
                // Check for duplicates
                for (EditingItem item : items) {
                    if (item != selection
                        && !item.isDeleted()
                        && item.getName().equals(nameIT.getText())) {
                        AppDialog.showMessage(mw, DUPLICATED_FIELD);
                        return;
                    }
                }
                selection.setName(nameIT.getText());
                // If the TF is empty the password is not added
                if (!passwordIT.getText().trim().isEmpty()) {
                    selection.setPassword(passwordIT.getText());
                }
                updateList();
            }
            closeEditPanel();
        }

        private void updateList() {
            listModel.clear();
            for (EditingItem item : items) {
                if (!item.isDeleted()) {
                    listModel.addElement(item);
                }
            }
        }

        private void save() {
            final StringResourceId msgRes = (type == Campus.class)
                                            ? UPDATING_CAMPUSES
                                            : UPDATING_CAREERS;
            final TaskDialog<Void> td = new TaskDialog<>(
                mw,
                UPDATE,
                msgRes,
                false
            );
            final WorkRunnable<Void> runnable = () -> {
                if (type == Campus.class) {
                    callback.updateCampuses(items);
                }
                else {
                    callback.updateCareers(items);
                }
                return null;
            };
            final TaskDialog.TaskDialogCallback<Void> taskCallback =
                new TaskDialog.TaskDialogCallback<Void>(mw) {
                    @Override
                    public void workFinished(Void result) {
                        AppDialog.showMessage(
                            mw,
                            SUCCESS_TO_SAVE,
                            AppDialog.Type.SUCCESS
                        );
                        dispose();
                    }

                    @Override
                    public void workFailed(Exception exception) {
                        AppDialog.showMessage(
                            mw,
                            exception.getMessage(),
                            AppDialog.Type.FAIL
                        );
                    }

                    @Override
                    public boolean cancelRequest() {
                        return false;
                    }
                };
            td.getBarLoadingView()
              .setStyle(BarLoadingView.STYLE_INCREASING_BAR_SPEED);
            td.setCallback(taskCallback);
            td.execute(runnable);
        }

        private void selectItem() {
            if (list.getSelectedIndex() == -1) {
                return;
            }
            final EditingItem selection = list.getSelectedValue();
            saveChangesButton.setEnabled(false);
            createButton.setEnabled(false);
            editTitleLabel.setText(selection.getName());
            nameIT.setText(selection.getName());
            passwordIT.setText(selection.getPassword());
            nameIT.setEnabled(true);
            passwordIT.setEnabled(true);
            deleteButton.setEnabled(true);
            updateButton.setEnabled(true);
        }

        private void openEditPanel() {
            editTitleLabel.setText(list.getSelectedValue().getName());
            createButton.setEnabled(false);
            saveChangesButton.setEnabled(false);
            nameIT.setEnabled(true);
            passwordIT.setEnabled(true);
            deleteButton.setEnabled(true);
            updateButton.setEnabled(true);
            nameIT.requestFocus();
        }

        private void closeEditPanel() {
            nameIT.setEnabled(false);
            passwordIT.setEnabled(false);
            deleteButton.setEnabled(false);
            updateButton.setEnabled(false);
            createButton.setEnabled(true);
            saveChangesButton.setEnabled(true);
            editTitleLabel.setText(" ");
            nameIT.setText("");
            passwordIT.setText("");
            list.setSelectedIndex(-1);
        }

        private EditingItem newItem() {
            return (type == Campus.class) ? new Campus(true, null, "Campus")
                                          : new Career(true, null, "Career");
        }

        private boolean isValidName(String name) {
            return name.trim().length() >= 2;
        }

        private boolean isValidPassword(String password) {
            // return PasswordProtocol.isAcceptedPassword(password);
            return true;
        }

        private boolean isValidNewItem(String name, String password) {
            return isValidName(name) && password != null && isValidPassword(
                password);
        }

        private boolean validateItem(
            boolean isNew,
            String name,
            String password
        ) {
            if (isNew && !isValidNewItem(name, password)) {
                final String html = "Minimum length for name is 2.<br>For "
                                    + "password"
                                    + ".<br>Check " + name;
                final String msg = Style.wrapDialogTextInHTML(
                    mw.getAppStyle(),
                    html
                );
                AppDialog.showMessage(mw, msg);
                return false;
            }
            else {
                if (!isValidName(name)) {
                    final String msg =
                        "Minimun length for name is 2. Check " + name;
                    AppDialog.showMessage(mw, msg);
                    return false;
                }
                else if (password != null && !isValidPassword(password)) {
                    final String html = ".<br>Check " + name;
                    final String msg = Style.wrapDialogTextInHTML(
                        mw.getAppStyle(),
                        html
                    );
                    AppDialog.showMessage(mw, msg);
                    return false;
                }
            }
            return true;
        }
    }

    private static final class Files {
        static void copy(File sourceLocation, File targetLocation)
            throws IOException {
            if (sourceLocation.isDirectory()) {
                copyDirectory(sourceLocation, targetLocation);
            }
            else {
                copyFile(sourceLocation, targetLocation);
            }
        }

        private static void copyDirectory(File source, File target)
            throws IOException {
            if (!target.exists()) {
                target.mkdir();
            }
            for (String f : source.list()) {
                copy(new File(source, f), new File(target, f));
            }
        }

        private static void copyFile(File source, File target)
            throws IOException {
            try (
                final InputStream is = new FileInputStream(source);
                final OutputStream os = new FileOutputStream(target)
            ) {
                final byte[] buf = new byte[1024];
                int length;
                while ((length = is.read(buf)) > 0) {
                    os.write(buf, 0, length);
                }
            }
        }
    }
}
