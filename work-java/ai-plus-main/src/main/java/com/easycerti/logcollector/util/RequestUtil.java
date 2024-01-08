package com.easycerti.logcollector.util;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/*
 * @ author      : 박경일
 * @ date        : 2023.05.04.thi
 * @ Beans :
 *     - String restRequest(String requestUrl, String data )
 * @ description : 
 *     POST method 로 anomal dectect rest api app server 에
 *     request and response 하는 code
 */
@Component
public class RequestUtil {
	
	public String restRequest(String requestUrl, String data ) {
		String result = "";
		
		// set request parameter
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("id", data);
		
		// set header
		HttpHeaders headers = new HttpHeaders();   
		headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
		headers.add("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8");
//		headers.add( "Content-Type", "text/plain;charset=UTF-8" );
		
		// conbine parameters and header 
		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>( params, headers );
		
		// initialize RestTemplate
		RestTemplate rt = new RestTemplate();
		
//		URI url = URI.create(requestUrl);
		
		// transmit and result proccessing
		ResponseEntity<String> response = rt.exchange(
												requestUrl,
												HttpMethod.POST,
												entity,
												String.class
											);
		// transmit
//		RequestEntity<String> req = new RequestEntity<>(headers, HttpMethod.POST, url);
		// response
//		ResponseEntity<String> response = rt.exchange(
//												req,
//												String.class
//											);
		
		// save return value to body
		result = response.getBody();
		
		return result;
	}

}
