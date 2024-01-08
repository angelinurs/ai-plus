package com.easycerti.logcollector.verification;

import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.easycerti.logcollector.util.ExternalAPI;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * Receiving JSON-formed string and saving this to file
 * @author      : 안상균
 * @date        : 2023.06.21. wed
 */

@Slf4j
@Component
public class SaveJSON {
	
	@Autowired
	private ExternalAPI externalAPI;
	
	/**
	 * 가져온 JSON string 데이터를 JSON 파일로 저장
	 * @author 안상균
	 * @param jsonStr
	 * @throws JsonMappingException
	 * @throws JsonProcessingException
	 */
	public void saveToFile(String jsonStr) throws JsonMappingException, JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> map = new HashMap<>();
		
		map = mapper.readValue(jsonStr, new TypeReference<HashMap<String, Object>>() {});
		
		JSONObject jsonObj = new JSONObject();
		
		for (String key : map.keySet()) {
			jsonObj.put(key, map.get(key));
		}
		
		try {
//			FileWriter fw = new FileWriter("/home/naru/lab/work/ai_plus_java/request.json");
			
			FileWriter fw = new FileWriter( externalAPI.getSave_json_path() );
			
			fw.write(jsonObj.toString());
			fw.flush();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
