package com.usepressbox.pressbox.adapter;

import android.app.Dialog;
import android.content.Context;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.usepressbox.pressbox.R;
import com.usepressbox.pressbox.utils.SessionManager;


/*Created BY RajeshKanna on 24/08/2018
*  Modified by Prasanth.S */

public class OnboardPageAdapter extends PagerAdapter {

    Context mContext;
    Dialog mdialog;
    View v;
    SessionManager sessionManager;
    public OnboardPageAdapter(Context context, Dialog dialog) {
        mContext = context;
        mdialog =dialog;
        sessionManager   = new SessionManager(context);
    }


    @NonNull
    public Object instantiateItem(final ViewGroup collection, int position) {

        v = new View(collection.getContext());


        LayoutInflater inflater =
                (LayoutInflater) collection.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        int resId = 0;
        switch (position) {
            case 0:
                resId = R.layout.onboard_placeitems;
                v = inflater.inflate(R.layout.onboard_placeitems, null, false);
                break;
            case 1:
                resId = R.layout.onboard_submitorders;
                v = inflater.inflate(R.layout.onboard_submitorders, null, false);
                break;
            case 2:
                resId = R.layout.onboard_pickup;
                v = inflater.inflate(R.layout.onboard_pickup, null, false);
                break;
        }

        ((ViewPager) collection).addView(v, 0);

        return v;


    }

    @Override
    public int getCount() {
        return 3;
    }
    @Override
    public void destroyItem(View container, int position, Object object) {
        ((ViewPager) container).removeView((View) object);
    }
    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }


}