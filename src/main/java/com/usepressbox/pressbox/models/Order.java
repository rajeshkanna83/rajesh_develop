package com.usepressbox.pressbox.models;

import android.content.Context;

import com.usepressbox.pressbox.utils.ApiUrlGenerator;
import com.usepressbox.pressbox.utils.Constants;
import com.usepressbox.pressbox.utils.SessionManager;
import com.usepressbox.pressbox.utils.Signature;

import java.util.HashMap;

/**
 * Created by kruno on 21.04.16..
 * This model class is used to set and get the placed order values with params
 */
public class Order {

    private String lockerName;
    private String orderNotes;
    private String orderType;


    public Order() {

        this.lockerName = "";
        this.orderNotes = "";
        this.orderType = "";

    }

    public Order(String lockerName, String orderNotes, String orderType) {

        this.lockerName = lockerName;
        this.orderNotes = orderNotes;
        this.orderType = orderType;

    }

    public String getLockerName() {
        return lockerName;
    }

    public void setLockerName(String lockerName) {
        this.lockerName = lockerName;
    }

    public String getOrderNotes() {
        return orderNotes;
    }

    public void setOrderNotes(String orderNotes) {
        this.orderNotes = orderNotes;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }


    public ApiCallParams claimsCreate(Context context) {

        String endpoint = "claims/create";
        String url = "http://droplocker.com/api/v2_0/claims/create";

        HashMap<String, String> namevaluepair = new HashMap<String, String>();
        namevaluepair.put("token", Constants.TOKEN);
        namevaluepair.put("lockerName", new SessionManager(context).getLockerNumber());
       String starchID= SessionManager.CUSTOMER.getStarchOnShirtsId();
        String detergentName= SessionManager.ORDER_PREFERENCE.getDetergentName();
        namevaluepair.put("orderType".trim(), orderType);
        if (!orderNotes.equalsIgnoreCase("")) {
            orderNotes = starchID +","+detergentName +","+orderNotes;
            namevaluepair.put("orderNotes", orderNotes);
        }
        namevaluepair.put("sessionToken", new SessionManager(context).getSessionToken());
        namevaluepair.put("business_id", Constants.BUSINESS_ID);
        namevaluepair.put("signature", Signature.getUrlConversion(namevaluepair));

        return new ApiCallParams(namevaluepair, url, endpoint);
    }


    public ApiCallParams getClaims(Context context) {

        String url = "http://droplocker.com/api/v1_8_0_1/customers/getClaims";

        String endpoint = "customers/getClaims";

        HashMap<String, String> namevaluepair = new HashMap<String, String>();
        namevaluepair.put("token", Constants.TOKEN);
        namevaluepair.put("sessionToken", new SessionManager(context).getSessionToken());
        namevaluepair.put("business_id", Constants.BUSINESS_ID);
        namevaluepair.put("signature", Signature.getUrlConversion(namevaluepair));
        return new ApiCallParams(namevaluepair, url, endpoint);
    }

    public ApiCallParams getOrders(Context context) {
        String url = "http://droplocker.com/api/v2_1/customers/getOrders";

        String endpoint = "customers/getOrders";

        HashMap<String, String> namevaluepair = new HashMap<String, String>();
        namevaluepair.put("token", Constants.TOKEN);
        namevaluepair.put("sessionToken", new SessionManager(context).getSessionToken());
        namevaluepair.put("business_id", Constants.BUSINESS_ID);
        namevaluepair.put("signature", Signature.getUrlConversion(namevaluepair));
        return new ApiCallParams(namevaluepair, url, endpoint);
    }


    public ApiCallParams getServiceType(Context context) {

        String endpoint = "businesses/get_serviceTypes";
        String url = "http://droplocker.com/api/v2_2/businesses/get_serviceTypes";

        HashMap<String, String> namevaluepair = new HashMap<String, String>();
        namevaluepair.put("token", Constants.TOKEN);
        namevaluepair.put("business_id", Constants.BUSINESS_ID);
        namevaluepair.put("signature", Signature.getUrlConversion(namevaluepair));
        return new ApiCallParams(namevaluepair, url, endpoint);
    }


    public ApiCallParams confirmOrderType() {

        String endpoint = "locations/search";

        String url = "http://droplocker.com/api/v2_2/locations/search";

        return new ApiCallParams(null, url, endpoint);
    }

    public ApiCallParams getLockerType() {

        String endpoint = "lockers/get";

        String url = "http://droplocker.com/api/v2_2/lockers/get";

        return new ApiCallParams(null, url, endpoint);
    }

    public ApiCallParams getNearByLocation() {

        String endpoint = "locations/get_by_distance_for_business";
        String url = "http://droplocker.com/api/v2_2/locations/get_by_distance_for_business";

        return new ApiCallParams(null, url, endpoint);
    }
}
