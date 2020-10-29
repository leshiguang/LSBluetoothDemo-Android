package com.bluetooth.demo.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bluetooth.demo.R;
import com.lifesense.ble.bean.WifiInfo;

import java.util.List;

public class ScanWifiResultsAdapter extends ArrayAdapter<WifiInfo> {

    private Context context;
    private List<WifiInfo> wifiInfos;

    public ScanWifiResultsAdapter(Context context,List<WifiInfo> wifiInfos) {
        super(context, R.layout.scan_wifi_list_item,wifiInfos);
        this.context = context;
        this.wifiInfos = wifiInfos;
    }

    public void setWifiInfos(List<WifiInfo> wifiInfos) {
        this.wifiInfos = wifiInfos;
    }

    @Override
    public int getCount() {
        return wifiInfos == null ? 0 : wifiInfos.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        WifiInfo wifiInfo = wifiInfos.get(position);
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.scan_wifi_list_item,parent,false);
            viewHolder = new ViewHolder();
            viewHolder.wifiName = view.findViewById(R.id.tv_wifi_name);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.wifiName.setText(wifiInfo.getSsid());
        return view;
    }

    class ViewHolder{
        public TextView wifiName;
    }
}
