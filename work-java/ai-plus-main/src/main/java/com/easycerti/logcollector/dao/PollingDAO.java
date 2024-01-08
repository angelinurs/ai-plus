package com.easycerti.logcollector.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.easycerti.logcollector.util.ExternalAPI;
import com.easycerti.logcollector.vo.BizLogSummaryVO;
import com.easycerti.logcollector.vo.EmpDetailVO;
import com.easycerti.logcollector.vo.RuleBizVO;
import com.easycerti.logcollector.vo.RuleTblVO;
import com.easycerti.logcollector.vo.UserDetailVO;

import lombok.extern.slf4j.Slf4j;

/**
 * Polling 후 emp_detail, rule_biz에 비정상 의심행위 관련 데이터를 넣을 때 필요한 class
 * @author 안상균
 * @date 2023.05.26 fri.
 */

@Slf4j
@Component
public class PollingDAO {
	
	@Autowired
	DataSource dataSource;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private ExternalAPI externalAPI;
	
	private Pattern patternDate = Pattern.compile("([0-9]{4})-([0-9]{2})-([0-9]{2})");
	private Pattern patternTime = Pattern.compile("([0-9]{2}):([0-9]{2}):([0-9]{2})");
	private Matcher matcherDate = null;
	private Matcher matcherTime = null;
	
	private String date = null;
	private String time = null;
	
	/**
	 * 회원의 ID, 이름, 부서ID, 부서명 가져옴
	 * @author 안상균
	 * @param id - 사용자 ID
	 * @param date - 날짜
	 * @return 선택한 날짜와 ID에 해당되는 로그의 ID, 이름, 부서ID, 부서명
	 */
	public List<UserDetailVO> getUserDetail(String id, String date) {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(dataSource);
		}
		
		List<UserDetailVO> results = new ArrayList<>();
		
		final String sql =
				"SELECT "
				+ "emp_user_id, system_seq, emp_user_name, dept_id, "
				+ "  ("
				+ "  SELECT dept_name "
				+ "  FROM dept_tree dt "
				+ "  WHERE bl.dept_id = dt.dept_id"
				+ "  ) as dept_name "
				+ String.format("FROM %s bl ", externalAPI.getTable())
				+ "WHERE "
				+ String.format("emp_user_id = '%s' ", id)
				+ " order by proc_date desc;";
		
//		 log.info("\n---------------------- SQL ----------------------\n{}\n-------------------------------------------------", sql );
		
		results = jdbcTemplate.query( 
				sql,				
				(rs, rowNum) -> new UserDetailVO(
					rs.getString("emp_user_id"),
					rs.getString("system_seq"),
					rs.getString("emp_user_name"),
					rs.getString("dept_id"),
					rs.getString("dept_name")
				));
		
		return results;
	}
	
	/**
	 * 비정상행위 규칙 관련 정보를 가져옴
	 * @author 안상균
	 * @param ruleSeq - 규칙 코드
	 * @return 해당 규칙 코드의 정보
	 */
	public List<RuleTblVO> getRule(Integer ruleSeq) {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(dataSource);
		}
		
		List<RuleTblVO> results = new ArrayList<>();
		
		final String sql = 
				"SELECT "
				+ "rule_seq, log_delimiter, rule_nm, dng_val, limit_cnt, rule_result_type, rule_view_type "
				+ "FROM ruletbl "
				+ "WHERE "
				+ String.format("rule_seq = %d;", ruleSeq);
		
		// log.info("\n---------------------- SQL ----------------------\n{}\n-------------------------------------------------", sql );
		
		results = jdbcTemplate.query(
				sql,
				(rs, rowNum) -> new RuleTblVO(
					rs.getInt("rule_seq"),
					rs.getString("log_delimiter"),
					rs.getString("rule_nm"),
					rs.getInt("dng_val"),
					rs.getInt("limit_cnt"),
					rs.getString("rule_result_type"),
					rs.getString("rule_view_type")
				));
		
		return results;
	}
	
	/**
	 * 특정 사용자의 탐지한 비정상행위 관련 로그 개수
	 * @author 안상균
	 * @param anormal - 비정상행위 관련 로그 관련 정보(ds, y)
	 * @param name - 사용자 ID
	 * @return 비정상행위 관련 로그 개수
	 */
	public Integer getAnormalLogsCount(String anormal, String name) {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(dataSource);
		}
		
		Integer result = null;
		
		matcherDate = patternDate.matcher(anormal);
		matcherTime = patternTime.matcher(anormal);
		
		if (matcherDate.find()) {
			date = matcherDate.group();
		}
		if (matcherTime.find()) {
			time = matcherTime.group();
		}
		
		final String sql = 
				"SELECT "
				+ "COUNT(*) as cnt "
				+ String.format(" FROM %s ", externalAPI.getTable())
				+ "WHERE "
				+ String.format("proc_date = to_char(to_date('%s', 'YYYY-MM-DD'), 'YYYYMMDD') ", date)
				+ "AND (proc_time between "
				+ String.format("'%s'", time.replace(":", "")) + " AND "
				+ String.format("to_char('%s' + interval '%s minute' - interval '1 second', 'HH24MISS')", time, externalAPI.getProphet_model_period()) + ") AND "
				+ String.format("emp_user_id = '%s';", name);
		
		// log.info("\n---------------------- SQL ----------------------\n{}\n-------------------------------------------------", sql );
		
		result = jdbcTemplate.queryForObject(sql, Integer.class);
		
		return result;
	}
	
	/**
	 * 탐지한 비정상행위 관련 로그
	 * @author 안상균
	 * @param anormal - 비정상행위 관련 로그 관련 정보(ds, y)
	 * @param name - 사용자 ID
	 * @return 비정상행위 관련 로그
	 */
	public List<BizLogSummaryVO> getAnormalLogs(String anormal, String name) {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(dataSource);
		}
		
		List<BizLogSummaryVO> results = new ArrayList<>();;
		
		matcherDate = patternDate.matcher(anormal);
		matcherTime = patternTime.matcher(anormal);
		
		if (matcherDate.find()) {
			date = matcherDate.group();
		}
		if (matcherTime.find()) {
			time = matcherTime.group();
		}
		
		final String sql = 
				"SELECT "
				+ "log_seq, proc_date, proc_time, user_ip, emp_user_id, system_seq "
				+ String.format(" FROM %s ", externalAPI.getTable())
				+ "WHERE "
				+ String.format("proc_date = to_char(to_date('%s', 'YYYY-MM-DD'), 'YYYYMMDD') ", date)
				+ "AND (proc_time between "
				+ String.format("'%s'", time.replace(":", "")) + " AND "
				+ String.format("to_char('%s' + interval '%s minute' - interval '1 second', 'HH24MISS')", time, externalAPI.getProphet_model_period()) + ") AND "
				+ String.format("emp_user_id = '%s';", name);
		
		// log.info("\n---------------------- SQL ----------------------\n{}\n-------------------------------------------------", sql );
		
		results = jdbcTemplate.query(
				sql,
				(rs, rowNum) -> new BizLogSummaryVO(
					rs.getInt("log_seq"),
					rs.getString("proc_date"),
					rs.getString("proc_time"),
					rs.getString("user_ip"),
					rs.getString("emp_user_id"),
					rs.getString("system_seq")
				));
		
		return results;
	}
	
	/**
	 * 특정 user ID 및 비정상행위 규칙 코드의 emp_detail_seq 가져옴
	 * @author 안상균
	 * @param id - 사용자 ID
	 * @param ruleSeq - 비정상행위 규칙 코드
	 * @return 특정 user ID 및 비정상행위 규칙 코드의 emp_detail_seq
	 */
	public List<EmpDetailVO> getEmpDetail(String id, Integer ruleSeq) {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(dataSource);
		}
		
		List<EmpDetailVO> results = new ArrayList<>();
		
		final String sql = 
				"SELECT emp_detail_seq "
				+ String.format("FROM %s ", externalAPI.getEmp_detail())
				+ "WHERE "
				+ String.format("emp_user_id = '%s' ", id) + " AND "
				+ String.format("rule_cd = %d", ruleSeq) + " AND "
				+ String.format("occr_dt = to_char(to_date('%s', 'YYYY-MM-DD'), 'YYYYMMDD');", date);
		
		// log.info("\n---------------------- SQL ----------------------\n{}\n-------------------------------------------------", sql );
		
		results = jdbcTemplate.query(
				sql,
				(rs, rowNum) -> new EmpDetailVO(
					rs.getInt("emp_detail_seq")
				));
		
		return results;
	}
	
	/**
	 * emp_detail에 비정상행위 관련 정보 기록
	 * @author 안상균
	 * @param userDetail - 사용자 상세 정보
	 * @param rule - 비정상행위 규칙 관련 정보
	 * @param count - 비정상행위 관련 로그 개수
	 */
	public void insertEmpDetail(UserDetailVO userDetail, RuleTblVO rule, Integer count) {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(dataSource);
		}
		
		String todayDate = String.format("to_char(to_date('%s', 'YYYY-MM-DD'), 'YYYYMMDD') ", date);
		
		String emp_detail_columns = String.join(", ", externalAPI.getEmp_detail_columns() );
		
		final String sql = String.format(
				  " INSERT INTO %s ( ", externalAPI.getEmp_detail()) + emp_detail_columns + ") "
				+ " VALUES ( "
				+ String.format("%s, ", todayDate)
				+ String.format("'%s', ", userDetail.getDept_id())
				+ String.format("'%s', ", userDetail.getEmp_user_id())
				+ String.format("%d, ", rule.getRule_seq())
				+ String.format("%d, ", count)
				+ String.format("%d, ", rule.getDng_val())
				+ String.format("'%s', ", userDetail.getDept_name())
				+ String.format("'%s', ", userDetail.getEmp_user_name())
				+ String.format("'%s', ", rule.getRule_nm())
				+ String.format("'%s', ", rule.getLog_delimiter())
				+ String.format("'%s', ", userDetail.getSystem_seq())
				+ String.format("'%s', ", rule.getRule_result_type())
				+ String.format("'%s', ", rule.getRule_view_type())
				+ String.format("%d", rule.getLimit_cnt())
				+ ");";
		
		 log.info("\n---------------------- SQL ----------------------\n{}\n-------------------------------------------------", sql );
		
		 jdbcTemplate.update(sql);
	}
	
	/**
	 * rule_biz에 탐지한 비정상행위 관련 데이터 기록
	 * @author 안상균
	 * @param anormalLog - 비정상행위 관련 로그
	 * @param empDetail - emp_detail 관련 정보
	 * @param username - 사용자 ID
	 */
	public void insertRuleBiz(BizLogSummaryVO anormalLog, EmpDetailVO empDetail, String username) {
		if (jdbcTemplate == null) {
			jdbcTemplate = new JdbcTemplate(dataSource);
		}
		
		final String sql = String.format("INSERT INTO %s (", externalAPI.getRule_biz())
				+ "emp_detail_seq, "
				+ "occr_dt, "
				+ "log_seq, "
				+ "proc_time, "
				+ "ip, "
				+ "emp_user_id"
				+ ") VALUES ("
				+ String.format("%d, ", empDetail.getEmp_detail_seq())
				+ String.format("'%s', ", anormalLog.getProc_date())
				+ String.format("%d, ", anormalLog.getLog_seq())
				+ String.format("'%s', ", anormalLog.getProc_time())
				+ String.format("'%s', ", anormalLog.getUser_ip())
				+ String.format("'%s');", anormalLog.getEmp_user_id());
		
		// log.info("\n---------------------- SQL ----------------------\n{}\n-------------------------------------------------", sql );
		
		jdbcTemplate.update(sql);
	}

}
