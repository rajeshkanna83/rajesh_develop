package com.usepressbox.pressbox.ui.activity.order;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.survicate.surveys.Survicate;
import com.usepressbox.pressbox.R;
import com.usepressbox.pressbox.asyntasks.BackgroundTask;
import com.usepressbox.pressbox.asyntasks.GetDetergentIdsTask;
import com.usepressbox.pressbox.asyntasks.GetStarchIdsTask;
import com.usepressbox.pressbox.asyntasks.SaveOrderPreferenceTask;
import com.usepressbox.pressbox.interfaces.IOrderPreferenceListener;
import com.usepressbox.pressbox.models.OrderPreference;
import com.usepressbox.pressbox.utils.Constants;
import com.usepressbox.pressbox.utils.SessionManager;
import com.usepressbox.pressbox.utils.UtilityClass;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by kruno on 20.04.16..
 * Modified by Prasanth.S on 08/13/2018
 * This class is used to set the order preferences to place the order
 */
public class OrderPreferences extends AppCompatActivity implements IOrderPreferenceListener {

    private Toolbar toolbar;
    Boolean flag = true, flag1 = true;
    @BindView(R.id.sb_starch_level)
    SeekBar starchLevel;
    @BindView(R.id.tw_order_preferences_light)
    TextView preferences_light;
    @BindView(R.id.tw_order_preferences_medium)
    TextView preferences_medium;
    @BindView(R.id.tw_order_preferences_heavy)
    TextView preferences_heavy;
    @BindView(R.id.switch_dryer)
    android.support.v7.widget.SwitchCompat switch_dryer;
    @BindView(R.id.switch_fabric_softner)
    android.support.v7.widget.SwitchCompat switch_fabric_softner;
    @BindView(R.id.tw_order_preferences_scented_tide)
    TextView scented_tide;
    @BindView(R.id.tw_order_preferences_unscented_tide)
    TextView unscented_tide;
    @BindView(R.id.btn_save_order)
    TextView save;
    private String From;
    private int starchlevelStatus;

    private static FirebaseAnalytics firebaseAnalytics;

    public static final String SCREEN_NAME = "Preferences";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_preferences);
        ButterKnife.bind(this);
        Survicate.enterScreen(SCREEN_NAME);
        setToolbarTitle();

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        if (SessionManager.ORDER_PREFERENCE == null)
            SessionManager.ORDER_PREFERENCE = new OrderPreference();

        getDetergentValuesTask();
        getStarchValuesTask();

        if (this.getIntent().getExtras() != null && this.getIntent().getExtras().containsKey("From")) {
            From = getIntent().getExtras().getString("From");
        }


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UtilityClass.isConnectingToInternet(OrderPreferences.this))
                    chechSwitchActive();
                else
                    finish();
            }
        });
        setSeekerGravity();
        setPreferences();
    }

    private void getDetergentValuesTask() {
        if (UtilityClass.isConnectingToInternet(OrderPreferences.this)) {
            GetDetergentIdsTask getDetergentValuesTask = new GetDetergentIdsTask(OrderPreferences.this, SessionManager.ORDER_PREFERENCE.getBusinessSettings(this), "OrderPreferences");
            getDetergentValuesTask.ResponseTask();
        }

    }
    private void getStarchValuesTask() {
        if (UtilityClass.isConnectingToInternet(OrderPreferences.this)) {
            GetStarchIdsTask getstarchValuesTask = new GetStarchIdsTask(OrderPreferences.this, SessionManager.ORDER_PREFERENCE.getBusinessSettings(this), "OrderPreferences");
            getstarchValuesTask.ResponseTask();

        }


    }
    @OnClick(R.id.tw_order_preferences_scented_tide)
    void setGain() {

        setDetergent(Constants.DETERGENTNAME1);
    }

    @OnClick(R.id.tw_order_preferences_unscented_tide)
    void setTide() {

        setDetergent(Constants.DETERGENTNAME2);
    }


    public void setToolbarTitle() {
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.back);
        TextView toolbar_title = (TextView) toolbar.findViewById(R.id.toolbar_title);
        toolbar_title.setText("Preferences");
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    public void setSeekerGravity() {
        starchLevel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            int p = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                p = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                if (p >= 0 && p < 25) {
                    p = 0;
                    starchLevel.setProgress(p);
                    starchlevelStatus =0;

                    setStarchTextColor(starchLevel.getProgress());

                } else if (p >= 25 && p < 75) {
                    p = 50;
                    starchlevelStatus =50;
                    starchLevel.setProgress(p);
                    setStarchTextColor(starchLevel.getProgress());

                } else if (p >= 75 && p <= 100) {
                    p = 100;
                    starchlevelStatus =100;
                    starchLevel.setProgress(p);
                    setStarchTextColor(starchLevel.getProgress());
                }
            }
        });
    }

    public void setStarchTextColor(int i) {
        if (i == 00) {
            setStarchOn(preferences_light);
            setStarchOff(preferences_medium);
            setStarchOff(preferences_heavy);
            Log.e("set ","Starch");
            SessionManager.CUSTOMER.setStarchOnShirtsId("Light");
            SessionManager.ORDER_PREFERENCE.setStarchID(Constants.STARCHID2);

        }

        if (i == 50) {
            setStarchOn(preferences_medium);
            setStarchOff(preferences_light);
            setStarchOff(preferences_heavy);
            Log.e("set ","Starch2");
            SessionManager.CUSTOMER.setStarchOnShirtsId("Normal");
            SessionManager.ORDER_PREFERENCE.setStarchID(Constants.STARCHID);
        }

        if (i == 100) {
            setStarchOn(preferences_heavy);
            setStarchOff(preferences_medium);
            setStarchOff(preferences_light);
            SessionManager.CUSTOMER.setStarchOnShirtsId("Heavy");
            SessionManager.ORDER_PREFERENCE.setStarchID(Constants.STARCHID1);
        }
    }

    public void setDetergent(String s) {

        if (s.equals(Constants.DETERGENTNAME1) || s.equals("")) {
            setDetergentOn(scented_tide);
            setDetergentOff(unscented_tide);
            SessionManager.ORDER_PREFERENCE.setDetergentID(Constants.TIDEID);
            SessionManager.ORDER_PREFERENCE.setDetergentName(Constants.DETERGENTNAME1);
        } else if (s.equals(Constants.DETERGENTNAME2) || s.equals(Constants.DETERGENTNAME3)) {
            setDetergentOn(unscented_tide);
            setDetergentOff(scented_tide);
            SessionManager.ORDER_PREFERENCE.setDetergentID(Constants.TIDEFREEID);
            SessionManager.ORDER_PREFERENCE.setDetergentName(Constants.DETERGENTNAME2);
        }
    }

    public void setDetergentOn(TextView tw) {
        tw.setBackgroundResource(R.drawable.shape_orange_bg);
        tw.setTextColor(ContextCompat.getColor(this, R.color.pressbox));
        tw.setTypeface(tw.getTypeface(), Typeface.BOLD);
        tw.setTextSize(16);
    }

    public void setDetergentOff(TextView tw) {
        tw.setBackgroundResource(R.drawable.shape);
        tw.setTextColor(ContextCompat.getColor(this, R.color.Gray));
        tw.setTextSize(13);
    }

    public void setStarchOn(TextView tw) {
        tw.setTextColor(ContextCompat.getColor(this, R.color.black));
    }

    public void setStarchOff(TextView tw) {
        tw.setTextColor(ContextCompat.getColor(this, R.color.Gray));
    }

    public void chechSwitchActive() {
        Bundle bundle = new Bundle();
//@"event_name": @"preference_updated",
//                                 @"action": @"preference_page_save_button_pressed",
//                                 @"label": @"Preferences Update",
//                                 @"old_starch_preference": @"",
//                                 @"new_starch_preference": @"",
//                                 @"old_detergent_preference": @"",
//                                 @"new_detergent_preference": @""
        if (switch_dryer.isChecked()) {
           SessionManager.ORDER_PREFERENCE.setDryerSheetID(getResources().getString(R.string.dryer_sheet_on));
        } else {
            SessionManager.ORDER_PREFERENCE.setDryerSheetID(getResources().getString(R.string.dryer_sheet_off));
        }

        if (switch_fabric_softner.isChecked()) {
            SessionManager.ORDER_PREFERENCE.setFabricSoftnerID(getResources().getString(R.string.fabric_on));
        } else {
            SessionManager.ORDER_PREFERENCE.setFabricSoftnerID(getResources().getString(R.string.fabric_off));
        }
        bundle.putString("event_name", "preference_updated");
        bundle.putString("action", "Pressed Save on Preferences page");
        bundle.putString("label", "Preferences Update");
        bundle.putString("old_starch_preference", "");
        bundle.putString("new_starch_preference", "");
        bundle.putString("old_detergent_preference", "");
        bundle.putString("new_detergent_preference", "");

        firebaseAnalytics.logEvent("preference_updated", bundle);

        updateProfile();
        updateStarch();
    }
    public void updateStarch() {

        try {
            new BackgroundTask(OrderPreferences.this, SessionManager.ORDER_PREFERENCE.updateStarch(OrderPreferences.this),"orderPreferences");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

        public void updateProfile() {

        try {
            new BackgroundTask(OrderPreferences.this, SessionManager.CUSTOMER.updateProfile(OrderPreferences.this), "orderPreferences");

        } catch (Exception e) {
            e.printStackTrace();
        }



    }

    public void setDryerSheet(String s) {
        if (s.equals(getResources().getString(R.string.dryer_sheet_on))) {
            switch_dryer.setChecked(true);
        }
    }

    public void setFabricSoftner(String s) {
        if (s.equals(getResources().getString(R.string.fabric_on))) {
            switch_fabric_softner.setChecked(true);
        }
    }

    public void setPreferences() {

        if (SessionManager.ORDER_PREFERENCE == null)
            SessionManager.ORDER_PREFERENCE = new OrderPreference();

         if(SessionManager.CUSTOMER.getStarchOnShirtsId().equals("Light")){


            starchLevel.setProgress(0);
            setStarchTextColor(0);
        }
         else  if(SessionManager.CUSTOMER.getStarchOnShirtsId().equals("Normal")) {

             starchLevel.setProgress(50);
             setStarchTextColor(50);
         } else  if(SessionManager.CUSTOMER.getStarchOnShirtsId().equals("Heavy"))  {

             starchLevel.setProgress(100);
             setStarchTextColor(100);
         }



        setDryerSheet(SessionManager.ORDER_PREFERENCE.getDryerSheetID());
        setFabricSoftner(SessionManager.ORDER_PREFERENCE.getFabricSoftnerID());
        setDetergent(SessionManager.ORDER_PREFERENCE.getDetergentName());
    }

    @OnClick(R.id.btn_save_order)
    void saveOrder() {


        if (UtilityClass.isConnectingToInternet(OrderPreferences.this)) {
            chechSwitchActive();

        }
        else {

            finish();
        }

    }

    @Override
    public void onBackPressed() {
        if (UtilityClass.isConnectingToInternet(OrderPreferences.this))
            chechSwitchActive();
        else
            finish();

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void orderPreferenceStatus(String status) {
        if (UtilityClass.isConnectingToInternet(this)) {
            if (status.equalsIgnoreCase("success")) {
                SaveOrderPreferenceTask saveOrderPreferenceTask = new SaveOrderPreferenceTask(this, SessionManager.ORDER_PREFERENCE.updateDetergent(this), "OrderPreferences");
                saveOrderPreferenceTask.ResponseTask();
            } else {
                this.finish();
            }
        } else {
            this.finish();
        }

    }

    @Override
    public void updateDetergent(String status) {
        if (status.equalsIgnoreCase("success"))
            this.finish();
        else
            this.finish();
    }


    @Override
    public void getDetergentId(String tideID, String tideFreeID, String detergentName1, String detergentname2) {

        if (tideID != null || !tideID.equalsIgnoreCase("")) {
            Constants.TIDEID = tideID;
        }
        if (tideFreeID != null || !tideFreeID.equalsIgnoreCase("")) {
            Constants.TIDEFREEID = tideFreeID;
        }
        if (detergentName1 != null || !detergentName1.equalsIgnoreCase("")) {
            Constants.DETERGENTNAME1 = detergentName1;
        }
        if (detergentname2 != null || !detergentname2.equalsIgnoreCase("")) {

                Constants.DETERGENTNAME2=detergentname2;

        }

    }
    @Override
    public void getStarchID(String starchID, String starchID1, String starchID2) {

        if (starchID != null || !starchID.equalsIgnoreCase("")) {
            Constants.STARCHID = starchID;
        }

        if (starchID1 != null || !starchID1.equalsIgnoreCase("")) {
            Constants.STARCHID1 = starchID1;
        }
        if (starchID2 != null || !starchID2.equalsIgnoreCase("")) {

            Constants.STARCHID2=starchID2;

        }

    }

}
