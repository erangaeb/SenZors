package com.score.senzors.ui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ListView;
import android.widget.TextView;
import com.score.senzors.application.SenzorApplication;
import com.score.senzors.pojos.User;
import com.score.senzors.R;

import java.util.ArrayList;

/**
 * Display friend list/ Fragment
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class FriendList extends Fragment {
    // use to populate list
    private SenzorApplication application;
    private ListView friendListView;
    private ArrayList<User> userList;
    private FriendListAdapter adapter;

    Typeface typeface;

    // to handle empty view
    private ViewStub emptyView;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // after creating fragment we initialize friend list
        // TODO need to fill friend list with backend data
        //getActivity().getActionBar().setTitle("Friends");
        typeface = Typeface.createFromAsset(this.getActivity().getAssets(), "fonts/Roboto-Thin.ttf");
        application = (SenzorApplication) this.getActivity().getApplication();
        initEmptyView();
        initFriendList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.friend_list_layout, null);
        initUI(root);

        return root;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();

        // construct list adapter
        if(userList.size()>0) {
            adapter = new FriendListAdapter(FriendList.this.getActivity(), userList);
            friendListView.setAdapter(adapter);
        } else {
            friendListView.setEmptyView(emptyView);
        }
    }

    /**
     * Initialize UI components
     */
    private void initUI(View view) {
        friendListView = (ListView)view.findViewById(R.id.friend_list_layout_friend_list);

        // add header and footer for list
        View headerView = View.inflate(this.getActivity(), R.layout.list_header, null);
        View footerView = View.inflate(this.getActivity(), R.layout.list_header, null);
        friendListView.addHeaderView(headerView);
        friendListView.addFooterView(footerView);
    }

    /**
     * Initialize empty view for list view
     * empty view need to be display when no sensors available
     */
    private void initEmptyView() {
        //Log.d(TAG, "InitEmptyView: initializing empty view");
        emptyView = (ViewStub) getActivity().findViewById(R.id.sensor_list_layout_empty_view);
        View inflatedEmptyView = emptyView.inflate();
        TextView emptyText = (TextView) inflatedEmptyView.findViewById(R.id.empty_text);
        emptyText.setText("Sensor not shared with any user");
        emptyText.setTypeface(typeface, Typeface.BOLD);
    }

    /**
     * Create sensor list
     */
    private void initFriendList() {
        // populate sample data to list
        userList = new ArrayList<User>();
        //userList.add(new User("0", "eranga", "erangaeb@gmail.com", ""));
        //userList.add(new User("0", "pagero", "pagero@gmail.com", ""));
        //userList.add(new User("0", "test", "test@gmail.com", ""));
        //userList.add(new User("0", "herath", "herath@gmail.com", ""));
        //userList.add(new User("0", "vijith", "vijith@gmail.com", ""));
        if(application.getCurrentSensor().getSharedUsers() != null)
            userList = application.getCurrentSensor().getSharedUsers();

        // construct list adapter
        if(userList.size()>0) {
            adapter = new FriendListAdapter(FriendList.this.getActivity(), userList);
            friendListView.setAdapter(adapter);
        } else {
            friendListView.setEmptyView(emptyView);
        }
    }
}
