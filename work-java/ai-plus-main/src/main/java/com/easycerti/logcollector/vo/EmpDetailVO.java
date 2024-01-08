package com.easycerti.logcollector.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmpDetailVO {
	
	Integer emp_detail_seq;
	String occr_dt;
	String dept_id;
	String emp_user_id;
	Integer rule_cd;
	Integer rule_cnt;
	Integer dng_val;
	String dept_name;
	String emp_user_name;
	String rule_nm;
	String log_delimiter;
	Integer cll_dmnd_id;
	String is_check;
	String system_seq;
	String followup;
	String is_summon_yn;
	String extract_key;
	String rule_result_type;
	String rule_view_type;
	Integer limit_cnt;
	String log_seqs;
	String result_content;
	
	public EmpDetailVO(Integer emp_detail_seq) {
		this.emp_detail_seq = emp_detail_seq;
	}

}
