/**
 * 
 */
package com.bluetooth.demo.ui.adapter;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bluetooth.demo.R;
import com.bluetooth.demo.device.SettingItem;

/**
 * @author CaiChiXiang
 *
 */
@SuppressWarnings("rawtypes")
@SuppressLint("DefaultLocale")
public class SettingItemAdapter extends ArrayAdapter{

	private ArrayList<SettingItem> mItems;
	
	private Context context;

	@SuppressWarnings("unchecked")
	public SettingItemAdapter(Context context, ArrayList<SettingItem> items) {

		super(context, R.layout.setting_item, items);

		this.context = context;
		this.mItems = items;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		// 1. Create inflater
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// 2. Get rowView from inflater
		View rowView=null;
		if(this.mItems.size()!=0)
		{
			rowView =inflater.inflate(R.layout.setting_item, null);
			TextView title = (TextView) rowView.findViewById(R.id.setting_title_text_view);
			TextView value=(TextView) rowView.findViewById(R.id.setting_value_text_view);
			title.setText(this.mItems.get(position).getTitle());
			value.setText(this.mItems.get(position).getTextViewValue());
		}
		// 5. retrn rowView
		return rowView;
	}
	
	@Override
	public boolean isEnabled(int position) 
	{
		SettingItem item=this.mItems.get(position);
		if(item==null){
			return false;
		}
		return item.isEnable();
	}
}
