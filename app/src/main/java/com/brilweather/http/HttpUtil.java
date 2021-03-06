package com.brilweather.http;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtil {
	public static void sendHttpRequest(final String address,
			final HttpCallbackListene listener) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				HttpURLConnection connection = null;
				try {
					URL url = new URL(address);
					connection = (HttpURLConnection)url.openConnection();
					connection.setRequestMethod("GET");
					connection.setReadTimeout(8000);
					InputStream inputStream = connection.getInputStream();
					BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
					StringBuilder stringBuilder = new StringBuilder();
					String line;
					while((line = reader.readLine()) != null){
						stringBuilder.append(line);
					}
					
					if(listener != null){
						listener.onFinish(stringBuilder.toString());
					}
				} catch (Exception e){
					if(listener != null)
						listener.onError(e);
				}finally{
					if(connection != null)
						connection.disconnect();
				}
				
			}
		}).start();
	}
}
