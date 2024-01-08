package com.easycerti.logcollector.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RuleBizVO {
	
	private String occr_dt;
	private String proc_time;
	private String emp_user_id;
	
	public RuleBizVO(String occr_dt, String proc_time, String emp_user_id) {
		this.occr_dt = occr_dt;
		this.proc_time = proc_time;
		this.emp_user_id = emp_user_id;
	}

}
