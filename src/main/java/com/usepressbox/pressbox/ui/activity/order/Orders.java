package com.usepressbox.pressbox.ui.activity.order;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.survicate.surveys.Survicate;
import com.usepressbox.pressbox.R;
import com.usepressbox.pressbox.adapter.OnboardPageAdapter;
import com.usepressbox.pressbox.adapter.OrdersAdapter;
import com.usepressbox.pressbox.asyntasks.BackgroundTask;
import com.usepressbox.pressbox.asyntasks.ConfirmOrderTypeTask;
import com.usepressbox.pressbox.asyntasks.GetOrdersTask;
import com.usepressbox.pressbox.interfaces.IOrderListListener;
import com.usepressbox.pressbox.interfaces.ISignUpListener;
import com.usepressbox.pressbox.models.GetOrdersModel;
import com.usepressbox.pressbox.models.LocationModel;
import com.usepressbox.pressbox.models.Order;
import com.usepressbox.pressbox.ui.MyAcccount;
import com.usepressbox.pressbox.utils.Constants;
import com.usepressbox.pressbox.utils.SessionManager;
import com.usepressbox.pressbox.utils.Signature;
import com.usepressbox.pressbox.utils.UtilityClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_IDLE;

/**
 * Created by kruno on 14.04.16..
 * Modified by Prasanth.S on 27/08/2018
 * This class is used to list the placed orders by the customer
 */
public class Orders extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, IOrderListListener, ISignUpListener {

    private Toolbar toolbar;
    static Orders orders;
    private String from, code, percentage;
    public static ArrayList<GetOrdersModel> dataArray;
    public static OrdersAdapter adapter;
    @BindView(R.id.lw_orders)
    ListView lw_orders;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipe_refresh_layout;
    @BindView(R.id.btn_contact_support)
    ImageView cntctSupport;
    @BindView(R.id.place_order_button)
    Button place_order_button;
    boolean doubleBackToExitPressedOnce = false;
    private ViewPager pager;
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private int selectedPageIndex = -1;
    private boolean exitWhenScrollNextPage = false;
    SessionManager sessionManager;
    private String From = "Default";
    private String FromScreen;
    View view;
    TextView empty_textView;

    ArrayList<GetOrdersModel> getCliamsList;
    ArrayList<GetOrdersModel> getOrdersModelArrayList;

    private static FirebaseAnalytics firebaseAnalytics;
    public static final String SCREEN_NAME = "Home";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders_screen);
        Survicate.enterScreen(SCREEN_NAME);
        sessionManager = new SessionManager(this);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey("From")) {
                from = extras.getString("From");
                showalert();
            } else if (extras.containsKey("percentage") && extras.containsKey("code")) {
                percentage = extras.getString("percentage");
                code = extras.getString("code");
                Log.e("orderpercent", percentage);
                Log.e("ordercod", code);
                if (!percentage.equalsIgnoreCase("null") && !code.equalsIgnoreCase("null") && !percentage.equalsIgnoreCase("") && !code.equalsIgnoreCase("")) {
                    UtilityClass.showNotificationAlert(Orders.this, percentage, code);
                }

            }

        } else {

        }

        ButterKnife.bind(this);
        place_order_button.setText("Place a New Order");

        setToolbarTitle();
        orders = this;
        getCliamsList = new ArrayList<>();
        getOrdersModelArrayList = new ArrayList<>();

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        dataArray = new ArrayList<>();

        adapter = new OrdersAdapter(dataArray, this);

        view = getLayoutInflater().inflate(R.layout.order_empty_view, null);

        ViewGroup viewGroup = (ViewGroup) lw_orders.getParent();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 50, 0, 0);
        view.setLayoutParams(params);
        empty_textView = view.findViewById(R.id.empty_textview);

        viewGroup.addView(view);
        if(Constants.BUSINESS_ID.equalsIgnoreCase("468")){
            empty_textView.setText(getResources().getString(R.string.empty_list_text));

        }else {

            empty_textView.setText(getResources().getString(R.string.empty_list_text1));
        }

        lw_orders.setEmptyView(view);
        lw_orders.setAdapter(adapter);

        lw_orders.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                LinearLayout lockerId = (LinearLayout) view.findViewById(R.id.linear_layout_locker_id);

                if (lockerId.getVisibility() == View.VISIBLE) {

                    lockerId.setVisibility(View.INVISIBLE);
                    OrdersAdapter.selectedPosition = -1;

                } else {
                    lockerId.setVisibility(View.VISIBLE);
                    OrdersAdapter.selectedPosition = position;
                }

                adapter.notifyDataSetChanged();
            }
        });

        lw_orders.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                boolean enable = false;
                if (lw_orders != null && lw_orders.getChildCount() > 0) {
                    boolean firstItemVisible = lw_orders.getFirstVisiblePosition() == 0;
                    boolean topOfFirstItemVisible = lw_orders.getChildAt(0).getTop() == 0;
                    enable = firstItemVisible && topOfFirstItemVisible;
                }
                swipe_refresh_layout.setEnabled(enable);
            }
        });

        swipe_refresh_layout.setOnRefreshListener(this);

        if (SessionManager.ORDER == null) SessionManager.ORDER = new Order();
        new BackgroundTask(this, SessionManager.ORDER.getClaims(this), lw_orders, swipe_refresh_layout, adapter, dataArray);

    }
        @Override
        protected void onDestroy(){
            super.onDestroy();
           Survicate.leaveScreen(SCREEN_NAME);
        }


    public void replaceFragment(android.support.v4.app.Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fragment, fragment);
        transaction.commit();
    }

    private void showOnBoardDialog() {
        final Dialog dialog = new Dialog(Orders.this, R.style.Empty_dialog_theme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.dialogbg));
        dialog.setContentView(R.layout.onboard_dialog);

        Rect displayRectangle = new Rect();
        Window window = Orders.this.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        dialog.getWindow().setLayout((int) (displayRectangle.width() * 0.8f), (int) (displayRectangle.height() * 0.7f));

        dialog.setCanceledOnTouchOutside(false);

        OnboardPageAdapter adapters = new OnboardPageAdapter(Orders.this, dialog);
        pager = (ViewPager) dialog.findViewById(R.id.viewpager);
        dotsLayout = (LinearLayout) dialog.findViewById(R.id.layoutDots);
        LinearLayout close_button = (LinearLayout) dialog.findViewById(R.id.close_button);
        addBottomDots(0);

        pager.setAdapter(adapters);
        pager.addOnPageChangeListener(viewPagerPageChangeListener);

        close_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (dialog != null) {
                    dialog.cancel();
                    dialog.dismiss();
                    sessionManager.SetOnboard(true);
                }
                return false;
            }
        });
        dialog.show();


    }

    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onPageSelected(int position) {
            selectedPageIndex = position;
            addBottomDots(position);
        }

        @Override
        public void onPageScrolled(int position, float arg1, int arg2) {
            if (exitWhenScrollNextPage && position == pager.getAdapter().getCount() - 1) {
                exitWhenScrollNextPage = false;
            }

        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == SCROLL_STATE_IDLE) {
                exitWhenScrollNextPage = selectedPageIndex == pager.getAdapter().getCount() - 1;
            }
        }
    };

    private void addBottomDots(int currentPage) {
        dots = new TextView[3];

        int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
        int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(20, 0, 20, 0);
            dots[i].setLayoutParams(params);

            dots[i].setTextColor(colorsInactive[currentPage]);
            dotsLayout.addView(dots[i]);
        }

        if (dots.length > 0)
            dots[currentPage].setTextColor(colorsActive[currentPage]);
    }

    public void setToolbarTitle() {
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toMyAccount = new Intent(Orders.this, MyAcccount.class);
                startActivity(toMyAccount);
                finish();
            }
        });
        ImageView toolbar_image = (ImageView) toolbar.findViewById(R.id.toolbar_image);
        toolbar_image.setVisibility(View.VISIBLE);

        int offset = (toolbar.getWidth() / 2) - (toolbar_image.getWidth() / 2);
        toolbar_image.setX(offset);

        TextView toolbar_title = (TextView) toolbar.findViewById(R.id.toolbar_title);
        toolbar_title.setVisibility(View.GONE);

        TextView toolbar_right = (TextView) toolbar.findViewById(R.id.toolbar_right);
        toolbar_right.setVisibility(View.GONE);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @OnClick(R.id.place_order_button)
    void newOrder() {

        Bundle bundle = new Bundle();
        bundle.putString("event_name", "order_initiated");
        bundle.putString("action", "Pressed Place a New Order button on home screen");
        bundle.putString("label", "Place a New Order");
        firebaseAnalytics.logEvent("order_initiated", bundle);

        Intent newOrder = new Intent(Orders.this, NewOrder.class);
        newOrder.putExtra("From", "Orders");
        startActivity(newOrder);

        finish();
    }

    public static Orders getInstance() {
        return orders;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem cancelMenuItem = menu.getItem(0);

        VectorDrawableCompat vectorDrawableCompat = VectorDrawableCompat.create(getResources(), R.drawable.ic_question, null);
        cancelMenuItem.setIcon(vectorDrawableCompat);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_mail) {

            Bundle bundle = new Bundle();
            bundle.putString("event_name", "viewed_help");
            bundle.putString("action", "Pressed question mark on home screen");
            bundle.putString("label", "Help");
            firebaseAnalytics.logEvent("viewed_help", bundle);

            UtilityClass.goToUrl(this, Constants.SUPPORT);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {

        if (doubleBackToExitPressedOnce) {
            finish();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, Orders.this.getResources().getString(R.string.exit_msg), Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    @Override
    public void onRefresh() {
        swipe_refresh_layout.setRefreshing(true);
        if (SessionManager.ORDER == null) SessionManager.ORDER = new Order();
        new BackgroundTask(this, SessionManager.ORDER.getClaims(this), lw_orders, swipe_refresh_layout, adapter, dataArray);

    }

    public void showalert() {
        if (from.equals("IntroFinishFragment") || from.equals("claims")) {

            UtilityClass.showAlertWithOk(this, "Thanks for your Order!", "Details will be sent to your email shortly.", "confirm-order");


        } else if (from.equals("nolocker")) {
            Bundle b = getIntent().getExtras();
            if (b != null) {
                String sourceString = b.getString("Data");
                UtilityClass.showAlertWithEmailRedirect(this, "null", sourceString, "null");

            }
        } else {

        }
    }

    @Override
    public void orderTypeData(ArrayList<GetOrdersModel> orderTypeModels) {
        if (orderTypeModels != null) {

            ArrayList<GetOrdersModel> arrayList = null;
            if (dataArray.size() > 0) {
                arrayList = new ArrayList<>();
                for (int i = 0; i < dataArray.size(); i++) {
                    GetOrdersModel getOrdersModel = new GetOrdersModel();
                    getOrdersModel = dataArray.get(i);
                    arrayList.add(getOrdersModel);
                }
                dataArray.clear();
                orderTypeModels.addAll(arrayList);

              //  Collections.sort(orderTypeModels);
              Collections.reverse(orderTypeModels);
                //Collections.sort(arraylist, Collections.reverseOrder());
                dataArray.addAll(orderTypeModels);
            }else {
                dataArray.addAll(orderTypeModels);
            }

            adapter.notifyDataSetChanged();
            swipe_refresh_layout.setRefreshing(false);


            swipe_refresh_layout.setRefreshing(false);
        }
    }

    @Override
    public void getClaimsData(ArrayList<GetOrdersModel> orderTypeModels) {



        if (orderTypeModels != null) {
            getCliamsList.clear();
            getCliamsList = orderTypeModels;

            adapter.setData(getCliamsList);
        }

    }


    public class NonUnderlinedClickableSpan extends ClickableSpan {

        String clicked;

        public NonUnderlinedClickableSpan(String string) {
            super();
            clicked = string;
        }

        public void onClick(View tv) {

        }

        public void updateDrawState(TextPaint ds) {
            ds.setColor(Color.BLACK);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void getOrderType() {

        if (SessionManager.ORDER == null) SessionManager.ORDER = new Order();
        if (sessionManager.getUserAddress() != null) {
            if (sessionManager.getUserShortAddress() != null) {
                UtilityClass.getLocationFromAddress(sessionManager.getUserShortAddress(), this);
            } else {
                UtilityClass.getLocationFromAddress(sessionManager.getUserAddress(), this);
            }

            if (sessionManager.getUserGeoLocation() == null) {
                sessionManager.saveAddressType("other");
                if (empty_textView != null)

                    if(Constants.BUSINESS_ID.equalsIgnoreCase("468")){
                        empty_textView.setText(getResources().getString(R.string.empty_list_text));

                    }else {

                        empty_textView.setText(getResources().getString(R.string.empty_list_text1));
                    }
            } else {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("token", Constants.TOKEN);
                if (sessionManager.getUserShortAddress() != null) {
                    params.put("address", sessionManager.getUserShortAddress());
                } else {
                    params.put("address", sessionManager.getUserAddress());
                }
                params.put("geolocation", sessionManager.getUserGeoLocation());
                params.put("sessionToken", sessionManager.getSessionToken());
                params.put("signature", Signature.getUrlConversion(params));
                ConfirmOrderTypeTask confirmOrderTypeTask = new ConfirmOrderTypeTask(this, SessionManager.ORDER.confirmOrderType(), this, params, "getOrderType", "orders");
                confirmOrderTypeTask.ResponseTask();
            }

        } else {
            sessionManager.saveAddressType("other");
            if (empty_textView != null)
                if(Constants.BUSINESS_ID.equalsIgnoreCase("468")){
                    empty_textView.setText(getResources().getString(R.string.empty_list_text));

                }else {

                    empty_textView.setText(getResources().getString(R.string.empty_list_text1));
                }        }
    }


    @Override
    public void addressMatchCase(String value, LocationModel locationModel) {
        switch (value) {
            case "true":
                if (!locationModel.getLocationType().equalsIgnoreCase("null")) {
                    if (locationModel.getLocationType().equalsIgnoreCase("Lockers")) {
                        sessionManager.saveAddressType("other");
                        if (empty_textView != null)
                            if(Constants.BUSINESS_ID.equalsIgnoreCase("468")){
                                empty_textView.setText(getResources().getString(R.string.empty_list_text));

                            }else {

                                empty_textView.setText(getResources().getString(R.string.empty_list_text1));
                            }

                    } else if (locationModel.getLocationType().equalsIgnoreCase("Concierge")
                            || locationModel.getLocationType().equalsIgnoreCase("Offices")) {
                        sessionManager.saveAddressType("Concierge");
                        if (empty_textView != null){
                            if(Constants.BUSINESS_ID.equalsIgnoreCase("468")){
                                empty_textView.setText(getResources().getString(R.string.empty_list_text));

                            }else {

                                empty_textView.setText(getResources().getString(R.string.empty_list_text1));
                            }
                        }

                    } else if (locationModel.getLocationType().equalsIgnoreCase("Kiosk")) {
                        sessionManager.saveAddressType("other");
                        if (empty_textView != null)
                            if(Constants.BUSINESS_ID.equalsIgnoreCase("468")){
                                empty_textView.setText(getResources().getString(R.string.empty_list_text));

                            }else {

                                empty_textView.setText(getResources().getString(R.string.empty_list_text1));
                            }                    } else {
                        sessionManager.saveAddressType("other");
                        if (empty_textView != null)
                            if(Constants.BUSINESS_ID.equalsIgnoreCase("468")){
                                empty_textView.setText(getResources().getString(R.string.empty_list_text));

                            }else {

                                empty_textView.setText(getResources().getString(R.string.empty_list_text1));
                            }                    }
                } else {
                    sessionManager.saveAddressType("other");
                    if (empty_textView != null)
                        if(Constants.BUSINESS_ID.equalsIgnoreCase("468")){
                            empty_textView.setText(getResources().getString(R.string.empty_list_text));

                        }else {

                            empty_textView.setText(getResources().getString(R.string.empty_list_text1));
                        }                }
                break;
            case "false":
                sessionManager.saveAddressType("other");
                if (empty_textView != null)
                    if(Constants.BUSINESS_ID.equalsIgnoreCase("468")){
                        empty_textView.setText(getResources().getString(R.string.empty_list_text));

                    }else {

                        empty_textView.setText(getResources().getString(R.string.empty_list_text1));
                    }
                    break;
        }
    }
}
