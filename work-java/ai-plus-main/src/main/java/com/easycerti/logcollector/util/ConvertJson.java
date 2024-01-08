package com.easycerti.logcollector.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
//import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

/*
 * @ author      : 박경일
 * @ date        : 2023.05.08.mon
 * @ Beans :
 *     - Map<String, String> paramMap( Object object ) throws JSONException
 * @ description : 
 *     deserialization String Data to Map
 */

@Component
public class ConvertJson {
	public Map<String, String> paramMap( Object object ) throws Exception {
		Map<String, String> map = new HashMap<>();
		
		JSONObject json = new JSONObject( String.valueOf(object) );
		
		Iterator<?> it = json.keys();
		
		while( it.hasNext() ) {
			String key = it.next().toString();
			
			if (key.equals("anomal")) continue;
			map.put( key, json.getString(key) );
		}
		
		return map;
	}
	
	public JSONArray getAnomals(Object object) {
		JSONObject json = new JSONObject( String.valueOf(object) );
		JSONArray anomals = json.getJSONArray("anomal");
		
		return anomals;
	}
}
