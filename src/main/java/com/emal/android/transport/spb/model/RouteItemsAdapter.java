package com.emal.android.transport.spb.model;

import android.content.Context;
import android.graphics.*;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.emal.android.transport.spb.R;
import com.emal.android.transport.spb.VehicleType;
import com.emal.android.transport.spb.portal.Route;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author alexey.emelyanenko@gmail.com
 * @since: 1.5
 */
public class RouteItemsAdapter extends ArrayAdapter<Route>{
    private int rowResourceId;
    private Map<VehicleType, Bitmap> bitmaps = new HashMap<VehicleType, Bitmap>();

    public RouteItemsAdapter(Context context, int resource, List<Route> objects) {
        super(context, resource, objects);
        this.rowResourceId = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(rowResourceId, parent, false);

        TextView routeNum = (TextView) rowView.findViewById(R.id.searchRouteNumber);
        ImageView routeType = (ImageView) rowView.findViewById(R.id.searchRouteType);
        TextView routeDesc = (TextView) rowView.findViewById(R.id.searchRouteDescr);

        Route item = getItem(position);
        routeNum.setText(item.getRouteNumber());
        routeType.setImageBitmap(getVehicleBitmap(item.getTransportType()));
        routeDesc.setText(item.getName().toUpperCase());
        return rowView;
    }

    private Bitmap getVehicleBitmap(VehicleType type) {
        Bitmap b = bitmaps.get(type);
        if (b != null) {
            return b;
        }

        int bHeigth = 25;
        int bWidth = 25;
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = Bitmap.createBitmap(bWidth, bHeigth, conf);

        Paint textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setFilterBitmap(true);
        textPaint.setTextSize(17);
        textPaint.setColor(Color.WHITE);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextAlign(Paint.Align.CENTER);

        Paint rectPaint = new Paint();
        rectPaint.setColor(type.getColor());
        rectPaint.setStyle(Paint.Style.FILL);
        rectPaint.setFilterBitmap(true);
        rectPaint.setAntiAlias(true);

        Canvas canvas = new Canvas(bitmap);
        int xPos = (canvas.getWidth() / 2);
        int yPos = (int) ((canvas.getHeight() / 2) - ((textPaint.descent() + textPaint.ascent()) / 2)) ;

        canvas.drawRect(0, 0, bHeigth, bWidth, rectPaint);
        canvas.save();
        if (type.isUpsideDown()) {
            int x = canvas.getClipBounds().centerX();
            int y = canvas.getClipBounds().centerY();
            canvas.rotate(180, x, y);

            yPos += 2; //TODO fix
        } else {
            yPos++; //TODO fix
        }

        canvas.drawText(type.getLetter(), xPos, yPos, textPaint);
        canvas.restore();

        bitmaps.put(type, bitmap);
        return bitmap;
    }
}
