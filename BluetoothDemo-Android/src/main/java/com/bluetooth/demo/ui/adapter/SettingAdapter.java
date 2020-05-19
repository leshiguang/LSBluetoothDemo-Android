package com.bluetooth.demo.ui.adapter;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

import com.bluetooth.demo.R;
import com.bluetooth.demo.device.SettingItem;
import com.bluetooth.demo.device.SettingOptions;

@SuppressLint("NewApi")
public class SettingAdapter extends BaseExpandableListAdapter {

	private Context context;
	private List<SettingItem> expandableListTitle;
	private Calendar calendar;

	@SuppressLint("UseSparseArrays") 
	public SettingAdapter(Context context, List<SettingItem> items) {
		this.context = context;
		this.expandableListTitle = items;
	}

	@Override
	public Object getChild(int listPosition, int expandedListPosition) 
	{
		return this.expandableListTitle.get(listPosition);
	}

	@Override
	public long getChildId(int listPosition, int expandedListPosition) {
		return expandedListPosition;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getChildView(final int listPosition, final int expandedListPosition,
			boolean isLastChild, View convertView, ViewGroup parent) 
	{
		final SettingItem item=(SettingItem) getGroup(listPosition);
		if(item.getOptions() == SettingOptions.TimePicker){
			Log.e("LS-BLE", "init time view now");

			//初始化TimePicker View
			LayoutInflater layoutInflater = (LayoutInflater) this.context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layoutInflater.inflate(R.layout.time_select, null);
			TimePicker timePicker=(TimePicker) convertView.findViewById(R.id.timePicker);
			timePicker.setIs24HourView(true);
			//init
			int hour=Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
			int min=Calendar.getInstance().get(Calendar.MINUTE);
			final int year = Calendar.getInstance().get(Calendar.YEAR);
			final int month = Calendar.getInstance().get(Calendar.MONTH)+1;
			final int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
			item.setYear(year);
			item.setMonth(month);
			item.setDay(day);
			String time=hour+":"+min;
			item.setTime(time);
			String timeStr=item.getTime()+" "+day+"/"+month+"/"+year;
			item.setTextViewValue(timeStr);
			timePicker.setOnTimeChangedListener(new OnTimeChangedListener() {
				@Override
				public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
					String time=hourOfDay+":"+minute;
					item.setTime(time);
					String newTimeStr=item.getTime()+" "+day+"/"+month+"/"+year;
					item.setTextViewValue(newTimeStr);
					Log.e("LS-BLE", "from dialog my choice time >> "+newTimeStr);

				}
			});
			DatePicker datePicker=(DatePicker)convertView.findViewById(R.id.datePicker);
			if(item.isEnableDatePicker()){
				datePicker.setVisibility(View.VISIBLE);
				OnDateChangedListener listener = new OnDateChangedListener() {
					@Override
					public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) 
					{
						Log.e("LS-BLE", "OnDateChanfed,year="+year+"; month= "+(monthOfYear+1)+"; day="+dayOfMonth);
						//根据年、月，日 设置
						item.setYear(year);
						item.setMonth(monthOfYear+1);
						item.setDay(dayOfMonth);
						String newTimeStr=item.getTime()+" "+dayOfMonth+"/"+(monthOfYear+1)+"/"+year;
						item.setTextViewValue(newTimeStr);
					}
				};
				datePicker.init(year, month-1, day,listener);
			}
		}
		else if(item.getOptions() == SettingOptions.SingleChoice){
			//初始化SingleChoice View
			LayoutInflater layoutInflater = (LayoutInflater) this.context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layoutInflater.inflate(R.layout.single_choice_layout, null);
			RadioGroup group=(RadioGroup) convertView.findViewById(R.id.single_choice_group);
			if(item.getChoiceItems()!=null){
				for(String str:item.getChoiceItems())
				{
					RadioButton radioBtn=new RadioButton(context);    
					radioBtn.setText(str);
					group.addView(radioBtn);
				}
				//设置listener
				group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						RadioButton btn=(RadioButton) group.findViewById(checkedId);
						Log.e("LS-BLE", "from dialog my choice switch >> "+checkedId+"; select ="+btn.isChecked()+"; title="+btn.getText().toString());
						item.setTextViewValue(btn.getText().toString());
						item.setIndex(item.getChoiceItems().indexOf(btn.getText().toString()));
					}
				});
			}
		}
		else if(item.getOptions() == SettingOptions.NumberPicker){
			//初始化SingleChoice View
			LayoutInflater layoutInflater = (LayoutInflater) this.context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layoutInflater.inflate(R.layout.number_select, null);
			NumberPicker numberPicker=(NumberPicker) convertView.findViewById(R.id.number_picker);
			numberPicker.setMaxValue(item.getMaxValue());
			numberPicker.setMinValue(item.getMinValue());
			numberPicker.setOnValueChangedListener(new OnValueChangeListener() {
				@Override
				public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
					item.setTextViewValue(newVal+item.getUnit());
					item.setIndex(newVal);
				}
			});
		}


		return convertView;
	}

	@Override
	public int getChildrenCount(int listPosition) {
		return 1;
	}

	@Override
	public Object getGroup(int listPosition) {
		return this.expandableListTitle.get(listPosition);
	}

	@Override
	public int getGroupCount() {
		return this.expandableListTitle.size();
	}

	@Override
	public long getGroupId(int listPosition) {
		return listPosition;
	}

	@Override
	public View getGroupView(int listPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		final SettingItem item = (SettingItem) getGroup(listPosition);
		LayoutInflater layoutInflater = (LayoutInflater) this.context.
				getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		convertView = layoutInflater.inflate(R.layout.setting_item, null);
		TextView listTitleTextView = (TextView) convertView.findViewById(R.id.setting_title_text_view);
		listTitleTextView.setText(item.getTitle());

		TextView valueTextView=(TextView) convertView.findViewById(R.id.setting_value_text_view);
		if(item.getOptions() == SettingOptions.TimePicker){
			if(item.isEnableDatePicker()){
				valueTextView.setText(item.getTextViewValue());
			}
			else{
			valueTextView.setText(item.getTime());}
		}
		else if(item.getOptions() == SettingOptions.NumberPicker){
			valueTextView.setText(item.getTextViewValue());
		}
		else{
			valueTextView.setText(item.getTextViewValue());
		}
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int listPosition, int expandedListPosition) {
		return true;
	}
}
