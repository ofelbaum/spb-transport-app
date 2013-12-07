package com.emal.android.transport.spb.activity;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.view.*;
import android.view.MenuItem;
import android.widget.*;
import com.emal.android.transport.spb.R;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        _this = this;
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.search);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setTitle(R.string.search_page);

        appParams = new ApplicationParams(getSharedPreferences(Constants.APP_SHARED_SOURCE, 0));

        portalClient = new PortalClient();
        searchView = (SearchView) findViewById(R.id.searchView);
        searchView.setIconified(false);
        listView = (ListView) findViewById(R.id.searchResultView);

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
                selectedRoutes.add(route);
                redrawSelection(selectedRoutes);
                appParams.getRoutesToTrack().add(Route.encode(route));

            }
        });

        Button button = (Button) findViewById(R.id.clearSelectedRoutes);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedRoutes.clear();
                redrawSelection(selectedRoutes);
                appParams.getRoutesToTrack().clear();
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

    private void redrawSelection(Collection<Route> routes) {
        StringBuilder builder = new StringBuilder();
        Iterator<Route> it = routes.iterator();
        while (it.hasNext()) {
            builder.append(it.next().getRouteNumber());
            if (it.hasNext()) {
                builder.append(", ");
            }
        }
        TextView textView = (TextView) findViewById(R.id.selectedRoutesList);
        textView.setText(builder.toString());
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
            setProgressBarIndeterminateVisibility(true);
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
            setProgressBarIndeterminateVisibility(false);
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
}
