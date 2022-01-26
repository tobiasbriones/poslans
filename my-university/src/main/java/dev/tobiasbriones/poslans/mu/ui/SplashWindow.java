/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.mu.ui;

import dev.tobiasbriones.poslans.mu.Strings;
import dev.tobiasbriones.poslans.mu.database.User;
import engineer.mathsoftware.jdesk.App;
import engineer.mathsoftware.jdesk.AppInstance;
import engineer.mathsoftware.jdesk.Window;
import engineer.mathsoftware.jdesk.ui.dialog.TaskDialog;
import engineer.mathsoftware.jdesk.ui.view.Panel;
import engineer.mathsoftware.jdesk.ui.view.loading.BarLoadingView;
import engineer.mathsoftware.jdesk.work.WorkRunnable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class SplashWindow extends Window {
    private static final long serialVersionUID = -6459558772233102627L;

    public interface Callback {
        void done(String dbName);

        void failed();
    }

    private final Callback callback;
    private final User user;

    public SplashWindow(AppInstance app, Callback callback, User user) {
        super(app);
        this.callback = callback;
        this.user = user;
    }

    @Override
    protected void createWindow(Panel panel) {
    }

    @Override
    protected void windowCreated() {
        final TaskDialog<String> td = new TaskDialog<>(
            this,
            Strings.CONNECTING,
            Strings.CONNECTING_ELLIPSIS
        );
        final WorkRunnable<String> runnable = () -> {
            final String databaseName;
            try (
                final Connection connection = user.connect();
                final Statement statement = connection.createStatement()
            ) {
                final ResultSet resultSet = statement.executeQuery(
                    "SELECT DATABASE()");
                if (resultSet.next()) {
                    databaseName = resultSet.getString(1);
                }
                else {
                    throw new SQLException("Database not selected/found");
                }
            }
            return databaseName;
        };
        final TaskDialog.TaskDialogCallback<String> dialogCallback =
            new TaskDialog.TaskDialogCallback<String>(this) {
                @Override
                public void workFinished(String result) {
                    callback.done(result);
                }

                @Override
                public void workFailed(Exception exception) {
                    super.workFailed(exception);
                    callback.failed();
                }

                @Override
                public boolean cancelRequest() {
                    System.exit(0);
                    return true;
                }
            };
        td.getBarLoadingView()
          .setSpeed(BarLoadingView.BAR_WIDTH_CHANGE_PER_SECOND_FAST);
        td.setCallback(dialogCallback);
        td.execute(runnable);
    }

    @Override
    protected void windowVisible(boolean visible) {
        super.windowVisible(visible);
    }
}
