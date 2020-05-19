package com.bluetooth.demo.ui.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.view.ContextThemeWrapper;
import android.widget.Toast;

public class DialogUtils {

	private static Handler mMainHandler;
	
	public static void initDialogHandler(Handler handler){
		mMainHandler=handler;
	}
	
	
	/**
	 * show toast message
	 * @param msg
	 */
	public static void showToastMessage(final Context context,final String msg){
		if(mMainHandler==null || context==null){
			return ;
		}
		mMainHandler.post(new Runnable() {
			
			@Override
			public void run() {
				Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
			}
		});
	}
	
	
	/**
	 * 显示提示对话框
	 * @param context
	 * @param title
	 * @param message
	 */
	public static void showPromptDialog(final Context context,final String title, final String message) 
	{
		if(mMainHandler==null || context==null){
			return ;
		}
		mMainHandler.post(new Runnable() {
			@Override
			public void run() {
				AlertDialog dialog=null;
				ContextThemeWrapper ctw = new ContextThemeWrapper(context, android.R.style.Theme_Holo_Light);
				 AlertDialog.Builder promptDialog= new AlertDialog.Builder(ctw)
				.setTitle(title)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss();
					}
				})
				.setMessage(message);
				dialog=promptDialog.create();
				dialog.show();				
			}
		});
		
	}
}
