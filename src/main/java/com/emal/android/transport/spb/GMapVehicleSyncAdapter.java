package com.emal.android.transport.spb;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.view.View;
import com.emal.android.transport.spb.utils.GeoConverter;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;

/**
 * User: alexey.emelyanenko@gmail.com
 * Date: 5/19/13 1:18 AM
 */
public class GMapVehicleSyncAdapter implements VehicleSyncAdapter {
    private static final String TAG = GMapVehicleSyncAdapter.class.getName();
    private SupportMapFragment mapFragment;
    private View progressBar;
    private View errorSign;
    private GroundOverlay vehicleOverlay;
    private LatLngBounds latLngBounds;

    public GMapVehicleSyncAdapter(SupportMapFragment mapFragment) {
        this.mapFragment = mapFragment;
        this.progressBar = mapFragment.getActivity().findViewById(com.emal.android.transport.spb.R.id.progressBar);
        this.errorSign = mapFragment.getActivity().findViewById(com.emal.android.transport.spb.R.id.errorSignLayout);
    }

    @Override
    public void beforeSync(boolean clearBeforeUpdate) {
        if (errorSign.getVisibility() == View.INVISIBLE) {
            progressBar.setVisibility(View.VISIBLE);
        }

        if (clearBeforeUpdate) {
            if (vehicleOverlay != null) {
                vehicleOverlay.remove();
            }
        }
    }

    @Override
    public void afterSync(Bitmap result) {
        progressBar.setVisibility(View.INVISIBLE);

        if (result != null) {
            errorSign.setVisibility(View.INVISIBLE);

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize=4;
            BitmapDescriptor image= BitmapDescriptorFactory.fromBitmap(result);

            LatLngBounds latLngBounds = mapFragment.getMap().getProjection().getVisibleRegion().latLngBounds;
            GroundOverlay vehicleOverlayNew = mapFragment.getMap().addGroundOverlay(new GroundOverlayOptions()
                    .image(image)
                    .zIndex(Float.MAX_VALUE)
                    .positionFromBounds(latLngBounds));

            if (vehicleOverlay != null) {
                vehicleOverlay.remove();
            }
            vehicleOverlay = vehicleOverlayNew;
            result.recycle();
        } else {
            new Handler().postAtTime(new Runnable() {
                @Override
                public void run() {
                    errorSign.setVisibility(View.VISIBLE);
                }
            }, 1000);
        }
    }

    @Override
    public int getScreenWidth() {
        return mapFragment.getView().getWidth();
    }

    @Override
    public int getScreenHeight() {
        return mapFragment.getView().getHeight();
    }

    @Override
    public String getBBox() {
//        LatLngBounds latLngBounds = mapFragment.getMap().getProjection().getVisibleRegion().latLngBounds;
        return GeoConverter.calculateBBox(latLngBounds);
    }

    @Override
    public void setBBox(LatLngBounds latLngBounds) {
        this.latLngBounds = latLngBounds;
    }

    @Override
    public void clearOverlay() {
        if (vehicleOverlay != null) {
            vehicleOverlay.remove();
        }
        progressBar.setVisibility(View.INVISIBLE);
        errorSign.setVisibility(View.INVISIBLE);
    }
}
