/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.mu.ui;

import dev.tobiasbriones.poslans.mu.Strings;
import engineer.mathsoftware.jdesk.Window;
import engineer.mathsoftware.jdesk.resources.Resources;
import engineer.mathsoftware.jdesk.ui.style.TextStyle;
import engineer.mathsoftware.jdesk.ui.view.TextLabel;

import javax.swing.*;
import java.awt.*;

final class PoslansLabel {
    private PoslansLabel() {}

    static TextLabel newInstance(Window window) {
        final TextLabel poslans = new TextLabel(
            window,
            Strings.POWERED_BY_POSLANS,
            SwingConstants.RIGHT
        );
        poslans.setTextStyle(TextStyle.ITALIC, Resources.SMALL_FONT_SIZE);
        poslans.setForeground(Color.decode("#737373"));
        return poslans;
    }
}
