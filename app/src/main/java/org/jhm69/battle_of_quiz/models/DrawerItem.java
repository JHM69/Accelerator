package org.jhm69.battle_of_quiz.models;

import android.view.ViewGroup;

import org.jhm69.battle_of_quiz.adapters.DrawerAdapter;

/**
 * Created by jhm69
 */

public abstract class DrawerItem<T extends DrawerAdapter.ViewHolder> {

    protected boolean isChecked;

    public abstract T createViewHolder(ViewGroup parent);

    public abstract void bindViewHolder(T holder);

    public boolean isChecked() {
        return isChecked;
    }

    public DrawerItem setChecked(boolean isChecked) {
        this.isChecked = isChecked;
        return this;
    }

    @SuppressWarnings("SameReturnValue")
    public boolean isSelectable() {
        return true;
    }

}