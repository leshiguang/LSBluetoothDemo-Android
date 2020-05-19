package com.bluetooth.demo.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.bluetooth.demo.R;

public class DFUDialog extends Dialog {

    private boolean enableBackPressed = true;

    protected DFUDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public DFUDialog(Context context) {
        super(context);
    }

    public DFUDialog(Context context, int theme) {
        super(context, theme);
    }

    public DFUDialog setEnableBackPressed(boolean enable) {
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
        private String mContent;
        private String mPositiveButtonText;
        private String mNegativeButtonText;
        private OnClickListener positiveButtonClickListener;
        private OnClickListener negativeButtonClickListener;

        public Builder(Context context) {
            this.mContext = context;
        }

        public Builder setContent(String content) {
            this.mContent = content;
            return this;
        }

        public Builder setContent(int rId) {
            if (rId == 0)
                return this;
            this.mContent = mContext.getString(rId);
            return this;
        }

        public Builder setPositiveButton(int positiveButtonText,
                                         OnClickListener listener) {
            if (positiveButtonText == 0)
                return this;
            this.mPositiveButtonText = mContext.getString(positiveButtonText);
            this.positiveButtonClickListener = listener;
            return this;
        }

        public Builder setPositiveButton(String positiveButtonText,
                                         OnClickListener listener) {
            this.mPositiveButtonText = positiveButtonText;
            this.positiveButtonClickListener = listener;
            return this;
        }

        public Builder setNegativeButton(int negativeButtonText,
                                         OnClickListener listener) {
            if (negativeButtonText == 0)
                return this;
            this.mNegativeButtonText = mContext.getString(negativeButtonText);
            this.negativeButtonClickListener = listener;
            return this;
        }

        public Builder setNegativeButton(String negativeButtonText,
                                         OnClickListener listener) {
            this.mNegativeButtonText = negativeButtonText;
            this.negativeButtonClickListener = listener;
            return this;
        }

        public DFUDialog create() {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // instantiate the dialog with the custom Theme
            final DFUDialog dialog = new DFUDialog(mContext,
                    R.style.CustomDialog);
            View layout = inflater.inflate(R.layout.dialog_dfu, null);
            // set the content message
            if (mContent != null)
                ((TextView) layout.findViewById(R.id.dialog_content)).setText(mContent);
            else
                layout.findViewById(R.id.dialog_content).setVisibility(View.GONE);
            // set the confirm button
            if (mPositiveButtonText != null) {
                ((TextView) layout.findViewById(R.id.dialog_btn_positive)).setText(mPositiveButtonText);
                if (positiveButtonClickListener != null)
                    layout.findViewById(R.id.dialog_btn_positive).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            positiveButtonClickListener.onClick(dialog,
                                    DialogInterface.BUTTON_POSITIVE);
                        }
                    });
            } else {
                // if no confirm button just set the visibility to GONE
                layout.findViewById(R.id.dialog_btn_positive).setVisibility(View.GONE);
            }
            // set the cancel button
            if (mNegativeButtonText != null) {
                ((TextView) layout.findViewById(R.id.dialog_btn_negative)).setText(mNegativeButtonText);
                if (negativeButtonClickListener != null)
                    layout.findViewById(R.id.dialog_btn_negative).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            negativeButtonClickListener.onClick(dialog,
                                    DialogInterface.BUTTON_NEGATIVE);
                        }
                    });
            } else
                // if no confirm button just set the visibility to GONE
                layout.findViewById(R.id.dialog_btn_negative).setVisibility(View.GONE);
            dialog.setContentView(layout);
            dialog.setCanceledOnTouchOutside(false);
            return dialog;
        }
    }

}
