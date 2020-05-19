package com.bluetooth.demo.ui.dialog;

import java.io.File;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.bluetooth.demo.R;
import com.bluetooth.demo.ui.adapter.FileAdapter;

public class SelectFileDialog extends Dialog {

    private boolean enableBackPressed = true;

    protected SelectFileDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public SelectFileDialog(Context context) {
        super(context);
    }

    public SelectFileDialog(Context context, int theme) {
        super(context, theme);
    }

    public SelectFileDialog setEnableBackPressed(boolean enable) {
        enableBackPressed = enable;
        return this;
    }

    @Override
    public void onBackPressed() {
        if (enableBackPressed)
            super.onBackPressed();
    }

    public static class Builder {
        private Context mContext;
        private List<File> mFileList;
        private AdapterView.OnItemClickListener mOnItemClickListener;

        public Builder(Context context) {
            this.mContext = context;
        }

        public Builder setFileList(List<File> fileList) {
            mFileList = fileList;
            return this;
        }

        public Builder setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
            this.mOnItemClickListener = onItemClickListener;
            return this;
        }

        public SelectFileDialog create() {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // instantiate the dialog with the custom Theme
            final SelectFileDialog dialog = new SelectFileDialog(mContext,
                    R.style.CustomDialog);
            View layout = inflater.inflate(R.layout.dialog_select_file, null);
            ListView listView = (ListView) layout.findViewById(R.id.list_view);
            FileAdapter adapter = new FileAdapter(mContext, mFileList);
            listView.setAdapter(adapter);
            if (mOnItemClickListener != null)
                listView.setOnItemClickListener(mOnItemClickListener);
            dialog.setContentView(layout);
            return dialog;
        }
    }

}
