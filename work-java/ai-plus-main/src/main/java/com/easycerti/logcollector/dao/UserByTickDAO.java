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
import com.easycerti.logcollector.vo.AccessLogVO;
import com.easycerti.logcollector.vo.UserVO;

import lombok.extern.slf4j.Slf4j;

/**
 * biz_log_summary의 로그를 가져오는데 필요한 class
 * @author 박경일
 */
@Slf4j
@Component
public class UserByTickDAO {
	
	@Autowired
	private DataSource dataSource;

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private ExternalAPI externalAPI;
	
	/**
	 * 로그가 존재하는 모든 사용자 정보 가져옴
	 * 정의한 정규식에 따라 규칙에 어긋나는 ID는 필터링
	 * @author 박경일
	 * @name getUserList
	 * @return All user list
	 */
	public List<UserVO> getUserList( ) {
		if( jdbcTemplate == null ) {
			jdbcTemplate = new JdbcTemplate( dataSource );
		}
		
		final String sql =
				  "SELECT "
				+ "  DISTINCT emp_user_id AS user \r\n"
				+ String.format("FROM %s \r\n", externalAPI.getTable())
				+ "WHERE emp_user_id IS NOT NULL \r\n"
				+ "  AND NOT emp_user_id = user_ip \r\n"
				+ "  AND emp_user_id NOT IN ('null', '') \r\n";
		
//		 log.info("[sql query : unique user list] : {}", sql );
		
		List<UserVO> temp = null;
		
		temp = jdbcTemplate.query( 
				sql,				
				(rs, rowNum) -> new UserVO( rs.getString( "user" ) )
				);
		
		Pattern pattern = Pattern.compile("[^a-zA-Z0-9_-]");
		Matcher matcher;
		
		List<UserVO> results = new ArrayList<>();
		
		// 불필요 데이터 제거
		for (UserVO result : temp) {
			matcher = pattern.matcher(result.getName());
			
			if (!matcher.find()) {
				results.add(result);
			}
		}
		
		return results.isEmpty() ? null: results;
	}
	
	/**
	 * 특정 사용자의 로그를 가져옴
	 * @author 박경일
	 * @name getUserAccessLogList
	 * @param username - 사용자 ID
	 * @param selectedDate - 오늘로 기준을 잡을 날짜
	 * @param days - 가져올 로그의 기간
	 * @return User access log list
	 */
	public List<AccessLogVO> getUserAccessLogList( String username, String selectedDate, int days ) {
		if( jdbcTemplate == null ) {
			jdbcTemplate = new JdbcTemplate( dataSource );
		}
		
		String dateRangeSql = "";
		
		if (days == 0) { // 오늘 날짜
			dateRangeSql = String.format(" AND proc_date = TO_CHAR(TO_DATE('%s', 'YYYYMMDD') - INTERVAL '1 day', 'YYYYMMDD') \r\n", selectedDate);
		} else if (days == -1) { // 선택한 사용자 로그 전체 조회
			dateRangeSql = "\r\n";
		} else if (days > 0) { // 어제 ~ 지정한 기간
			dateRangeSql = String.format(" AND  proc_date >= TO_CHAR(TO_DATE('%s', 'YYYYMMDD') - INTERVAL '%d day', 'YYYYMMDD') \r\n", selectedDate, days + 1 )
					 + String.format(" AND  proc_date <= TO_CHAR(TO_DATE('%s', 'YYYYMMDD') - INTERVAL '2 day', 'YYYYMMDD') \r\n", selectedDate);
		}
		
		final String sql =
				"SELECT \r\n"
					+ " TO_CHAR(DATE_TRUNC('hour', TO_TIMESTAMP(proc_date || proc_time, 'YYYYMMDDHH24MISS')) \r\n"
					+ String.format("+ ((TRUNC((DATE_PART('minute', TO_TIMESTAMP(proc_date || proc_time, 'YYYYMMDDHH24MISS')) / %s)::FLOAT)) \r\n", externalAPI.getPeriod())
					+ String.format("* INTERVAL '%s MINUTE'), 'YYYYMMDD-HH24MI') AS ds, \r\n", externalAPI.getPeriod())
					+ " COUNT(*) as count \r\n"
			 + String.format(" FROM %s \r\n", externalAPI.getTable())
			 + String.format( " WHERE emp_user_id = '%s' \r\n", username )
			 + dateRangeSql
			 + " GROUP BY ds, emp_user_id \r\n"
			 + " ORDER BY ds ASC";
		
		// log.info("\n---------------------- SQL ----------------------\n{}\n-------------------------------------------------", sql );
		
		List<AccessLogVO> results = null;
		
		results = jdbcTemplate.query( 
				sql,				
				(rs, rowNum) -> new AccessLogVO(
						rs.getString( "ds" ),
						rs.getInt( "count" ))
				);
		
		int period = Integer.parseInt(externalAPI.getPeriod());
		
//		StringBuilder sb = new StringBuilder();
//		for( AccessLogVO tvo : results ) {
//			sb.append( tvo.toString() );
//			
//		}
//		log.info( sb.toString() );
		
		return results.isEmpty() ? null: results;
	}
	
}
