/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.sm.ui;

import dev.tobiasbriones.poslans.sm.Main;
import dev.tobiasbriones.poslans.sm.career.CareerDataHolder;
import dev.tobiasbriones.poslans.sm.career.Class;
import dev.tobiasbriones.poslans.sm.career.Classroom;
import dev.tobiasbriones.poslans.sm.career.Professor;
import dev.tobiasbriones.poslans.sm.current.HistoryItem;
import dev.tobiasbriones.poslans.sm.current.ProfessorAcademicLoad;
import dev.tobiasbriones.poslans.sm.current.Section;
import dev.tobiasbriones.poslans.sm.current.Time;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public final class MainWindow extends JFrame implements R,
                                                        Strings,
                                                        ActionListener,
                                                        MouseListener,
                                                        SectionDialog.Callback,
                                                        HistoryDialog.Callback {
    private static final long serialVersionUID = -7050635316113527857L;
    private static final Dimension FRAME_SIZE = new Dimension(520, 600);
    private static final Dimension TOOLBAR_SIZE = new Dimension(
        FRAME_SIZE.width,
        34
    );
    private static final Dimension TOP_PANEL_SIZE = new Dimension(
        FRAME_SIZE.width,
        38
    );
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private static final Color TOP_BACKGROUND_COLOR = new Color(255, 255, 255);
    private static final Color PRESSED_COLOR = new Color(210, 210, 210);

    public interface Callback {
        CareerDataDialog.Callback getCareerDataDialogCallback();

        List<String> getFilter();

        void setFilter(List<String> filter);

        List<HistoryItem> getHistory();

        HashMap<String, String> getClassroomsTakenHours();

        Enumeration<ProfessorAcademicLoad> getProfessorsLoad();

        Iterator<Section> listSections();

        void createNewTerm(String name);

        void saveToHistory();

        String openSection(
            Class course, Professor professor,
            Classroom classroom, Time time, int[] days
        );

        String editSection(
            int position, Class course, Professor professor,
            Classroom classroom, Time time, int[] days
        );

        void deleteSection(int position);

        void setNoFilter();
    }

    private final CareerDataHolder careerData;
    private final Callback callback;
    private final JList<Section> list;
    private final DefaultListModel<Section> listModel;
    private final JLabel titleLabel;
    private final JLabel termLabel;
    private final JPopupMenu popup;
    private Section popupTarget;

    public MainWindow(CareerDataHolder info, Callback callback) {
        super(Strings.APP_NAME);
        this.careerData = info;
        this.callback = callback;
        this.list = new JList<>();
        this.listModel = new DefaultListModel<>();
        this.titleLabel = new JLabel();
        this.termLabel = new JLabel();
        this.popup = new JPopupMenu();
        this.popupTarget = null;
        final JPanel panel = new JPanel();
        final JToolBar toolBar = new JToolBar();
        final JButton openSectionButton = new JButton(new ImageIcon(
            Main.getIconPath("ic_add.png")));
        final JButton sectionsInfoButton = new JButton(new ImageIcon(
            Main.getIconPath("ic_sections_info.png")));
        final JButton classroomsInfoButton = new JButton(new ImageIcon(
            Main.getIconPath("ic_classroom.png")));
        final JButton professorsButton = new JButton(new ImageIcon(
            Main.getIconPath("ic_professors.png")));
        final JButton historyButton = new JButton(new ImageIcon(
            Main.getIconPath("ic_history.png")));
        final JButton filterButton = new JButton(new ImageIcon(
            Main.getIconPath("ic_filter.png")));
        final JButton careerDataButton = new JButton(new ImageIcon(
            Main.getIconPath("ic_career_data.png")));
        final JButton aboutButton = new JButton(new ImageIcon(
            Main.getIconPath("ic_about.png")));
        final JButton newTermButton = new JButton();
        final JPanel topPanel = new JPanel();
        final JScrollPane scrollPane = new JScrollPane(list);
        final JMenuItem menuEdit = new JMenuItem(Strings.EDIT);
        final JMenuItem menuDelete = new JMenuItem(Strings.DELETE);
        // Set popup
        menuEdit.setName(R.MW_MENU_EDIT);
        menuEdit.addActionListener(this);
        menuDelete.setName(R.MW_MENU_DELETE);
        menuDelete.addActionListener(this);
        popup.add(menuEdit);
        popup.add(menuDelete);
        // ToolBar
        openSectionButton.setName(R.MW_TOOLBAR_OPEN_SECTION);
        openSectionButton.setToolTipText(Strings.OPEN_SECTION_TIP);
        openSectionButton.setBackground(TOP_BACKGROUND_COLOR);
        openSectionButton.addActionListener(this);
        sectionsInfoButton.setName(R.MW_TOOLBAR_SECTIONS_INFO);
        sectionsInfoButton.setToolTipText(Strings.SECTIONS_INFO_TIP);
        sectionsInfoButton.setBackground(TOP_BACKGROUND_COLOR);
        sectionsInfoButton.addActionListener(this);
        classroomsInfoButton.setName(R.MW_TOOLBAR_CLASSROOMS_INFO);
        classroomsInfoButton.setToolTipText(Strings.CLASSROOMS_INFO_TIP);
        classroomsInfoButton.setBackground(TOP_BACKGROUND_COLOR);
        classroomsInfoButton.addActionListener(this);
        professorsButton.setName(R.MW_TOOLBAR_PROFESSORS);
        professorsButton.setToolTipText(Strings.PROFESSORS_TIP);
        professorsButton.setBackground(TOP_BACKGROUND_COLOR);
        professorsButton.addActionListener(this);
        historyButton.setName(R.MW_TOOLBAR_HISTORY);
        historyButton.setToolTipText(Strings.HISTORY_TIP);
        historyButton.setBackground(TOP_BACKGROUND_COLOR);
        historyButton.addActionListener(this);
        filterButton.setName(R.MW_TOOLBAR_FILTER);
        filterButton.setToolTipText(Strings.FILTER_TIP);
        filterButton.setBackground(TOP_BACKGROUND_COLOR);
        filterButton.addActionListener(this);
        careerDataButton.setName(R.MW_TOOLBAR_CAREER_DATA);
        careerDataButton.setToolTipText(Strings.CAREER_DATA_TIP);
        careerDataButton.setBackground(TOP_BACKGROUND_COLOR);
        careerDataButton.addActionListener(this);
        aboutButton.setName(R.MW_TOOLBAR_ABOUT);
        aboutButton.setToolTipText(Strings.ABOUT_TIP);
        aboutButton.setBackground(TOP_BACKGROUND_COLOR);
        aboutButton.addActionListener(this);
        toolBar.setMaximumSize(TOOLBAR_SIZE);
        toolBar.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
        toolBar.setFloatable(false);
        toolBar.setBackground(TOP_BACKGROUND_COLOR);
        toolBar.add(openSectionButton);
        toolBar.add(sectionsInfoButton);
        toolBar.add(classroomsInfoButton);
        toolBar.add(professorsButton);
        toolBar.add(historyButton);
        toolBar.add(filterButton);
        toolBar.add(careerDataButton);
        toolBar.add(aboutButton);
        // Top panel
        newTermButton.setName(R.MW_NEW_TERM);
        newTermButton.setText("<html><span style='font-family:Roboto;'>"
                              + Strings.NEW_TERM.toUpperCase() + "</span></html>");
        newTermButton.addActionListener(this);
        topPanel.setMaximumSize(TOP_PANEL_SIZE);
        topPanel.setLayout(new BorderLayout());
        topPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        topPanel.setBackground(TOP_BACKGROUND_COLOR);
        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(newTermButton, BorderLayout.EAST);
        topPanel.add(termLabel, BorderLayout.SOUTH);
        // Sections list
        list.setModel(listModel);
        list.setCellRenderer(new Row());
        list.setBackground(BACKGROUND_COLOR);
        list.addMouseListener(this);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);
        // Panel
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BACKGROUND_COLOR);
        panel.add(toolBar);
        panel.add(topPanel);
        panel.add(scrollPane);
        getContentPane().add(panel);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setSize(FRAME_SIZE);
        setLocationRelativeTo(null);
        setIconImage(Toolkit.getDefaultToolkit()
                            .getImage(Main.getIconPath("ic_launcher.png")));
        setVisible(true);
    }

    public void setTerm(String term) {
        termLabel.setText(
            "<html><span style='font-family:Roboto Light;font-weight:400;"
            + "font-size:10px;'>"
            + term + "</span></html>");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final String name = ((Component) e.getSource()).getName();
        switch (name) {
            case R.MW_TOOLBAR_OPEN_SECTION:
                new SectionDialog(this, careerData);
                break;
            case R.MW_TOOLBAR_SECTIONS_INFO:
                new SectionsInfoDialog(this, callback.listSections());
                break;
            case R.MW_TOOLBAR_CLASSROOMS_INFO:
                new ClassroomsInfoDialog(
                    this,
                    callback.getClassroomsTakenHours()
                );
                break;
            case R.MW_TOOLBAR_PROFESSORS:
                new ProfessorsLoadDialog(this, callback.getProfessorsLoad());
                break;
            case R.MW_TOOLBAR_HISTORY:
                new HistoryDialog(this, callback.getHistory());
                break;
            case R.MW_TOOLBAR_FILTER:
                new FilterDialog(this, callback);
                break;
            case R.MW_TOOLBAR_CAREER_DATA:
                new CareerDataDialog(
                    this,
                    careerData,
                    callback.getCareerDataDialogCallback()
                );
                break;
            case R.MW_TOOLBAR_ABOUT:
                final int aboutType = JOptionPane.INFORMATION_MESSAGE;
                final Icon icon = new ImageIcon("icons/ic_about.png");
                JOptionPane.showMessageDialog(
                    this,
                    Strings.ABOUT_MSG,
                    Strings.ABOUT,
                    aboutType,
                    icon
                );
                break;
            case R.MW_NEW_TERM:
                final int termType = JOptionPane.YES_NO_OPTION;
                final int msgType = JOptionPane.QUESTION_MESSAGE;
                final int option = JOptionPane.showConfirmDialog(
                    this,
                    Strings.NEW_TERM_MSG,
                    Strings.NEW_TERM,
                    termType,
                    msgType
                );
                if (option == JOptionPane.YES_OPTION) {
                    final String newTermName = JOptionPane.showInputDialog(
                        this,
                        Strings.SET_TERM_NAME
                    );
                    if (newTermName != null && !newTermName.isEmpty()) {
                        callback.createNewTerm(newTermName);
                    }
                    else {
                        JOptionPane.showMessageDialog(this,
                                                      Strings.ACTION_CANCELLED
                        );
                    }
                }
                break;
            case R.MW_MENU_EDIT:
                new SectionDialog(this, careerData, popupTarget);
                popupTarget = null;
                break;
            case R.MW_MENU_DELETE:
                final int delete = list.getSelectedIndex();
                if (delete != -1) {
                    callback.deleteSection(delete);
                }
                break;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        checkPopupMenu(e);
        if (e.getClickCount() == 2) {
            new SectionDialog(this, careerData, list.getSelectedValue());
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        checkPopupMenu(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        checkPopupMenu(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        checkPopupMenu(e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        checkPopupMenu(e);
    }

    // Returns null from Main when there's no overlap so the dialog can be
    // closed
    @Override
    public String openSection(
        int classValue, int professorValue, int classroomValue,
        Time time, int[] days
    ) {
        final Class course = careerData.getClasses().get(classValue);
        final Professor professor = careerData.getProfessors()
                                              .get(professorValue);
        final Classroom classroom = careerData.getClassrooms()
                                              .get(classroomValue);
        if (!checkSection(course, professorValue, classroomValue, days)) {
            return null;
        }
        return callback.openSection(course, professor, classroom, time, days);
    }

    @Override
    public String editSection(
        int classValue, int professorValue, int classroomValue,
        Time time, int[] days
    ) {
        final Class course = careerData.getClasses().get(classValue);
        final Professor professor = careerData.getProfessors()
                                              .get(professorValue);
        final Classroom classroom = careerData.getClassrooms()
                                              .get(classroomValue);
        if (!checkSection(course, professorValue, classroomValue, days)) {
            return null;
        }
        return callback.editSection(
            list.getSelectedIndex(),
            course,
            professor,
            classroom,
            time,
            days
        );
    }

    @Override
    public void saveToHistory() {
        callback.saveToHistory();
    }

    public void setTitle(String title) {
        titleLabel.setText(
            "<html><span style='font-family:Roboto Light;font-weight:400;"
            + "font-size:10px;'>"
            + title + "</span></html>");
    }

    public void welcome() {
        final String msg = Strings.WELCOME_MSG;
        final String title = Strings.GETTING_STARTED;
        final int optionType = JOptionPane.OK_CANCEL_OPTION;
        final int msgType = JOptionPane.INFORMATION_MESSAGE;
        final int option = JOptionPane.showConfirmDialog(
            this,
            msg,
            title,
            optionType,
            msgType
        );
        if (option == JOptionPane.OK_OPTION) {
            new CareerDataDialog(
                this,
                careerData,
                callback.getCareerDataDialogCallback()
            );
        }
    }

    public void addSection(Section section) {
        listModel.addElement(section);
    }

    public void clear() {
        popupTarget = null;
        listModel.clear();
    }

    private void checkPopupMenu(MouseEvent e) {
        if (e.isPopupTrigger()) {
            list.setSelectedIndex(list.locationToIndex(e.getPoint()));
            popup.show(list, e.getX(), e.getY());
            popupTarget = list.getSelectedValue();
        }
    }

    private boolean checkSection(
        Class course,
        int professorValue,
        int classroomValue,
        int[] days
    ) {
        // Class required days validation
        if (course.getDaysPerWeek() != days.length) {
            final String msg =
                course + " requires " + course.getDaysPerWeek() +
                " days";
            JOptionPane.showMessageDialog(
                this,
                msg,
                Strings.CHECK_YOUR_INPUT,
                JOptionPane.WARNING_MESSAGE
            );
            return false;
        }
        return true;
    }

    private static final class Row extends JPanel implements ListCellRenderer<Section> {
        private static final long serialVersionUID = -4318250611361812955L;
        private static final Border BORDER = new EmptyBorder(10, 20, 10, 20);
        private final JPanel panelLeft;
        private final JLabel labelLeft0;
        private final JLabel labelLeft1;
        private final JLabel labelLeft2;
        private final JLabel labelRight;

        Row() {
            this.panelLeft = new JPanel();
            this.labelLeft0 = new JLabel();
            this.labelLeft1 = new JLabel();
            this.labelLeft2 = new JLabel();
            this.labelRight = new JLabel();
            panelLeft.setLayout(new GridLayout(3, 1));
            panelLeft.setOpaque(true);
            panelLeft.setBackground(BACKGROUND_COLOR);
            panelLeft.add(labelLeft0);
            panelLeft.add(labelLeft1);
            panelLeft.add(labelLeft2);
            setLayout(new BorderLayout());
            setBorder(BORDER);
            setBackground(BACKGROUND_COLOR);
            add(panelLeft, BorderLayout.WEST);
            add(labelRight, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(
            JList<? extends Section> list, Section section,
            int index, boolean isSelected, boolean cellHasFocus
        ) {
            final String a = "<html><span style='font-family:Roboto;"
                             + "font-size:12px;'>";
            final String b = "<html><span style='font-family:Roboto Light;"
                             + "font-size:10px;'>";
            final String c = "<html><span style='font-family:Roboto;"
                             + "font-size:10px;color:#737373;'>";
            labelLeft0.setText(a + section.getSectionClass()
                                          .toString() + "</span></html>");
            labelLeft1.setText(b + section.getClassroom().toString()
                               + "<span style='font-family:Roboto;"
                               + "font-style:italic;color:#737373;'>	"
                               + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + section.getMiniDays()
                               + " &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
                               + "</span></span></html>");
            labelLeft2.setText(b + section.getProfessor()
                                          .toString() + "</span></html>");
            labelRight.setText(c + section.getTime()
                                          .toString() + "</span></html>");
            if (isSelected) {
                setBackground(PRESSED_COLOR);
                panelLeft.setBackground(PRESSED_COLOR);
            }
            else {
                setBackground(BACKGROUND_COLOR);
                panelLeft.setBackground(BACKGROUND_COLOR);
            }
            return this;
        }
    }

    private static final class FilterDialog extends JDialog implements ActionListener {

        private static final long serialVersionUID = 8131151297294945556L;
        private final Callback callback;
        private final JList<String> list;

        FilterDialog(MainWindow mw, Callback callback) {
            super(mw, Strings.FILTER_TIP);
            this.callback = callback;
            this.list = new JList<>();
            final JPanel panel = new JPanel();
            final DefaultListModel<String> listModel = new DefaultListModel<>();
            final JPanel actionsPanel = new JPanel();
            final JButton discardButton = new JButton(Strings.DISCARD.toUpperCase());
            final JButton noFilterButton = new JButton(Strings.NO_FILTER.toUpperCase());
            final JButton saveButton = new JButton(Strings.SAVE.toUpperCase());
            final List<String> filter = callback.getFilter();
            final int[] selectedIndices = new int[filter.size()];
            int i = 0;
            int k = 0;
            list.setModel(listModel);
            list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            for (String specialization : callback.getCareerDataDialogCallback()
                                                 .getProfessorSpecializations()) {
                if (filter.contains(specialization)) {
                    selectedIndices[k] = i;
                    k++;
                }
                listModel.addElement(specialization);
                i++;
            }
            list.setSelectedIndices(selectedIndices);
            discardButton.setName("discard");
            discardButton.addActionListener(this);
            noFilterButton.setName("nofilter");
            noFilterButton.addActionListener(this);
            saveButton.setName("save");
            saveButton.addActionListener(this);
            // Actions panel
            actionsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
            actionsPanel.add(discardButton);
            actionsPanel.add(noFilterButton);
            actionsPanel.add(saveButton);
            // Panel
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(new EmptyBorder(5, 15, 0, 15));
            panel.add(new JScrollPane(list));
            panel.add(actionsPanel);
            getContentPane().add(panel);
            pack();
            setResizable(false);
            setLocationRelativeTo(null);
            setModalityType(ModalityType.APPLICATION_MODAL);
            setIconImage(Toolkit.getDefaultToolkit()
                                .getImage("icons/ic_filter.png"));
            setVisible(true);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final String name = ((Component) e.getSource()).getName();
            if (name.equals("nofilter")) {
                callback.setNoFilter();
            }
            else if (name.equals("save")) {
                callback.setFilter(list.getSelectedValuesList());
            }
            dispose();
        }
    }
}
