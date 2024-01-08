package com.easycerti.logcollector.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

@Component
public class DateUtil {
	
	// get current date format -> YYYYMMDD
	public String getCurrentDate( String format ) {
		LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        String formattedNow = now.format(formatter);
        
        return formattedNow;
	}

}
