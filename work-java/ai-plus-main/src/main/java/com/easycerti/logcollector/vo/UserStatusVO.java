package com.easycerti.logcollector.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserStatusVO {
	
	String seq;
	String status_code;
	
	public UserStatusVO(String seq, String status_code) {
		this.seq = seq;
		this.status_code = status_code;
	}

}
