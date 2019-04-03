package com.usepressbox.pressbox.interfaces;

import com.usepressbox.pressbox.models.OrderTypeModel;

import java.util.ArrayList;

/**
 * Created by Prasanth.S on 01/02/2018.

 */

public interface IOrderPreferenceListener {
    void orderPreferenceStatus(String status);
    void updateDetergent(String status);

    void getDetergentId(String tideID,String tideFreeID,String detergentName1,String detergentname2);
    void getStarchID(String starchID,String starchID1,String starchID2 );

}
