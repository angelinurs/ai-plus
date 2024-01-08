package com.easycerti.logcollector.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserVO {
	String name;

	public UserVO(String name) {
		this.name = name;
	}
	
}
