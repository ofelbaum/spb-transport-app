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


import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author alexey.emelyanenko@gmail.com
 * @since 1.5
 */
public class SearchActivity extends Activity {
    private static final String TAG = SearchActivity.class.getName();
    private SearchView searchView;
    private ListView listView;
    private AsyncTask searchTask;
    private PortalClient portalClient = PortalClient.getInstance();
    public static final String SELECTED_ROUTES = "SELECTED_ROUTES";
    private ApplicationParams appParams;
    private RoutesStorage routesStorage = new RoutesStorage();
    private List<Route> findedRoutes;
    private Set<Route> selectedRoutes = new ConcurrentSkipListSet<Route>();
    private LinearLayout selectedRoutesPics;
    private ImageButton clearFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        appParams = new ApplicationParams(getSharedPreferences(Constants.APP_SHARED_SOURCE, 0));
        setTheme(appParams.getTheme().getCode());

        setContentView(R.layout.search);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(R.string.search_page);

        searchView = (SearchView) findViewById(R.id.searchView);
        searchView.setIconified(false);
        listView = (ListView) findViewById(R.id.searchResultView);
        selectedRoutesPics = (LinearLayout) findViewById(R.id.selectedRoutesList);
        clearFilter = (ImageButton) findViewById(R.id.clearFilter);
        clearFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedRoutesPics.removeAllViews();
                selectedRoutesPics.refreshDrawableState();
                appParams.getRoutesToTrack().clear();
                clearFilter.setVisibility(View.INVISIBLE);
            }
        });

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
                Set<String> routesToTrack = appParams.getRoutesToTrack();
                String encode = Route.encode(route);
                if (!routesToTrack.contains(encode)) {
                    routesToTrack.add(encode);
                    selectedRoutes.add(route);
                    drawSelection(route);
                    clearFilter.setVisibility(View.VISIBLE);
                }
            }
        });

        Bundle b = getIntent().getExtras();
        selectedRoutes.addAll((ArrayList<Route>) b.getSerializable(SELECTED_ROUTES));
        redrawSelection(selectedRoutes);

        initIndex();
    }

    @Override
    protected void onPause() {
        appParams.saveAll();
        super.onPause();
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
                Boolean res = appParams.getRoutesToTrack().remove(Route.encode(next));
                if (selectedRoutesPics.getChildCount() == 0) {
                    clearFilter.setVisibility(View.INVISIBLE);
                }
                Log.d(TAG, res.toString());
            }
        });
        selectedRoutesPics.addView(imageView);
        clearFilter.setVisibility(View.VISIBLE);
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
            } catch (IOException e) {
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

            if (list != null) {
                findedRoutes = routesStorage.rebuildStorage(list, appParams.getSelectedVehicleTypes());
                queryAndShowResult(searchView.getQuery().toString());
            } else if (!isFinishing()) {
                //An error occurred
                UIHelper.getErrorDialog(SearchActivity.this);
            }
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
        canvas.drawText(route.getRouteNumber(), xPos, yPos, textPaint);
        Bitmap bm = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_delete), 25, 25, true);
        canvas.drawBitmap(bm, 38, -3, rectPaint);
        canvas.restore();

        return bitmap;
    }
}
