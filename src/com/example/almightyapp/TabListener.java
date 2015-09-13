package com.example.almightyapp;

import com.example.almightyapp.R;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Fragment;
import android.app.FragmentTransaction;

public class TabListener implements ActionBar.TabListener {
    private Fragment _fragment;

    /** Constructor used each time a new tab is created.
      * @param fragment The fragment that this tab listener is tied to 
      */
    public TabListener(Fragment fragment) {
        _fragment = fragment;
    }

    public void onTabSelected(Tab tab, FragmentTransaction ft) {
    	ft.replace(R.id.layout_activity_main, _fragment);
    }

    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
    	ft.remove(_fragment);
    }

    // User selected the already selected tab. Usually do nothing.
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
    	
    }
}