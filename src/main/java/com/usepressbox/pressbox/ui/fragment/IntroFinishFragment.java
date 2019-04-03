package com.usepressbox.pressbox.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.usepressbox.pressbox.R;
import com.usepressbox.pressbox.asyntasks.BackgroundTask;
import com.usepressbox.pressbox.asyntasks.ConfirmOrderTypeTask;
import com.usepressbox.pressbox.asyntasks.SaveOrderPreferenceTask;
import com.usepressbox.pressbox.asyntasks.SaveUserAddressTask;
import com.usepressbox.pressbox.interfaces.ISignUpListener;
import com.usepressbox.pressbox.models.LocationModel;
import com.usepressbox.pressbox.models.Order;
import com.usepressbox.pressbox.ui.activity.order.NewOrder;
import com.usepressbox.pressbox.ui.activity.order.OrderPreferences;
import com.usepressbox.pressbox.ui.activity.order.Orders;
import com.usepressbox.pressbox.ui.activity.order.Video;
import com.usepressbox.pressbox.ui.activity.register.Intro;
import com.usepressbox.pressbox.utils.Constants;
import com.usepressbox.pressbox.utils.SessionManager;
import com.usepressbox.pressbox.utils.Signature;
import com.usepressbox.pressbox.utils.UtilityClass;

import java.util.Calendar;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by kruno on 26.04.16..
 * This fragment is used to update the card details and order preferences on registration
 */
public class IntroFinishFragment extends Fragment implements ISignUpListener {

    private View v;
    private Toolbar toolbar;
    @BindView(R.id.toolbar_left)
    TextView skip;
    @BindView(R.id.toolbar_right)
    TextView next;
    @BindView(R.id.toolbar_title)
    TextView title;
    @BindView(R.id.tw_go_home)
    TextView goHome;
    @BindView(R.id.credit_card_number)
    EditText credit_card_number;
    @BindView(R.id.credit_card_date)
    EditText credit_card_date;
    @BindView(R.id.credit_card_CVV)
    EditText credit_card_CVV;
    SessionManager sessionManager;
    @BindView(R.id.tw_locker_message)
    TextView tw_locker_message;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UtilityClass.hideKeyboard(getActivity());
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (container == null) {
            return null;
        }

        v = inflater.inflate(R.layout.fragment_intro_finish, container, false);
        ButterKnife.bind(this, v);
        sessionManager = new SessionManager(getContext());
        if(Constants.BUSINESS_ID.equalsIgnoreCase("468")){
            tw_locker_message.setText(getResources().getString(R.string.text_cinci));

        }else {

            tw_locker_message.setText(getResources().getString(R.string.text1));
        }
        setToolbarTitle();
        setDateFormat();
        return v;
    }

    @OnClick(R.id.tw_go_home)
    void toOrders() {

        updateBilling();
    }

    @OnClick(R.id.linear_layout_order_pref)
    void toOrderPreferences() {
        Intent orderPreferences = new Intent(getContext(), OrderPreferences.class);
        orderPreferences.putExtra("From", "IntroFinishFragment");
        startActivity(orderPreferences);
        Constants.firstTime = true;
    }

    public void setToolbarTitle() {
        toolbar = (Toolbar) v.findViewById(R.id.tool_bar);
        skip.setText("");
        next.setText("");
        title.setText("");
    }

    public boolean checkCreditCard() {

        boolean creditCardOk = false;
        if (credit_card_number.getText().toString().length() > 0) {
            if (credit_card_date.getText().toString().length() > 0) {
                if (credit_card_CVV.getText().toString().length() > 0) {
                    creditCardOk = true;
                }
            }
        }
        return creditCardOk;
    }

    public void updateBilling() {

        try {
            if (checkCreditCard()) {
                if (credit_card_number.getText().toString().length() > 4) {
                    SessionManager.CUSTOMER.setCardNumber(credit_card_number.getText().toString());
                }
                SessionManager.CUSTOMER.setExpMonth(credit_card_date.getText().toString().split("/")[0]);
                SessionManager.CUSTOMER.setExpYear("20" + credit_card_date.getText().toString().split("/")[1]);
                SessionManager.CUSTOMER.setCsc(credit_card_CVV.getText().toString());

                new BackgroundTask(getActivity(), SessionManager.CUSTOMER.updateBillling(getActivity()));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Constants.register = false;

        if (UtilityClass.isConnectingToInternet(getContext())) {
            getOrderType();
        } else {
            Intent intent = new Intent(getActivity(), Orders.class);
            startActivity(intent);
        }
    }


    private void getOrderType() {

        if (SessionManager.ORDER == null) SessionManager.ORDER = new Order();
        if (sessionManager.getUserAddress() != null) {
            if (sessionManager.getUserShortAddress() != null) {
                UtilityClass.getLocationFromAddress(sessionManager.getUserShortAddress(), getContext());
            } else {
                UtilityClass.getLocationFromAddress(sessionManager.getUserAddress(), getContext());
            }

            if (sessionManager.getUserGeoLocation() == null) {
                Intent toLocker = new Intent(getActivity(), Orders.class);
                startActivity(toLocker);

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
                ConfirmOrderTypeTask confirmOrderTypeTask = new ConfirmOrderTypeTask(getContext(), SessionManager.ORDER.confirmOrderType(), this, params, "getOrderType", "Register");
                confirmOrderTypeTask.ResponseTask();
            }

        } else {
            Intent toLocker = new Intent(getActivity(), Orders.class);
            startActivity(toLocker);
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


    @Override
    public void addressMatchCase(String value, LocationModel locationModel) {

        switch (value) {
            case "true":
                if (!locationModel.getLocationType().equalsIgnoreCase("null")) {
                    if (locationModel.getLocationType().equalsIgnoreCase("Lockers")) {
                        sessionManager.saveUserFlow("Register");
                        Intent newOrder = new Intent(getActivity(), NewOrder.class);
                        newOrder.putExtra("From", "Orders");
                        startActivity(newOrder);

                    } else if (locationModel.getLocationType().equalsIgnoreCase("Concierge")
                            || locationModel.getLocationType().equalsIgnoreCase("Offices")) {
                        sessionManager.saveAddressType("Concierge");
                        Intent toLocker = new Intent(getActivity(), Orders.class);
                        startActivity(toLocker);
                    } else if (locationModel.getLocationType().equalsIgnoreCase("Kiosk")) {
                        sessionManager.saveUserFlow("Register");
                        Intent newOrder = new Intent(getActivity(), NewOrder.class);
                        newOrder.putExtra("From", "Orders");
                        startActivity(newOrder);


                    } else {

                        Intent toLocker = new Intent(getActivity(), Orders.class);
                        startActivity(toLocker);
                    }
                } else {
                    Intent toLocker = new Intent(getActivity(), Orders.class);
                    startActivity(toLocker);
                }
                break;
            case "false":
                Intent toLocker = new Intent(getActivity(), Orders.class);
                startActivity(toLocker);
                break;
        }


    }
}
