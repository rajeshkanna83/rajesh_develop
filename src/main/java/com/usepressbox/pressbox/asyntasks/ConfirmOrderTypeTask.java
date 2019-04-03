package com.usepressbox.pressbox.asyntasks;

import android.content.Context;
import android.text.Html;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.usepressbox.pressbox.interfaces.IConfirmOrderTypeListener;
import com.usepressbox.pressbox.interfaces.ISignUpListener;
import com.usepressbox.pressbox.models.ApiCallParams;
import com.usepressbox.pressbox.support.CustomProgressDialog;
import com.usepressbox.pressbox.support.ServerResponse;
import com.usepressbox.pressbox.support.VolleyResponseListener;
import com.usepressbox.pressbox.utils.AbstractClass;
import com.usepressbox.pressbox.utils.UtilityClass;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Prasanth.S on 8/20/2018.
 */
public class ConfirmOrderTypeTask extends AbstractClass {


    private Context context;
    private ApiCallParams apiCallParams;
    private String tag,nearBylocation,fromScreen;
    private ProgressBar progBar;
    private TextView lblMessage;
    private IConfirmOrderTypeListener iConfirmOrderType;
    private HashMap<String, String> params;
    private CustomProgressDialog progress;
    private ISignUpListener iSignUpListener;

    public ConfirmOrderTypeTask(Context context, ApiCallParams apiCallParams, IConfirmOrderTypeListener iConfirmOrderType, HashMap<String, String> params, String tag,String fromScreen) {
        this.context = context;
        this.apiCallParams = apiCallParams;
        this.iConfirmOrderType = iConfirmOrderType;
        this.tag = tag;
        this.params=params;
        this.fromScreen=fromScreen;
    }

    public ConfirmOrderTypeTask(Context context, ApiCallParams apiCallParams, ISignUpListener iSignUpListener, HashMap<String, String> params, String tag, String fromScreen) {
        this.context = context;
        this.apiCallParams = apiCallParams;
        this.iSignUpListener = iSignUpListener;
        this.tag = tag;
        this.params = params;
        this.fromScreen = fromScreen;
    }

    public ConfirmOrderTypeTask(Context context, ApiCallParams apiCallParams, IConfirmOrderTypeListener iConfirmOrderType, HashMap<String, String> params, String nearBylocation, String tag,String fromScreen) {
        this.context = context;
        this.apiCallParams = apiCallParams;
        this.iConfirmOrderType = iConfirmOrderType;
        this.tag = tag;
        this.nearBylocation=nearBylocation;
        this.params=params;
        this.fromScreen=fromScreen;
    }


    public void ResponseTask() {
            progress = CustomProgressDialog.show(context, false);


        new ServerResponse(apiCallParams.getUrl()).getJSONObjectfromURL(ServerResponse.RequestType.POST, params, context, new VolleyResponseListener() {
            @Override
            public void onError(String message) {
                    progress.dismiss();
                UtilityClass.showAlertWithOk(context, "Alert!", "Please try again", "place-order");

            }

            @Override
            public void onResponse(String response) {
                    progress.dismiss();


                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.optString("status");
                    if (status.equalsIgnoreCase("success")) {
                        switch (tag) {
                            case "getLockerNumber":
                                saveLocationData(context, jsonObject, tag, iConfirmOrderType,iSignUpListener,nearBylocation,fromScreen);
                                break;
                            case "getNearByLocation":
                                saveLocationData(context, jsonObject, tag, iConfirmOrderType,iSignUpListener,nearBylocation,fromScreen);
                                break;
                            case "getOrderType":
                                saveLocationData(context, jsonObject, tag, iConfirmOrderType,iSignUpListener,nearBylocation,fromScreen);
                                break;
                        }

                    } else {
                        String message = Html.fromHtml(jsonObject.getString("message")).toString();
                        UtilityClass.showAlertWithOk(context, "null", message, "ConfirmOrderTypeTask");
                        throw new JSONException("Api call returned unrecognised status: " + apiCallParams.getUrl());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                        progress.dismiss();

                }
            }
        });
    }


}
