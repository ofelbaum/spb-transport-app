package com.emal.android.transport.spb;

import android.app.Activity;
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
    private Activity activity;
    private SupportMapFragment mapFragment;
    private View errorSign;
    private GroundOverlay vehicleOverlay;
    private LatLngBounds latLngBounds;

    public GMapVehicleSyncAdapter(Activity activity, SupportMapFragment mapFragment) {
        this.activity = activity;
        this.mapFragment = mapFragment;
        this.errorSign = mapFragment.getActivity().findViewById(com.emal.android.transport.spb.R.id.errorSignLayout);
    }

    @Override
    public void beforeSync(boolean clearBeforeUpdate) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setProgressBarIndeterminateVisibility(true);
            }
        });
        if (errorSign.getVisibility() == View.INVISIBLE) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.setProgressBarIndeterminateVisibility(true);
                }
            });
        }

        if (clearBeforeUpdate) {
            if (vehicleOverlay != null) {
                vehicleOverlay.remove();
            }
        }
    }

    @Override
    public void afterSync(boolean result) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setProgressBarIndeterminateVisibility(false);
            }
        });

        if (Boolean.TRUE.equals(result)) {
            errorSign.setVisibility(View.INVISIBLE);

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
    public void afterSync(Bitmap result) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setProgressBarIndeterminateVisibility(false);
            }
        });

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
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setProgressBarIndeterminateVisibility(false);
            }
        });

        errorSign.setVisibility(View.INVISIBLE);
    }
}
