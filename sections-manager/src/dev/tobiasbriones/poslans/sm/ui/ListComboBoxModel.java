/*
 * Copyright (c) 2017 Tobias Briones. All rights reserved.
 */

package dev.tobiasbriones.poslans.sm.ui;

import javax.swing.*;
import java.util.List;

final class ListComboBoxModel<E> extends AbstractListModel<E> implements ComboBoxModel<E> {
    private static final long serialVersionUID = 2302194139328466532L;
    private final List<E> list;
    private Object selectedItem;

    ListComboBoxModel(List<E> list) {
        this.list = list;
        this.selectedItem = null;
    }

    @Override
    public int getSize() {
        return list.size();
    }

    @Override
    public E getElementAt(int index) {
        return list.get(index);
    }

    @Override
    public Object getSelectedItem() {
        return selectedItem;
    }

    @Override
    public void setSelectedItem(Object anItem) {
        selectedItem = anItem;
    }
}
