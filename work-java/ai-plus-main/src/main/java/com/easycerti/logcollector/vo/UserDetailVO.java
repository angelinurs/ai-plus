package com.easycerti.logcollector.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDetailVO {
	
	String emp_user_id;
	String system_seq;
	String emp_user_name;
	String dept_id;
	String dept_name;
	
	public UserDetailVO(String emp_user_id, String system_seq, String emp_user_name, String dept_id, String dept_name) {
		this.emp_user_id = emp_user_id;
		this.system_seq = system_seq;
		this.emp_user_name = emp_user_name;
		this.dept_id = dept_id;
		this.dept_name = dept_name;
	}

}
