package com.emal.android.transport.spb.utils;

import android.view.Menu;

/**
 * @author alexey.emelyanenko@gmail.com
 * @since: 2.0
 */
public class MenuErrorSignCallback implements ErrorSignCallback {

    private Menu menu;

    public MenuErrorSignCallback(Menu menu) {
        this.menu = menu;
    }

    @Override
    public void show() {
        if (menu == null) {
            return;
        }
        menu.getItem(0).setVisible(true);
    }

    @Override
    public void hide() {
        if (menu == null) {
            return;
        }

        menu.getItem(0).setVisible(false);
    }

    @Override
    public boolean isShowed() {
        return menu != null && menu.getItem(0).isVisible();
    }
}
