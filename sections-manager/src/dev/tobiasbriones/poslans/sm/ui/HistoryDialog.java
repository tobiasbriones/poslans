/*
 * Copyright (c) 2017-2018 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.sm.ui;

import dev.tobiasbriones.poslans.sm.current.HistoryItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.List;

public final class HistoryDialog extends JDialog implements Strings,
                                                            ActionListener,
                                                            MouseListener {
    private static final long serialVersionUID = -4947718754960672116L;
    public interface Callback {
        void saveToHistory();
    }

    private final Callback callback;
    private final JList<HistoryItem> list;

    public HistoryDialog(MainWindow mw, List<HistoryItem> history) {
        super(mw, Strings.HISTORY);
        this.callback = mw;
        this.list = new JList<>();
        final JPanel panel = new JPanel();
        final JScrollPane scroll = new JScrollPane(list);
        final JButton saveCurrentButton =
            new JButton(Strings.SAVE_CURRENT_DATA.toUpperCase());
        final DefaultListModel<HistoryItem> model = new DefaultListModel<>();
        for (HistoryItem item : history) {
            model.addElement(item);
        }
        list.setModel(model);
        list.addMouseListener(this);
        saveCurrentButton.addActionListener(this);
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 15, 10, 15));
        panel.add(scroll, BorderLayout.NORTH);
        panel.add(saveCurrentButton, BorderLayout.SOUTH);
        getContentPane().add(panel);
        pack();
        setResizable(false);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setIconImage(Toolkit.getDefaultToolkit()
                            .getImage("icons/ic_history.png"));
        setLocationRelativeTo(null);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        callback.saveToHistory();
        dispose();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            try {
                Desktop.getDesktop().open(list.getSelectedValue().getFile());
            }
            catch (IOException ex) {
                JOptionPane.showMessageDialog(this, Strings.FAIL + ". " + ex);
                dispose();
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}
}
