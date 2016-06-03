package com.brilweather.sortlist;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.TextView;
/**
 * @Description: 文字图片，这个相信大家都知道，比如QQ底部导航上的未读消息�?
 * 
 * @author http://blog.csdn.net/finddreams
 */ 
public class ImageTextView extends TextView {
	private Bitmap bitmap;
	private String text;
	Drawable d;

	public ImageTextView(Context context) {
		super(context);
		
	}

	public ImageTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
	}

	public ImageTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
	}
	
	public void setIconText(Context context,String text){
		text = this.getText().toString().substring(0, 1);
		bitmap = BitmapUtil.getIndustry(context, text);
		d = BitmapUtil.bitmapTodrawable(bitmap);
		this.setCompoundDrawables(d, null, null, null);
	}

}
