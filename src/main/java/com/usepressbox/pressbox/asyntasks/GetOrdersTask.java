package com.usepressbox.pressbox.asyntasks;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import com.usepressbox.pressbox.interfaces.IAddressStatusListener;
import com.usepressbox.pressbox.interfaces.IOrderListListener;
import com.usepressbox.pressbox.models.ApiCallParams;
import com.usepressbox.pressbox.models.GetOrdersModel;
import com.usepressbox.pressbox.support.CustomProgressDialog;
import com.usepressbox.pressbox.support.ServerResponse;
import com.usepressbox.pressbox.support.VolleyResponseListener;
import com.usepressbox.pressbox.ui.activity.order.Orders;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Prasanth.S on 9/11/2018.
 */
public class GetOrdersTask {


    private Context context;
    private ApiCallParams apiCallParams;
    private String redirect;
    private TextView lblMessage;
    private HashMap<String, String> params;
    private CustomProgressDialog progress;

    public GetOrdersTask(Context context, ApiCallParams apiCallParams, String tag) {
        this.context = context;
        this.apiCallParams = apiCallParams;
        this.redirect = tag;
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
                    if (status.equalsIgnoreCase("success")) {
                        JSONArray dataObject = jsonObject.getJSONArray("data");

                        if (dataObject != null && dataObject.length() > 0) {

                            ArrayList<GetOrdersModel> arrayList = new ArrayList<>();
                            int count=0;
                            for (int i = dataObject.length()-1; i >=0; i--) {
                                   count++;
                                JSONObject jsonObject1 = dataObject.getJSONObject(i);

                                GetOrdersModel model = new GetOrdersModel();
                                model.setAddress(jsonObject1.getString("address"));
                                model.setLockerId(jsonObject1.getString("lockerName"));

                                model.setNotes(jsonObject1.getString("notes"));
                                model.setOrderID(jsonObject1.getString("orderID"));
                                model.setOrderTotal(jsonObject1.getString("orderTotal"));
                                model.setAccessCode(jsonObject1.getString("accessCode"));
                                model.setOrderType(jsonObject1.getString("orderType"));
                                model.setPayment(jsonObject1.getString("payment"));

                                if (jsonObject1.has("dateCreated")) {
                                    model.setDate(jsonObject1.getString("dateCreated"));
                                } else if (jsonObject1.has("updated")) {
                                    model.setDate(jsonObject1.getString("updated"));
                                }
                                if (jsonObject1.has("status")) {
                                    model.setStatus(jsonObject1.getString("status"));
                                } else {
                                    model.setStatus("Waiting for Service");
                                }
                                arrayList.add(model);

                                if(count==5)
                                    break;

                            }

                            IOrderListListener iOrderListListener = (IOrderListListener) context;
                            iOrderListListener.orderTypeData(arrayList);
                        }
                    } else {
                        String message = Html.fromHtml(jsonObject.getString("message")).toString();

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
