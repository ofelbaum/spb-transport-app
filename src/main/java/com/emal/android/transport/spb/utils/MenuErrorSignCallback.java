package com.emal.android.transport.spb.utils;

import android.view.Menu;
import com.emal.android.transport.spb.activity.AbstractDrawerActivity;

/**
 * @author alexey.emelyanenko@gmail.com
 * @since: 2.0
 */
public class MenuErrorSignCallback implements ErrorSignCallback {

    private AbstractDrawerActivity activity;

    public MenuErrorSignCallback(AbstractDrawerActivity menu) {
        this.activity = menu;
    }

    @Override
    public void show() {
        Menu menu = activity.getMenu();
        if (menu == null) {
            return;
        }
        menu.getItem(0).setVisible(true);
    }

    @Override
    public void hide() {
        Menu menu = activity.getMenu();
        if (menu == null) {
            return;
        }

        menu.getItem(0).setVisible(false);
    }

    @Override
    public boolean isShowed() {
        Menu menu = activity.getMenu();
        return menu != null && menu.getItem(0).isVisible();
    }
}
