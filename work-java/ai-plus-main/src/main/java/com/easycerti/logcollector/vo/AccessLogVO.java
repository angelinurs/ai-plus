package com.easycerti.logcollector.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccessLogVO {
	private String ds;
    private int y;
    
	public AccessLogVO(String ds, int y) {
		this.ds = ds;
		this.y = y;
	}
	
	@Override
	public String toString() {
		return "ds=" + ds + ", y=" + y + "\n";
	}
	
}
