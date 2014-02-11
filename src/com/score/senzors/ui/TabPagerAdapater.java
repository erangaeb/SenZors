package com.score.senzors.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Tab adapter to implement swipe view
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class TabPagerAdapater extends FragmentStatePagerAdapter {
    public TabPagerAdapater(FragmentManager fm) {
        super(fm);
    }
    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                //Fragement for Android Tab
                return new SensorMapFragment();
            case 1:
                //Fragment for Ios Tab
                return new FriendList();
        }
        return null;
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return 2;
    }
}
