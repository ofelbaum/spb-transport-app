package com.emal.android.transport.spb.utils;

import android.graphics.*;
import android.support.v4.util.LruCache;
import com.emal.android.transport.spb.VehicleType;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: alexey.emelyanenko@gmail.com
 * Date: 10/15/14 11:16 PM
 */
public class DrawHelper {
    private static Map<String, BitmapDescriptor> cacheTypes = new ConcurrentHashMap<String, BitmapDescriptor>(4);
    private static LruCache<String, BitmapDescriptor> cacheNumbers = new LruCache<String, BitmapDescriptor>((int)(Runtime.getRuntime().maxMemory() / 1024 / 16));

    public static void evictCaches() {
        cacheTypes.clear();
        cacheNumbers.evictAll();
    }

    public static BitmapDescriptor getVechicleTypeBitmapDescriptor(VehicleType transportType, float scaleFactor) {
        String key = transportType.name() + "_" + String.valueOf(scaleFactor);
        BitmapDescriptor bitmapDescriptor = cacheTypes.get(key);
        if (bitmapDescriptor == null) {
            Bitmap bitmap = getVehicleBitmap(transportType, scaleFactor);
            bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
            cacheTypes.put(key, bitmapDescriptor);

        }
        return bitmapDescriptor;
    }

    public static BitmapDescriptor getVechicleNumberBitmapDescriptor(String routeNumber, float scaleFactor) {
        String key = routeNumber + "_" + String.valueOf(scaleFactor);
        BitmapDescriptor bitmapDescriptor = cacheNumbers.get(key);
        if (bitmapDescriptor == null) {
            Bitmap bitmap = getVehicleNumberBitmap(routeNumber, scaleFactor);
            bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
            cacheNumbers.put(key, bitmapDescriptor);

        }
        return bitmapDescriptor;
    }

    private static Bitmap getVehicleBitmap(VehicleType transportType, float scaleFactor) {
        int magic = (int) (10 * scaleFactor);
        int bHeigth = magic * 2;
        int bWidth = magic * 2;
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = Bitmap.createBitmap(bWidth, bHeigth, conf);

        Paint vehiclePaint = new Paint();
        vehiclePaint.setTextSize(magic + 5);
        vehiclePaint.setColor(Color.WHITE);
        vehiclePaint.setTypeface(Typeface.DEFAULT_BOLD);
        vehiclePaint.setTextAlign(Paint.Align.CENTER);
        vehiclePaint.setAntiAlias(true);
        vehiclePaint.setFilterBitmap(true);

        Paint vehicleNumberPaint = new Paint();
        vehicleNumberPaint.setTextSize(magic + 5);
        vehicleNumberPaint.setColor(Color.BLACK);
        vehicleNumberPaint.setTypeface(Typeface.DEFAULT_BOLD);
        vehicleNumberPaint.setTextAlign(Paint.Align.CENTER);
        vehicleNumberPaint.setAntiAlias(true);
        vehicleNumberPaint.setFilterBitmap(true);

        Paint rectPaint = new Paint();
        rectPaint.setColor(transportType.getColor());
        rectPaint.setStyle(Paint.Style.FILL);
        rectPaint.setFilterBitmap(true);
        rectPaint.setAntiAlias(true);

        Canvas canvas = new Canvas(bitmap);
        int x = canvas.getClipBounds().centerX();
        int y = canvas.getClipBounds().centerY();

        canvas.save();
        canvas.drawRect(x - magic + magic/3.0f, y + magic, x + magic - magic/3.0f, y - magic, rectPaint);

        int xPos = (canvas.getWidth() / 2);
        int yPos = (int) ((canvas.getHeight() / 2) - ((vehiclePaint.descent() + vehiclePaint.ascent()) / 2)) ;

        canvas.drawText(transportType.getLetter(), xPos, yPos, vehiclePaint);
        canvas.restore();
        return bitmap;
    }

    private static Bitmap getVehicleNumberBitmap(String routeNumber, float scaleFactor) {
        int magic = (int) (10 * scaleFactor);
        int bHeigth = magic * 4;
        int bWidth = magic * 4;
        Bitmap.Config conf = Bitmap.Config.ALPHA_8;
        Bitmap bitmap = Bitmap.createBitmap(bWidth, bHeigth, conf);

        Paint vehicleNumberPaint = new Paint();
        vehicleNumberPaint.setTextSize(magic + 5);
        vehicleNumberPaint.setColor(Color.BLACK);
        vehicleNumberPaint.setTypeface(Typeface.DEFAULT_BOLD);
        vehicleNumberPaint.setTextAlign(Paint.Align.CENTER);
        vehicleNumberPaint.setAntiAlias(true);
        vehicleNumberPaint.setFilterBitmap(true);

        Canvas canvas = new Canvas(bitmap);
        canvas.drawText(routeNumber, magic, magic, vehicleNumberPaint);
        canvas.save();

        canvas.restore();
        return bitmap;
    }
}
