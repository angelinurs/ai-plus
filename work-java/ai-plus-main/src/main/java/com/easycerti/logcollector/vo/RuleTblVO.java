package com.easycerti.logcollector.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RuleTblVO {
	
	Integer rule_seq;
	String rule_nm;
	String rule_desc;
	String log_delimiter;
	Integer dng_val;
	String script;
	String group_yn;
	String group_field_index;
	Integer limit_type;
	Integer limit_cnt;
	Integer trend_compare_dt;
	Integer trend_compare_dt_error_range;
	String is_realtime_extract;
	String realtime_extract_time;
	String auto_summon;
	String use_yn;
	String user_limit_cnt_yn;
	Integer scen_seq;
	String indv_yn;
	Integer user_dng_prct;
	String result_type;
	String alarm_yn;
	Integer limit_type_cnt;
	String system_seq;
	String privacy_seq;
	String ref_val;
	String script_desc;
	String rule_view_type;
	String rule_result_type;
	String result_type_yn;
	String time_view_yn;
	String ip_yn;
	
	public RuleTblVO(Integer rule_seq, String log_delimiter, String rule_nm, Integer dng_val, Integer limit_cnt, String rule_result_type, String rule_view_type) {
		this.rule_seq = rule_seq;
		this.log_delimiter = log_delimiter;
		this.rule_nm = rule_nm;
		this.dng_val = dng_val;
		this.limit_cnt = limit_cnt;
		this.rule_result_type = rule_result_type;
		this.rule_view_type = rule_view_type;
	}
	
	
}
