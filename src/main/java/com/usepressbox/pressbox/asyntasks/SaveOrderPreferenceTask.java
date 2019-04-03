package com.usepressbox.pressbox.asyntasks;

import android.content.Context;
import android.text.Html;
import android.widget.TextView;
import android.widget.Toast;

import com.usepressbox.pressbox.interfaces.IOrderPreferenceListener;
import com.usepressbox.pressbox.models.ApiCallParams;
import com.usepressbox.pressbox.support.CustomProgressDialog;
import com.usepressbox.pressbox.support.ServerResponse;
import com.usepressbox.pressbox.support.VolleyResponseListener;
import com.usepressbox.pressbox.ui.activity.order.OrderPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Prasanth.S on 9/11/2018.
 */
public class SaveOrderPreferenceTask {


    private Context context;
    private ApiCallParams apiCallParams;
    private String redirect;

    public SaveOrderPreferenceTask(Context context, ApiCallParams apiCallParams, String tag) {
        this.context = context;
        this.apiCallParams = apiCallParams;
        this.redirect = tag;
    }


    public void ResponseTask() {

        new ServerResponse(apiCallParams.getUrl()).getJSONObjectfromURL(ServerResponse.RequestType.POST, apiCallParams.getParams(), context, new VolleyResponseListener() {
            @Override
            public void onError(String message) {
                IOrderPreferenceListener iOrderPreferenceListener = (OrderPreferences) context;
                iOrderPreferenceListener.updateDetergent("Failure");
            }

            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.optString("status");
                    if (status.equalsIgnoreCase("success")) {
                        IOrderPreferenceListener iOrderPreferenceListener = (OrderPreferences) context;
                        iOrderPreferenceListener.updateDetergent("success");

                    } else {
                        String message = Html.fromHtml(jsonObject.getString("message")).toString();
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                        IOrderPreferenceListener iOrderPreferenceListener = (OrderPreferences) context;
                        iOrderPreferenceListener.updateDetergent("Failure");
                        throw new JSONException("Api call returned unrecognised status: " + apiCallParams.getUrl());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                    IOrderPreferenceListener iOrderPreferenceListener = (OrderPreferences) context;
                    iOrderPreferenceListener.updateDetergent("Failure");
                }
            }
        });
    }


}
