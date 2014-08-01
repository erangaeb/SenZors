package com.score.senzors.ui;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import com.score.senzors.R;
import com.score.senzors.application.SenzorApplication;
import com.score.senzors.pojos.User;

import java.util.ArrayList;

/**
 * Contact list when sharing sensor
 *
 * @author eranga herath(erangeb@gmail.com)
 */
public class FriendListActivity extends Activity {

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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.overridePendingTransition(R.anim.stay_in, R.anim.bottom_out);
    }
}
