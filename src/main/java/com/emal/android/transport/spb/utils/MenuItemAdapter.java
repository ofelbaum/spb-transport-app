package com.emal.android.transport.spb.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.emal.android.transport.spb.R;

/**
 * @author alexey.emelyanenko@gmail.com
 * @since: 1.5
 */
public class MenuItemAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[] Ids;
    private final int rowResourceId;
    private MenuModel menuModel;

    public MenuItemAdapter(Context context, int textViewResourceId, MenuModel menuModel) {
        super(context, textViewResourceId, menuModel.getIds());

        this.context = context;
        this.Ids = menuModel.getIds();
        this.menuModel = menuModel;
        this.rowResourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(rowResourceId, parent, false);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView);
        TextView textView = (TextView) rowView.findViewById(R.id.textView);

        int id = Integer.parseInt(Ids[position]);

        MenuItem menuItem = menuModel.getById(id);
        textView.setText(menuItem.getTitle());

        Drawable imageFile = menuItem.getIconFile();
        if (imageFile != null) {
            imageView.setImageDrawable(imageFile);
        }
        return rowView;
    }
}
