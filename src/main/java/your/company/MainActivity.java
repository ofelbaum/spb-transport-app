package your.company;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ZoomButtonsController;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

public class MainActivity extends MapActivity {
    private MapView mapView;
    private LocationManager lm;
    GeoPoint currentPoint;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mapView = (MapView) findViewById(R.id.mapView);


        mapView.setBuiltInZoomControls(true);
        mapView.displayZoomControls(true);
        mapView.getController().setZoom(15);

        MyLocationOverlay mylocationOverlay = new MyLocationOverlay(mapView.getContext(), mapView);
        mylocationOverlay.enableMyLocation();
        mapView.getOverlays().add(mylocationOverlay);


        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
            public void onLocationChanged(Location location) {
                currentPoint = new GeoPoint((int) (location.getLatitude() * 1E6), (int) (location.getLongitude() * 1E6));
                mapView.getController().animateTo(currentPoint);
            }

            public void onStatusChanged(String s, int i, Bundle bundle) {
            }

            public void onProviderEnabled(String s) {
            }

            public void onProviderDisabled(String s) {
            }
        });

        mapView.setOnGenericMotionListener(new View.OnGenericMotionListener() {
            public boolean onGenericMotion(View v, MotionEvent event) {
                if (MotionEvent.ACTION_HOVER_MOVE == event.getAction()) {
                    MapView mapView = (MapView) v;

                    int latitudeSpan = mapView.getLatitudeSpan();
                    int longitudeSpan = mapView.getLongitudeSpan();
                    int latitudeE6 = mapView.getMapCenter().getLatitudeE6();
                    int longitudeE6 = mapView.getMapCenter().getLongitudeE6();
                    int windowH = mapView.getHeight();
                    int windowW = mapView.getWidth();

                    int x1 = latitudeE6 - latitudeSpan / 2;
                    int y1 = longitudeE6 - longitudeSpan / 2;
                    int x2 = latitudeE6 + latitudeSpan / 2;
                    int y2 = longitudeE6 + longitudeSpan / 2;
                    String str = "[" + windowH + " ," + windowW +"] [" + latitudeSpan + " ," + longitudeSpan +"] {" + x1 + "," + y1 + "} - {" + x2 + "," + y2 + "}";
                    System.out.println(str);

                    Toast.makeText(mapView.getContext(), str, Toast.LENGTH_LONG).show();
                    return true;
                }

                return false;
            }
        });

    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
}
