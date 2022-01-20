/*
 * Copyright (c) 2017 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.sm.ui;

// Anti-pattern!
interface Strings {
    String OPEN_SECTION_TIP = "Open a new section";

    String PROFESSORS_TIP = "View professors academic load";

    String HISTORY_TIP = "View history";

    String CAREER_DATA_TIP = "View and load your career's"
                             + " data";

    String ABOUT_TIP = "About Sections Manager";

    String CAREER_INFO_TIP = "Set your university and "
                             + "career";

    String IMPORT_EXPORT_TIP = "Import / Export";

    String DELETE_FOREVER_TIP = "Delete forever";

    String EDIT = "Edit";

    String DELETE = "Delete";

    String NEW_TERM = "New term";

    String NEW_TERM_MSG = "<html>Start a new term?<br>The"
                          + " current term will be added "
                          + "to the history.</html>";

    String SET_TERM_NAME = "Set a name for the new term";

    String ACTION_CANCELLED = "Action cancelled";

    String WELCOME_MSG = "<html>Welcome! Let's start "
                         + "loading your career's data"
                         + ".<br>You wil be able to "
                         + "import your xlsx file or use "
                         + "the app's data editor.</html>";

    String GETTING_STARTED = "Getting started";

    String CHECK_YOUR_INPUT = "Check your input";

    String CAREER = "Career";

    String ADD = "Add";

    String DISCARD = "Discard";

    String SAVE = "Save";

    String UNIVERSITY_BELONG = "What university do you "
                               + "belong?";

    String SET_CAREER_NAME = "Set your career's name";

    String FILL_ALL_FIELDS = "You didn't fill all the "
                             + "fields";

    String DELETE_CAREER_DATA = "<html>Delete career "
                                + "data?<br>This can't be"
                                + " undone</html>";

    String HISTORY = "History";

    String SAVE_CURRENT_DATA = "Save current data";

    String FAIL = "Fail";

    String CLASS = "Class";

    String CLASSES = "Classes";

    String PROFESSORS = "Professors";

    String PROFESSOR = "Professor";

    String CLASSROOM = "Classroom";

    String CLASSROOMS = "Classrooms";

    String TIME = "Time";

    String DAYS = "Days";

    String CANCEL = "Cancel";

    String OPEN_SECTION = "Open section";

    String OPEN = "Open";

    String EDIT_SECTION = "Edit section";

    String INVALID_TIME = "Invalid time. Use hh:mm or "
                          + "just hh";

    String SECTION_NOT_AVAILABLE = "This sections is not "
                                   + "available!";

    String IMPORT_EXPORT = "Import/Export";

    String IMPORT_EXPORT_LABEL = "<html>Import your data "
                                 + "or export the current"
                                 + " data, Excel file "
                                 + "format.<br>Importing "
                                 + "data will overwrite "
                                 + "your current data"
                                 + ".</html>";

    String IMPORT = "Import";

    String EXPORT = "Export";

    String SELECT_EXCEL = "Select your excel file";

    String EXCEL_FILES = "Excel files (xlsx)";

    String ERROR_OPENING = "Error when opening";

    String INVALID_RANGE = "Invalid range.";

    String ENTER_VALID_NUMBER = "<html>Please enter a "
                                + "valid number"
                                + ".<br></html>";

    String CLASS_CODE = "Class code";

    String CLASS_NAME = "Class name";

    String WEIGHT = "Weight";

    String DAYS_PER_WEEK = "Days per week";

    String DURATION_HOURS = "Duration (Hours)";

    String NAME = "Name";

    String TITLE = "Title";

    String EDIT_PROFESSOR = "Edit professor";

    String CREATE_PROFESSOR = "Create professor";

    String CREATE = "Create";

    String BUILDING = "Building";

    String CLASSROOM_NUMBER = "Classroom number";

    String EDIT_CLASSROOM = "Edit classroom";

    String CREATE_CLASSROOM = "Create classroom";

    String PROFESSORS_LOAD = "Professors academic load";

    String TITLE_PAL = "<html><span "
                       + "style='font-weight:900;"
                       + "'>Professor</span></html>";

    String TITLE_LOAD = "<html><span "
                        + "style='font-weight:900;"
                        + "'>Load</span></html>";

    String ABOUT = "About";

    String ABOUT_MSG = "<html>Sections Manager v0.1-alpha.1"
                       + ".0<br>This software includes"
                       + " the following libraries:<br>- Apache POI library "
                       + "under "
                       + "the Apache License, Version 2.0 - See <a "
                       + "href='https://poi.apache.org/'>https://poi.apache"
                       + ".org/</a>"
                       + "<br>- JSON library - See <a href='http://www.json"
                       + ".org/'>http://www.json.org/</a><br>"
                       + "<strong>© Tobias Briones, "
                       + "Sections "
                       + "Manager 2017</strong></html>";
}