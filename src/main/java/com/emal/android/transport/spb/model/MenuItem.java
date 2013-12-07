package com.emal.android.transport.spb.model;

import android.graphics.drawable.Drawable;

/**
 * @author alexey.emelyanenko@gmail.com
 * @since: 1.5
 */
public class MenuItem {
    private int id;
    private Drawable iconFile;
    private String title;

    public MenuItem(int id, Drawable iconFile, String name) {
        this.id = id;
        this.iconFile = iconFile;
        this.title = name;
    }

    public int getId() {
        return id;
    }

    public Drawable getIconFile() {
        return iconFile;
    }

    public String getTitle() {
        return title;
    }
}
