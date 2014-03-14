package com.emal.android.transport.spb.activity;

import android.os.Bundle;
import android.os.Handler;
import android.view.*;
import android.widget.FrameLayout;
import com.google.android.gms.maps.GoogleMap;

/**
 * @author alexey.emelyanenko@gmail.com
 * @since: 1.5
 */
public class TouchableMapFragment extends com.google.android.gms.maps.SupportMapFragment {
    public View mOriginalContentView;
    public TouchableWrapper mTouchView;
    private onMapReady onMapReadyCallback;
    private ZoomSupport zoomSupport;
    private GestureDetector gestureDetector;

    private class TouchableWrapper extends FrameLayout {
        private GMapsV2Activity activity;

        public TouchableWrapper(GMapsV2Activity activity) {
            super(activity);
            this.activity = activity;
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    activity.setTouched(true);
                    break;
                case MotionEvent.ACTION_UP:
                    activity.setTouched(false);
                    break;
            }

            int maskedAction = event.getActionMasked();
            System.out.println("maskedAction - " + maskedAction);
            switch (maskedAction) {
//                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN: {
                    // We have a new pointer. Lets add it to the list of pointers
                    zoomSupport.before();
                    activity.setTouched(true);
                    break;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                case MotionEvent.ACTION_CANCEL: {
//                    zoomSupport.after();
                    activity.setTouched(false);
                    break;
                }
            }
            gestureDetector.onTouchEvent(event);
            return super.dispatchTouchEvent(event);
        }
    }

    public interface onMapReady {
        void setMap(GoogleMap map);
        void updateMap(GoogleMap map);
    }

    public interface ZoomSupport {
        void before();
        void after();
    }

    public void setOnMapReadyCallback(onMapReady onMapReadyCallback) {
        this.onMapReadyCallback = onMapReadyCallback;
    }

    public void setZoomSupport(final ZoomSupport zoomSupport) {
        this.zoomSupport = zoomSupport;
        this.gestureDetector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                zoomSupport.before();
                return super.onDoubleTapEvent(e);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        mOriginalContentView = super.onCreateView(inflater, parent, savedInstanceState);
        mTouchView = new TouchableWrapper((GMapsV2Activity) getActivity());
        mTouchView.addView(mOriginalContentView);
        return mTouchView;
    }

    @Override
    public View getView() {
        return mOriginalContentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                GoogleMap map = getMap();

                if (map != null) {
                    onMapReadyCallback.setMap(map);
                    handler.removeCallbacksAndMessages(null);
                } else {
                    handler.postDelayed(this, 500);
                }
            }
        }, 500);
    }

    @Override
    public void onResume() {
        super.onResume();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                GoogleMap map = getMap();

                if (map != null) {
                    onMapReadyCallback.updateMap(map);
                    handler.removeCallbacksAndMessages(null);
                } else {
                    handler.postDelayed(this, 500);
                }
            }
        }, 500);
    }
}
