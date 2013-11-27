package com.emal.android.transport.spb.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.emal.android.transport.spb.R;
import com.emal.android.transport.spb.portal.PortalClient;
import com.emal.android.transport.spb.portal.Route;
import com.emal.android.transport.spb.utils.UIHelper;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        _this = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

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
                if (searchTask != null) {
                    searchTask.cancel(true);
                }
                searchTask = new SearchTask(listView, _this);
                searchTask.execute(newText);
                return true;
            }
        });

    }

    private class SearchTask extends AsyncTask<Object, Void, List<Route>> {

        private ListView listView;
        private Context context;

        private SearchTask(ListView listView, Context context) {
            this.listView = listView;
            this.context = context;
        }

        @Override
        protected List<Route> doInBackground(Object... params) {
            List<Route> routes;
            try {
                routes = portalClient.findRoutes((String) params[0]);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            return routes != null ? routes : Collections.<Route>emptyList();
        }

        @Override
        protected void onPostExecute(final List<Route> list) {
            if (list == null) {
                //An error occurred
                UIHelper.getErrorDialog(listView.getContext());
                return;
            }
            List<String> results = new ArrayList<String>();
            for (Route r : list) {
                String routeNumber = r.getRouteNumber();
                String routePoints = r.getName();
                String routeType = r.getTransportType().name();
                results.add(routeNumber + "/" + routeType + "/" + routePoints);
            }

            ArrayAdapter<String> listArrayAdapter = new ArrayAdapter<String>(context, R.layout.search_list_item, results);
            listView.setAdapter(listArrayAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent mapIntent = new Intent(SEARCH_INTEND_ID);
                    mapIntent.putExtra(ROUTE_DATA_KEY, list.get(position));
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(mapIntent);
                    finish();
                }
            });
        }
    }
}
