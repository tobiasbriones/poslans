/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.mu.ui;

import dev.tobiasbriones.poslans.mu.Strings;
import dev.tobiasbriones.poslans.mu.models.career.Course;
import dev.tobiasbriones.poslans.mu.models.career.curriculum.CareerCurriculum;
import dev.tobiasbriones.poslans.mu.models.career.curriculum.CourseNode;
import engineer.mathsoftware.jdesk.App;
import engineer.mathsoftware.jdesk.AppInstance;
import engineer.mathsoftware.jdesk.Window;
import engineer.mathsoftware.jdesk.resources.StringResourceId;
import engineer.mathsoftware.jdesk.resources.StringResources;
import engineer.mathsoftware.jdesk.ui.view.Button;
import engineer.mathsoftware.jdesk.ui.view.Panel;
import engineer.mathsoftware.jdesk.ui.view.ScrollPane;
import engineer.mathsoftware.jdesk.ui.view.*;
import engineer.mathsoftware.jdesk.ui.view.loading.CircularCirclesLoadingView;
import engineer.mathsoftware.jdesk.work.AppWorkCallback;
import engineer.mathsoftware.jdesk.work.AppWorker;
import engineer.mathsoftware.jdesk.work.WorkCallback;
import engineer.mathsoftware.jdesk.work.WorkRunnable;

import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.Map;

import static dev.tobiasbriones.poslans.mu.Strings.*;
import static dev.tobiasbriones.poslans.mu.Strings.CREDITS;
import static dev.tobiasbriones.poslans.mu.Strings.NAME;

public final class CareerCurriculumWindow extends Window implements ClickListener {
    private static final long serialVersionUID = -9134554299437532517L;
    private static final Dimension MAX_SIZE = new Dimension(720, 600);
    private static final Color TEXT_COLOR = Color.decode("#141414");

    public interface Controller {
        Map<String, Course> getCourses();

        void load() throws SQLException;

        CareerCurriculum retrieveCurriculum() throws SQLException;

        void saveCurriculum() throws SQLException;
    }

    private final Window ccw;
    private final Controller controller;
    private final WindowDialogCallback wdc;
    private final Panel curriculumPanel;
    private final CircularCirclesLoadingView tlv;
    private final Font italicFont;
    private final Font boldFontSmall;
    private final Font boldFont;
    private final ScrollPane scroll;
    private CareerCurriculum curriculum;

    public CareerCurriculumWindow(App app, Window ccw, Controller controller) {
        super((AppInstance) app);
        this.ccw = ccw;
        this.controller = controller;
        this.wdc = new WindowDialogCallback(controller);
        this.curriculumPanel = new Panel(this);
        this.tlv = new CircularCirclesLoadingView(this);
        this.italicFont = getAppStyle().getFont().deriveFont(Font.ITALIC);
        this.boldFontSmall = getAppStyle().getFont().deriveFont(Font.BOLD, 10);
        this.boldFont = getAppStyle().getFont().deriveFont(Font.BOLD);
        this.scroll = new ScrollPane(this);
        this.curriculum = null;
        setResizable(false);
        setIcon("ic_curriculum");
    }

    WindowDialogCallback getWDC() {
        return wdc;
    }

    @Override
    public void paint(Graphics g) {
        final Dimension size = getSize();
        if (size.width > MAX_SIZE.width || size.height > MAX_SIZE.height) {
            if (size.width > MAX_SIZE.width && !(size.height > MAX_SIZE.height)) {
                scroll.setHorizontalScrollBarPolicy(ScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                setSize(new Dimension(MAX_SIZE.width, size.height));
            }
            else if (size.height > MAX_SIZE.height && !(size.width > MAX_SIZE.width)) {
                setSize(new Dimension(size.width, MAX_SIZE.height));
            }
            else {
                scroll.setHorizontalScrollBarPolicy(ScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                setSize(MAX_SIZE);
            }
            setLocationRelativeTo(null);
        }
        super.paint(g);
    }

    @Override
    public void onClick(Object view, StringResourceId viewTextId) {
        final Map<String, Course> courses = controller.getCourses();
        new CareerCurriculumEditorDialog(this, curriculum, courses);
    }

    @Override
    protected void createWindow(Panel panel) {
        final StringResources sr = getStringResources();
        final String[] columns = {
            sr.get(CODE),
            sr.get(NAME),
            sr.get(CREDITS),
            sr.get(REQUIREMENTS)
        };
        final Button editButton = new Button(this, EDIT);
        final GridBagConstraints gbc = new GridBagConstraints();
        TextLabel label;

        scroll.setHorizontalScrollBarPolicy(ScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        curriculumPanel.setLayout(new GridBagLayout());
        curriculumPanel.setPadding(0, 15, 0, 15);
        curriculumPanel.setBackground(Color.WHITE);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.ipadx = 24;
        gbc.ipady = 4;
        label = new TextLabel(this, columns[0]);
        label.setFont(boldFont);
        gbc.gridx = 1;
        gbc.gridy = 0;
        curriculumPanel.add(label, gbc);
        label = new TextLabel(this, columns[1]);
        label.setFont(boldFont);
        gbc.gridx = 2;
        gbc.gridy = 0;
        curriculumPanel.add(label, gbc);
        label = new TextLabel(this, columns[2]);
        label.setFont(boldFont);
        gbc.gridx = 3;
        gbc.gridy = 0;
        curriculumPanel.add(label, gbc);
        label = new TextLabel(this, columns[3]);
        label.setFont(boldFont);
        gbc.gridx = 4;
        gbc.gridy = 0;
        curriculumPanel.add(label, gbc);
        scroll.setViewportView(curriculumPanel);
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(Color.WHITE);
        panel.add(editButton, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        editButton.setBackground(Color.WHITE);
        editButton.setFocusable(false);
    }

    @Override
    protected void windowCreated() {
        ccw.setVisible(false);
        setVisible(true);
        load();
    }

    @Override
    protected void windowDetached() {
        ccw.setVisible(true);
    }

    private void finishLoopWork(
        Panel curriculumPanel,
        CourseNode node,
        GridBagConstraints gbc
    ) {
        final Course course = node.getCourse();
        TextLabel label = new TextLabel(this, course.getCode());

        label.setFont(boldFontSmall);
        label.setForeground(TEXT_COLOR);
        gbc.gridx = 1;
        curriculumPanel.add(label, gbc);
        label = new TextLabel(this, course.getName());
        label.setForeground(TEXT_COLOR);
        gbc.gridx = 2;
        curriculumPanel.add(label, gbc);
        label = new TextLabel(this, String.valueOf(course.getCredits()));
        label.setForeground(TEXT_COLOR);
        gbc.gridx = 3;
        curriculumPanel.add(label, gbc);
        if (node.isTopLevel()) {
            label = new TextLabel(this, "-");
        }
        else {
            label = new TextLabel(this, node.getRequirementCodes());
        }
        label.setFont(italicFont);
        label.setForeground(TEXT_COLOR);
        gbc.gridx = 4;
        curriculumPanel.add(label, gbc);
        gbc.gridy++;
    }

    private void load() {
        final WorkRunnable<CareerCurriculum> runnable = () -> {
            controller.load();
            return controller.retrieveCurriculum();
        };
        final WorkCallback<CareerCurriculum> atc =
            new AppWorkCallback<CareerCurriculum>(this) {
                @Override
                public void workFinished(CareerCurriculum curriculum) {
                    System.out.println(curriculum);
                    final CareerCurriculumWindow window =
                        CareerCurriculumWindow.this;
                    final GridBagConstraints gbc = new GridBagConstraints();
                    int currentLevel = -1;
                    TextLabel label;
                    window.curriculum = curriculum;
                    setTitle(getStringResources().get(CAREER_CURRICULUM) + " "
                             + "- " + curriculum.getCareerName());
                    gbc.gridx = 1;
                    gbc.gridy = 1;
                    for (CourseNode node : curriculum) {
                        if (node.getLevel() != currentLevel) {
                            currentLevel = node.getLevel();
                            label = new TextLabel(
                                window,
                                String.valueOf(currentLevel)
                            );
                            gbc.gridx = 0;
                            curriculumPanel.add(label, gbc);
                            gbc.gridy++;
                        }
                        // Fonts: Code, Other, Requirements
                        finishLoopWork(curriculumPanel, node, gbc);
                    }
                }

                @Override
                public void workFailed(Exception exception) {
                    super.workFailed(exception);
                    dispose();
                }
            };
        final AppWorker<CareerCurriculum, Void> task =
            new AppWorker<>(
                tlv,
                atc
            );
        task.execute(runnable);
    }

    private static final class WindowDialogCallback implements CareerCurriculumEditorDialog.Callback {
        private final Controller controller;

        private WindowDialogCallback(Controller controller) {
            this.controller = controller;
        }

        @Override
        public Map<String, Course> getCourses() {
            // TODO Auto-generated method stub
            return null;
        }
    }
}
