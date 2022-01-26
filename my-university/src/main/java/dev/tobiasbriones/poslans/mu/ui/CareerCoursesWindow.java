/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.mu.ui;

import dev.tobiasbriones.poslans.mu.Strings;
import dev.tobiasbriones.poslans.mu.models.career.Course;
import dev.tobiasbriones.poslans.mu.ui.controller.Item;
import dev.tobiasbriones.poslans.mu.ui.editing.FieldValidationException;
import dev.tobiasbriones.poslans.mu.ui.editing.State;
import engineer.mathsoftware.jdesk.App;
import engineer.mathsoftware.jdesk.AppInstance;
import engineer.mathsoftware.jdesk.Window;
import engineer.mathsoftware.jdesk.resources.Resources;
import engineer.mathsoftware.jdesk.ui.dialog.AppDialog;
import engineer.mathsoftware.jdesk.ui.view.Button;
import engineer.mathsoftware.jdesk.ui.view.Panel;
import engineer.mathsoftware.jdesk.ui.view.ScrollPane;
import engineer.mathsoftware.jdesk.ui.view.*;
import engineer.mathsoftware.jdesk.ui.view.loading.CircularCirclesLoadingView;
import engineer.mathsoftware.jdesk.work.AppWorkCallback;
import engineer.mathsoftware.jdesk.work.AppWorker;
import engineer.mathsoftware.jdesk.work.WorkCallback;
import engineer.mathsoftware.jdesk.work.WorkRunnable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;

import static dev.tobiasbriones.poslans.mu.Strings.*;

public final class CareerCoursesWindow extends Window implements ClickListener,
                                                                 Form.FormListener {

    /*
     * codesSet 		  - saves the codes of the listed courses to verify
     * uniqueness when editing a course.
     * editingCourse 	  - holds the reference to the course in the form.
     * editingState 	  - stores the state of the editingCourse in the form.
     * loadingCoursesTask - task to perform the load of the courses, is
     * canceled when clicking on
     * 				        another career on the careersList
     */
    private static final long serialVersionUID = 1549364515881119739L;
    private static final Dimension WINDOW_SIZE = new Dimension(900, 600);
    private static final Dimension CAREERS_PANE_SIZE = new Dimension(
        (int) (WINDOW_SIZE.width * 0.3),
        WINDOW_SIZE.height
    );
    private static final Dimension COURSES_PANE_SIZE = new Dimension(
        (int) (WINDOW_SIZE.width * 0.5),
        WINDOW_SIZE.height
    );
    private static final Dimension EDIT_PANE_SIZE = new Dimension(
        (int) (WINDOW_SIZE.width * 0.2),
        80
    );

    public interface EditingController {
        boolean setEditingCareer(int careerInEdition);

        boolean addToCourseChanges(Course change);

        boolean hasChanges();

        void saveChanges() throws SQLException;

        void discardChanges();
    }

    public interface Controller extends EditingController {
        void openCCEW(Window window);

        List<Item> listCareerItems() throws SQLException;

        List<Course> listCareerCourses(Item career) throws SQLException;

        boolean isValidUserPassword(String password);
    }

    private final Controller controller;
    private final DefaultListModel<Item> careersListModel;
    private final ListPane<Item> careersList;
    private final DefaultListModel<Course> coursesListModel;
    private final ListPane<Course> coursesList;
    private final Form editForm;
    private final InputText editCodeIT;
    private final InputText editNameIT;
    private final InputText editCreditsIT;
    private final Button addButton;
    private final Button editButton;
    private final Button deleteButton;
    private final Button discardButton;
    private final Button saveButton;
    private final MainActionButton mab;
    private final CircularCirclesLoadingView tlv;
    private final HashSet<String> codesSet;
    private Course editingCourse;
    private State editingState;
    private AppWorker<List<Course>, Void> loadingCoursesTask;

    public CareerCoursesWindow(App app, Controller controller) {
        super((AppInstance) app, WINDOW_SIZE);
        this.controller = controller;
        this.careersListModel = new DefaultListModel<>();
        this.careersList = new ListPane<>(this, careersListModel);
        this.coursesListModel = new DefaultListModel<>();
        this.coursesList = new ListPane<>(this, coursesListModel);
        this.editForm = new Form(this, 0);
        this.editCodeIT = new InputText(this);
        this.editNameIT = new InputText(this);
        this.editCreditsIT = new InputText(this);
        this.addButton = new Button(this, ADD);
        this.editButton = new Button(this, EDIT);
        this.deleteButton = new Button(this, DELETE);
        this.discardButton = new Button(this, DISCARD);
        this.saveButton = new Button(this, SAVE);
        this.mab = new MainActionButton(
            this,
            Resources.loadIcon("ic_career_curriculum"),
            CAREER_CURRICULUM
        );
        this.tlv = new CircularCirclesLoadingView(this);
        this.codesSet = new HashSet<>();
        this.editingCourse = null;
        this.editingState = State.NONE;
        this.loadingCoursesTask = null;
        setResizable(false);
        setTitle(CAREER_COURSES);
    }

    private Dimension getEditFormPanelSize() {
        final Dimension formSize = editForm.getPreferredSize();
        return new Dimension(formSize.width, formSize.height + 6);
    }

    private int getSelectedCareerId() {
        if (careersList.getSelectedIndex() == -1) {
            return -1;
        }
        return careersList.getSelectedValue().id;
    }

    private void setEditPanelCoursesLoaded(boolean loaded) {
        setEditPanelDisabled();
        if (loaded) {
            addButton.setEnabled(true);
        }
        mab.setEnabled(true);
    }

    @Override
    public void onClick(Object view, int viewTextId) {
        if (getSelectedCareerId() == -1) {
            return;
        }
        if (editingCourse != null) {
            return;
        }
        final boolean isCourseSelected = coursesList.getSelectedIndex() != -1;
        switch (viewTextId) {
            case ADD:
                openFormAdd();
                break;
            case EDIT:
                if (!isCourseSelected) {
                    return;
                }
                if (coursesList.getSelectedIndices().length != 1) {
                    return;
                }
                openFormEdit(coursesList.getSelectedValue());
                break;
            case DELETE:
                if (!isCourseSelected) {
                    return;
                }
                if (AppDialog.showConfirm(
                    this,
                    DELETE,
                    DELETE_COURSES_QUESTION,
                    DELETE
                ) == AppDialog.ConfirmResult.RESULT_OK) {
                    deleteSelectedCourses();
                    discardButton.setEnabled(true);
                    saveButton.setEnabled(true);
                }
                break;
            case DISCARD:
                if (AppDialog.showConfirm(
                    this,
                    DISCARD,
                    DISCARD_CHANGES_QUESTION,
                    DISCARD
                ) == AppDialog.ConfirmResult.RESULT_OK) {
                    discard();
                }
                break;
            case SAVE:
                saveCourses();
                break;
            case CAREER_CURRICULUM:
                controller.openCCEW(this);
                break;
        }
    }

    @Override
    public void onSubmit() {
        if (!validateEditForm()) {
            editForm.enableSubmit();
            return;
        }
        final String code = editCodeIT.getText();
        final String name = editNameIT.getText();
        final int credits = Integer.parseInt(editCreditsIT.getText());

        // If it has changes
        if (!(code.equals(editingCourse.getCode()) && name.equals(editingCourse.getName()) && credits == editingCourse.getCredits())) {
            controller.addToCourseChanges(editingCourse);
            codesSet.add(code);
            newChange();
        }
        switch (editingState) {
            case EDITED:
                if (editingCourse.getState() != State.NEW) {
                    editingCourse.setState(State.EDITED);
                }
                if (!editingCourse.getCode().equals(code)) {
                    codesSet.remove(editingCourse.getCode());
                }
                editingCourse.set(code, name, credits);
                coursesList.repaint();
                break;
            case NEW:
                editingCourse.setState(State.NEW);
                editingCourse.set(code, name, credits);
                coursesListModel.addElement(editingCourse);
                break;
            default:
                return;
        }
        editForm.setVisible(false);
        editForm.clear();
        mab.setVisible(true);
        addButton.setEnabled(true);
        editButton.setEnabled(true);
        deleteButton.setEnabled(true);
        editingCourse = null;
        editingState = State.NONE;
    }

    @Override
    public void onCancel() {
        editingCourse = null;
        editingState = State.NONE;
        editForm.setVisible(false);
        editForm.clear();
        mab.setVisible(true);
        addButton.setEnabled(true);
        editButton.setEnabled(true);
        deleteButton.setEnabled(true);
        discardButton.setEnabled(controller.hasChanges());
        saveButton.setEnabled(controller.hasChanges());
    }

    @Override
    protected void createWindow(Panel panel) {
        final ScrollPane careersScroll = new ScrollPane(this, careersList);
        final ScrollPane coursesScroll = new ScrollPane(this, coursesList);
        final Panel editPanel = new Panel(this);
        final Panel editFormPanel = new Panel(this);
        final Panel editButtonsPanel = new Panel(this);
        final Panel editMABPanel = new Panel(this);
        final Panel editTLVPanel = new Panel(this);
        final Panel editBottomPanel = new Panel(this);
        final Insets buttonPadding = saveButton.getBorder()
                                               .getBorderInsets(saveButton);
        final Border discardSaveButtonsBorder = new EmptyBorder(
            buttonPadding.top,
            0,
            buttonPadding.bottom,
            0
        );
        final GridBagConstraints gbc = new GridBagConstraints();
        final ListSelectionModel careersSelectionModel =
            new DefaultListSelectionModel() {

                private static final long serialVersionUID =
                    -5237599407058206261L;

                @Override
                public void setSelectionInterval(int index0, int index1) {
                    if (controller.hasChanges()) {
                        final Window w = CareerCoursesWindow.this;
                        final AppDialog.ConfirmResult result =
                            AppDialog.showConfirm(
                            w,
                            DISCARD,
                            DISCARD_CHANGES_QUESTION,
                            DISCARD
                        );
                        if (result == AppDialog.ConfirmResult.RESULT_OK) {
                            discard();
                        }
                        return;
                    }
                    super.setSelectionInterval(index0, index1);
                    if (careersList.getSelectedIndex() != -1) {
                        final Item career = careersList.getSelectedValue();
                        controller.setEditingCareer(career.id);
                        loadCareerCourses(career);
                    }
                }
            };
        final MouseListener coursesMouseListener = new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openFormEdit(coursesList.getSelectedValue());
                }
            }
        };
        final ListSelectionListener coursesSelectionListener =
            new ListSelectionListener() {

                @Override
                public void valueChanged(ListSelectionEvent e) {
                    if (e.getValueIsAdjusting()) {
                        return;
                    }
                    onCancel();
                }
            };
        // Careers pane
        careersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        careersList.setModel(careersListModel);
        careersList.setSelectionModel(careersSelectionModel);
        careersScroll.setPreferredSize(CAREERS_PANE_SIZE);
        // Courses pane
        coursesScroll.setPreferredSize(COURSES_PANE_SIZE);
        coursesList.addListSelectionListener(coursesSelectionListener);
        coursesList.addMouseListener(coursesMouseListener);
        // Edit pane
        editForm.addInput(CODE, editCodeIT);
        editForm.addInput(NAME, editNameIT);
        editForm.addInput(CREDITS, editCreditsIT);
        editForm.addCancelSubmitButtons(SAVE, this, false);
        editForm.addErrorLabel();
        addButton.setEnabled(false);
        discardButton.setEnabled(false);
        discardButton.setBorder(discardSaveButtonsBorder);
        saveButton.setEnabled(false);
        saveButton.setBorder(discardSaveButtonsBorder);
        mab.setEnabled(false);
        tlv.setPreferredSize(new Dimension(24, 24));
        editFormPanel.setLayout(new GridLayout(1, 1));
        editFormPanel.setPreferredSize(getEditFormPanelSize());
        editFormPanel.add(editForm);
        editButtonsPanel.setLayout(new GridBagLayout());
        editButtonsPanel.setPreferredSize(EDIT_PANE_SIZE);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.insets = new Insets(4, 10, 4, 10);
        gbc.weightx = 1.0;
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        editButtonsPanel.add(addButton, gbc);
        gbc.insets.top = 4;
        gbc.gridy++;
        editButtonsPanel.add(editButton, gbc);
        gbc.gridy++;
        editButtonsPanel.add(deleteButton, gbc);
        gbc.insets.right = 2;
        gbc.gridwidth = 1;
        gbc.gridy++;
        editButtonsPanel.add(discardButton, gbc);
        gbc.insets.left = 2;
        gbc.insets.right = 10;
        gbc.gridx++;
        editButtonsPanel.add(saveButton, gbc);
        editMABPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        editMABPanel.add(mab);
        editTLVPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        editTLVPanel.add(tlv);
        editBottomPanel.setLayout(new BoxLayout(
            editBottomPanel,
            BoxLayout.Y_AXIS
        ));
        editBottomPanel.setPadding(20, 10, 20, 10);
        editBottomPanel.add(editMABPanel);
        editBottomPanel.add(editTLVPanel);
        editPanel.setLayout(new BorderLayout());
        editPanel.setPreferredSize(EDIT_PANE_SIZE);
        editPanel.add(editFormPanel, BorderLayout.NORTH);
        editPanel.add(editButtonsPanel, BorderLayout.CENTER);
        editPanel.add(editBottomPanel, BorderLayout.SOUTH);
        setEditPanelDisabled();
        // Panel
        panel.setLayout(new BorderLayout());
        panel.add(careersScroll, BorderLayout.WEST);
        panel.add(coursesScroll, BorderLayout.CENTER);
        panel.add(editPanel, BorderLayout.EAST);
    }

    @Override
    protected void windowCreated() {
        setVisible(true);
        loadCareers();
    }

    private void setEditPanelDisabled() {
        editForm.setVisible(false);
        addButton.setEnabled(false);
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
        discardButton.setEnabled(false);
        saveButton.setEnabled(false);
        mab.setEnabled(false);
    }

    private void setEditPanelCareerSelected() {
        editForm.setVisible(false);
        addButton.setEnabled(true);
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
        discardButton.setEnabled(false);
        saveButton.setEnabled(false);
        mab.setEnabled(true);
    }

    private void newChange() {
        discardButton.setEnabled(true);
        saveButton.setEnabled(true);
        mab.setEnabled(false);
    }

    private void addCareers(List<Item> careers) {
        for (Item career : careers) {
            careersListModel.addElement(career);
        }
    }

    private void loadCareers() {
        final WorkRunnable<List<Item>> runnable =
            () -> controller.listCareerItems();
        final WorkCallback<List<Item>> atc = new AppWorkCallback<List<Item>>(
            this) {
            @Override
            public void workFinished(List<Item> careers) {
                addCareers(careers);
                //TODO
                controller.setEditingCareer(careers.get(0).id);
                controller.openCCEW(CareerCoursesWindow.this);
            }

            @Override
            public void workFailed(Exception exception) {
                super.workFailed(exception);
                dispose();
            }
        };
        final AppWorker<List<Item>, Void> task = new AppWorker<>(
            tlv,
            atc
        );
        task.execute(runnable);
    }

    private void loadCourses(List<Course> courses) {
        for (Course course : courses) {
            codesSet.add(course.getCode());
            coursesListModel.addElement(course);
        }
        setEditPanelCoursesLoaded(true);
    }

    private void unloadCourses() {
        codesSet.clear();
        coursesListModel.clear();
        setEditPanelCoursesLoaded(false);
    }

    private void loadCareerCourses(Item career) {
        if (loadingCoursesTask != null) {
            loadingCoursesTask.cancel();
            loadingCoursesTask = null;
        }
        final WorkRunnable<List<Course>> runnable = () -> {
            Thread.sleep(600);
            return controller.listCareerCourses(career);
        };
        final AppWorkCallback<List<Course>> atc =
            new AppWorkCallback<List<Course>>(this) {
                @Override
                public void workFinished(List<Course> courses) {
                    loadingCoursesTask = null;
                    if (courses == null) {
                        return;
                    }
                    loadCourses(courses);
                }
            };
        final AppWorker<List<Course>, Void> task = new AppWorker<>(
            tlv,
            atc
        );
        loadingCoursesTask = task;
        unloadCourses();
        task.execute(runnable);
    }

    private boolean validateEditForm() {
        final String code = editCodeIT.getText();
        final String name = editNameIT.getText();
        final int credits;
        if (!editForm.checkAllInputText()) {
            editForm.setErrorText(SET_ALL_FIELDS);
            return false;
        }
        try {
            credits = Integer.parseInt(editCreditsIT.getText());
        }
        catch (NumberFormatException e) {
            editForm.setErrorText(ERROR_CREDITS_IS_INT);
            return false;
        }
        try {
            Course.validate(getStringResources(), code, name, credits);
        }
        catch (FieldValidationException e) {
            editForm.setErrorText(e.getMessage());
            return false;
        }
        if (editingState == State.NEW && codesSet.contains(code)) {
            editForm.setErrorText(ERROR_CODE_IS_UNIQUE);
            return false;
        }
        else if (editingState != State.NEW && !editingCourse.getCode()
                                                            .equals(code) && codesSet.contains(
            code)) {
            editForm.setErrorText(ERROR_CODE_IS_UNIQUE);
            return false;
        }
        return true;
    }

    private void deleteSelectedCourses() {
        final List<Course> deleteSelection =
            coursesList.getSelectedValuesList();
        for (Course delete : deleteSelection) {
            delete.setState(State.DELETE);
            coursesListModel.removeElement(delete);
            controller.addToCourseChanges(delete);
            codesSet.remove(delete.getCode());
        }
        newChange();
    }

    private void openFormAdd() {
        editingCourse = new Course(getSelectedCareerId());
        editingState = State.NEW;
        addButton.setEnabled(false);
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
        discardButton.setEnabled(false);
        saveButton.setEnabled(false);
        mab.setVisible(false);
        editForm.setVisible(true);
        editForm.requestFocus();
    }

    private void openFormEdit(Course edit) {
        editingCourse = edit;
        editingState = State.EDITED;
        final String credits = String.valueOf(editingCourse.getCredits());
        addButton.setEnabled(false);
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
        discardButton.setEnabled(false);
        saveButton.setEnabled(false);
        editForm.setInputTexts(
            editingCourse.getCode(),
            editingCourse.getName(),
            credits
        );
        editForm.setVisible(true);
    }

    private void discard() {
        controller.discardChanges();
        codesSet.clear();
        editingCourse = null;
        editingState = State.NONE;
        if (careersList.getSelectedIndex() != -1) {
            final Item careerSelected = careersList.getSelectedValue();
            controller.setEditingCareer(careerSelected.id);
            loadCareerCourses(careerSelected);
        }
    }

    private void saveCourses() {
        final WorkRunnable<Void> runnable = () -> {
            controller.saveChanges();
            return null;
        };
        final AppWorkCallback<Void> atc =
            new AppWorkCallback<Void>(this) {
                @Override
                public void workFinished(Void result) {
                    if (careersList.getSelectedIndex() != -1) {
                        final Item careerSelected =
                            careersList.getSelectedValue();
                        controller.setEditingCareer(careerSelected.id);
                        loadCareerCourses(careerSelected);
                    }
                }

                @Override
                public void workFailed(Exception exception) {
                    super.workFailed(exception);
                    final int index = coursesList.getSelectedIndex();
                    setEditPanelCareerSelected();
                    if (index != -1) {
                        editButton.setEnabled(true);
                        deleteButton.setEnabled(true);
                    }
                    discardButton.setEnabled(true);
                    saveButton.setEnabled(true);
                }
            };
        final AppWorker<Void, Void> task = new AppWorker<>(
            tlv,
            atc
        );
        setEditPanelDisabled();
        task.execute(runnable);
    }
}
