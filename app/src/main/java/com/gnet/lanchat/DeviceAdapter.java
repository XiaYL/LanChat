package com.gnet.lanchat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gnet.lan_manager.search.LanDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description: java类作用描述
 * @Author: yanlei.xia
 * @CreateDate: 2020/12/4 11:20
 */
public class DeviceAdapter extends BaseAdapter {

    private List<LanDevice> deviceList = new ArrayList<>();

    public void addDevice(LanDevice device) {
        if (!deviceList.contains(device)) {
            deviceList.add(device);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return deviceList.size();
    }

    @Override
    public LanDevice getItem(int position) {
        return deviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
            holder = new Holder();
            convertView = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            holder.nameTxt = convertView.findViewById(android.R.id.text1);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        holder.nameTxt.setText(getItem(position).toString());
        return convertView;
    }

    class Holder {
        TextView nameTxt;
    }
}
