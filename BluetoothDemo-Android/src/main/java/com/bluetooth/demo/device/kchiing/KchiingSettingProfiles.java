package com.bluetooth.demo.device.kchiing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale.LanguageRange;

import android.text.InputType;
import android.text.TextUtils;

import com.bluetooth.demo.R;
import com.bluetooth.demo.device.DeviceSettiingProfiles;
import com.bluetooth.demo.device.SettingItem;
import com.bluetooth.demo.device.SettingOptions;
import com.bluetooth.demo.ui.dialog.IDialogActionListener;
import com.bluetooth.demo.utils.DeviceDataUtils;
import com.lifesense.ble.LsBleManager;
import com.lifesense.ble.OnSettingListener;
import com.lifesense.ble.bean.PedometerEventReminder;
import com.lifesense.ble.bean.constant.VibrationMode;
import com.lifesense.ble.bean.kchiing.KAppointmentReminder;
import com.lifesense.ble.bean.kchiing.KMessageReminder;
import com.lifesense.ble.bean.kchiing.KRepeatSetting;
import com.lifesense.ble.bean.kchiing.KRepeatType;
import com.lifesense.ble.bean.kchiing.KSimpleReminder;
import com.lifesense.ble.bean.kchiing.KWakeupReminder;

public class KchiingSettingProfiles extends DeviceSettiingProfiles {

	public static int reminderIndex=0;

	private static final String[] REPEAT_MODES=new String[]{
		"Once",
		"1x",
		"2x",
		//		"3x",
		//		"4x",
		//		"Every Hour",
		//		"Every Day",
		//		"Every Week",
		//		"Every Month",
		"Every 3 minutes",
		//		"Every 10 minutes",
		//		"Every 1 hrs",
	};

	private static final String[] STATUS_OF_JOIN_AGENDA=new String[]{
		"YES",
		"NO"
	};

	/**
	 * Add Appointment Reminder
	 * @param deviceMac
	 * @param listener
	 */
	public static void addAppointmentReminder(final String deviceMac,final OnSettingListener listener)
	{
		String title=getResourceString(R.string.title_reminder_appointment);
		final String labelStatus=getResourceString(R.string.label_switch);
		final String labelTitle=getResourceString(R.string.label_reminder_title);
		final String labelDescription=getResourceString(R.string.label_reminder_description);
		final String labelAppointmentTime=getResourceString(R.string.label_appointment_time);
		final String labelReminderTime=getResourceString(R.string.label_reminder_time);
		final String labelLocation=getResourceString(R.string.label_location);
		final String labelRepeatMode=getResourceString(R.string.label_repeat_mode);
		final String labelVibrationLength=getResourceString(R.string.label_vibration_length);
		final String labelJoinAgenda=getResourceString(R.string.label_join_agenda);
		final String labelTimeperiodStart=getResourceString(R.string.label_time_period_start);
		final String labelTimeperiodEnd=getResourceString(R.string.label_time_period_end);
		final String labelEndsTime=getResourceString(R.string.label_reminder_ends_time);

		//init setting items
		SettingItem enableItem=new SettingItem(SettingOptions.SingleChoice);
		enableItem.setTitle(getResourceString(R.string.label_switch));
		enableItem.setChoiceItems(Arrays.asList(SWITCH_STATUS));
		//title
		SettingItem titleItem=new SettingItem(SettingOptions.Text);
		titleItem.setTitle(labelTitle);
		titleItem.setInputType(InputType.TYPE_CLASS_TEXT);
		//description 
		SettingItem descriptionItem=new SettingItem(SettingOptions.Text);
		descriptionItem.setTitle(labelDescription);
		descriptionItem.setInputType(InputType.TYPE_CLASS_TEXT);
		//appointment time
		SettingItem appointmentTimeItem=new SettingItem();
		appointmentTimeItem.setOptions(SettingOptions.TimePicker);
		appointmentTimeItem.setTitle(labelAppointmentTime);
		//reminder time
		SettingItem reminderTimeItem=new SettingItem();
		reminderTimeItem.setOptions(SettingOptions.TimePicker);
		reminderTimeItem.setTitle(labelReminderTime);
		//loaction
		SettingItem locationItem=new SettingItem(SettingOptions.Text);
		locationItem.setTitle(labelLocation);
		locationItem.setInputType(InputType.TYPE_CLASS_TEXT);
		//repeat mode
		SettingItem repeatModeItem=new SettingItem(SettingOptions.SingleChoice);
		repeatModeItem.setTitle(labelRepeatMode);
		repeatModeItem.setChoiceItems(Arrays.asList(REPEAT_MODES));
		//vibration length
		SettingItem vibrationlengthItem=new SettingItem();
		vibrationlengthItem.setOptions(SettingOptions.NumberPicker);
		vibrationlengthItem.setTitle(labelVibrationLength);
		vibrationlengthItem.setMinValue(1);
		vibrationlengthItem.setMaxValue(60);
		//join agenda
		SettingItem joinAgendaItem=new SettingItem(SettingOptions.SingleChoice);
		joinAgendaItem.setTitle(labelJoinAgenda);
		joinAgendaItem.setChoiceItems(Arrays.asList(STATUS_OF_JOIN_AGENDA));
		//between hours-start time 
		SettingItem startTimeItem=new SettingItem(SettingOptions.TimePicker);
		startTimeItem.setTitle(labelTimeperiodStart);
		//between hours-end time 
		SettingItem endTimeItem=new SettingItem(SettingOptions.TimePicker);
		endTimeItem.setTitle(labelTimeperiodEnd);
		//ends time 
		SettingItem endsDateItem=new SettingItem(SettingOptions.TimePicker);
		endsDateItem.setTitle(labelEndsTime);

		//add item to list
		List<SettingItem> items=new ArrayList<SettingItem>();
		items.add(enableItem);
		items.add(titleItem);
		items.add(descriptionItem);
		items.add(appointmentTimeItem);
		items.add(reminderTimeItem);
		items.add(locationItem);
		items.add(vibrationlengthItem);
		items.add(repeatModeItem);
		items.add(joinAgendaItem);
		items.add(startTimeItem);
		items.add(endTimeItem);
		items.add(endsDateItem);
		
		//show setting dialog
		showSettingDialog(title, items, new IDialogActionListener() {
			@Override
			public void onSettingItems(List<SettingItem> items) 
			{
				if(!checkSettingItems(items, listener)){
					return ;
				}
				boolean status=false;
				String title=null;
				String description=null;
				String location=null;
				long appointmentTime=0;
				long remindTime=0;
				int vibrationLength=0;
				KRepeatSetting repeatSetting=null;
				boolean joinAgenda=false;
				long startTime=0;
				long endTime=0;
				long expirationDate=0;
				for(SettingItem item:items){
					if(item.getTitle().equalsIgnoreCase(labelStatus)){
						if(SWITCH_STATUS[0].equalsIgnoreCase(item.getTextViewValue())){
							status=true;
						}
					}
					else if(item.getTitle().equalsIgnoreCase(labelTitle)){
						title=item.getTextViewValue();
					}
					else if(item.getTitle().equalsIgnoreCase(labelDescription)){
						description=item.getTextViewValue();
					}
					else if(item.getTitle().equalsIgnoreCase(labelLocation)){
						location=item.getTextViewValue();
					}
					else if(item.getTitle().equalsIgnoreCase(labelVibrationLength)){
						vibrationLength=item.getIndex();
					}
					else if(item.getTitle().equalsIgnoreCase(labelAppointmentTime)){
						int hour=DeviceDataUtils.getHourFromTime(item.getTime());
						int minute=DeviceDataUtils.getMinuteFromTime(item.getTime());
						appointmentTime=DeviceDataUtils.formatUtcTime(hour, minute);
					}
					else if(item.getTitle().equalsIgnoreCase(labelReminderTime)){
						int hour=DeviceDataUtils.getHourFromTime(item.getTime());
						int minute=DeviceDataUtils.getMinuteFromTime(item.getTime());
						remindTime=DeviceDataUtils.formatUtcTime(hour, minute);
					}
					else if(item.getTitle().equalsIgnoreCase(labelRepeatMode)){
						logMessage("RepeatMode >> "+REPEAT_MODES[item.getIndex()].toString());
						repeatSetting=getKRepeatSetting(remindTime, item.getIndex());
						if(repeatSetting!=null){
							repeatSetting.setWeekDays(getWeekDays());
						}
					}
					else if(item.getTitle().equalsIgnoreCase(labelEndsTime)){
						int hour=DeviceDataUtils.getHourFromTime(item.getTime());
						int minute=DeviceDataUtils.getMinuteFromTime(item.getTime());
						expirationDate=DeviceDataUtils.formatUtcTime(hour, minute);
					}
					else if(item.getTitle().equalsIgnoreCase(labelTimeperiodStart) && !TextUtils.isEmpty(item.getTime())){
						int hour=DeviceDataUtils.getHourFromTime(item.getTime());
						int minute=DeviceDataUtils.getMinuteFromTime(item.getTime());
						startTime=DeviceDataUtils.formatUtcTime(hour, minute);
					}
					else if(item.getTitle().equalsIgnoreCase(labelTimeperiodEnd) && !TextUtils.isEmpty(item.getTime())){
						int hour=DeviceDataUtils.getHourFromTime(item.getTime());
						int minute=DeviceDataUtils.getMinuteFromTime(item.getTime());
						endTime=DeviceDataUtils.formatUtcTime(hour, minute);
					}
					else if(item.getTitle().equalsIgnoreCase(labelJoinAgenda)){
						if(STATUS_OF_JOIN_AGENDA[item.getIndex()].equalsIgnoreCase(item.getTextViewValue())){
							joinAgenda=true;
						}
					}
				}
				//call interface
				KAppointmentReminder appointmentReminder=new KAppointmentReminder(appointmentTime);
				appointmentReminder.setReminderIndex(createReminderIndex());
				appointmentReminder.setStatus(status);
				appointmentReminder.setDescription(description);
				appointmentReminder.setTitle(title);
				appointmentReminder.setLocation(location);
				appointmentReminder.setRemindTime(remindTime);
				appointmentReminder.setVibrationLength(vibrationLength);
				if(repeatSetting!=null){
					repeatSetting.setStartTime(startTime);
					repeatSetting.setEndsTime(endTime);;
					repeatSetting.setExpirationDate(expirationDate);
				}
				appointmentReminder.setRepeatSetting(repeatSetting);
				appointmentReminder.setJoinAgenda(joinAgenda);
				logMessage("appointment reminder >> "+appointmentReminder.toString());
				//调用设置接口
				LsBleManager.getInstance().pushDeviceMessage(deviceMac, appointmentReminder, listener);
			}
		});	
	}

	/**
	 * Add Appointment Reminder
	 * @param deviceMac
	 * @param listener
	 */
	public static void addMessageReminder(final String deviceMac,final OnSettingListener listener)
	{
		String title=getResourceString(R.string.title_reminder_message);
		final String labelStatus=getResourceString(R.string.label_switch);
		final String labelDescription=getResourceString(R.string.label_reminder_description);
		final String labelReminderTime=getResourceString(R.string.label_reminder_time);
		final String labelVibrationLength=getResourceString(R.string.label_vibration_length);
		final String labelJoinAgenda=getResourceString(R.string.label_join_agenda);

		//init setting items
		SettingItem enableItem=new SettingItem(SettingOptions.SingleChoice);
		enableItem.setTitle(getResourceString(R.string.label_switch));
		enableItem.setChoiceItems(Arrays.asList(SWITCH_STATUS));

		//description 
		SettingItem descriptionItem=new SettingItem(SettingOptions.Text);
		descriptionItem.setTitle(labelDescription);
		descriptionItem.setInputType(InputType.TYPE_CLASS_TEXT);
		//reminder time
		SettingItem reminderTimeItem=new SettingItem();
		reminderTimeItem.setOptions(SettingOptions.TimePicker);
		reminderTimeItem.setTitle(labelReminderTime);
		//vibration length
		SettingItem vibrationlengthItem=new SettingItem();
		vibrationlengthItem.setOptions(SettingOptions.NumberPicker);
		vibrationlengthItem.setTitle(labelVibrationLength);
		vibrationlengthItem.setMinValue(1);
		vibrationlengthItem.setMaxValue(60);
		//join agenda
		SettingItem joinAgendaItem=new SettingItem(SettingOptions.SingleChoice);
		joinAgendaItem.setTitle(labelJoinAgenda);
		joinAgendaItem.setChoiceItems(Arrays.asList(STATUS_OF_JOIN_AGENDA));

		//add item to list
		List<SettingItem> items=new ArrayList<SettingItem>();
		items.add(enableItem);
		items.add(descriptionItem);
		items.add(reminderTimeItem);
		items.add(vibrationlengthItem);
		items.add(joinAgendaItem);

		//show setting dialog
		showSettingDialog(title, items, new IDialogActionListener() {
			@Override
			public void onSettingItems(List<SettingItem> items) 
			{
				if(!checkSettingItems(items, listener)){
					return ;
				}
				boolean status=false;
				String description=null;
				String location=null;
				long remindTime=0;
				int vibrationLength=0;
				boolean joinAgenda=false;
				for(SettingItem item:items){
					if(item.getTitle().equalsIgnoreCase(labelStatus)){
						if(SWITCH_STATUS[0].equalsIgnoreCase(item.getTextViewValue())){
							status=true;
						}
					}
					else if(item.getTitle().equalsIgnoreCase(labelDescription)){
						description=item.getTextViewValue();
					}
					else if(item.getTitle().equalsIgnoreCase(labelVibrationLength)){
						vibrationLength=item.getIndex();
					}
					else if(item.getTitle().equalsIgnoreCase(labelReminderTime)){
						int hour=DeviceDataUtils.getHourFromTime(item.getTime());
						int minute=DeviceDataUtils.getMinuteFromTime(item.getTime());
						remindTime=DeviceDataUtils.formatUtcTime(hour, minute);
					}
					else if(item.getTitle().equalsIgnoreCase(labelJoinAgenda)){
						if(STATUS_OF_JOIN_AGENDA[item.getIndex()].equalsIgnoreCase(item.getTextViewValue())){
							joinAgenda=true;
						}
					}
				}
				//call interface
				KMessageReminder msgReminder=new KMessageReminder(description);
				msgReminder.setReminderIndex(createReminderIndex());
				msgReminder.setStatus(status);
				msgReminder.setRemindTime(remindTime);
				msgReminder.setVibrationLength(vibrationLength);
				msgReminder.setJoinAgenda(joinAgenda);
				logMessage("message reminder >> "+msgReminder.toString());
				//调用设置接口
				LsBleManager.getInstance().pushDeviceMessage(deviceMac, msgReminder, listener);
			}
		});	
	}

	public static void addSimpleReminder(final String deviceMac,final OnSettingListener listener) 
	{
		String title=getResourceString(R.string.title_reminder_simple);
		final String labelStatus=getResourceString(R.string.label_switch);
		final String labelIcon=getResourceString(R.string.label_reminder_icon);
		final String labelTitle=getResourceString(R.string.label_reminder_title);
		final String labelDescription=getResourceString(R.string.label_reminder_description);
		//		final String labelAppointmentTime=getResourceString(R.string.label_appointment_time);
		final String labelReminderTime=getResourceString(R.string.label_reminder_time);
		//		final String labelLocation=getResourceString(R.string.label_location);
		final String labelRepeatMode=getResourceString(R.string.label_repeat_mode);
		final String labelVibrationLength=getResourceString(R.string.label_vibration_length);
		final String labelJoinAgenda=getResourceString(R.string.label_join_agenda);
		final String labelEndsTime=getResourceString(R.string.label_reminder_ends_time);
		final String labelTimeperiodStart=getResourceString(R.string.label_time_period_start);
		final String labelTimeperiodEnd=getResourceString(R.string.label_time_period_end);

		//init setting items
		SettingItem iconItem=new SettingItem(SettingOptions.NumberPicker);
		iconItem.setMaxValue(25);
		iconItem.setMinValue(1);
		iconItem.setTitle(labelIcon);
		iconItem.setUnit("");
		//status
		SettingItem enableItem=new SettingItem(SettingOptions.SingleChoice);
		enableItem.setTitle(labelStatus);
		enableItem.setChoiceItems(Arrays.asList(SWITCH_STATUS));
		//title
		SettingItem titleItem=new SettingItem(SettingOptions.Text);
		titleItem.setTitle(labelTitle);
		titleItem.setInputType(InputType.TYPE_CLASS_TEXT);
		//description 
		SettingItem descriptionItem=new SettingItem(SettingOptions.Text);
		descriptionItem.setTitle(labelDescription);
		descriptionItem.setInputType(InputType.TYPE_CLASS_TEXT);
		//reminder time
		SettingItem reminderTimeItem=new SettingItem();
		reminderTimeItem.setOptions(SettingOptions.TimePicker);
		reminderTimeItem.setTitle(labelReminderTime);
		//repeat mode
		SettingItem repeatModeItem=new SettingItem(SettingOptions.SingleChoice);
		repeatModeItem.setTitle(labelRepeatMode);
		repeatModeItem.setChoiceItems(Arrays.asList(REPEAT_MODES));
		//vibration length
		SettingItem vibrationlengthItem=new SettingItem();
		vibrationlengthItem.setOptions(SettingOptions.NumberPicker);
		vibrationlengthItem.setTitle(labelVibrationLength);
		vibrationlengthItem.setMinValue(1);
		vibrationlengthItem.setMaxValue(60);
		//join agenda
		SettingItem joinAgendaItem=new SettingItem(SettingOptions.SingleChoice);
		joinAgendaItem.setTitle(labelJoinAgenda);
		joinAgendaItem.setChoiceItems(Arrays.asList(STATUS_OF_JOIN_AGENDA));
		//ends time 
		SettingItem endsDateItem=new SettingItem(SettingOptions.TimePicker);
		endsDateItem.setTitle(labelEndsTime);

		//between hours-start time 
		SettingItem startTimeItem=new SettingItem(SettingOptions.TimePicker);
		startTimeItem.setTitle(labelTimeperiodStart);

		//between hours-end time 
		SettingItem endTimeItem=new SettingItem(SettingOptions.TimePicker);
		endTimeItem.setTitle(labelTimeperiodEnd);

		//add item to list
		List<SettingItem> items=new ArrayList<SettingItem>();
		items.add(iconItem);
		items.add(enableItem);
		items.add(titleItem);
		items.add(descriptionItem);
		items.add(reminderTimeItem);
		items.add(vibrationlengthItem);
		items.add(repeatModeItem);
		items.add(joinAgendaItem);
		items.add(startTimeItem);
		items.add(endTimeItem);
		items.add(endsDateItem);

		//show setting dialog
		showSettingDialog(title, items, new IDialogActionListener() {
			@Override
			public void onSettingItems(List<SettingItem> items) 
			{
				if(!checkSettingItems(items, listener)){
					return ;
				}
				boolean status=false;
				String title=null;
				String description=null;
				String location=null;
				long remindTime=0;
				long expirationDate=0;
				int vibrationLength=0;
				KRepeatSetting repeatSetting=null;
				boolean joinAgenda=false;
				int iconIndex=0;
				long startTime=0;
				long endTime=0;
				for(SettingItem item:items){
					if(item.getTitle().equalsIgnoreCase(labelStatus)){
						if(SWITCH_STATUS[0].equalsIgnoreCase(item.getTextViewValue())){
							status=true;
						}
					}
					else if(item.getTitle().equalsIgnoreCase(labelIcon)){
						iconIndex=item.getIndex();
					}
					else if(item.getTitle().equalsIgnoreCase(labelTitle)){
						title=item.getTextViewValue();
					}
					else if(item.getTitle().equalsIgnoreCase(labelDescription)){
						description=item.getTextViewValue();
					}
					else if(item.getTitle().equalsIgnoreCase(labelVibrationLength)){
						vibrationLength=item.getIndex();
					}
					else if(item.getTitle().equalsIgnoreCase(labelEndsTime)){
						int hour=DeviceDataUtils.getHourFromTime(item.getTime());
						int minute=DeviceDataUtils.getMinuteFromTime(item.getTime());
						expirationDate=DeviceDataUtils.formatUtcTime(hour, minute);
					}
					else if(item.getTitle().equalsIgnoreCase(labelReminderTime)){
						int hour=DeviceDataUtils.getHourFromTime(item.getTime());
						int minute=DeviceDataUtils.getMinuteFromTime(item.getTime());
						remindTime=DeviceDataUtils.formatUtcTime(hour, minute);
					}
					else if(item.getTitle().equalsIgnoreCase(labelRepeatMode)){
						logMessage("RepeatMode >> "+REPEAT_MODES[item.getIndex()].toString());
						repeatSetting=getKRepeatSetting(remindTime, item.getIndex());
						if(repeatSetting!=null){
							repeatSetting.setWeekDays(getWeekDays());
						}
					}
					else if(item.getTitle().equalsIgnoreCase(labelTimeperiodStart) && !TextUtils.isEmpty(item.getTime())){
						int hour=DeviceDataUtils.getHourFromTime(item.getTime());
						int minute=DeviceDataUtils.getMinuteFromTime(item.getTime());
						startTime=DeviceDataUtils.formatUtcTime(hour, minute);
					}
					else if(item.getTitle().equalsIgnoreCase(labelTimeperiodEnd) && !TextUtils.isEmpty(item.getTime())){
						int hour=DeviceDataUtils.getHourFromTime(item.getTime());
						int minute=DeviceDataUtils.getMinuteFromTime(item.getTime());
						endTime=DeviceDataUtils.formatUtcTime(hour, minute);
					}
					else if(item.getTitle().equalsIgnoreCase(labelJoinAgenda)){
						if(STATUS_OF_JOIN_AGENDA[item.getIndex()].equalsIgnoreCase(item.getTextViewValue())){
							joinAgenda=true;
						}
					}
				}
				//call interface
				KSimpleReminder simppleReminder=new KSimpleReminder();
				simppleReminder.setIconIndex(iconIndex);
				simppleReminder.setReminderIndex(createReminderIndex());
				simppleReminder.setStatus(status);
				simppleReminder.setDescription(description);
				simppleReminder.setTitle(title);
				simppleReminder.setRemindTime(remindTime);
				simppleReminder.setVibrationLength(vibrationLength);
				simppleReminder.setRepeatSetting(repeatSetting);
				simppleReminder.setJoinAgenda(joinAgenda);
				if(repeatSetting!=null){
					repeatSetting.setStartTime(startTime);
					repeatSetting.setEndsTime(endTime);;
					repeatSetting.setExpirationDate(expirationDate);
					logMessage("repeat setting >> "+repeatSetting.toString()+"; endsTime >> "+expirationDate);
				}
				logMessage("simple reminder >> "+simppleReminder.toString());
				//调用设置接口
				LsBleManager.getInstance().pushDeviceMessage(deviceMac, simppleReminder, listener);
			}
		});	
	}

	public static void addWakeupReminder(final String deviceMac, final OnSettingListener listener) 
	{
		String title=getResourceString(R.string.title_reminder_simple);
		final String labelStatus=getResourceString(R.string.label_switch);
		final String labelTitle=getResourceString(R.string.label_reminder_title);
		final String labelDescription=getResourceString(R.string.label_reminder_description);
		final String labelReminderTime=getResourceString(R.string.label_reminder_time);
		final String labelRepeatMode=getResourceString(R.string.label_repeat_mode);
		final String labelVibrationLength=getResourceString(R.string.label_vibration_length);
		final String labelJoinAgenda=getResourceString(R.string.label_join_agenda);
		final String labelSnoozeLength=getResourceString(R.string.label_snooze_length);

		//init setting items
		SettingItem enableItem=new SettingItem(SettingOptions.SingleChoice);
		enableItem.setTitle(labelStatus);
		enableItem.setChoiceItems(Arrays.asList(SWITCH_STATUS));
		//title
		SettingItem titleItem=new SettingItem(SettingOptions.Text);
		titleItem.setTitle(labelTitle);
		titleItem.setInputType(InputType.TYPE_CLASS_TEXT);
		//description 
		SettingItem descriptionItem=new SettingItem(SettingOptions.Text);
		descriptionItem.setTitle(labelDescription);
		descriptionItem.setInputType(InputType.TYPE_CLASS_TEXT);
		//reminder time
		SettingItem reminderTimeItem=new SettingItem();
		reminderTimeItem.setOptions(SettingOptions.TimePicker);
		reminderTimeItem.setTitle(labelReminderTime);
		//repeat mode
		SettingItem repeatModeItem=new SettingItem(SettingOptions.SingleChoice);
		repeatModeItem.setTitle(labelRepeatMode);
		repeatModeItem.setChoiceItems(Arrays.asList(REPEAT_MODES));
		//vibration length
		SettingItem vibrationlengthItem=new SettingItem();
		vibrationlengthItem.setOptions(SettingOptions.NumberPicker);
		vibrationlengthItem.setTitle(labelVibrationLength);
		vibrationlengthItem.setMinValue(1);
		vibrationlengthItem.setMaxValue(60);
		//join agenda
		SettingItem joinAgendaItem=new SettingItem(SettingOptions.SingleChoice);
		joinAgendaItem.setTitle(labelJoinAgenda);
		joinAgendaItem.setChoiceItems(Arrays.asList(STATUS_OF_JOIN_AGENDA));
		//ends time 
		//vibration length
		SettingItem snoozeLengthItem=new SettingItem(SettingOptions.NumberPicker);
		snoozeLengthItem.setTitle(labelSnoozeLength);
		snoozeLengthItem.setMinValue(1);
		snoozeLengthItem.setMaxValue(60);

		//add item to list
		List<SettingItem> items=new ArrayList<SettingItem>();
		items.add(enableItem);
		items.add(titleItem);
		items.add(descriptionItem);
		items.add(reminderTimeItem);
		items.add(vibrationlengthItem);
		items.add(repeatModeItem);
		items.add(joinAgendaItem);
		items.add(snoozeLengthItem);

		//show setting dialog
		showSettingDialog(title, items, new IDialogActionListener() {
			@Override
			public void onSettingItems(List<SettingItem> items) 
			{
				if(!checkSettingItems(items, listener)){
					return ;
				}
				boolean status=false;
				String title=null;
				String description=null;
				String location=null;
				long remindTime=0;
				long endsTimes=0;
				int vibrationLength=0;
				KRepeatSetting repeatSetting=null;
				boolean joinAgenda=false;
				int snoozeLength=0;
				for(SettingItem item:items){
					if(item.getTitle().equalsIgnoreCase(labelStatus)){
						if(SWITCH_STATUS[0].equalsIgnoreCase(item.getTextViewValue())){
							status=true;
						}
					}
					else if(item.getTitle().equalsIgnoreCase(labelTitle)){
						title=item.getTextViewValue();
					}
					else if(item.getTitle().equalsIgnoreCase(labelDescription)){
						description=item.getTextViewValue();
					}
					else if(item.getTitle().equalsIgnoreCase(labelVibrationLength)){
						vibrationLength=item.getIndex();
					}
					else if(item.getTitle().equalsIgnoreCase(labelSnoozeLength)){
						snoozeLength=item.getIndex();
					}
					else if(item.getTitle().equalsIgnoreCase(labelReminderTime)){
						int hour=DeviceDataUtils.getHourFromTime(item.getTime());
						int minute=DeviceDataUtils.getMinuteFromTime(item.getTime());
						remindTime=DeviceDataUtils.formatUtcTime(hour, minute);
					}
					else if(item.getTitle().equalsIgnoreCase(labelRepeatMode)){
						logMessage("RepeatMode >> "+REPEAT_MODES[item.getIndex()].toString());
						repeatSetting=getKRepeatSetting(remindTime, item.getIndex());
						if(repeatSetting!=null){
							repeatSetting.setWeekDays(getWeekDays());
						}
					}
					else if(item.getTitle().equalsIgnoreCase(labelJoinAgenda)){
						if(STATUS_OF_JOIN_AGENDA[item.getIndex()].equalsIgnoreCase(item.getTextViewValue())){
							joinAgenda=true;
						}
					}
				}
				//call interface
				KWakeupReminder wakeupReminder=new KWakeupReminder();
				wakeupReminder.setReminderIndex(createReminderIndex());
				wakeupReminder.setStatus(status);
				wakeupReminder.setDescription(description);
				wakeupReminder.setTitle(title);
				wakeupReminder.setRemindTime(remindTime);
				wakeupReminder.setVibrationLength(vibrationLength);
				wakeupReminder.setRepeatSetting(repeatSetting);
				wakeupReminder.setJoinAgenda(joinAgenda);
				wakeupReminder.setSnoozeLength(snoozeLength);
				if(repeatSetting!=null){
					repeatSetting.setEndsTime(endsTimes);
				}
				logMessage("wakeup reminder >> "+wakeupReminder.toString());
				//调用设置接口
				LsBleManager.getInstance().pushDeviceMessage(deviceMac, wakeupReminder, listener);
			}
		});	
	}

	
	public static void createRepeatSetting(final SettingItem item,final long remindTime,final IDialogActionListener listener){

		String title=getResourceString(R.string.title_reminder_appointment);
		final String labelRepeatMode=getResourceString(R.string.label_repeat_mode);
		final String labelTimeperiodStart=getResourceString(R.string.label_time_period_start);
		final String labelTimeperiodEnd=getResourceString(R.string.label_time_period_end);
		final String labelEndsTime=getResourceString(R.string.label_reminder_ends_time);

		//repeat mode
		SettingItem repeatModeItem=new SettingItem(SettingOptions.SingleChoice);
		repeatModeItem.setTitle(labelRepeatMode);
		repeatModeItem.setChoiceItems(Arrays.asList(REPEAT_MODES));
		//between hours-start time 
		SettingItem startTimeItem=new SettingItem(SettingOptions.TimePicker);
		startTimeItem.setTitle(labelTimeperiodStart);
		startTimeItem.setEnableDatePicker(true);
		
		//between hours-end time 
		SettingItem endTimeItem=new SettingItem(SettingOptions.TimePicker);
		endTimeItem.setTitle(labelTimeperiodEnd);
		endTimeItem.setEnableDatePicker(true);
		
		//ends time 
		SettingItem endsDateItem=new SettingItem(SettingOptions.TimePicker);
		endsDateItem.setTitle(labelEndsTime);
		endsDateItem.setEnableDatePicker(true);

		//add item to list
		List<SettingItem> items=new ArrayList<SettingItem>();
		items.add(repeatModeItem);
		items.add(startTimeItem);
		items.add(endTimeItem);
		items.add(endsDateItem);
		
		//show setting dialog
		showSettingDialog(title, items, new IDialogActionListener() {
			@Override
			public void onSettingItems(List<SettingItem> items) 
			{
				KRepeatSetting repeatSetting=new KRepeatSetting(KRepeatType.None, 0);
				long startTime=0;
				long endTime=0;
				long expirationDate=0;
				for(SettingItem item:items){
					int year=item.getYear();
					int month=item.getMonth();
					int day=item.getDay();
					
					if(item.getTitle().equalsIgnoreCase(labelRepeatMode)){
						logMessage("RepeatMode >> "+REPEAT_MODES[item.getIndex()].toString());
						repeatSetting=getKRepeatSetting(remindTime, item.getIndex());
						if(repeatSetting!=null){
							repeatSetting.setWeekDays(getWeekDays());
						}
					}
					else if(item.getTitle().equalsIgnoreCase(labelEndsTime)){
						int hour=DeviceDataUtils.getHourFromTime(item.getTime());
						int minute=DeviceDataUtils.getMinuteFromTime(item.getTime());
						expirationDate=DeviceDataUtils.formatUtcTime(year,month,day,hour, minute);
					}
					else if(item.getTitle().equalsIgnoreCase(labelTimeperiodStart) && !TextUtils.isEmpty(item.getTime())){
						int hour=DeviceDataUtils.getHourFromTime(item.getTime());
						int minute=DeviceDataUtils.getMinuteFromTime(item.getTime());
						startTime=DeviceDataUtils.formatUtcTime(year,month,day,hour, minute);
					}
					else if(item.getTitle().equalsIgnoreCase(labelTimeperiodEnd) && !TextUtils.isEmpty(item.getTime())){
						int hour=DeviceDataUtils.getHourFromTime(item.getTime());
						int minute=DeviceDataUtils.getMinuteFromTime(item.getTime());
						endTime=DeviceDataUtils.formatUtcTime(year,month,day,hour, minute);
					}
				}
				//call interface
				repeatSetting.setEndsTime(endTime);
				repeatSetting.setStartTime(startTime);
				repeatSetting.setExpirationDate(expirationDate);

				logMessage("repeating setting >> "+repeatSetting.toString());
				//调用设置接口
				listener.onRepeatSettingCreate(item,repeatSetting);
			}
		});	
	
	}
	
	
	public static void createSimpleReminder(final SettingItem item, final IDialogActionListener listener)
	{
		String title=getResourceString(R.string.title_reminder_simple);
		final String labelStatus=getResourceString(R.string.label_switch);
		final String labelIcon=getResourceString(R.string.label_reminder_icon);
		final String labelTitle=getResourceString(R.string.label_reminder_title);
		final String labelDescription=getResourceString(R.string.label_reminder_description);
		final String labelVibrationLength=getResourceString(R.string.label_vibration_length);
		final String labelJoinAgenda=getResourceString(R.string.label_join_agenda);
		final String labelReminderTime=getResourceString(R.string.label_reminder_time);
		
		//init setting items
		SettingItem iconItem=new SettingItem(SettingOptions.NumberPicker);
		iconItem.setMaxValue(25);
		iconItem.setMinValue(1);
		iconItem.setTitle(labelIcon);
		iconItem.setUnit("");
		//status
		SettingItem enableItem=new SettingItem(SettingOptions.SingleChoice);
		enableItem.setTitle(labelStatus);
		enableItem.setChoiceItems(Arrays.asList(SWITCH_STATUS));
		//title
		SettingItem titleItem=new SettingItem(SettingOptions.Text);
		titleItem.setTitle(labelTitle);
		titleItem.setInputType(InputType.TYPE_CLASS_TEXT);
		//description 
		SettingItem descriptionItem=new SettingItem(SettingOptions.Text);
		descriptionItem.setTitle(labelDescription);
		descriptionItem.setInputType(InputType.TYPE_CLASS_TEXT);
		//reminder time
		SettingItem reminderTimeItem=new SettingItem();
		reminderTimeItem.setOptions(SettingOptions.TimePicker);
		reminderTimeItem.setTitle(labelReminderTime);
		reminderTimeItem.setEnableDatePicker(true);
		
		//vibration length
		SettingItem vibrationlengthItem=new SettingItem();
		vibrationlengthItem.setOptions(SettingOptions.NumberPicker);
		vibrationlengthItem.setTitle(labelVibrationLength);
		vibrationlengthItem.setMinValue(1);
		vibrationlengthItem.setMaxValue(60);
		//join agenda
		SettingItem joinAgendaItem=new SettingItem(SettingOptions.SingleChoice);
		joinAgendaItem.setTitle(labelJoinAgenda);
		joinAgendaItem.setChoiceItems(Arrays.asList(STATUS_OF_JOIN_AGENDA));

		//add item to list
		List<SettingItem> items=new ArrayList<SettingItem>();
		items.add(iconItem);
		items.add(enableItem);
		items.add(titleItem);
		items.add(descriptionItem);
		items.add(reminderTimeItem);
		items.add(vibrationlengthItem);
		items.add(joinAgendaItem);

		//show setting dialog
		showSettingDialog(title, items, new IDialogActionListener() {
			@Override
			public void onSettingItems(List<SettingItem> items) 
			{
				boolean status=false;
				String title=null;
				String description=null;
				long remindTime=0;
				int vibrationLength=0;
				boolean joinAgenda=false;
				int iconIndex=0;
				for(SettingItem item:items){
					if(item.getTitle().equalsIgnoreCase(labelStatus)){
						if(SWITCH_STATUS[0].equalsIgnoreCase(item.getTextViewValue())){
							status=true;
						}
					}
					else if(item.getTitle().equalsIgnoreCase(labelIcon)){
						iconIndex=item.getIndex();
					}
					else if(item.getTitle().equalsIgnoreCase(labelTitle)){
						title=item.getTextViewValue();
					}
					else if(item.getTitle().equalsIgnoreCase(labelDescription)){
						description=item.getTextViewValue();
					}
					else if(item.getTitle().equalsIgnoreCase(labelVibrationLength)){
						vibrationLength=item.getIndex();
					}
					else if(item.getTitle().equalsIgnoreCase(labelReminderTime)){
						int hour=DeviceDataUtils.getHourFromTime(item.getTime());
						int minute=DeviceDataUtils.getMinuteFromTime(item.getTime());
						int year=item.getYear();
						int month=item.getMonth();
						int day=item.getDay();
						remindTime=DeviceDataUtils.formatUtcTime(year,month,day,hour, minute);
					}
					else if(item.getTitle().equalsIgnoreCase(labelJoinAgenda)){
						if(STATUS_OF_JOIN_AGENDA[item.getIndex()].equalsIgnoreCase(item.getTextViewValue())){
							joinAgenda=true;
						}
					}
				}
				//call interface
				KSimpleReminder simppleReminder=new KSimpleReminder();
				simppleReminder.setIconIndex(iconIndex);
				simppleReminder.setReminderIndex(createReminderIndex());
				simppleReminder.setStatus(status);
				simppleReminder.setDescription(description);
				simppleReminder.setTitle(title);
				simppleReminder.setRemindTime(remindTime);
				simppleReminder.setVibrationLength(vibrationLength);
				simppleReminder.setJoinAgenda(joinAgenda);
				logMessage("simple reminder >> "+simppleReminder.toString());
				//调用设置接口
				listener.onKchiingReminderCreate(item,simppleReminder);
			}
		});	
	}
	
	
	public static void createAppointmentReminder(final SettingItem item,final IDialogActionListener listener)
	{
		String title=getResourceString(R.string.title_reminder_appointment);
		final String labelStatus=getResourceString(R.string.label_switch);
		final String labelTitle=getResourceString(R.string.label_reminder_title);
		final String labelDescription=getResourceString(R.string.label_reminder_description);
		final String labelAppointmentTime=getResourceString(R.string.label_appointment_time);
		final String labelReminderTime=getResourceString(R.string.label_reminder_time);
		final String labelLocation=getResourceString(R.string.label_location);
		final String labelVibrationLength=getResourceString(R.string.label_vibration_length);
		final String labelJoinAgenda=getResourceString(R.string.label_join_agenda);

		//init setting items
		SettingItem enableItem=new SettingItem(SettingOptions.SingleChoice);
		enableItem.setTitle(getResourceString(R.string.label_switch));
		enableItem.setChoiceItems(Arrays.asList(SWITCH_STATUS));
		//title
		SettingItem titleItem=new SettingItem(SettingOptions.Text);
		titleItem.setTitle(labelTitle);
		titleItem.setInputType(InputType.TYPE_CLASS_TEXT);
		//description 
		SettingItem descriptionItem=new SettingItem(SettingOptions.Text);
		descriptionItem.setTitle(labelDescription);
		descriptionItem.setInputType(InputType.TYPE_CLASS_TEXT);
		//appointment time
		SettingItem appointmentTimeItem=new SettingItem();
		appointmentTimeItem.setOptions(SettingOptions.TimePicker);
		appointmentTimeItem.setTitle(labelAppointmentTime);
		//reminder time
		SettingItem reminderTimeItem=new SettingItem();
		reminderTimeItem.setOptions(SettingOptions.TimePicker);
		reminderTimeItem.setTitle(labelReminderTime);
		reminderTimeItem.setEnableDatePicker(true);
		
		//loaction
		SettingItem locationItem=new SettingItem(SettingOptions.Text);
		locationItem.setTitle(labelLocation);
		locationItem.setInputType(InputType.TYPE_CLASS_TEXT);
		//vibration length
		SettingItem vibrationlengthItem=new SettingItem();
		vibrationlengthItem.setOptions(SettingOptions.NumberPicker);
		vibrationlengthItem.setTitle(labelVibrationLength);
		vibrationlengthItem.setMinValue(1);
		vibrationlengthItem.setMaxValue(60);
		//join agenda
		SettingItem joinAgendaItem=new SettingItem(SettingOptions.SingleChoice);
		joinAgendaItem.setTitle(labelJoinAgenda);
		joinAgendaItem.setChoiceItems(Arrays.asList(STATUS_OF_JOIN_AGENDA));

		//add item to list
		List<SettingItem> items=new ArrayList<SettingItem>();
		items.add(enableItem);
		items.add(titleItem);
		items.add(descriptionItem);
		items.add(appointmentTimeItem);
		items.add(reminderTimeItem);
		items.add(locationItem);
		items.add(vibrationlengthItem);
		items.add(joinAgendaItem);
		
		//show setting dialog
		showSettingDialog(title, items, new IDialogActionListener() {
			@Override
			public void onSettingItems(List<SettingItem> items) 
			{
				boolean status=false;
				String title=null;
				String description=null;
				String location=null;
				long appointmentTime=0;
				long remindTime=0;
				int vibrationLength=0;
				boolean joinAgenda=false;
				for(SettingItem item:items){
					if(item.getTitle().equalsIgnoreCase(labelStatus)){
						if(SWITCH_STATUS[0].equalsIgnoreCase(item.getTextViewValue())){
							status=true;
						}
					}
					else if(item.getTitle().equalsIgnoreCase(labelTitle)){
						title=item.getTextViewValue();
					}
					else if(item.getTitle().equalsIgnoreCase(labelDescription)){
						description=item.getTextViewValue();
					}
					else if(item.getTitle().equalsIgnoreCase(labelLocation)){
						location=item.getTextViewValue();
					}
					else if(item.getTitle().equalsIgnoreCase(labelVibrationLength)){
						vibrationLength=item.getIndex();
					}
					else if(item.getTitle().equalsIgnoreCase(labelAppointmentTime)){
						int hour=DeviceDataUtils.getHourFromTime(item.getTime());
						int minute=DeviceDataUtils.getMinuteFromTime(item.getTime());
						int year=item.getYear();
						int month=item.getMonth();
						int day=item.getDay();
						appointmentTime=DeviceDataUtils.formatUtcTime(year,month,day,hour, minute);
					}
					else if(item.getTitle().equalsIgnoreCase(labelReminderTime)){
						int hour=DeviceDataUtils.getHourFromTime(item.getTime());
						int minute=DeviceDataUtils.getMinuteFromTime(item.getTime());
						int year=item.getYear();
						int month=item.getMonth();
						int day=item.getDay();
						remindTime=DeviceDataUtils.formatUtcTime(year,month,day,hour, minute);
					}
					else if(item.getTitle().equalsIgnoreCase(labelJoinAgenda)){
						if(STATUS_OF_JOIN_AGENDA[item.getIndex()].equalsIgnoreCase(item.getTextViewValue())){
							joinAgenda=true;
						}
					}
				}
				//call interface
				KAppointmentReminder appointmentReminder=new KAppointmentReminder(appointmentTime);
				appointmentReminder.setReminderIndex(createReminderIndex());
				appointmentReminder.setStatus(status);
				appointmentReminder.setDescription(description);
				appointmentReminder.setTitle(title);
				appointmentReminder.setLocation(location);
				appointmentReminder.setRemindTime(remindTime);
				appointmentReminder.setVibrationLength(vibrationLength);
				appointmentReminder.setJoinAgenda(joinAgenda);
				logMessage("appointment reminder >> "+appointmentReminder.toString());
				//调用设置接口
				listener.onKchiingReminderCreate(item,appointmentReminder);
			}
		});	
	}
	
	private static KRepeatSetting getKRepeatSetting(long remindTime,int index)
	{
		if(remindTime == 0){
			remindTime=System.currentTimeMillis()/1000;
		}
		if(index == 0){
			//once
			return new KRepeatSetting(KRepeatType.None, 0);
		}
		else if(index == 1){
			//1x
			KRepeatSetting repeat=new KRepeatSetting(KRepeatType.Numbers, 1);
			//next repeat time
			List<Long> timestamps=new ArrayList<Long>();
			//after 2 minutes of remindTime
			//for test
			long nextTime=remindTime+2*60;
			timestamps.add(nextTime);
			repeat.setMultiRemindTimes(timestamps);
			return repeat;
		}
		else if(index == 2){
			//2x
			KRepeatSetting repeat=new KRepeatSetting(KRepeatType.Numbers, 2);
			//next repeat time
			List<Long> timestamps=new ArrayList<Long>();
			//after 2 minutes of remindTime
			//for test
			timestamps.add(remindTime+2*60);
			timestamps.add(remindTime+4*60);
			repeat.setMultiRemindTimes(timestamps);
			return repeat;
		}
		//		else if(index == 3){
		//			//3x
		//		}
		//		else if(index == 4){
		//			//4x
		//		}
		//		else if(index == 5){
		//			//"Every Hour"
		//		}
		//		else if(index == 6){
		//			//"Every Day"
		//		}
		//		else if(index == 7){
		//			//"Every Week"
		//		}
		//		else if(index == 8){
		//			//"Every Month"
		//		}
		else if(index == 3){
			//"Every 3 minutes"
			KRepeatSetting repeat=new KRepeatSetting(KRepeatType.Minutes, 3);
			//set start time and ends time
			repeat.setStartTime(System.currentTimeMillis()/1000);
			repeat.setEndsTime(System.currentTimeMillis()/1000+24*3600);
			return repeat;
		}
		//		else if(index == 10){
		//			//"Every 10 minutes"
		//
		//		}
		//		else if(index == 11){
		//			//"Every 1 hrs",
		//		}

		return null;

	}

	private static int createReminderIndex(){
		reminderIndex++;
		return reminderIndex;
	}
}
