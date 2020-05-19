package com.bluetooth.demo.ui.adapter;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bluetooth.demo.R;

public class FileAdapter extends ArrayAdapter<File> {

    private List<File> mFileList;

    public FileAdapter(Context context, List<File> fileList) {
        super(context, R.layout.list_select_file);
        this.mFileList = fileList;
    }

    @Override
    public int getCount() {
        if (mFileList == null)
            return 0;
        return mFileList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (mFileList.size() > 0) {
            if (convertView == null) {
                holder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.list_select_file, null);
                holder.list_content_main = (TextView) convertView.findViewById(R.id.list_content_main);
                convertView.setTag(holder);
            } else
                holder = (ViewHolder) convertView.getTag();
            File file = mFileList.get(position);
            holder.list_content_main.setText(file.getName());
        }
        return convertView;
    }

    private class ViewHolder {
        TextView list_content_main;
    }

}
