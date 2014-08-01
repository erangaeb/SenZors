package com.score.senzors.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.score.senzors.R;
import com.score.senzors.pojos.User;

import java.util.ArrayList;

/**
 * Display friend list
 *
 * @author eranga herath(erangaeb@gmail.com)
 */
public class FriendListAdapter extends BaseAdapter {

    private FriendListActivity activity;
    private Typeface typeface;
    private ArrayList<User> friendList;

    /**
     * Initialize context variables
     * @param activity friend list activity
     * @param friendList friend list
     */
    public FriendListAdapter(FriendListActivity activity, ArrayList<User> friendList) {
        this.activity = activity;
        this.friendList = friendList;

        typeface = Typeface.createFromAsset(activity.getAssets(), "fonts/vegur_2.otf");
    }

    /**
     * Get size of user list
     * @return userList size
     */
    @Override
    public int getCount() {
        return friendList.size();
    }

    /**
     * Get specific item from user list
     * @param i item index
     * @return list item
     */
    @Override
    public Object getItem(int i) {
        return friendList.get(i);
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
     * @param position index
     * @param view current list item view
     * @param parent parent
     * @return view
     */
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        // A ViewHolder keeps references to children views to avoid unnecessary calls
        // to findViewById() on each row.
        final ViewHolder holder;
        final User user = (User) getItem(position);

        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.friend_list_row_layout, parent, false);
            holder = new ViewHolder();
            //holder.userIcon = (ImageView) view.findViewById(R.id.user_icon);
            holder.iconText = (TextView) view.findViewById(R.id.icon_text);
            holder.name = (TextView) view.findViewById(R.id.friend_list_row_layout_name);
            holder.iconText.setTypeface(typeface, Typeface.BOLD);
            holder.iconText.setTextColor(activity.getResources().getColor(R.color.yello));
            holder.name.setTypeface(typeface, Typeface.NORMAL);

            view.setTag(holder);
        } else {
            // get view holder back
            holder = (ViewHolder) view.getTag();
        }

        // bind text with view holder content view for efficient use
        //holder.userIcon.setBackgroundResource(R.drawable.my_icon);
        holder.name.setText(user.getEmail());
        holder.name.setText(user.getEmail());
        view.setBackgroundResource(R.drawable.friend_list_selector);

        return view;
    }

    /**
     * Keep reference to children view to avoid unnecessary calls
     */
    static class ViewHolder {
        ImageView userIcon;
        TextView iconText;
        TextView name;
    }

}
