package com.brilweather.citymanage;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.brilweather.DB.WeatherDB;
import com.brilweather.model.City;
import com.example.brilweather.R;

import java.util.List;

/**
 * Created by Administrator on 2016/6/17.
 */
public class CityAdapter extends ArrayAdapter<City> {
    private final String TAG = "LEE CityAdapter";
    List<City> cities;
    Context mContext;
    WeatherDB weatherDB;

    private int resourceId;

    public CityAdapter(Context context, int resource, int textViewResourceId,
                         List<City> objects) {
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

        Log.v(TAG, "getItem(position).getCityName()" + getItem(position).getCityName());
        if(convertView == null){
            holder = new ViewHolder();
            view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            holder.itemTextView = (TextView)view.findViewById(R.id.city_tex);
            view.setTag(holder);
            Log.v(TAG, "convertView == null");
        }else {
            view = convertView;
            holder = (ViewHolder)view.getTag();
            Log.v(TAG, "convertView != null");
        }

        holder.itemTextView.setText(getItem(position).getCityName());
        return view;
    }

    public final class ViewHolder{
        public TextView itemTextView;
    }

}
