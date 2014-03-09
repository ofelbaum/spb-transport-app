package com.emal.android.transport.spb;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.view.Menu;
import com.emal.android.transport.spb.portal.PortalClient;
import com.emal.android.transport.spb.portal.Route;
import com.emal.android.transport.spb.utils.Constants;
import com.emal.android.transport.spb.utils.ErrorSignCallback;
import com.emal.android.transport.spb.utils.GeoConverter;
import com.emal.android.transport.spb.utils.MenuErrorSignCallback;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: alexey.emelyanenko@gmail.com
 * Date: 5/19/13 1:18 AM
 */
public class GMapVehicleSyncAdapter implements VehicleSyncAdapter {
    private static final String TAG = GMapVehicleSyncAdapter.class.getName();
    private Activity activity;
    private SupportMapFragment mapFragment;
    private int syncTime = Constants.DEFAULT_SYNC_MS;
    private GroundOverlay vehicleOverlay;
    private LatLngBounds latLngBounds;
    private ErrorSignCallback errorSignCallback;
    private int iconSize = Constants.DEFAULT_ICON_SIZE;
    private Map<Route, List<Marker>> markers;

    public GMapVehicleSyncAdapter(SupportMapFragment mapFragment, Menu menu) {
        this.mapFragment = mapFragment;
        this.activity = mapFragment.getActivity();
        this.errorSignCallback = new MenuErrorSignCallback(menu);
        this.markers = new ConcurrentHashMap<Route, List<Marker>>();
    }

    @Override
    public void beforeSync(boolean clearBeforeUpdate) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.setProgressBarIndeterminateVisibility(true);
            }
        });
        if (errorSignCallback.isShowed()) {
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
            errorSignCallback.hide();
        } else {
            new Handler().postAtTime(new Runnable() {
                @Override
                public void run() {
                    errorSignCallback.show();
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
            errorSignCallback.hide();

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = 4;
            BitmapDescriptor image = BitmapDescriptorFactory.fromBitmap(result);

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
                    errorSignCallback.show();
                }
            }, 1000);
        }
    }

    @Override
    public int getScreenWidth() {
        return (int) (mapFragment.getView().getWidth() / getScaleFactor());
    }

    @Override
    public int getScreenHeight() {
        return (int) (mapFragment.getView().getHeight() / getScaleFactor());
    }

    @Override
    public String getBBox() {
        return GeoConverter.calculateBBox(latLngBounds);
    }

    @Override
    public void setBBox() {
        //TODO
        this.latLngBounds = mapFragment.getMap().getProjection().getVisibleRegion().latLngBounds;
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

        errorSignCallback.hide();
    }

    @Override
    public Marker addMarker(MarkerOptions options) {
        GoogleMap map = mapFragment.getMap();
        return map != null ? map.addMarker(options) : null;
    }

    @Override
    public PortalClient getPortalClient() {
        return PortalClient.getInstance();
    }

    @Override
    public void hideError() {
        errorSignCallback.hide();
    }

    @Override
    public void showError() {
        errorSignCallback.show();
    }

    @Override
    public void moveCamera(CameraUpdate cameraUpdate) {
        mapFragment.getMap().moveCamera(cameraUpdate);
    }

    @Override
    public float getScaleFactor() {
        return iconSize / 5f + 1;
    }

    public void setIconSize(int iconSize) {
        this.iconSize = iconSize;
    }

    @Override
    public int getSyncTime() {
        return syncTime;
    }

    public void setSyncTime(int syncTime) {
        this.syncTime = syncTime;
    }

    @Override
    public void updateMarkers(Route route, List<Marker> list) {
        removeMarkers(route);
        markers.put(route, list);
    }

    @Override
    public void removeMarkers(Route route) {
        List<Marker> list = markers.get(route);
        if (list == null) {
            return;
        }
        for (Marker marker : list) {
            marker.remove();
        }
    }
}
