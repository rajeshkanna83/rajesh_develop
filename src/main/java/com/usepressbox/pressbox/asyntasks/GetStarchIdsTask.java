package com.usepressbox.pressbox.asyntasks;
        import android.content.Context;
        import android.text.Html;
        import android.util.Log;
        import android.widget.TextView;

        import com.usepressbox.pressbox.interfaces.IAddressStatusListener;
        import com.usepressbox.pressbox.interfaces.IOrderPreferenceListener;
        import com.usepressbox.pressbox.models.ApiCallParams;
        import com.usepressbox.pressbox.support.CustomProgressDialog;
        import com.usepressbox.pressbox.support.ServerResponse;
        import com.usepressbox.pressbox.support.VolleyResponseListener;
        import com.usepressbox.pressbox.ui.activity.order.OrderPreferences;
        import com.usepressbox.pressbox.utils.Constants;

        import org.json.JSONArray;
        import org.json.JSONException;
        import org.json.JSONObject;

        import java.util.HashMap;
        import java.util.Iterator;

        import javax.sql.StatementEvent;



public class GetStarchIdsTask {
    private Context context;
    private ApiCallParams apiCallParams;
    private String redirect;
    private CustomProgressDialog progress;

    public GetStarchIdsTask(Context context, ApiCallParams apiCallParams, String tag) {
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
                        String starchID = null, starchID1 = null, starchID2 = null,starchforme1 = null, starchname2 = null, starchname3 = null;

                        if (data.has("Wash and Fold")) {
                            JSONObject washNdfold = data.getJSONObject("Dry Clean");

                            if(washNdfold.has("starchonshirts")) {
                                JSONObject DryClean = washNdfold.getJSONObject("starchonshirts");
                                Log.e("Response ","****starchonshirts***"+ DryClean);

                                Iterator iteratorS = DryClean.keys();
                                JSONArray convertedStarchArray = new JSONArray();
                                String key = null;

                                while (iteratorS.hasNext()) {
                                    key = (String) iteratorS.next();
                                    convertedStarchArray.put(DryClean.get(key));


                                }
                                for (int i = 0; i < convertedStarchArray.length(); i++) {

                                    JSONObject value = convertedStarchArray.getJSONObject(i);

                                    if (value.has("name")) {
                                        String name = value.getString("displayName");
                                        if (name.equalsIgnoreCase("Normal")) {

                                            starchID = value.getString("productID");
                                        } else if (name.equalsIgnoreCase("Heavy")){

                                            starchID1 = value.getString("productID");
                                        }
                                        else if (name.equalsIgnoreCase("Light")){

                                            starchID2 = value.getString("productID");
                                        }

                                    }
                            }  }


                              else if (washNdfold.has("starch")) {
                                JSONObject DryClean = washNdfold.getJSONObject("starch");

                                Iterator iteratorS = DryClean.keys();
                                JSONArray convertedStarchArray = new JSONArray();
                                String key = null;
                                Log.e("Response 2 ","****starch***"+ DryClean);
                                while (iteratorS.hasNext()) {
                                    key = (String) iteratorS.next();
                                    convertedStarchArray.put(DryClean.get(key));


                                }

                                for (int i = 0; i < convertedStarchArray.length(); i++) {

                                    JSONObject value = convertedStarchArray.getJSONObject(i);

                                    if (value.has("name")) {
                                        String name = value.getString("displayName");
                                        if (name.equalsIgnoreCase("Medium Starch")) {

                                            starchID = value.getString("productID");
                                        } else if (name.equalsIgnoreCase("Heavy Starch")){

                                            starchID1 = value.getString("productID");
                                        }
                                        else if (name.equalsIgnoreCase("Light Starch")){

                                            starchID2 = value.getString("productID");
                                        }

                                    }
                                }



                            }
                        IOrderPreferenceListener iOrderPreferenceListener = (OrderPreferences) context;
                        iOrderPreferenceListener.getStarchID(starchID, starchID1, starchID2);
                        }
                    } else {

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
