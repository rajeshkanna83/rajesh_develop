package com.usepressbox.pressbox.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.usepressbox.pressbox.R;
import com.usepressbox.pressbox.models.GetOrdersModel;
import com.usepressbox.pressbox.utils.SessionManager;
import com.usepressbox.pressbox.utils.UtilityClass;

import java.util.ArrayList;

/* Created By Prasanth.S on 03/16/2018*/

public class OrdersAdapterRefractor extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    private ArrayList<GetOrdersModel> orderItemList;

    public OrdersAdapterRefractor(Context context) {
        this.context = context;
    }

    public void setData(ArrayList<GetOrdersModel> data) {
        if (orderItemList != data) {
            orderItemList = data;
            notifyDataSetChanged();
        }
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tw_order_row_lockerID;
        TextView tw_order_row_date;
        TextView tw_order_row_month;
        TextView tw_order_row_info;
        TextView tw_order_row_address;
        TextView tw_unlock_code;

        public ItemViewHolder(View view) {
            super(view);

            tw_order_row_address = (TextView) view.findViewById(R.id.tw_order_row_address);
            tw_order_row_info = (TextView) view.findViewById(R.id.tw_order_row_info);
            tw_order_row_month = (TextView) view.findViewById(R.id.tw_order_row_month);
            tw_order_row_date = (TextView) view.findViewById(R.id.tw_order_row_date);
            tw_order_row_lockerID = (TextView) view.findViewById(R.id.tw_order_row_locker_id);
            tw_unlock_code = (TextView) view.findViewById(R.id.tw_unlock_code);
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.order_row, parent, false);
        ItemViewHolder holder = new ItemViewHolder(itemView);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewholder, final int position) {

        final ItemViewHolder holder = (ItemViewHolder) viewholder;
        GetOrdersModel getOrdersModel = orderItemList.get(position);

        try {

            String date = getOrdersModel.getDate();
            holder.tw_order_row_month.setText(UtilityClass.convertDate(date.split("-")[1]));
            holder.tw_order_row_date.setText(date.split("-")[2].substring(0, 2));

            holder.tw_order_row_address.setText(getOrdersModel.getAddress());
            holder.tw_order_row_lockerID.setText(getOrdersModel.getLockerId());
            holder.tw_order_row_info.setText(getOrdersModel.getStatus());


            String unlock_code = SessionManager.CUSTOMER.getPhone().substring(SessionManager.CUSTOMER.getPhone().length() - 4);
            holder.tw_unlock_code.setText(unlock_code);
        } catch (Exception e) {
            e.printStackTrace();
        }



        holder.itemView.setTag(position);
    }


    @Override
    public int getItemCount() {
        return (null != orderItemList ? orderItemList.size() : 0);
    }


}
