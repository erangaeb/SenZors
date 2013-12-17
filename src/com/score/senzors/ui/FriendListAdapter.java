package com.score.senzors.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.score.senzors.pojos.User;
import com.score.senzors.R;

import java.util.ArrayList;

/**
 * Adapter class to display friend/user list
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class FriendListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<User> userList;
    //private Pay2nApplication application;

    // set custom font
    Typeface face;

    /**
     * Initialize context variables
     * @param context activity context
     * @param userList user list
     */
    public FriendListAdapter(Context context, ArrayList<User> userList) {
        //application = (Pay2nApplication) context.getApplicationContext();

        face = Typeface.createFromAsset(context.getAssets(), "fonts/vegur_2.otf");

        this.context = context;
        this.userList = userList;
    }

    /**
     * Get size of user list
     * @return userList size
     */
    @Override
    public int getCount() {
        return userList.size();
    }

    /**
     * Get specific item from user list
     * @param i item index
     * @return list item
     */
    @Override
    public Object getItem(int i) {
        return userList.get(i);
    }

    /**
     * Get user list item id
     * @param i item index
     * @return current item id
     */
    @Override
    public long getItemId(int i) {
        return i;
    }

    /**
     * Create list row view
     * @param i index
     * @param view current list item view
     * @param viewGroup parent
     * @return view
     */
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        // A ViewHolder keeps references to children views to avoid unnecessary calls
        // to findViewById() on each row.
        final ViewHolder holder;

        final User user = (User) getItem(i);

        if (view == null) {
            //inflate sharing_list_row_layout
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.attribute_list_row_layout, viewGroup, false);

            //create view holder to store reference to child views
            holder = new ViewHolder();
            holder.name = (TextView) view.findViewById(R.id.attribute_name);
            holder.email = (TextView) view.findViewById(R.id.attribute_value);

            holder.name.setTypeface(face);
            holder.email.setTypeface(face);

            view.setTag(holder);
        } else {
            //get view holder back
            holder = (ViewHolder) view.getTag();
        }

        // bind text with view holder content view for efficient use
        holder.name.setText(user.getUsername());
        holder.email.setText(user.getEmail());
        view.setBackgroundResource(R.drawable.friend_list_selector);

        return view;
    }

    /**
     * Keep reference to children view to avoid unnecessary calls
     */
    static class ViewHolder {
        TextView name;
        TextView email;
    }
}
