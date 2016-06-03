package com.brilweather.citymanage;

import java.util.List;

import org.w3c.dom.Text;

import com.brilweather.DB.WeatherDB;
import com.brilweather.model.City;
import com.example.brilweather.R;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class  DeleteAdapter  extends ArrayAdapter<String> {
	private final String TAG = "LEE";
	List<String> cities;
	Context mContext;
	WeatherDB weatherDB;

	private int resourceId;
	
	public DeleteAdapter(Context context, int resource, int textViewResourceId,
			List<String> objects) {
		super(context, resource, textViewResourceId, objects);
		resourceId = resource;
		cities = objects;
		mContext = context;
		try {
			weatherDB = WeatherDB.getInstanceDatabase(mContext);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		View view;
		
		final int positionId = position;
		
		if(convertView == null){
			holder = new ViewHolder();
			view = LayoutInflater.from(getContext()).inflate(resourceId, null);
			holder.itemTextView = (TextView)view.findViewById(R.id.city_tex);
			holder.deleteButton = (Button)view.findViewById(R.id.delete_but);
			view.setTag(holder);
			Log.v(TAG, "convertView == null");
		}else {
			view = convertView;
			holder = (ViewHolder)view.getTag();
			Log.v(TAG, "convertView != null");
		}
		
		Log.v(TAG, "Bttton:" + holder.deleteButton);
		holder.itemTextView.setText(getItem(position));
		
		holder.deleteButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				weatherDB.deleteCity(cities.get(positionId));
				cities.remove(positionId);
				notifyDataSetChanged();
//				Toast.makeText(getContext(), "click No." + positionId + "button", 
//						Toast.LENGTH_LONG).show();
			}
		});
		
		return view;
	}

	public final class ViewHolder{
		public TextView itemTextView;
		public Button deleteButton;
		
	}
	
}
