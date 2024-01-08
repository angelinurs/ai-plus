package com.easycerti.logcollector.scheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.easycerti.logcollector.dao.PollingDAO;
import com.easycerti.logcollector.dao.UserByTickDAO;
import com.easycerti.logcollector.util.ConvertJson;
import com.easycerti.logcollector.util.DateUtil;
import com.easycerti.logcollector.util.ExternalAPI;
import com.easycerti.logcollector.util.RequestUtil;
import com.easycerti.logcollector.vo.AccessLogVO;
import com.easycerti.logcollector.vo.BizLogSummaryVO;
import com.easycerti.logcollector.vo.EmpDetailVO;
import com.easycerti.logcollector.vo.RuleTblVO;
import com.easycerti.logcollector.vo.UserDetailVO;
import com.easycerti.logcollector.vo.UserStatusVO;
import com.easycerti.logcollector.vo.UserVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 스케줄링
 * @author 안상균
 * @date 2023.06.02 fri.
 * @description The class related to scheduling
 */

@Slf4j
@Component
public class Scheduler {
	
	@Autowired
	private UserByTickDAO userByTickDAO;
	
	@Autowired
	private PollingDAO pollingDAO;
	
	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	private RequestUtil requestUtil;
	
	@Autowired
	private ConvertJson convertJson;
	
	@Autowired
	private ExternalAPI externalAPI;
	
	@Autowired
	private DateUtil dateUtil;
	
	// @Scheduled(cron = "0 0 1 * * ?")
	public void test() {
		String currentDate = dateUtil.getCurrentDate("yyyyMMdd");
		log.info("Current time : {}", currentDate);
		// doExecute();
	}
	
	// @Scheduled(cron = "0 0 1 * * ?")
	public String doExecute() {
		List<UserVO> users = userByTickDAO.getUserList();
		Map<String, UserStatusVO> nameAndData = null;
		Map<String, String> res = null;
		
		try {
			nameAndData = request_data(users);
			res = polling_get_data(nameAndData);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return (res == null) ? null : "Is completed? " + res.get("Completed");
	}
	
	private Map<String, UserStatusVO> request_data(List<UserVO> userVOList) throws JsonProcessingException {
		
		Map<String, Object> mapAsUser = new HashMap<>();
		List<String> usernames = new ArrayList<>();
		
		// UserVO list에서 user id 가져오기
		for (UserVO user : userVOList) {
			usernames.add(user.getName());
		}
		
//		final String date = "20230619";
		String date = dateUtil.getCurrentDate("yyyyMMdd");
		
		Map<String, UserStatusVO> result = new HashMap<>();
		
		// 각 user마다 request data
		for (String username : usernames) {
			// 3 달치
			List<AccessLogVO> accessLogs = userByTickDAO.getUserAccessLogList( username, date, 90 );
			// today
			List<AccessLogVO> todayLogs = userByTickDAO.getUserAccessLogList( username, date, 0 );
			
			mapAsUser.put("name", username );
			
			String resp = "";
			
			// 다양하게 변경해보세요
			if( (todayLogs == null || accessLogs == null) || accessLogs.size() <= 2 ) {
				mapAsUser.put("result", "0");
				resp = String.format("%s: empty...", username );
			} else {
				mapAsUser.put("result", "1");
				// train 할 log ( 어제부터 3달 )
				mapAsUser.put("logs", accessLogs );
				// predict 할 log ( 오늘 날짜 )		
				mapAsUser.put("today_logs", todayLogs );
				
				String toJson = mapper.writeValueAsString( mapAsUser );
				
				//log.info("toJson : {}", toJson);
				
				resp = requestUtil.restRequest( externalAPI.getUrl_req(), toJson );
				try {
					log.info("\n-------------------------------- User --------------------------------\n"
							+ "  id             | " + username + "\n"
							+ "------------------------------ Request data -------------------------\n"
							+ "  req-date       | " + convertJson.paramMap(resp).get( "req-date" ) + "\n"
							+ "  seq            | " + convertJson.paramMap(resp).get( "seq" ) + "\n"
							+ "  status-code    | " + convertJson.paramMap(resp).get( "status-code" ) + "\n"
							+ "---------------------------------------------------------------------");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if (!resp.contains("empty...")) {
				try {
					String seq = convertJson.paramMap(resp).get( "seq" );
					result.put(username, new UserStatusVO(seq, "0"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return result;
	}
	
	private Map<String, String> polling_get_data(Map<String, UserStatusVO> nameAndData) throws Exception {
		
		int allStatusCodeAnd = 0; // 모든 user status code bitwize AND 값
		
		final long startTime = System.currentTimeMillis();
		final long limitTime = 10 * 1000; // polling 제한시간(millisecond 단위): 10초
		
		// allStatusCodeAnd가 1일 때, 즉 모든 status code가 1일 때 while문 종료
		while (allStatusCodeAnd == 0) {
			long presentTime = System.currentTimeMillis() - startTime; // 현재 경과 시간
			// 제한 시간 초과 시 break
			if (presentTime > limitTime) {
				break;
			}
			
			List<Integer> statusCodeList = new ArrayList<>(); // 모든 user의 status code들
			
			// status code가 1이 아니면 polling 실시
			for (String key : nameAndData.keySet()) {
				// key: user name
				// status-code가 1일 시 polling 안함
				if (nameAndData.get(key).getStatus_code().equals("1")) {
					continue;
				} else {
					String seq = nameAndData.get(key).getSeq();
					
					Map<String, Object> mapAsUser = new HashMap<>();
					
					mapAsUser.put("seq", seq  );
					
					String toJson = mapper.writeValueAsString( mapAsUser );
					
					//log.info("{}", toJson);
					
					String resp = requestUtil.restRequest( externalAPI.getUrl_polling(), toJson );
					
					String status =  convertJson.paramMap(resp).get( "status-code" );
					
					String username = "";
					for (String name : nameAndData.keySet()) {
						if (nameAndData.get(name).getSeq().equals(seq)) {
							username = name;
							break;
						}
					}
					
//					final String date = "20230619";
					String date = dateUtil.getCurrentDate("yyyyMMdd");
					
					UserDetailVO userDetail = pollingDAO.getUserDetail(username, date).get(0);
					
					RuleTblVO rule = pollingDAO.getRule(5500).get(0);
					
					if ( Integer.valueOf( status ) == 0 ) {
						try {
							log.info("\n-------------------------------- User --------------------------------\n"
									+ "  id                 | " + username + "\n"
									+ "-------------------------------- Polling ----------------------------\n"
									+ "  req-date           | " + convertJson.paramMap(resp).get( "req-date" ) + "\n"
									+ "  seq                | " + convertJson.paramMap(resp).get( "seq" ) + "\n"
									+ "  status-code        | " + convertJson.paramMap(resp).get( "status-code" ) + "\n"
									+ "---------------------------------------------------------------------");
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else if( Integer.valueOf( status ) != 0 ) {
						// status code가 1이면 nameAndData의 해당 user의 status code를 1로 변경
						UserStatusVO tempVO = nameAndData.get(key);
						tempVO.setStatus_code("1");
						nameAndData.put(key, tempVO);
						statusCodeList.add(Integer.parseInt(nameAndData.get(key).getStatus_code()));
						
//						String anomal =  convertJson.paramMap(resp).get( "anomal" );
						JSONArray anomal = convertJson.getAnomals(resp);
						//log.info( "anomal : {}", anomal );
						
						try {
							log.info("\n-------------------------------- User --------------------------------\n"
									+ "  id                 | " + username + "\n"
									+ "-------------------------------- Polling ----------------------------\n"
									+ "  req-date           | " + convertJson.paramMap(resp).get( "req-date" ) + "\n"
									+ "  seq                | " + convertJson.paramMap(resp).get( "seq" ) + "\n"
									+ "  status-code        | " + convertJson.paramMap(resp).get( "status-code" ) + "\n"
									+ "  completion-date    | " + convertJson.paramMap(resp).get( "completion-date" ) + "\n"
									+ "  anomal             | " + convertJson.paramMap(resp).get( "anomal" ) + "\n"
									+ "---------------------------------------------------------------------");
						} catch (Exception e) {
							e.printStackTrace();
						}
						
						// 비정상행위가 있는 경우 rule_biz에 추가
//						if (!anomal.equals("[]")) {
						if (anomal.length() != 0) {
//							anomal = anomal.replace("[", "").replace("]", "");
//							String[] anomalArr = anomal.split(",");
							
//							for (String ano : anomalArr) {
							for (int i = 0; i < anomal.length(); i++) {
								// log.info("anomal: {}", ano);
								String ano = anomal.getString(i);
								Integer count = pollingDAO.getAnormalLogsCount(ano, username);
								List<BizLogSummaryVO> anormalLogs = pollingDAO.getAnormalLogs(ano, username);
								
								pollingDAO.insertEmpDetail(userDetail, rule, count);
								
								EmpDetailVO empDetail = pollingDAO.getEmpDetail(username, 5500).get(0);
								
								for (BizLogSummaryVO anormalLog : anormalLogs) {
									//log.info("anormalLog: {}", anormalLog.toString());
									pollingDAO.insertRuleBiz(anormalLog, empDetail, username);
								}
							}
						}
					}
				}
			}
			
			Thread.sleep(1000);
			
			int temp = 1;
			
			// 모든 status code bizwise AND 연산
			for (Integer statusCode : statusCodeList) {
				temp &= statusCode;
			}
			allStatusCodeAnd = temp;
		}
		
		Map<String, String> result = new HashMap<>();
		
		if (allStatusCodeAnd == 1) {
			result.put("Completed", "Yes");
		} else {
			result.put("Completed", "No");
		}
		
		return result;
	}

}
