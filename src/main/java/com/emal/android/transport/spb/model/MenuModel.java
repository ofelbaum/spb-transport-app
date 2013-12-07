package com.emal.android.transport.spb.model;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import com.emal.android.transport.spb.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author alexey.emelyanenko@gmail.com
 * @since: 1.5
 */
public class MenuModel {
    public List<MenuItem> items;

    public MenuModel(Resources resources) {
        items = new ArrayList<MenuItem>();
        String[] stringArray = resources.getStringArray(R.array.menu_items_array);

        TypedArray icons = resources.obtainTypedArray(R.array.menu_items_icon_array);

        for (int i = 1; i <= stringArray.length; i++) {
            Drawable drawable = null;
            try {
                drawable = icons.getDrawable(i - 1);
            } catch (Resources.NotFoundException e) {
                //nothing to do
            }
            MenuItem menuItem = new MenuItem(i, drawable, stringArray[i - 1]);
            items.add(menuItem);
        }
    }

    public MenuItem getById(int id) {
        for (MenuItem item : items) {
            if (item.getId() == id) {
                return item;
            }
        }
        return null;
    }

    public String[] getIds() {
        String[] ids = new String[items.size()];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = Integer.toString(i + 1);
        }
        return ids;
    }
}
