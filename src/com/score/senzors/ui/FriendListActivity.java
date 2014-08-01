package com.score.senzors.ui;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import com.score.senzors.R;
import com.score.senzors.application.SenzorApplication;
import com.score.senzors.pojos.User;
import com.score.senzors.utils.ActivityUtils;

import java.util.ArrayList;

/**
 * Contact list when sharing sensor
 *
 * @author eranga herath(erangeb@gmail.com)
 */
public class FriendListActivity extends Activity implements SearchView.OnQueryTextListener {

    private SenzorApplication application;
    private ListView friendListView;
    private FriendListAdapter friendListAdapter;
    private ArrayList<User> friendList;

    /**
     * {@inheritDoc}
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend_list_layout);
        application = (SenzorApplication) this.getApplication();

        setActionBar();
        initFriendList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE );
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(this);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                FriendListActivity.this.finish();
                FriendListActivity.this.overridePendingTransition(R.anim.stay_in, R.anim.bottom_out);
                ActivityUtils.hideSoftKeyboard(this);

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Set action bar
     *      1. properties
     *      2. title with custom font
     */
    private void setActionBar() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle("Friends");

        Typeface typeface = Typeface.createFromAsset(this.getAssets(), "fonts/vegur_2.otf");
        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView actionBarTitle = (TextView) (this.findViewById(titleId));
        actionBarTitle.setTextColor(getResources().getColor(R.color.white));
        actionBarTitle.setTypeface(typeface);
    }

    /**
     * Initialize friend list
     */
    private void initFriendList() {
        friendList = application.getContactList();
        friendListView = (ListView) findViewById(R.id.friend_list);
        friendListAdapter = new FriendListAdapter(this, friendList);

        // add header and footer for list
        View headerView = View.inflate(this, R.layout.list_header, null);
        View footerView = View.inflate(this, R.layout.list_header, null);
        friendListView.addHeaderView(headerView);
        friendListView.addFooterView(footerView);
        friendListView.setAdapter(friendListAdapter);
        friendListView.setTextFilterEnabled(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.overridePendingTransition(R.anim.stay_in, R.anim.bottom_out);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        friendListAdapter.getFilter().filter(newText);

        return true;
    }
}
