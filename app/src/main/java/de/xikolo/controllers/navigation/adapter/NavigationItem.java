package de.xikolo.controllers.navigation.adapter;

import android.content.Context;
import android.support.annotation.StringRes;

public class NavigationItem {

    @StringRes
    private final int icon;

    @StringRes
    private final int title;

    private final ViewType viewType;

    private final int position;

    public enum ViewType {
        PROFILE(0), MAIN(1), SUB(2);

        private final int value;

        ViewType(int value) {
            this.value = value;
        }

        public int toInteger() {
            return value;
        }
    }

    public NavigationItem(@StringRes int icon, @StringRes int title, ViewType viewType, int position) {
        this.icon = icon;
        this.title = title;
        this.viewType = viewType;
        this.position = position;
    }

    public String getIcon(Context context) {
        return context.getString(icon);
    }

    public String getTitle(Context context) {
        return context.getString(title);
    }

    public ViewType getViewType() {
        return viewType;
    }

    public int getPosition() {
        return position;
    }

}
