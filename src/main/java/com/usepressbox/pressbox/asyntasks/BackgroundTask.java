package com.usepressbox.pressbox.asyntasks;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.usepressbox.pressbox.R;
import com.usepressbox.pressbox.adapter.OrdersAdapter;
import com.usepressbox.pressbox.interfaces.IConfirmOrderTypeListener;
import com.usepressbox.pressbox.interfaces.IOrderListListener;
import com.usepressbox.pressbox.interfaces.IOrderPreferenceListener;
import com.usepressbox.pressbox.interfaces.IPromoCodeStatusListener;
import com.usepressbox.pressbox.interfaces.ISelectServiceListener;
import com.usepressbox.pressbox.interfaces.ISignUpListener;
import com.usepressbox.pressbox.models.ApiCallParams;
import com.usepressbox.pressbox.models.GetOrdersModel;
import com.usepressbox.pressbox.support.CustomProgressDialog;
import com.usepressbox.pressbox.support.CustomerDetails;
import com.usepressbox.pressbox.support.ServerResponse;
import com.usepressbox.pressbox.support.ServiceTypeDetails;
import com.usepressbox.pressbox.support.VolleyResponseListener;
import com.usepressbox.pressbox.ui.activity.order.OrderPreferences;
import com.usepressbox.pressbox.ui.activity.order.Orders;
import com.usepressbox.pressbox.ui.activity.register.Intro;
import com.usepressbox.pressbox.ui.activity.register.Login;
import com.usepressbox.pressbox.ui.fragment.SelectServices;
import com.usepressbox.pressbox.utils.Constants;
import com.usepressbox.pressbox.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by kruno on 12.04.16..
 * This class holds the background tasks to update the values to the api with respect to endpoints
 */
public class BackgroundTask {

    private static FirebaseAnalytics firebaseAnalytics;
    private Activity context;
    private ApiCallParams apiCallParams;
    private ListView mListView;
    private String tag = "test";
    private SwipeRefreshLayout swipe_refresh_layout;
    private ArrayList<GetOrdersModel> dataArray = new ArrayList<>();
    private OrdersAdapter adapter;
    private ISelectServiceListener iSelectServiceListener;
    private FragmentManager fragmentManager;
    private IConfirmOrderTypeListener iConfirmOrderType;
    private IPromoCodeStatusListener iPromoCodeStatusListener;

    private CustomProgressDialog progress;
    TextView lblMessage;
    private ISignUpListener iSignUpListener;

    public BackgroundTask(Activity context, ApiCallParams apiCallParams, String tag) {
        this.context = context;
        this.apiCallParams = apiCallParams;
        this.tag = tag;

        if (apiCallParams.getTag().equalsIgnoreCase("customers/validate") ||
                (apiCallParams.getTag().equalsIgnoreCase("customers/create")) ||
                (apiCallParams.getTag().equalsIgnoreCase("customers/addCoupon"))) {
            progress = CustomProgressDialog.show(context, false);
        }
        ResponseTask();
    }

    public BackgroundTask(Activity context, ApiCallParams apiCallParams, ISignUpListener iSignUpListener, String tag) {
        this.context = context;
        this.apiCallParams = apiCallParams;
        this.tag = tag;
        this.iSignUpListener = iSignUpListener;
        progress = CustomProgressDialog.show(context, false);

        ResponseTask();
    }

    public BackgroundTask(Activity context, ApiCallParams apiCallParams, FragmentManager fragmentManager, String tag) {
        this.context = context;
        this.apiCallParams = apiCallParams;
        this.tag = tag;
        this.fragmentManager = fragmentManager;
        progress = CustomProgressDialog.show(context, false);


        ResponseTask();
    }

    public BackgroundTask(Activity context, ApiCallParams apiCallParams) {
        this.context = context;
        this.apiCallParams = apiCallParams;

        ResponseTask();
    }

    public BackgroundTask(Activity context, ApiCallParams apiCallParams, ListView listView, SwipeRefreshLayout swipe_refresh_layout, OrdersAdapter adapter, ArrayList<GetOrdersModel> dataArray) {
        this.context = context;
        this.apiCallParams = apiCallParams;
        this.mListView = listView;
        this.swipe_refresh_layout = swipe_refresh_layout;
        this.adapter = adapter;
        this.dataArray = dataArray;
        ResponseTask();
    }


    public BackgroundTask(Activity context, SelectServices selectServices, ApiCallParams apiCallParams) {

        this.context = context;
        this.apiCallParams = apiCallParams;
        this.iSelectServiceListener = selectServices;

        progress = CustomProgressDialog.show(context, false);

        ResponseTask();
    }

    public BackgroundTask(Activity activity, ApiCallParams apiCallParams, IConfirmOrderTypeListener iConfirmOrderType, String from) {
        this.context = activity;
        this.apiCallParams = apiCallParams;
        this.iConfirmOrderType = iConfirmOrderType;
        this.tag = from;
        progress = CustomProgressDialog.show(context, false);


        ResponseTask();
    }

    public BackgroundTask(Activity activity, ApiCallParams apiCallParams, IPromoCodeStatusListener iPromoCodeStatusListener, String from) {
        this.context = activity;
        this.apiCallParams = apiCallParams;
        this.iPromoCodeStatusListener = iPromoCodeStatusListener;
        this.tag = from;
        progress = CustomProgressDialog.show(context, false);


        ResponseTask();
    }


    public void ResponseTask() {


        if (tag.equals("bleach") || tag.equals("dryerSheets") || tag.equals("softner") || tag.equals("login")) {

        } else {

        }

        new ServerResponse(apiCallParams.getUrl()).getJSONObjectfromURL(ServerResponse.RequestType.POST, apiCallParams.getParams(), context, new VolleyResponseListener() {
            @Override
            public void onError(String message) {
                if (progress != null)
                    progress.dismiss();

            }
            @Override
            public void onResponse(String response) {
                if (progress != null)
                    progress.dismiss();

                if (tag.equals("orderPreferences") || tag.equals("bleach") || tag.equals("dryerSheets") || tag.equals("softner") || tag.equals("login") || tag.equalsIgnoreCase("nil")) {
                } else {
                    if (progress != null) {
                        if (progress.isShowing())
                            progress.dismiss();
                    }
                }
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if (status.equals("success")) {

                        SessionManager sessionManager = new SessionManager(context);
                        switch (apiCallParams.getTag()) {
                            case "customers/create":
                                sessionManager.saveSessionToken(jsonObject.getJSONObject("data").getString("sessionToken"));
                                SessionManager.CUSTOMER.setId(jsonObject.getJSONObject("data").getInt("customer_id"));

                                sessionManager.saveUserName(SessionManager.CUSTOMER.getEmail());
                                sessionManager.savePassword(SessionManager.CUSTOMER.getPassword());

                                SaveUserAddressTask addressTask = new SaveUserAddressTask(context, SessionManager.CUSTOMER.updateUSerAddress(context), "Register");
                                addressTask.ResponseTask();

                                if (SessionManager.CUSTOMER.getPromoCode() != "") {
                                    SavePromoCodeTask savePromoCodeTask = new SavePromoCodeTask(context, SessionManager.CUSTOMER.savePromoCode(context), "backgroundTask");
                                    savePromoCodeTask.ResponseTask();
                                }


                                Intent toLocker = new Intent(context, Intro.class);
                                context.startActivity(toLocker);
                                context.finish();

                                break;

                            case "customers/updateProfile":
                                if (tag.equals("myAccount")) {

                                    Intent toOrders = new Intent(context, Orders.class);
                                    context.startActivity(toOrders);
                                    context.finish();

                                } else if (tag.equals("orderPreferences")) {
                                    IOrderPreferenceListener iOrderPreferenceListener = (OrderPreferences) context;
                                    iOrderPreferenceListener.orderPreferenceStatus("success");


                                } else {


                                }
                                break;

                            case "customers/validate":
                                sessionManager.saveSessionToken(jsonObject.getJSONObject("data").getString("sessionToken"));
                                sessionManager.saveUserName(SessionManager.CUSTOMER.getEmail());
                                sessionManager.savePassword(SessionManager.CUSTOMER.getPassword());
                                new BackgroundTask(context, SessionManager.CUSTOMER.details(context), "login");

                                firebaseAnalytics = FirebaseAnalytics.getInstance(context);

                                Bundle bundle = new Bundle();
                                bundle.putString("event_name", "logged_in");
                                bundle.putString("action", "After authentication completed on login page");
                                bundle.putString("label", "Logged in Successfully");
                                firebaseAnalytics.logEvent("logged_in", bundle);

                                break;

                            case "customers/sendForgotPasswordEmail":
                                if (Constants.FROM.equals("Myaccount")) {
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

                                    alertDialogBuilder.setMessage(context.getResources().getString(R.string.resetpwd_sucess)).setCancelable(false)
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                    dialog.dismiss();
                                                    Constants.FROM = "";


                                                }
                                            });

                                    AlertDialog alertDialog = alertDialogBuilder.create();
                                    alertDialog.show();

                                } else {
                                    Intent intent_settings = new Intent(context, Login.class);
                                    intent_settings.putExtra("from", "ForgotPassword");
                                    context.startActivity(intent_settings);
                                    context.finish();
                                }
                                break;

                            case "customers/details":
                                new CustomerDetails(jsonObject, context);

                                if (tag.equals("orderPreference")) {

                                } else {

                                    Intent toOrderScreen = new Intent(context, Orders.class);
                                    String percent = sessionManager.getPerentage();
                                    String code = sessionManager.getCode();
                                    toOrderScreen.putExtra("percentage", percent);
                                    toOrderScreen.putExtra("code", code);
                                    sessionManager.saveCode("null");
                                    sessionManager.savePercentage("null");
                                    context.startActivity(toOrderScreen);
                                }
                                context.finish();
                                break;

                            case "claims/create":

                                Intent toOrder = new Intent(context, Orders.class);
                                toOrder.putExtra("From", "claims");
                                context.startActivity(toOrder);
                                context.finish();


                                break;

                            case "customers/getClaims":
                                JSONArray claimsObject = jsonObject.getJSONObject("data").getJSONArray("claims");

                                dataArray.clear();
                                for (int i = 0; i < claimsObject.length(); i++) {

                                    JSONObject jsonObject1 = claimsObject.getJSONObject(i);

                                    GetOrdersModel model = new GetOrdersModel();
                                    model.setAddress(jsonObject1.getString("address"));
                                    model.setDate(jsonObject1.getString("updated"));
                                    model.setLockerId(jsonObject1.getString("lockerName"));
                                    if (jsonObject1.has("status")) {
                                        model.setStatus(jsonObject1.getString("status"));
                                    } else {
                                        model.setStatus("Waiting for Service");
                                    }

                                    dataArray.add(model);
                                }

                                if(dataArray!=null)
                                    Collections.reverse(dataArray);

                                GetOrdersTask getOrdersTask = new GetOrdersTask(context, SessionManager.ORDER.getOrders(context), "BackgroungTask");
                                getOrdersTask.ResponseTask();
                                adapter.notifyDataSetChanged();
                                swipe_refresh_layout.setRefreshing(false);
                                break;

                            case "customers/updateBilling":

                                break;

                            case "customers/addCoupon":
                                SessionManager.CUSTOMER.setPromoCode("");


                                if (tag.equalsIgnoreCase("NewLockerFragment")) {
                                    String statusNewLockerFragment = jsonObject.optString("status");
                                    if (statusNewLockerFragment.equals("success")) {
                                        JSONObject data = jsonObject.optJSONObject("data");
                                        String discount = data.optString("discount");
                                        String promoCode = data.optString("promoCode");

                                        Bundle bundlePromo = new Bundle();
                                        bundlePromo.putString("promocode", "Promo sucessfully applied");
                                        bundlePromo.putString("action", "Pressed Apply Promo code on the New order screen and promo successfully applied");
                                        bundlePromo.putString("label", "Promo Applied - Order");
                                        bundlePromo.putString("promocode_used", promoCode);
                                        bundlePromo.putString("promocode_type", "");
                                        bundlePromo.putString("promocode_amount", discount);

                                        firebaseAnalytics.logEvent("promo_applied_order", bundlePromo);

                                    } else {
                                        Bundle bundlePromo = new Bundle();
                                        bundlePromo.putString("event_name", "promo_failed_order");
                                        bundlePromo.putString("action", "Pressed Apply Promo code on the New order screen and promo unsuccessful");
                                        bundlePromo.putString("label", "Promo Failed - Order");
                                        bundlePromo.putString("promocode_used", "");
                                        bundlePromo.putString("promocode_type", "");
                                        bundlePromo.putString("promocode_amount", "");

                                        firebaseAnalytics.logEvent("promo_failed_order", bundlePromo);

                                    }
                                    iConfirmOrderType.promoCodeStatus(jsonObject.optString("status"), jsonObject.optString("message"));
                                } else {
                                    iPromoCodeStatusListener.promoCodeStatus(jsonObject.optString("status"), jsonObject.optString("message"));
                                }

                                break;

                            case "customers/laundryPreferences":
                                break;

                            case "businesses/getStarchOnShirts":
                                break;
                            case "businesses/get_serviceTypes":
                                new ServiceTypeDetails(jsonObject, iSelectServiceListener);

                                break;

                            case "business/settings":
                                break;

                            case "customers/updateLaundryPreference":


                                if (tag.equals("dryerSheets")) {
                                    new BackgroundTask(context, SessionManager.ORDER_PREFERENCE.updateDryerSheet(context), "softner");
                                }
                                if (tag.equals("softner")) {
                                    new BackgroundTask(context, SessionManager.ORDER_PREFERENCE.updateFabricSoftner(context));
                                    if (Constants.firstTime) {
                                        Intent toIntro = new Intent(context, Intro.class);
                                        context.startActivity(toIntro);
                                        context.finish();
                                    } else {
                                        context.finish();
                                    }
                                }
                                break;
                        }

                    } else if (status.equals("error")) {
                        String message = Html.fromHtml(jsonObject.getString("message")).toString();


                        if (tag.equals("claims")) {
                            String data = Html.fromHtml(jsonObject.getString("data")).toString();


                            if (data.equals("[]") || data.isEmpty() || data.equalsIgnoreCase("") || data.equalsIgnoreCase("null")) {

                                iConfirmOrderType.LockerStatus(message, data);

                            } else {

                                iConfirmOrderType.LockerStatus(message, data);

                            }

                        } else if (tag.equals("IntroFinishFragment")) {

                            String data = Html.fromHtml(jsonObject.getString("data")).toString();

                            if (data.equals("[]") || data.isEmpty() || data.equalsIgnoreCase("") || data.equalsIgnoreCase("null")) {
                                Intent toOrder = new Intent(context, Orders.class);
                                toOrder.putExtra("Data", message);
                                context.startActivity(toOrder);
                                context.finish();
                            } else {
                                Intent toOrder = new Intent(context, Orders.class);
                                toOrder.putExtra("Data", data);
                                context.startActivity(toOrder);
                                context.finish();
                            }

                        } else if (tag.equalsIgnoreCase("orderPreferences")) {
                            IOrderPreferenceListener iOrderPreferenceListener = (OrderPreferences) context;
                            iOrderPreferenceListener.orderPreferenceStatus("Failure");
                        } else {

                            switch (tag) {
                                case "NewLockerFragment":
                                    iConfirmOrderType.promoCodeStatus(jsonObject.optString("status"), jsonObject.optString("message"));
                                    break;
                                case "MyaccountClass":
                                    iPromoCodeStatusListener.promoCodeStatus(jsonObject.optString("status"), jsonObject.optString("message"));
                                    break;
                                default:
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                            }

                        }
                    } else if (status.equals("fail")) {
                        switch (apiCallParams.getTag()) {
                            case "customers/validate":
                                SessionManager sessionManager = new SessionManager(context);
                                sessionManager.clearSession();
                                Intent toLogin = new Intent(context, Login.class);
                                context.startActivity(toLogin);
                                context.finish();
                                break;
                        }

                        switch (tag) {
                            case "NewLockerFragment":
                                iConfirmOrderType.promoCodeStatus(jsonObject.optString("status"), jsonObject.optString("message"));
                                break;

                            default:
                                Toast.makeText(context, jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                        }

                    } else {
                        throw new JSONException("Api call returned unrecognised status: " + apiCallParams.getUrl());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                    if (progress != null)
                        progress.dismiss();
                }
            }
        });
    }
}
