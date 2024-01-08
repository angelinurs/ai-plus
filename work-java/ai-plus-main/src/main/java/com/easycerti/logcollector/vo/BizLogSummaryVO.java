package com.easycerti.logcollector.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BizLogSummaryVO {
	
	Integer log_seq;
	String proc_date;
	String proc_time;
	String user_ip;
	String emp_user_id;
	String system_seq;
	
	public BizLogSummaryVO(Integer log_seq, String proc_date, String proc_time, String user_ip, String emp_user_id, String system_seq) {
		this.log_seq = log_seq;
		this.proc_date = proc_date;
		this.proc_time = proc_time;
		this.user_ip = user_ip;
		this.emp_user_id = emp_user_id;
		this.system_seq = system_seq;
	}

	@Override
	public String toString() {
		return "BizLogSummaryVO [log_seq=" + log_seq + ", proc_date=" + proc_date + ", proc_time=" + proc_time
				+ ", user_ip=" + user_ip + ", emp_user_id=" + emp_user_id + "]";
	}

}
