package com.bluetooth.demo.device.kchiing;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;

import com.bluetooth.demo.R;
import com.bluetooth.demo.device.DeviceSettiingProfiles;
import com.bluetooth.demo.device.SettingItem;
import com.bluetooth.demo.device.SettingOptions;
import com.bluetooth.demo.ui.dialog.IDialogActionListener;
import com.bluetooth.demo.utils.DeviceDataUtils;
import com.lifesense.ble.LsBleManager;
import com.lifesense.ble.OnSettingListener;
import com.lifesense.ble.bean.bmp.BMPCommand;
import com.lifesense.ble.bean.bmp.BMPCommandPacket;
import com.lifesense.ble.bean.bmp.BMPDisplayState;
import com.lifesense.ble.bean.bmp.MeasurementTimeInterval;

public class BPMSettingProfiles extends DeviceSettiingProfiles {

	
	private static final String[] DELETE_BPM_DATA=new String[]{
		"All",
		"24h Measurements",
		"Single Measurements"
	};
	
	private static final String[] GET_BPM_DATA=new String[]{
		"24h Measurements",
		"Single Measurements"
	};
	
	
	
	public static void deleteBpmMeasurementData(final String deviceMac,final OnSettingListener listener){
		String title=getResourceString(R.string.title_delete_bpm_data);
		final String labelType=getResourceString(R.string.label_data_type);

		//init setting items
		SettingItem statusItem=new SettingItem(SettingOptions.SingleChoice);
		statusItem.setTitle(labelType);
		statusItem.setChoiceItems(Arrays.asList(DELETE_BPM_DATA));

		//add item to list
		List<SettingItem> menuItems=new ArrayList<SettingItem>();
		menuItems.add(statusItem);

		//show setting dialog
		showSettingDialog(title, menuItems, new IDialogActionListener() {
			@Override
			public void onSettingItems(List<SettingItem> items) 
			{
				if(!checkSettingItems(items, listener)){
					return ;
				}
				int index=0;
				com.lifesense.ble.bean.bmp.BMPCommand command=BMPCommand.DeleteAllMeasurements;
				for(SettingItem item:items){
					if(item.getTitle().equalsIgnoreCase(labelType)
							&& !TextUtils.isEmpty(item.getTextViewValue()) ){
						if(DELETE_BPM_DATA[1].equalsIgnoreCase(item.getTextViewValue())){
							command=BMPCommand.Delete24hMeasurements;
						}
						else if(DELETE_BPM_DATA[2].equalsIgnoreCase(item.getTextViewValue())){
							command=BMPCommand.DeleteSingleMeasurements;
						}
					}
				}
				//to mode
				BMPCommandPacket cmdPacket=new BMPCommandPacket(command);
				cmdPacket.setLatestNum(index);
				logMessage("Delete BPM Data setting >> "+cmdPacket.toString());
				//calling interface
				LsBleManager.getInstance().pushDeviceMessage(deviceMac, cmdPacket, listener);
			}
		});
	}


	/**
	 * 更新手环的勿扰模式设置
	 * @param deviceMac
	 * @param listener
	 */
	public static void getBpmMeasurementData(final String deviceMac,final OnSettingListener listener){
		String title=getResourceString(R.string.title_get_bpm_data);
		final String labelType=getResourceString(R.string.label_data_type);
		final String labelIndex=getResourceString(R.string.label_data_index);

		//init setting items
		SettingItem statusItem=new SettingItem(SettingOptions.SingleChoice);
		statusItem.setTitle(labelType);
		statusItem.setChoiceItems(Arrays.asList(GET_BPM_DATA));

		SettingItem indexItem=new SettingItem(SettingOptions.Text);
		indexItem.setTitle(labelIndex);
		indexItem.setInputType(InputType.TYPE_CLASS_NUMBER);
		indexItem.setUnit("");

		//add item to list
		List<SettingItem> menuItems=new ArrayList<SettingItem>();
		menuItems.add(statusItem);
		menuItems.add(indexItem);

		//show setting dialog
		showSettingDialog(title, menuItems, new IDialogActionListener() {
			@Override
			public void onSettingItems(List<SettingItem> items) 
			{
				if(!checkSettingItems(items, listener)){
					return ;
				}
				int index=0;
				com.lifesense.ble.bean.bmp.BMPCommand command=BMPCommand.GetLatest24hMeasurement;
				for(SettingItem item:items){
					if(item.getTitle().equalsIgnoreCase(labelIndex) 
							&& !TextUtils.isEmpty(item.getTextViewValue())){
						index=(int)Long.parseLong(item.getTextViewValue());
					}
					if(item.getTitle().equalsIgnoreCase(labelType)
							&& !TextUtils.isEmpty(item.getTextViewValue()) ){
						if(GET_BPM_DATA[1].equalsIgnoreCase(item.getTextViewValue())){
							command=BMPCommand.GetLatestSingleMeasurement;
						}
					}
				}
				//to mode
				BMPCommandPacket cmdPacket=new BMPCommandPacket(command);
				cmdPacket.setLatestNum(index);
				logMessage("get Latest Data setting >> "+cmdPacket.toString());
				//calling interface
				LsBleManager.getInstance().pushDeviceMessage(deviceMac, cmdPacket, listener);
			}
		});
	}



	public static void setMeasurementTimeInterval(final String deviceMac,final OnSettingListener listener) {
		String title=getResourceString(R.string.title_set_measurement_time_interval);
		final String napDuration=getResourceString(R.string.label_nap_duration);
		final String napStartTime=getResourceString(R.string.label_nap_start_time);
		final String nightDuration=getResourceString(R.string.label_night_duration);
		final String nightStartTime=getResourceString(R.string.label_night_start_time);
		final String nightIntervalMin=getResourceString(R.string.label_night_interval_min);
		final String dayIntervalMin=getResourceString(R.string.label_day_interval_min);
		final String displayState=getResourceString(R.string.label_display_state);
		final String sendData=getResourceString(R.string.label_auto_send_data);

		//init setting items
		SettingItem napDurationItem=new SettingItem(SettingOptions.Text);
		napDurationItem.setTitle(napDuration);
		napDurationItem.setInputType(InputType.TYPE_CLASS_NUMBER);
		napDurationItem.setUnit("");

		SettingItem nightDurationItem=new SettingItem(SettingOptions.Text);
		nightDurationItem.setTitle(nightDuration);
		nightDurationItem.setInputType(InputType.TYPE_CLASS_NUMBER);
		nightDurationItem.setUnit("");

		SettingItem nightIntervalMinItem=new SettingItem(SettingOptions.Text);
		nightIntervalMinItem.setTitle(nightIntervalMin);
		nightIntervalMinItem.setInputType(InputType.TYPE_CLASS_NUMBER);
		nightIntervalMinItem.setUnit("");

		SettingItem dayIntervalMinItem=new SettingItem(SettingOptions.Text);
		dayIntervalMinItem.setTitle(dayIntervalMin);
		dayIntervalMinItem.setInputType(InputType.TYPE_CLASS_NUMBER);
		dayIntervalMinItem.setUnit("");

		SettingItem napStartTimeItem=new SettingItem(SettingOptions.TimePicker);
		napStartTimeItem.setTitle(napStartTime);
		napStartTimeItem.setUnit("");

		SettingItem nightStartTimeItem=new SettingItem(SettingOptions.TimePicker);
		nightStartTimeItem.setTitle(nightStartTime);
		nightStartTimeItem.setUnit("");


		SettingItem sendDataItem=new SettingItem(SettingOptions.SingleChoice);
		sendDataItem.setTitle(sendData);
		sendDataItem.setUnit("");
		sendDataItem.setChoiceItems(Arrays.asList(SWITCH_STATUS));

		String[] displays=new String[BMPDisplayState.values().length];
		int index=0;
		for(BMPDisplayState state:BMPDisplayState.values()){
			displays[index]=state.toString();
			index++;
		}

		SettingItem displaysItem=new SettingItem(SettingOptions.SingleChoice);
		displaysItem.setTitle(displayState);
		displaysItem.setUnit("");
		displaysItem.setChoiceItems(Arrays.asList(displays));

		//add item to list
		List<SettingItem> menuItems=new ArrayList<SettingItem>();
		menuItems.add(napDurationItem);
		menuItems.add(napStartTimeItem);
		menuItems.add(nightDurationItem);
		menuItems.add(nightStartTimeItem);
		menuItems.add(nightIntervalMinItem);
		menuItems.add(dayIntervalMinItem);
		menuItems.add(displaysItem);
		menuItems.add(sendDataItem);
		//show setting dialog
		showSettingDialog(title, menuItems, new IDialogActionListener() {
			@Override
			public void onSettingItems(List<SettingItem> items) 
			{
				if(!checkSettingItems(items, listener)){
					return ;
				}
				MeasurementTimeInterval timeInterval=new MeasurementTimeInterval();
				timeInterval.setAutoSendData(false);
				timeInterval.setDisplayState(BMPDisplayState.TurnOff);
				for(SettingItem item:items){
					if(item.getTitle().equalsIgnoreCase(napDuration) 
							&& !TextUtils.isEmpty(item.getTextViewValue())){
						int index=(int)Long.parseLong(item.getTextViewValue());
						timeInterval.setNapDuration(index);
					}
					if(item.getTitle().equalsIgnoreCase(nightDuration) 
							&& !TextUtils.isEmpty(item.getTextViewValue())){
						int index=(int)Long.parseLong(item.getTextViewValue());
						timeInterval.setNightDuration(index);
					}
					if(item.getTitle().equalsIgnoreCase(nightIntervalMin) 
							&& !TextUtils.isEmpty(item.getTextViewValue())){
						int index=(int)Long.parseLong(item.getTextViewValue());
						timeInterval.setNightIntervalMin(index);
					}
					if(item.getTitle().equalsIgnoreCase(dayIntervalMin) 
							&& !TextUtils.isEmpty(item.getTextViewValue())){
						int index=(int)Long.parseLong(item.getTextViewValue());
						timeInterval.setDayIntervalMin(index);
					}
					if(item.getTitle().equalsIgnoreCase(napStartTime) 
							&& !TextUtils.isEmpty(item.getTime())){
						String time=item.getTime();
						int hour=DeviceDataUtils.getHourFromTime(time);
						int minute=DeviceDataUtils.getMinuteFromTime(time);
						timeInterval.setNapStartHour(hour);
						timeInterval.setNapStartMin(minute);
						Log.e("LS-BLE", "nap start time >> "+time+"; hour >> "+hour+"; minute >> "+minute);
					}
					if(item.getTitle().equalsIgnoreCase(nightStartTime) 
							&& !TextUtils.isEmpty(item.getTime())){
						String time=item.getTime();
						int hour=DeviceDataUtils.getHourFromTime(time);
						int minute=DeviceDataUtils.getMinuteFromTime(time);
						timeInterval.setNightStartHour(hour);
						timeInterval.setNightStartMinute(minute);
						Log.e("LS-BLE", "night start time >> "+time+"; hour >> "+hour+"; minute >> "+minute);

					}
					if(item.getTitle().equalsIgnoreCase(displayState)
							&& !TextUtils.isEmpty(item.getTextViewValue()) ){
						BMPDisplayState state=BMPDisplayState.valueOf(item.getTextViewValue());
						timeInterval.setDisplayState(state);
					}
					if(item.getTitle().equalsIgnoreCase(sendData) 
							&& !TextUtils.isEmpty(item.getTextViewValue())){
						if(SWITCH_STATUS[0].equalsIgnoreCase(item.getTextViewValue())){
							timeInterval.setAutoSendData(true);
						}
					}
				}

				logMessage("BPM Measurement Time Interval setting >> "+timeInterval.toString());
				//calling interface
				LsBleManager.getInstance().pushDeviceMessage(deviceMac, timeInterval, listener);
			}
		});		
	}
}
