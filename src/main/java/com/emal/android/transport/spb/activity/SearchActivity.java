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


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author alexey.emelyanenko@gmail.com
 * @since 1.5
 */
public class SearchActivity extends Activity {
    private static final String TAG = SearchActivity.class.getName();
    private SearchView searchView;
    private ListView listView;
    private AsyncTask searchTask;
    private PortalClient portalClient;
    private Activity _this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        _this = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        portalClient = new PortalClient();
        searchView = (SearchView) findViewById(R.id.searchView);
        listView = (ListView) findViewById(R.id.searchResultView);

        final Context a = this;
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
                searchTask = new SearchTask(listView, a);
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
            } catch (IOException e) {
                e.printStackTrace();
                routes = Collections.emptyList();
            }
            return routes;

        }

        @Override
        protected void onPostExecute(List<Route> list) {
            List<String> results = new ArrayList<String>();
            for (Route r : list) {
                String routeNumber = r.getRouteNumber();
                String routePoints = r.getName();
                String routeType = (String) ((Map)(r.getTransportType())).get("name");
                int id = r.getId();
                results.add(String.valueOf(id) + "#" + routeNumber + "/" + routeType + "/" + routePoints);
            }

            ArrayAdapter<String> listArrayAdapter = new ArrayAdapter<String>(context, com.emal.android.transport.spb.R.layout.drawer_list_item, results);
            listView.setAdapter(listArrayAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    CharSequence text = ((TextView) view).getText();
                    Intent mapIntent = new Intent("1");
                    mapIntent.putExtra("ROUTE_KEY", (String) text);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(mapIntent);
                    finish();
                }
            });
        }
    }
}
