package com.emal.android.transport.spb.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;
import com.emal.android.transport.spb.model.ApplicationParams;
import com.emal.android.transport.spb.model.MenuItemAdapter;
import com.emal.android.transport.spb.model.MenuModel;
import com.emal.android.transport.spb.model.Theme;
import com.emal.android.transport.spb.utils.Constants;
import com.google.android.gms.R;

/**
 * @author alexey.emelyanenko@gmail.com
 * @since: 2.0
 */
public abstract class AbstractDrawerActivity extends FragmentActivity {
    private static final String TAG = AbstractDrawerActivity.class.getName();
    private ApplicationParams appParams;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    protected Theme currentTheme;
    protected Menu menu;

    protected abstract void onMenuItemClick(int position);
    protected abstract boolean onOptionsItemSelected(int pos);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        appParams = new ApplicationParams(getSharedPreferences(Constants.APP_SHARED_SOURCE, 0));

        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        currentTheme = appParams.getTheme();
        setTheme(currentTheme.getCode());
        setContentView(R.layout.main);

        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(com.emal.android.transport.spb.R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(com.emal.android.transport.spb.R.id.left_drawer);

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        MenuModel menuModel = new MenuModel(getResources());
        mDrawerList.setAdapter(new MenuItemAdapter(this, com.emal.android.transport.spb.R.layout.drawer_list_item, menuModel));
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onMenuItemClick(position);
                mDrawerLayout.closeDrawer(mDrawerList);
            }
        });

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
//        getActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                com.emal.android.transport.spb.R.string.drawer_open,  /* "open drawer" description for accessibility */
                com.emal.android.transport.spb.R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(com.emal.android.transport.spb.R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(com.emal.android.transport.spb.R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return onOptionsItemSelected(item.getItemId());
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
}
