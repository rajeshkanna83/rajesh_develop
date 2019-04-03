package com.usepressbox.pressbox.asyntasks;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.usepressbox.pressbox.models.ApiCallParams;
import com.usepressbox.pressbox.support.ServerResponse;
import com.usepressbox.pressbox.support.VolleyResponseListener;
import com.usepressbox.pressbox.utils.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Prasanth.S on 9/11/2018.
 */
public class SavePromoCodeTask {


    private Context context;
    private ApiCallParams apiCallParams;
    private String redirect;
    private TextView lblMessage;
    private HashMap<String, String> params;
    private static FirebaseAnalytics firebaseAnalytics;

    public SavePromoCodeTask(Context context, ApiCallParams apiCallParams, String tag) {
        this.context = context;
        this.apiCallParams = apiCallParams;
        this.redirect = tag;
        firebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }



    public void ResponseTask() {


        new ServerResponse(apiCallParams.getUrl()).getJSONObjectfromURL(ServerResponse.RequestType.POST, apiCallParams.getParams(), context, new VolleyResponseListener() {
            @Override
            public void onError(String message) {

            }

            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.optString("status");
                    JSONObject data = jsonObject.optJSONObject("data");
                    String discount = data.optString("discount");
                    String promoCode = data.optString("promoCode");
                    if (status.equalsIgnoreCase("success")) {

                        Bundle bundle = new Bundle();
                        bundle.putString("event_name", "promo_applied_register");
                        bundle.putString("action", "Pressed register on the pre-registration screen and promo sucessfully applied");
                        bundle.putString("label", "Promo Applied - Register");
                        bundle.putString("promocode_used", promoCode);
                        bundle.putString("promocode_type", "");
                        bundle.putString("promocode_amount", discount);
                        firebaseAnalytics.logEvent("promo_applied_register", bundle);

                        if(!SessionManager.CUSTOMER.getPromoCode().equalsIgnoreCase( ""))
                        new SessionManager(context).savePromoCode(SessionManager.CUSTOMER.getPromoCode());

                    } else {

                        Bundle bundle = new Bundle();
                        bundle.putString("event_name", "promo_failed_register");
                        bundle.putString("action", "Pressed Register on the pre-registration screen and promo unsuccessfull");
                        bundle.putString("label", "Promo Failed - Register");
                        bundle.putString("promocode_used", SessionManager.CUSTOMER.getPromoCode());
                        bundle.putString("promocode_type", "");
                        bundle.putString("promocode_amount", "");
                        firebaseAnalytics.logEvent("promo_failed_register", bundle);

                        new SessionManager(context).savePromoCode(null);
                        String message = Html.fromHtml(jsonObject.getString("message")).toString();
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                        throw new JSONException("Api call returned unrecognised status: " + apiCallParams.getUrl());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


}
