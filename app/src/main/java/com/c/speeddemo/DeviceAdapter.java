package com.c.speeddemo;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by kang on 6/21/2016.
 */
public class DeviceAdapter extends BaseAdapter {
    private List<BluetoothDevice> mDate;
    private Context mContext;

    public DeviceAdapter(List<BluetoothDevice> data, Context ctx){
        mDate=data;
        mContext=ctx.getApplicationContext();
    }
    @Override
    public int getCount() {
        return mDate.size();
    }

    @Override
    public Object getItem(int position) {
        return mDate.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView =convertView;
        if(itemView==null){
            itemView= LayoutInflater.from(mContext).inflate(
                    android.R.layout.simple_list_item_2,parent,false);
        }
        TextView line1= (TextView) itemView.findViewById(android.R.id.text1);
        TextView line2= (TextView) itemView.findViewById(android.R.id.text2);
        BluetoothDevice device = (BluetoothDevice) getItem(position);
        line1.setText(device.getName());
        line2.setText(device.getAddress());
        return itemView;
    }

    public void refresh(List<BluetoothDevice> data){
        mDate=data;
        notifyDataSetChanged();
    }
}
