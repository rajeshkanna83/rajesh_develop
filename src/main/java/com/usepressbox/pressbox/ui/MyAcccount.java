package com.usepressbox.pressbox.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.survicate.surveys.Survicate;
import com.usepressbox.pressbox.LandingScreen;
import com.usepressbox.pressbox.R;
import com.usepressbox.pressbox.adapter.PlacesArrayAdapter;
import com.usepressbox.pressbox.asyntasks.BackgroundTask;
import com.usepressbox.pressbox.asyntasks.GetStarchIdsTask;
import com.usepressbox.pressbox.asyntasks.GetUserAddressTask;
import com.usepressbox.pressbox.asyntasks.SaveUserAddressTask;
import com.usepressbox.pressbox.interfaces.IAddressStatusListener;
import com.usepressbox.pressbox.interfaces.IPromoCodeStatusListener;
import com.usepressbox.pressbox.models.OrderPreference;
import com.usepressbox.pressbox.support.GPSTracker;
import com.usepressbox.pressbox.ui.activity.order.OrderPreferences;
import com.usepressbox.pressbox.ui.activity.order.Orders;
import com.usepressbox.pressbox.ui.activity.register.ChangePassword;
import com.usepressbox.pressbox.utils.Constants;
import com.usepressbox.pressbox.utils.SessionManager;
import com.usepressbox.pressbox.utils.UtilityClass;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.OnTouch;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


/**
 * Created by kruno on 14.04.16..
 * Modified by Prasanth.S on 27/08/2018
 * This activity is show the account details
 */
public class MyAcccount extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, IPromoCodeStatusListener, IAddressStatusListener {

    private static final String LOG_TAG = "MyAcccount";
    public static final String SCREEN_NAME = "My Account";
    private static FirebaseAnalytics firebaseAnalytics;


    private String[] citys;
    private Toolbar toolbar;
    private SessionManager objSession;
    @BindView(R.id.spinner_city)
    Spinner city;
    @BindView(R.id.et_account_name)
    EditText name;
    @BindView(R.id.et_account_last_name)
    EditText lastName;
    @BindView(R.id.et_account_phone)
    EditText phone;

    @BindView(R.id.et_account_email)
    TextView email;
    @BindView(R.id.tw_my_account_payment)
    TextView addCreditCard;
    @BindView(R.id.credit_card_detail)
    LinearLayout credit_card_detail;
    @BindView(R.id.credit_card_date)
    EditText credit_card_date;
    @BindView(R.id.credit_card_CVV)
    EditText credit_card_CVV;
    @BindView(R.id.credit_card_number)
    EditText credit_card_number;
    @BindView(R.id.et_my_account_promo_code)
    EditText promo_code;
    @BindView(R.id.userAddress)
    AutoCompleteTextView mAutocompleteTextView;
    @BindView(R.id.tw_my_account_last_four_card_numbers)
    TextView tw_my_account_last_four_card_numbers;
    @BindView(R.id.terms_and_conditions)
    RelativeLayout terms_and_conditions;
    @BindView(R.id.faq)
    RelativeLayout faq;
    @BindView(R.id.card_details_layout)
    RelativeLayout card_details_layout;
    @BindView(R.id.pricetable)
    RelativeLayout pricetable;
    @BindView(R.id.locations)
    RelativeLayout locations;
    @BindView(R.id.privacy_policy_layout)
    RelativeLayout privacy_policy_layout;

    @BindView(R.id.relative_layout_current_card_number)
    LinearLayout relative_layout_current_card_number;
    @BindView(R.id.my_account_parent_layout)
    LinearLayout my_account_parent_layout;
    @BindView(R.id.my_account_child_layout)
    LinearLayout my_account_child_layout;
    @BindView(R.id.my_account_scrollview)
    NestedScrollView my_account_scrollview;
    @BindView(R.id.apply_button)
    Button promocode_apply_button;

    private static final int GOOGLE_API_CLIENT_ID = 0;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0;

    private GoogleApiClient mGoogleApiClient;
    private PlacesArrayAdapter mPlaceArrayAdapter;
    private static LatLngBounds USER_BOUNDS = new LatLngBounds(
            new LatLng(37.398160, -122.180831), new LatLng(37.430610, -121.972090));
    private String provider;
    public Location userLocation;
    private LocationManager locationManager;

    private boolean mLocationPermissionGranted;

    FusedLocationProviderClient mFusedLocationProviderClient;
    private static final int REQUEST_CODE_PERMISSION = 2;
    String mPermission = Manifest.permission.ACCESS_FINE_LOCATION;
    GPSTracker gps;


    String streetAddress, shortAddress, userCity, state, country, postalCode, finalLongAddress;
    LatLng userAddressLatLong;
    SessionManager sessionManager;
    private LocationRequest mLocationRequest;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account_screen);
        ButterKnife.bind(this);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        Survicate.enterScreen(SCREEN_NAME);
        setToolbarTitle();
        setDateFormat();
        sessionManager = new SessionManager(this);

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        mGoogleApiClient = new GoogleApiClient.Builder(MyAcccount.this)
                .addApi(Places.GEO_DATA_API)
                .addApi(LocationServices.API)
                .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_LOW_POWER)
                .setInterval(10 * 1000)
                .setFastestInterval(1 * 1000);

        CallLocationData();

        getUserAddressTask();

        mAutocompleteTextView.setThreshold(3);
        mAutocompleteTextView.setOnItemClickListener(mAutocompleteClickListener);
        mPlaceArrayAdapter = new PlacesArrayAdapter(this, android.R.layout.simple_list_item_1,
                USER_BOUNDS, null);
        mAutocompleteTextView.setAdapter(mPlaceArrayAdapter);

        citys = getResources().getStringArray(R.array.citys);
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, citys);
        city.setAdapter(cityAdapter);

        name.setText(SessionManager.CUSTOMER.getName());
        lastName.setText(SessionManager.CUSTOMER.getLastName());
        email.setText(SessionManager.CUSTOMER.getEmail());
        phone.setText(SessionManager.CUSTOMER.getPhone());

        mAutocompleteTextView.setActivated(false);
        mAutocompleteTextView.setSelected(false);
        mAutocompleteTextView.setCursorVisible(false);


        objSession = new SessionManager(this);
        if (SessionManager.CUSTOMER.getCardNumber() != "0000" && SessionManager.CUSTOMER.getCardNumber() != null) {
            card_details_layout.setVisibility(View.GONE);
            relative_layout_current_card_number.setVisibility(View.VISIBLE);
            String creditCard = null;
            try {
                creditCard = SessionManager.CUSTOMER.getCardNumber().substring(SessionManager.CUSTOMER.getCardNumber().length() - 4);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
            tw_my_account_last_four_card_numbers.setText("***********" + creditCard);

        } else {
            tw_my_account_last_four_card_numbers.setText("");
        }


        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        switch (metrics.densityDpi) {
            case DisplayMetrics.DENSITY_LOW:
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                break;
            case DisplayMetrics.DENSITY_HIGH:
                mAutocompleteTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                break;
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAddress();
                updateBilling();

                if (changes()) {
                    Intent intent = new Intent(MyAcccount.this, Orders.class);
                    startActivity(intent);
                    finish();
                } else {
                    updateCustomer();

                }
            }
        });


        if (sessionManager.getPromoCode() != null) {
            promo_code.setText(sessionManager.getPromoCode());
        }
    }


    @OnClick(R.id.apply_button)
    void apply_button_click() {
        savePromoCode();
    }

    @OnClick(R.id.relative_layout_current_card_number)
    void relative_layout_current_card_number() {
        card_details_layout.setVisibility(View.VISIBLE);
        addCreditCard.setVisibility(View.GONE);
        credit_card_detail.setVisibility(View.VISIBLE);
        relative_layout_current_card_number.setVisibility(View.GONE);
    }

    @OnClick(R.id.tw_my_account_payment)
    void addCreditCard() {

        addCreditCard.setVisibility(View.GONE);
        credit_card_detail.setVisibility(View.VISIBLE);
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        Survicate.leaveScreen(SCREEN_NAME);
    }
    private void setBounds(Location location, int mDistanceInMeters) {
        double latRadian = Math.toRadians(location.getLatitude());

        double degLatKm = 110.574235;
        double degLongKm = 110.572833 * Math.cos(latRadian);
        double deltaLat = mDistanceInMeters / 1000.0 / degLatKm;
        double deltaLong = mDistanceInMeters / 1000.0 / degLongKm;

        double minLat = location.getLatitude() - deltaLat;
        double minLong = location.getLongitude() - deltaLong;
        double maxLat = location.getLatitude() + deltaLat;
        double maxLong = location.getLongitude() + deltaLong;

    }


    private void getUserAddressTask() {
        if (UtilityClass.isConnectingToInternet(this)) {
            GetUserAddressTask getUserAddressTask = new GetUserAddressTask(this, this, SessionManager.CUSTOMER.details(this), "Myaccount");
            getUserAddressTask.ResponseTask();
        } else {
            UtilityClass.showAlertWithOk(this, "Alert!", "Please check your internet connection", "locker");

        }
    }

    private void loadPermissions(String perm, int requestCode) {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, perm)) {
                ActivityCompat.requestPermissions(this, new String[]{perm}, requestCode);
            }
        }
    }

    private void getUserBounds(double lat, double lng) {

        double radiusDegrees = 1.0;
        LatLng center = new LatLng(lat, lng);
        Log.e("", "" + center.latitude);
        LatLng northEast = new LatLng(center.latitude + radiusDegrees, center.longitude + radiusDegrees);
        LatLng southWest = new LatLng(center.latitude - radiusDegrees, center.longitude - radiusDegrees);
        USER_BOUNDS = LatLngBounds.builder()
                .include(northEast)
                .include(southWest)
                .build();


    }

    private void CallLocationData() {
        try {
            if (ActivityCompat.checkSelfPermission(this, mPermission)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{mPermission},
                        REQUEST_CODE_PERMISSION);


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        gps = new GPSTracker(MyAcccount.this);

        if (gps.canGetLocation()) {

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            getUserBounds(latitude, longitude);
        } else {

            gps.showSettingsAlert();
        }

    }


    @OnTouch({R.id.userAddress})
    boolean touchAddress(View v, MotionEvent motionEvent) {
        mAutocompleteTextView.setActivated(true);
        mAutocompleteTextView.setSelected(true);
        mAutocompleteTextView.setCursorVisible(true);
        mAutocompleteTextView.requestFocus();
        mAutocompleteTextView.setText("");
        UtilityClass.showKeyboard(mAutocompleteTextView, this);
        return true;
    }

    @OnEditorAction(R.id.et_my_account_promo_code)
    protected boolean promocodeClick(int actionId) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            if (promo_code.getText().toString().length() > 0) {
                savePromoCode();
            }
            return true;
        }

        return false;
    }

    public void saveAddress() {
        if (mAutocompleteTextView.getText().toString().length() > 0) {

            sessionManager.saveUserAddress(mAutocompleteTextView.getText().toString());

            UtilityClass.getLocationFromAddress(mAutocompleteTextView.getText().toString(), this);
            if (sessionManager.getUserGeoLocation() != null) {
                String[] location = sessionManager.getUserGeoLocation().split(",");
                double lat = Double.parseDouble(location[0]);
                double longitude = Double.parseDouble(location[1]);

                retrieveUserSplitedAddress(lat, longitude, "longAddress");
            }

            if (userAddressLatLong != null) {
                retrieveUserSplitedAddress(userAddressLatLong.latitude, userAddressLatLong.longitude, "shortAddress");
            }

            if (UtilityClass.isConnectingToInternet(MyAcccount.this)) {
                try {
                    if (userCity != null && state != null && country != null && postalCode != null) {
                        if (finalLongAddress != null) {
                            SessionManager.CUSTOMER.setStreetLongAddress(finalLongAddress);
                        }
                        if (streetAddress != null) {
                            SessionManager.CUSTOMER.setStreetAddress(streetAddress);
                        }
                        SessionManager.CUSTOMER.setUserCity(userCity);
                        SessionManager.CUSTOMER.setState(state);
                        SessionManager.CUSTOMER.setCountry(country);
                        SessionManager.CUSTOMER.setZipcode(postalCode);
                        SaveUserAddressTask addressTask = new SaveUserAddressTask(this, SessionManager.CUSTOMER.updateUSerAddress(this), "Register");
                        addressTask.ResponseTask();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }
    }

    private void retrieveUserSplitedAddress(Double latitude, Double longitude, String addressType) {
        String address = null;

        try {
            String[] locationData = getCityNameByCoordinates(latitude, longitude);


            userCity = locationData[0];
            state = locationData[1];
            country = locationData[2];
            postalCode = locationData[3];
            address = mAutocompleteTextView.getText().toString();

            if (addressType.equalsIgnoreCase("longAddress")) {
                if (address != null && userCity !=null && postalCode !=null) {
                    String removedUserCity = address.replace(userCity, "");
                    String removedUserZipcode = removedUserCity.replace(postalCode, "");
                    finalLongAddress = removedUserZipcode.replaceAll(",", "");
                }
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    @OnTouch({R.id.my_account_parent_layout, R.id.my_account_child_layout})
    boolean touch(View v, MotionEvent motionEvent) {

        UtilityClass.hideKeyboard(MyAcccount.this);
        return true;
    }

    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            UtilityClass.hideKeyboard(MyAcccount.this);
            final PlacesArrayAdapter.PlaceAutocomplete item = mPlaceArrayAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);

            Log.i(LOG_TAG, "Selected: " + item.description);
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
        }
    };
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.e(LOG_TAG, "Place query did not complete. Error: " +
                        places.getStatus().toString());
                return;
            }

            final Place place = places.get(0);
            CharSequence attributions = places.getAttributions();


            try {

                Spanned address = Html.fromHtml(String.valueOf(place.getAddress()));
                shortAddress = address.toString();
                SessionManager sessionManager = new SessionManager(MyAcccount.this);
                sessionManager.saveUserShortAddress(shortAddress);


                Spanned street = Html.fromHtml(String.valueOf(place.getName()));
                streetAddress = street.toString();
                userAddressLatLong = place.getLatLng();

                saveAddress();


            } catch (RuntimeException e) {
                e.printStackTrace();
            }


        }
    };


    private String[] getCityNameByCoordinates(double lat, double lon) throws IOException {
        String[] strng = null;

        try {
            Geocoder mGeocoder = new Geocoder(MyAcccount.this, Locale.getDefault());

            List<Address> addresses = mGeocoder.getFromLocation(lat, lon, 1);
            if (addresses != null && addresses.size() > 0) {
                strng = new String[]{addresses.get(0).getLocality(), addresses.get(0).getAdminArea(),
                        addresses.get(0).getCountryName(), addresses.get(0).getPostalCode(), addresses.get(0).getAddressLine(0)};

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return strng;
    }

    @OnClick(R.id.btn_sign_out)
    void sign_out() {

        new SessionManager(this).clearSession();
        Intent toLandingScreen = new Intent(MyAcccount.this, LandingScreen.class);
        startActivity(toLandingScreen);
        Constants.firstTime = false;
        finishAffinity();

    }

    @OnClick(R.id.privacy_policy_layout)
    void PrivacyPolicy() {

        Bundle bundle = new Bundle();
        bundle.putString("event_name", "viewed_privacy_policy");
        bundle.putString("action", "Pressed Privacy Policy on My Account page");
        bundle.putString("label", "Privacy Policy");
        firebaseAnalytics.logEvent("viewed_privacy_policy", bundle);

        UtilityClass.goToUrl(this, Constants.PRIVACY_POLICY);
    }


    @OnClick(R.id.btn_reset_password)
    void resetPassword() {

        Constants.FROM = "Myaccount";
        new BackgroundTask(MyAcccount.this, SessionManager.CUSTOMER.sendForgotPasswordEmail());

    }

    @OnClick(R.id.tw_order_preferences)
    void orderPreferences() {


        Bundle bundle = new Bundle();
        bundle.putString("event_name", "preference_initiated");
        bundle.putString("action", "Pressed Order Preferences on My Account page");
        bundle.putString("label", "Order Preferences Initiated");
        firebaseAnalytics.logEvent("preference_initiated", bundle);

        Intent orderPreferences = new Intent(MyAcccount.this, OrderPreferences.class);
        orderPreferences.putExtra("From", "MyAccount");
        startActivity(orderPreferences);
        Constants.firstTime = false;

    }

    @OnClick(R.id.tw_account_change_password)
    void changePassword() {

        Intent changePassword = new Intent(MyAcccount.this, ChangePassword.class);
        startActivity(changePassword);
    }


    @OnClick(R.id.terms_and_conditions)
    void termsAndConditions() {
        Bundle bundle = new Bundle();
        bundle.putString("event_name", "viewed_terms_and_conditions");
        bundle.putString("action", "Pressed Terms and Conditions on My Account page");
        bundle.putString("label", "Terms and Conditions");
        firebaseAnalytics.logEvent("viewed_terms_and_conditions", bundle);

        UtilityClass.goToUrl(this, Constants.TANDC);

    }

    @OnClick(R.id.pricetable)
    void price() {

        Bundle bundle = new Bundle();
        bundle.putString("event_name", "viewed_pricing");
        bundle.putString("action", "Pressed Prices on My Account page");
        bundle.putString("label", "Prices");
        firebaseAnalytics.logEvent("viewed_pricing", bundle);
        if (SessionManager.CUSTOMER.getCity().equals("Chicago")) {
            Uri uri = Uri.parse("https://www.pressboxbytide.com/chicago-pricing");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } else if (SessionManager.CUSTOMER.getCity().equals("DC Metro")) {
            Uri uri = Uri.parse("https://www.pressboxbytide.com/dmv-pricing");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } else if (SessionManager.CUSTOMER.getCity().equals("Philadelphia")) {
            Uri uri = Uri.parse("https://www.pressboxbytide.com/philadelphia-pricing");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } else if (SessionManager.CUSTOMER.getCity().equals("Nashville")) {
            Uri uri = Uri.parse("https://www.pressboxbytide.com/nashville-pricing");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } else if(SessionManager.CUSTOMER.getCity().equals("Denver")){
            Uri uri = Uri.parse("https://www.pressboxbytide.com/denver-pricing");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
        else if(SessionManager.CUSTOMER.getCity().equals("Dallas")){
            Uri uri = Uri.parse("https://www.pressboxbytide.com/dallas-pricing");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
        else  {
            Uri uri = Uri.parse("https://www.tidecleaners.com/boston-pricing");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);

        }
    }

    @OnClick(R.id.faq)
    void faq() {

        Bundle bundle = new Bundle();
        bundle.putString("event_name", "viewed_help");
        bundle.putString("action", "Pressed Help on My Account page");
        bundle.putString("label", "Help");
        firebaseAnalytics.logEvent("viewed_help", bundle);

        Uri uri = Uri.parse("https://tidecleaners.com/help");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @OnClick(R.id.locations)
    void locations() {
        Bundle bundle = new Bundle();
        bundle.putString("event_name", "viewed_locations");
        bundle.putString("action", "Pressed Locations on My Account page");
        bundle.putString("label", "Locations");
        firebaseAnalytics.logEvent("viewed_locations", bundle);

        Uri uri = Uri.parse("http://www.usepressbox.com/locations/chicago/");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    public void setToolbarTitle() {
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        toolbar.setNavigationIcon(R.drawable.back);
        TextView toolbar_title = (TextView) toolbar.findViewById(R.id.toolbar_title);
        toolbar_title.setText("My Account");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

    }

    public boolean checkCreditCard() {

        boolean creditCardOk = false;
        if (credit_card_number.getText().toString().length() > 0) {
            if (credit_card_date.getText().toString().length() == 5) {
                if (credit_card_CVV.getText().toString().length() > 0) {
                    creditCardOk = true;
                }
            }
        }
        return creditCardOk;
    }

    public void updateBilling() {

        if (checkCreditCard()) {
            SessionManager.CUSTOMER.setCardNumber(credit_card_number.getText().toString());
            SessionManager.CUSTOMER.setExpMonth(credit_card_date.getText().toString().split("/")[0]);
            SessionManager.CUSTOMER.setExpYear("20" + credit_card_date.getText().toString().split("/")[1]);
            SessionManager.CUSTOMER.setCsc(credit_card_CVV.getText().toString());

            new BackgroundTask(this, SessionManager.CUSTOMER.updateBillling(this));
        }
    }

    public void setDateFormat() {

        TextWatcher mDateEntryWatcher = new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                onTextValidate(s, start, before, count);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        credit_card_date.addTextChangedListener(mDateEntryWatcher);
    }


    public void onTextValidate(CharSequence s, int start, int before, int count) {
        String working = s.toString();
        boolean isValid = true;
        if (working.length() == 2 && before == 0) {
            if (Integer.parseInt(working) < 1 || Integer.parseInt(working) > 12) {
                isValid = false;
            } else {
                working += "/";
                credit_card_date.setText(working);
                credit_card_date.setSelection(working.length());
            }
        } else if (working.length() == 5 && before == 0) {
            String enteredYear = working.substring(3);
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);

            String currentYearformat = String.valueOf(currentYear);
            currentYearformat = currentYearformat.substring(2, 4);
            currentYear = Integer.parseInt(currentYearformat);

            if (Integer.parseInt(enteredYear) < currentYear) {
                isValid = false;
            }
        } else if (working.length() != 5) {
            isValid = false;
        }

        if (!isValid) {
            credit_card_date.setError("Enter a valid date: MM/YY");
        } else {
            credit_card_date.setError(null);
        }

    }

    public boolean changes() {

        boolean changes = false;
        if (name.getText().toString().equals(SessionManager.CUSTOMER.getName())) {
            if (lastName.getText().toString().equals(SessionManager.CUSTOMER.getLastName())) {
                if (phone.getText().toString().equals(SessionManager.CUSTOMER.getPhone())) {

                    changes = true;
                }
            }
        }
        return changes;
    }

    public void updateCustomer() {

        if (name.getText().toString().length() > 0) {
            if (lastName.getText().toString().length() > 0) {
                if (phone.getText().toString().length() > 4 && phone.getText().toString().length() < 16) {
                    SessionManager.CUSTOMER.setName(name.getText().toString());
                    SessionManager.CUSTOMER.setLastName(lastName.getText().toString());
                    SessionManager.CUSTOMER.setPhone(phone.getText().toString());

                    try {
                        new BackgroundTask(this, SessionManager.CUSTOMER.updateProfile(this), "myAccount");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(MyAcccount.this, getResources().getString(R.string.toast_phone_number), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MyAcccount.this, getResources().getString(R.string.toast_last_name), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(MyAcccount.this, getResources().getString(R.string.toast_name), Toast.LENGTH_SHORT).show();
        }

    }

    public void savePromoCode() {
        if (promo_code.getText().toString().length() > 0) {
            SessionManager.CUSTOMER.setPromoCode(promo_code.getText().toString());
            new BackgroundTask(this, SessionManager.CUSTOMER.savePromoCode(this), (IPromoCodeStatusListener) this, "MyaccountClass");
        } else {
            UtilityClass.showAlertWithOk(this, "Alert!", "Please enter a promo code", "promocode");
        }
    }

    @Override
    public void onBackPressed() {

        saveAddress();
        updateBilling();

        if (changes()) {
            Intent intent = new Intent(MyAcccount.this, Orders.class);
            startActivity(intent);
            finish();
        } else {
            updateCustomer();
        }

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    public void scrollToTop(final NestedScrollView scrollView, final View view) {

        view.requestFocus();
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                int vTop = view.getTop();
                int vBottom = view.getBottom();
                int sHeight = scrollView.getHeight();
                int sTop = scrollView.getTop();
                int sBottom = scrollView.getBottom();
                scrollView.smoothScrollTo(0, ((sTop + sBottom) / 2) / 2);
            }
        });


    }


    @Override
    public void promoCodeStatus(String status, String message) {
        if (status.equalsIgnoreCase("success")) {
            UtilityClass.showAlertWithOk(this, "VALID PROMO CODE", message, "promocode-success");
        } else {
            if (message.toLowerCase().contains("Promotional code is invalid:".toLowerCase())) {
                String promomsg = Html.fromHtml(message).toString();
                String replacedString = promomsg.replace("Promotional code is invalid:", "");
                UtilityClass.showAlertWithEmailRedirect(this, "INVALID PROMO CODE", replacedString, "myaccount");
            } else {
                UtilityClass.showAlertWithEmailRedirect(this, "INVALID PROMO CODE", message, "myaccount");
            }
        }
    }


    @Override
    public void onConnected(Bundle bundle) {
        mPlaceArrayAdapter.setGoogleApiClient(mGoogleApiClient);

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onConnectionSuspended(int i) {
        mPlaceArrayAdapter.setGoogleApiClient(null);
    }


    @Override
    public void addressStatus(JSONObject jsonObject) {
        if (jsonObject != null)
            getCustomerObject(jsonObject);

    }

    public void getCustomerObject(JSONObject jsonObject) {
        SessionManager sessionManager = new SessionManager(MyAcccount.this);
        JSONObject customerObject = null;

        try {
            customerObject = jsonObject.getJSONObject("data").getJSONObject("customerDetails");
            Log.e("My Account","Account"+jsonObject.getJSONObject("data"));
            setOrderPreferences(jsonObject);
            SessionManager.CUSTOMER.setName(customerObject.getString("firstName"));
            SessionManager.CUSTOMER.setLastName(customerObject.getString("lastName"));
            SessionManager.CUSTOMER.setEmail(customerObject.getString("email"));
            SessionManager.CUSTOMER.setPhone(customerObject.getString("phone"));
            SessionManager.CUSTOMER.setCity(customerObject.getString("city"));

            SessionManager.CUSTOMER.setState(customerObject.getString("state"));
            SessionManager.CUSTOMER.setZipcode(customerObject.getString("zip"));





            if (!customerObject.getString("address2").trim().isEmpty()
                    && customerObject.getString("address2") != null
                    && !customerObject.getString("address2").equalsIgnoreCase("")) {

                    SessionManager.CUSTOMER.setStreetLongAddress(customerObject.getString("address2"));
                    sessionManager.saveUserShortAddress("");
                    sessionManager.saveUserShortAddress(customerObject.getString("address2") + " " +
                            customerObject.getString("city") + " " + customerObject.getString("state"));

            }

            if (!customerObject.getString("address1").trim().isEmpty()
                    && customerObject.getString("address1") != null
                    && !customerObject.getString("address1").equalsIgnoreCase("")) {


                    SessionManager.CUSTOMER.setStreetAddress(customerObject.getString("address1"));
                    sessionManager.saveUserAddress("");
                    sessionManager.saveUserAddress(customerObject.getString("address1") + " " +
                            customerObject.getString("city") + " " + customerObject.getString("state"));



            }

            if (sessionManager.getUserAddress() != null) {
                mAutocompleteTextView.setText(sessionManager.getUserAddress());
            } else {
                if (sessionManager.getUserShortAddress() != null)
                    mAutocompleteTextView.setText(sessionManager.getUserShortAddress());
                else
                    mAutocompleteTextView.setText("");
            }



        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    public void setOrderPreferences(JSONObject jsonObject ) {
        SessionManager.ORDER_PREFERENCE = new OrderPreference();

        JSONObject customerPreferencesObject = null;
        try {
            JSONObject customerDetailsObject = jsonObject.getJSONObject("data").getJSONObject("customerDetails");
            customerPreferencesObject = customerDetailsObject.getJSONObject("customerPreferences");
            Object check = customerPreferencesObject.get("preferences");

            if (check instanceof JSONObject) {
                if (customerPreferencesObject.has("preferences")) {
                    JSONObject preferences = customerPreferencesObject.getJSONObject("preferences");

                    if (preferences.has("dryersheet")) {
                        SessionManager.ORDER_PREFERENCE.setDryerSheetID(preferences
                                .getJSONObject("dryersheet").getString("productID"));
                    }

                    if (preferences.has("fabsoft")) {
                        SessionManager.ORDER_PREFERENCE.setFabricSoftnerID(preferences
                                .getJSONObject("fabsoft").getString("productID"));
                    }
              Log.e("","SCH&&&&&&&&&&&&"+Constants.BUSINESS_ID);
                    if(Constants.BUSINESS_ID.equals("150")){

                        if (preferences.has("starch")) {

                            if(preferences.getJSONObject("starch").getString("displayName").equals("Medium Starch")) {
                                SessionManager.CUSTOMER.setStarchOnShirtsId("Normal");
                            }
                            else if(preferences.getJSONObject("starch").getString("displayName").equals("Light Starch")){
                                SessionManager.CUSTOMER.setStarchOnShirtsId("Light");
                            }
                            else{
                                SessionManager.CUSTOMER.setStarchOnShirtsId("Heavy");
                            }

                        }
                    }
                    else {
                        if (preferences.has("starchonshirts")) {


                            SessionManager.CUSTOMER.setStarchOnShirtsId(preferences
                                    .getJSONObject("starchonshirts").getString("displayName"));

                        }

                    }

                    if (preferences.has("detergent")) {
                        SessionManager.ORDER_PREFERENCE.setDetergentID(preferences
                                .getJSONObject("detergent").getString("productID"));
                        Log.e("","SCH****************"+preferences
                                .getJSONObject("detergent").getString("displayName"));
                        if (preferences.getJSONObject("detergent").has("displayName")) {
                            SessionManager.ORDER_PREFERENCE.setDetergentName(preferences
                                    .getJSONObject("detergent").getString("displayName"));
                        }
                    }
                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void setAddress(String shortOrLong, String streetAddress, String city, String state, String contry) {

        switch (shortOrLong) {
            case "short":
                SessionManager.CUSTOMER.setStreetAddress(streetAddress);
                sessionManager.saveUserAddress("");
                sessionManager.saveUserAddress(
                        streetAddress + " "
                                + city + " "
                                + state + " "
                                + contry);

                break;
            case "long":
                SessionManager.CUSTOMER.setStreetLongAddress(streetAddress);
                sessionManager.saveUserShortAddress("");
                sessionManager.saveUserShortAddress(
                        streetAddress + " "
                                + city + " "
                                + state + " "
                                + contry);
                break;
        }


    }

}
