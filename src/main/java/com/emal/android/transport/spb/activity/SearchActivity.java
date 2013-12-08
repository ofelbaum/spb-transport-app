package com.emal.android.transport.spb.activity;

import android.app.Activity;
import android.graphics.*;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.view.*;
import android.view.MenuItem;
import android.widget.*;
import com.emal.android.transport.spb.R;
import com.emal.android.transport.spb.VehicleType;
import com.emal.android.transport.spb.model.ApplicationParams;
import com.emal.android.transport.spb.model.RouteItemsAdapter;
import com.emal.android.transport.spb.model.RoutesStorage;
import com.emal.android.transport.spb.portal.PortalClient;
import com.emal.android.transport.spb.portal.Route;
import com.emal.android.transport.spb.utils.*;


import java.util.*;

/**
 * @author alexey.emelyanenko@gmail.com
 * @since 1.5
 */
public class SearchActivity extends Activity {
    private static final String TAG = SearchActivity.class.getName();
    public static final String SEARCH_INTEND_ID = TAG;
    private SearchView searchView;
    private ListView listView;
    private AsyncTask searchTask;
    private PortalClient portalClient;
    private Activity _this;
    public static final String ROUTE_DATA_KEY = "ROUTE_DATA";
    public static final String SELECTED_ROUTES = "SELECTED_ROUTES";
    private ApplicationParams appParams;
    private RoutesStorage routesStorage = new RoutesStorage();
    private List<Route> findedRoutes;
    private Set<Route> selectedRoutes;
    private LinearLayout selectedRoutesPics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        _this = this;
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.search);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(R.string.search_page);

        appParams = new ApplicationParams(getSharedPreferences(Constants.APP_SHARED_SOURCE, 0));

        portalClient = new PortalClient();
        searchView = (SearchView) findViewById(R.id.searchView);
        searchView.setIconified(false);
        listView = (ListView) findViewById(R.id.searchResultView);
        selectedRoutesPics = (LinearLayout) findViewById(R.id.selectedRoutesList);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "Search query [" + newText + "]");
                queryAndShowResult(newText);
                return true;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                finish();
                return true;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Route route = findedRoutes.get(position);
//                Intent mapIntent = new Intent(SEARCH_INTEND_ID);
//                mapIntent.putExtra(ROUTE_DATA_KEY, findedRoutes.get(position));
//                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(mapIntent);
//                finish();
                Set<String> routesToTrack = appParams.getRoutesToTrack();
                String encode = Route.encode(route);
                if (!routesToTrack.contains(encode)) {
                    routesToTrack.add(encode);
                    selectedRoutes.add(route);
                    drawSelection(route);
                }
            }
        });

        Bundle b = getIntent().getExtras();
        ArrayList<Route> routes = (ArrayList<Route>) b.getSerializable(SELECTED_ROUTES);
        redrawSelection(routes);
        selectedRoutes = new HashSet<Route>(routes);

        initIndex();
    }

    private void queryAndShowResult(String newText) {
        findedRoutes = routesStorage.find(newText);
        RouteItemsAdapter adapter = new RouteItemsAdapter(searchView.getContext(), R.layout.search_list_item, findedRoutes);
        listView.setAdapter(adapter);
    }

    private void redrawSelection(final Collection<Route> routes) {
        Iterator<Route> it = routes.iterator();
        while (it.hasNext()) {
            final Route next = it.next();
            drawSelection(next);
        }
    }

    private void drawSelection(final Route next) {
        Bitmap vehicleBitmap = getVehicleBitmap(next);
        final ImageView imageView = new ImageView(searchView.getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(5, 5, 5, 5);
        imageView.setLayoutParams(lp);
        imageView.setImageBitmap(vehicleBitmap);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setImageBitmap(null);
                selectedRoutesPics.removeView(imageView);
                selectedRoutesPics.refreshDrawableState();
                appParams.getRoutesToTrack().remove(Route.encode(next));
            }
        });
        selectedRoutesPics.addView(imageView);
    }

    private void initIndex() {
        Log.d(TAG, "Init index");
        if (searchTask != null) {
            searchTask.cancel(true);
        }
        searchTask = new SearchTask();
        searchTask.execute();
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home: {
                finish();
            }
        }
        return true;
    }

    private class SearchTask extends AsyncTask<Object, Void, List<Route>> {

        @Override
        protected List<Route> doInBackground(Object... params) {
            SearchActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setProgressBarIndeterminateVisibility(true);
                }
            });

            List<Route> routes;
            try {
                routes = portalClient.findAllRoutes();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            return routes != null ? routes : Collections.<Route>emptyList();
        }

        @Override
        protected void onPostExecute(final List<Route> list) {
            SearchActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setProgressBarIndeterminateVisibility(false);
                }
            });

            if (list == null) {
                //An error occurred
                UIHelper.getErrorDialog(listView.getContext());
                return;
            }
            routesStorage.setRouteList(list);
            findedRoutes = list;
            queryAndShowResult(searchView.getQuery().toString());
        }
    }

    private Bitmap getVehicleBitmap(Route route) {
        VehicleType type = route.getTransportType();
        int bHeigth = 60;
        int bWidth = 60;
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = Bitmap.createBitmap(bWidth, bHeigth, conf);

        Paint textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setFilterBitmap(true);
        textPaint.setTextSize(25);
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
        int yPos = (int) ((canvas.getHeight() / 2) - ((textPaint.descent() + textPaint.ascent()) / 2));

        canvas.drawRect(0, 0, bHeigth, bWidth, rectPaint);
        canvas.save();
//        if (type.isUpsideDown()) {
//            int x = canvas.getClipBounds().centerX();
//            int y = canvas.getClipBounds().centerY();
//            canvas.rotate(180, x, y);
//
//            yPos += 2; //TODO fix
//        } else {
//        }

//        yPos++; //TODO fix
        canvas.drawText(route.getRouteNumber(), xPos, yPos, textPaint);
        canvas.restore();

        return bitmap;
    }
}
