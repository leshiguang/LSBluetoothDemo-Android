package com.bluetooth.demo.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.bluetooth.demo.R;

public class InputPasswordDialog extends Dialog {
    public InputPasswordDialog(@NonNull Context context) {
        super(context);
    }

    public InputPasswordDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected InputPasswordDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static class Builder{
        private Context mContext;
        private String mPositiveButtonText;
        private String mNegativeButtonText;
        private OnPositiveButtonClick onPositiveButtonClick;
        private OnClickListener negativeButtonClickListener;

        public Builder(Context context) {
            this.mContext = context;
        }

        public InputPasswordDialog.Builder setmPositiveButtonText(String mPositiveButtonText) {
            this.mPositiveButtonText = mPositiveButtonText;
            return this;
        }

        public InputPasswordDialog.Builder setmNegativeButtonText(String mNegativeButtonText) {
            this.mNegativeButtonText = mNegativeButtonText;
            return this;
        }

        public InputPasswordDialog.Builder setOnPositiveButtonClick(OnPositiveButtonClick onPositiveButtonClick) {
            this.onPositiveButtonClick = onPositiveButtonClick;
            return this;
        }

        public InputPasswordDialog.Builder setNegativeButtonClickListener(OnClickListener negativeButtonClickListener) {
            this.negativeButtonClickListener = negativeButtonClickListener;
            return this;
        }

        public InputPasswordDialog create() {
            final InputPasswordDialog dialog = new InputPasswordDialog(mContext, R.style.CustomDialog);
            View view = LayoutInflater.from(mContext).inflate(R.layout.input_password_dialog,null);
            final EditText input = view.findViewById(R.id.et_wifi_password);
            TextView cancel = view.findViewById(R.id.tv_cancel);
            TextView ok = view.findViewById(R.id.tv_ok);
            if (!TextUtils.isEmpty(mPositiveButtonText)) {
                ok.setText(mPositiveButtonText);
            }
            if (!TextUtils.isEmpty(mNegativeButtonText)) {
                cancel.setText(mNegativeButtonText);
            }
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onPositiveButtonClick == null) {
                        dialog.dismiss();
                    }
                    onPositiveButtonClick.onCommit(dialog,input.getText().toString());
                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (negativeButtonClickListener == null) {
                        dialog.dismiss();
                    }
                    negativeButtonClickListener.onClick(dialog,BUTTON_NEGATIVE);
                }
            });

            dialog.setContentView(view);
            dialog.setCanceledOnTouchOutside(false);
            return dialog;
        }
    }

    public interface OnPositiveButtonClick{
        void onCommit(Dialog dialog,String str);
    }
}
