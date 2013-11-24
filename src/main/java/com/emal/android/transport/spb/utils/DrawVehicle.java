package com.emal.android.transport.spb.utils;

import android.graphics.*;
import android.os.AsyncTask;
import com.emal.android.transport.spb.VehicleSyncAdapter;
import com.emal.android.transport.spb.VehicleType;
import com.emal.android.transport.spb.portal.*;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author alexey.emelyanenko@gmail.com
 * @since:
 */
public class DrawVehicle extends AsyncTask<Object, Void, List<Vehicle>> {
    private static final String TAG = DrawVehicle.class.getName();
    private Route route;
    private PortalClient portalClient;
    private GoogleMap mMap;
    private Map<String, List<Marker>> markers;
    private VehicleSyncAdapter vehicleSyncAdapter;

    public DrawVehicle(Route route, PortalClient portalClient, GoogleMap mMap, Map<String, List<Marker>> markers, VehicleSyncAdapter vehicleSyncAdapter) {
        this.route = route;
        this.portalClient = portalClient;
        this.mMap = mMap;
        this.markers = markers;
        this.vehicleSyncAdapter = vehicleSyncAdapter;
    }

    @Override
    protected void onPreExecute() {
        vehicleSyncAdapter.beforeSync(false);
    }

    @Override
    protected List<Vehicle> doInBackground(Object... params) {
        List<Vehicle> list = null;
        try {
            VehicleCollection routeData = portalClient.getRouteData(String.valueOf(route.getId()), PortalClient.BBOX);
            list = routeData.getList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list != null ? list : Collections.EMPTY_LIST;
    }

    @Override
    protected void onPostExecute(List<Vehicle> vehicles) {
        String routeId = String.valueOf(route.getId());
        List<Marker> markers1 = markers.get(routeId);
        if (markers1 != null && !markers1.isEmpty()) {
            for (Marker marker : markers1) {
                marker.remove();
            }
        } else {
            markers1 = new ArrayList<Marker>();
            markers.put(routeId, markers1);
        }

        for (Vehicle v : vehicles) {
            Double latitude = v.getGeometry().getLatitude();
            Double longtitude = v.getGeometry().getLongtitude();

            LatLng homePoint = new LatLng(latitude, longtitude);

            VehicleProps properties = v.getProperties();
            String stateNumber = "Number: " + properties.getStateNumber() + " Speed: " + properties.getVelocity() + " km/h";

            String routeLetter = "S";
            int routeColor = Color.YELLOW;
            VehicleType transportType = route.getTransportType();
            if (VehicleType.TROLLEY.equals(transportType)) {
                routeLetter = "U";
                routeColor = Color.GREEN;
            } else if (VehicleType.BUS.equals(transportType)) {
                routeLetter = "A";
                routeColor = Color.BLUE;
            } else if (VehicleType.TRAM.equals(transportType)) {
                routeLetter = "T";
                routeColor = Color.RED;
            }

            Bitmap.Config conf = Bitmap.Config.ARGB_8888;
            int zoom = (int) mMap.getCameraPosition().zoom;

            int bHeigth = 80;
            int bWidth = 80;
            Bitmap bitmap = Bitmap.createBitmap(bWidth, bHeigth, conf);

            Paint vehiclePaint = new Paint();
            vehiclePaint.setTextSize(30);
            vehiclePaint.setColor(Color.WHITE);
            vehiclePaint.setTypeface(Typeface.DEFAULT_BOLD);
            vehiclePaint.setTextAlign(Paint.Align.CENTER);

            Paint vehicleNumberPaint = new Paint();
            vehicleNumberPaint.setTextSize(25);
            vehicleNumberPaint.setColor(Color.BLACK);
            vehicleNumberPaint.setTypeface(Typeface.DEFAULT_BOLD);
            vehicleNumberPaint.setTextAlign(Paint.Align.CENTER);

            Paint rectPaint = new Paint();
            rectPaint.setColor(routeColor);
            rectPaint.setStyle(Paint.Style.FILL);

            Canvas canvas = new Canvas(bitmap);
            int x = canvas.getClipBounds().centerX();
            int y = canvas.getClipBounds().centerY();

            canvas.drawText(route.getRouteNumber(), 20, 20, vehicleNumberPaint);
            canvas.save();
            canvas.rotate(properties.getDirection(), x, y);
            canvas.drawRect(x - 15, y + 20, x + 15, y - 20, rectPaint);

            int xPos = (canvas.getWidth() / 2);
            int yPos = (int) ((canvas.getHeight() / 2) - ((vehiclePaint.descent() + vehiclePaint.ascent()) / 2)) ;

            canvas.drawText(routeLetter, xPos, yPos, vehiclePaint);
            canvas.scale(zoom, zoom);
            canvas.restore();

            BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
            MarkerOptions title = new MarkerOptions()
                    .position(homePoint)
                    .anchor(0.5f, 0.5f)
                    .icon(bitmapDescriptor)
                    .title(stateNumber);

            Marker marker = mMap.addMarker(title);
            markers1.add(marker);
        }
        vehicleSyncAdapter.afterSync(true);
    }
}
