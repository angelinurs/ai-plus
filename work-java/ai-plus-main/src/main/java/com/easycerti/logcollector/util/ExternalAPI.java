package com.easycerti.logcollector.util;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties("external")
public class ExternalAPI {
	private String url_req;
	private String url_polling;
	private String table;
	private String period;
	private String emp_detail;
	private String rule_biz;
	private String prophet_model_period;
	private String[] emp_detail_columns;
	private String save_json_path;
	

}
