package com.bluetooth.demo.device;

import java.util.List;

import android.text.InputType;

public class SettingItem {

	private int index;
	private String title;
	private SettingOptions options;

	private boolean isEnable;
	private String  time;
	private String  textViewValue;
	private boolean isEdit;
	private String editValue;
	private int maxValue;
	private int minValue;
	private List<String> choiceItems;
	private int inputType;
	private String unit=" min";
	private boolean isEnableDatePicker;
	private int year;
	private int month;
	private int day;
	

	public int getYear() {
		return year;
	}
	public void setYear(int year) {
		this.year = year;
	}
	public int getMonth() {
		return month;
	}
	public void setMonth(int month) {
		this.month = month;
	}
	public int getDay() {
		return day;
	}
	public void setDay(int day) {
		this.day = day;
	}
	public SettingItem() {
		super();
		setInputType(InputType.TYPE_CLASS_TEXT);
	}
	public SettingItem(SettingOptions options) {
		super();
		this.options = options;
		setInputType(InputType.TYPE_CLASS_TEXT);
	}

	public int getInputType() {
		return inputType;
	}
	public void setInputType(int inputType) {
		this.inputType = inputType;
	}
	public List<String> getChoiceItems() {
		return choiceItems;
	}
	public void setChoiceItems(List<String> choiceItems) {
		this.choiceItems = choiceItems;
	}

	public int getMaxValue() {
		return maxValue;
	}
	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
	}
	public int getMinValue() {
		return minValue;
	}
	public void setMinValue(int minValue) {
		this.minValue = minValue;
	}
	public boolean isEnable() {
		return isEnable;
	}
	public void setEnable(boolean isEnable) {
		this.isEnable = isEnable;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getTextViewValue() {
		return textViewValue;
	}
	public void setTextViewValue(String textViewValue) {
		this.textViewValue = textViewValue;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public SettingOptions getOptions() {
		return options;
	}
	public void setOptions(SettingOptions options) {
		this.options = options;
	}

	public boolean isEdit() {
		return isEdit;
	}
	public void setEdit(boolean isEdit) {
		this.isEdit = isEdit;
	}


	public String getEditValue() {
		return editValue;
	}
	public void setEditValue(String editValue) {
		this.editValue = editValue;
	}
	
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	
	public boolean isEnableDatePicker() {
		return isEnableDatePicker;
	}
	public void setEnableDatePicker(boolean isEnableDatePicker) {
		this.isEnableDatePicker = isEnableDatePicker;
	}
	
	@Override
	public String toString() {
		return "SettingItem [index=" + index + ", title=" + title
				+ ", options=" + options + ", isEnable=" + isEnable + ", time="
				+ time + ", textViewValue=" + textViewValue + ", isEdit="
				+ isEdit + ", editValue=" + editValue + ", maxValue="
				+ maxValue + ", minValue=" + minValue + ", choiceItems="
				+ choiceItems + ", inputType=" + inputType + ", unit=" + unit
				+ ", isEnableDatePicker=" + isEnableDatePicker + ", year="
				+ year + ", month=" + month + ", day=" + day + "]";
	}

	

}
