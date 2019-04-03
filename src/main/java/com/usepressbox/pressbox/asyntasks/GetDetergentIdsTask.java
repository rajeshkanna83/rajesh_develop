package com.usepressbox.pressbox.asyntasks;

import android.content.Context;
import android.text.Html;
import android.widget.TextView;

import com.usepressbox.pressbox.interfaces.IAddressStatusListener;
import com.usepressbox.pressbox.interfaces.IOrderPreferenceListener;
import com.usepressbox.pressbox.models.ApiCallParams;
import com.usepressbox.pressbox.support.CustomProgressDialog;
import com.usepressbox.pressbox.support.ServerResponse;
import com.usepressbox.pressbox.support.VolleyResponseListener;
import com.usepressbox.pressbox.ui.activity.order.OrderPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

import javax.sql.StatementEvent;

/**
 * Created by Prasanth.S on 9/11/2018.
 */
public class GetDetergentIdsTask {


    private Context context;
    private ApiCallParams apiCallParams;
    private String redirect;
    private TextView lblMessage;
    private HashMap<String, String> params;
    private CustomProgressDialog progress;

    public GetDetergentIdsTask(Context context, ApiCallParams apiCallParams, String tag) {
        this.context = context;
        this.apiCallParams = apiCallParams;
        this.redirect = tag;
    }


    public void ResponseTask() {
        progress = CustomProgressDialog.show(context, false);

        new ServerResponse(apiCallParams.getUrl()).getJSONObjectfromURL(ServerResponse.RequestType.POST, apiCallParams.getParams(), context, new VolleyResponseListener() {
            @Override
            public void onError(String message) {
                progress.dismiss();

            }

            @Override
            public void onResponse(String response) {
                progress.dismiss();

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.optString("status");
                    if (status.equalsIgnoreCase("success")) {

                        JSONObject data = jsonObject.getJSONObject("data");
                        if (data.has("Wash and Fold")) {
                            JSONObject washNdfold = data.getJSONObject("Wash and Fold");

                            if (washNdfold.has("detergent")) {
                                JSONObject detergent = washNdfold.getJSONObject("detergent");

                                Iterator iterator = detergent.keys();
                                JSONArray convertedArray = new JSONArray();
                                String key = null;
                                while (iterator.hasNext()) {
                                    key = (String) iterator.next();
                                    convertedArray.put(detergent.get(key));
                                }

                                String tideID = null, tideFreeID = null, detergentName1 = null, detergentname2 = null;
                                for (int i = 0; i < convertedArray.length(); i++) {

                                    JSONObject value = convertedArray.getJSONObject(i);
                                    if (value.has("name")) {
                                        String name=value.getString("name");
                                        if (name.equalsIgnoreCase("Tide")){
                                            detergentName1 = name;
                                            tideID = value.getString("productID");
                                        } else
                                        if (name.equalsIgnoreCase("Tide Free and Gentle")
                                                || name.equalsIgnoreCase("Tide Free & Gentle")) {
                                            detergentname2 =name;
                                            tideFreeID = value.getString("productID");
                                        }
                                    }
                                }
                                IOrderPreferenceListener iOrderPreferenceListener = (OrderPreferences) context;
                                iOrderPreferenceListener.getDetergentId(tideID, tideFreeID, detergentName1, detergentname2);
                            }
                        }

                    } else {
                        String message = Html.fromHtml(jsonObject.getString("message")).toString();

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
