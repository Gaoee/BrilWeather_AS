package com.brilweather.http;


public interface HttpCallbackListene {
	
	void onFinish(String reportString);
	
	void onError(Exception e);
}
