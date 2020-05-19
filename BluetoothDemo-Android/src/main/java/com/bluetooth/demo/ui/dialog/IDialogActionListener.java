package com.bluetooth.demo.ui.dialog;

import java.util.List;

import com.bluetooth.demo.device.SettingItem;
import com.lifesense.ble.bean.kchiing.KReminder;
import com.lifesense.ble.bean.kchiing.KRepeatSetting;

import android.content.Intent;

public abstract class IDialogActionListener {

	public void onSingleChoiceItemValue(int index){};
	
	public void onTimeChoiceValue(int hour,int minute){};
	
	public void onTitleAndTimeChoiceValue(String title,int hour,int minute){};

	public void onMultiChoiceItemValue(List<Integer> items){};
	
	public void onIntentResults(Intent intent){};
	
	public void onSettingItems(List<SettingItem> items){};
	
	public void onBindingResults(){};
	
	public void onKchiingReminderCreate(SettingItem item,KReminder reminder){};
	
	public void onRepeatSettingCreate(SettingItem item,KRepeatSetting repeatSetting){}

}
