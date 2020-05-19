/**
 * 
 */
package com.bluetooth.demo.ui.dialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.bluetooth.demo.R;
import com.bluetooth.demo.device.SettingItem;
import com.bluetooth.demo.device.SettingOptions;
import com.bluetooth.demo.device.kchiing.KchiingSettingProfiles;
import com.bluetooth.demo.ui.adapter.SettingItemAdapter;
import com.lifesense.ble.LsBleManager;
import com.lifesense.ble.OnSettingListener;
import com.lifesense.ble.bean.kchiing.KAppointmentReminder;
import com.lifesense.ble.bean.kchiing.KReminder;
import com.lifesense.ble.bean.kchiing.KRepeatSetting;

/**
 * @author sky
 *
 */
@SuppressLint({ "NewApi", "SimpleDateFormat" })
public class SettingDialogFragment  extends DialogFragment{

	private OnDialogClickListener mDialogClickListener;
	private SettingItemAdapter mAdapter;
	private ListView mSettingView;
	private Button mAddClockBtn;
	private List<SettingItem> mSettingItems;
	private String mDialogTitle;
	private KReminder mKReminder;
	private int reminderType;
	private KRepeatSetting mRepeatSetting;
	private SimpleDateFormat timeFormat=new SimpleDateFormat("HH:mm");
	private String mDeviceMac;
	private OnSettingListener mSettingListener;

	private IDialogActionListener mDialogActionListener=new IDialogActionListener() 
	{
		@Override
		public void onKchiingReminderCreate(SettingItem item,KReminder reminder) 
		{
			if(reminder.getRemindTime() > 0){
				String time=timeFormat.format(new Date(reminder.getRemindTime()));
				mKReminder=reminder;
				//enable repeat setting click
				mSettingItems.get(0).setTextViewValue(time);
				mSettingItems.get(1).setEnable(true);
				mAdapter.notifyDataSetChanged();
			}
		}

		@Override
		public void onRepeatSettingCreate(SettingItem item,KRepeatSetting repeatSetting) {
			mRepeatSetting=repeatSetting;
		}
		
	};
	
	public SettingDialogFragment() {

	}

	public SettingDialogFragment(Class<? extends KReminder> cls,String deviceMac,OnSettingListener listener)
	{
		if(cls!=null && KAppointmentReminder.class.isAssignableFrom(cls)){
			this.mDialogTitle="Appointment Reminder";
			this.reminderType=0;
		}
		else{
			this.mDialogTitle="Simple Reminder";
			this.reminderType=1;
		}
		this.mDeviceMac=deviceMac;
		this.mSettingListener=listener;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) 
	{
		ContextThemeWrapper ctw = new ContextThemeWrapper(this.getActivity(), android.R.style.Theme_Holo_Light);
		final AlertDialog.Builder builder = new AlertDialog.Builder(ctw);
		final View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_clocks, null);		
		mSettingView=(ListView)dialogView.findViewById(R.id.clock_list_view);
		builder.setTitle(this.mDialogTitle);
		//init clocks list view
		initSettingItemsView();
		//create dialog
		final AlertDialog dialog = builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				if(mKReminder!=null){
					mKReminder.setRepeatSetting(mRepeatSetting);
				}
				Log.e("LS-BLE", "reminder info >> "+mKReminder.toString());
				LsBleManager.getInstance().pushDeviceMessage(mDeviceMac, mKReminder, mSettingListener);
			}
		})
		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		})
		.setView(dialogView).create();
		return dialog;
	}

	private void initSettingItemsView(){
		mSettingItems=new ArrayList<SettingItem>();
		//reminder time
		SettingItem timeItem=new SettingItem();
		timeItem.setOptions(SettingOptions.TimePicker);
		timeItem.setTitle("Reminder Time");
		timeItem.setIndex(1);
		timeItem.setTextViewValue("---");
		timeItem.setEnable(true);
		//reminder repeating setting
		SettingItem repeatItem=new SettingItem(SettingOptions.Text);
		repeatItem.setTitle("Repeat Mode");
		repeatItem.setIndex(2);
		repeatItem.setTextViewValue("---");
		//setting items
		mSettingItems.add(timeItem);
		mSettingItems.add(repeatItem);
		//init listview adapter
		mAdapter=new SettingItemAdapter(getContext(), (ArrayList<SettingItem>) mSettingItems);
		mSettingView.setAdapter(mAdapter);
		mSettingView.setOnItemClickListener(new AdapterView.OnItemClickListener() 
		{
			@Override
			public void onItemClick(final AdapterView<?> parent, final View view,final int position, long id) 
			{
				SettingItem item = (SettingItem) parent.getAdapter().getItem(position);
				if(item.getIndex() == 1){
					//reminder time
					if(reminderType == 0){
						//appointment reminder
						KchiingSettingProfiles.createAppointmentReminder(item,mDialogActionListener);
					}
					else{
						//simple reminder
						KchiingSettingProfiles.createSimpleReminder(item,mDialogActionListener);
					}
				}
				else if(item.getIndex() ==2 && mKReminder!=null) {
					//reminder repeat setting
					KchiingSettingProfiles.createRepeatSetting(item,mKReminder.getRemindTime(), mDialogActionListener);
				}
				
				
//				DeviceSettiingProfiles.createAlarmClock(new IDialogActionListener() {
//					@Override
//					public void onAlarmClockCreate(PedometerAlarmClock clock) 
//					{
//						if(clock!=null && !TextUtils.isEmpty(clock.getTime())){
//							mClocks.add(clock);
//						}
//						final SettingItem item = (SettingItem) parent.getAdapter().getItem(position);
//						item.setTextViewValue(clock.getTime());
//						mAdapter.notifyDataSetChanged();
//						Log.e("LS-BLE", "alarm clock >> "+clock.toString());
//					}
//				});
			}
		});
//		mAddClockBtn.setOnClickListener(new View.OnClickListener() {
//
//			@SuppressWarnings("unchecked")
//			@Override
//			public void onClick(View v) {
//				//
//				mClockIndex++;
//				mSettingItems=new ArrayList<SettingItem>();
//				SettingItem timeItem=new SettingItem();
//				timeItem.setOptions(SettingOptions.TimePicker);
//				timeItem.setTitle("Clock "+mClockIndex);
//				timeItem.setIndex(mClockIndex);
//				timeItem.setTextViewValue("---");
//				//alarm clock 
//				mAdapter.add(timeItem);
//				mAdapter.notifyDataSetChanged();
//			}
//		});
	}

}
