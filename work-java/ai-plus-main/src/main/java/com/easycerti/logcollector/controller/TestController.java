package com.easycerti.logcollector.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.easycerti.logcollector.dao.PollingDAO;
import com.easycerti.logcollector.dao.UserByTickDAO;
import com.easycerti.logcollector.scheduler.Scheduler;
import com.easycerti.logcollector.util.ConvertJson;
import com.easycerti.logcollector.util.DateUtil;
import com.easycerti.logcollector.util.ExternalAPI;
import com.easycerti.logcollector.util.RequestUtil;
import com.easycerti.logcollector.verification.SaveJSON;
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
 * AI+ 인증 심사에 사용할 Test SpringBoot controller
 * @author 박경일
 *
 */

@Slf4j
@RestController
@RequestMapping("/")
public class TestController {
		
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
	private Scheduler testScheduler;
	
	@Autowired
	private SaveJSON saveJson;
	
	Map<String, UserStatusVO> nameAndData = new HashMap<>();
	
	@Autowired
	private DateUtil dateUtil = new DateUtil();
	
	/**
	 * 로그가 존재하는 모든 사용자 ID 확인
	 * @author 박경일
	 * @return 사용자 ID map
	 * @throws JsonProcessingException
	 */
	@GetMapping("/get/user/list")
	public Map<String, Object> getUserList( ) throws JsonProcessingException {
		
		Map<String, Object> map = new HashMap<>();
		
		List<UserVO> UserLogs = userByTickDAO.getUserList();
		
		map.put( "User List", UserLogs );	
		
		String toJson = mapper.writeValueAsString(UserLogs);
		
		log.info( "[ To JSon ] : {}", toJson );
		log.info("user list count : {}", UserLogs.size() );
		
		
		return map;
		
	}
	
	/**
	 * 특정 사용자의 로그 가져옴
	 * @author 박경일
	 * @param name - 사용자 ID
	 * @return 해당 사용자의 로그
	 * @throws JsonProcessingException
	 */
	@GetMapping("/user/{name}")
	public Map<String, Object> getUserLogs( @PathVariable String name ) throws JsonProcessingException {

		Map<String, Object> mapAsUser = new HashMap<>();
		
		String currentDate = dateUtil.getCurrentDate("yyyyMMdd");
		
		List<AccessLogVO> accessLogs = userByTickDAO.getUserAccessLogList( name, currentDate, -1 );
		
		mapAsUser.put(name, accessLogs );
		
		String toJson = mapper.writeValueAsString( mapAsUser );
		
		log.info( "[ To JSon ] : {}", toJson );
		
		return mapAsUser;
		
	}
	
	/**
	 * 모든 사용자의 로그를 가져옴<br>
	 * 실행 시 과부하가 걸리는 문제로 인해 해당 함수에 접근하지 않는 것을 추천
	 * @author 박경일
	 * @return 모든 로그
	 * @throws JsonProcessingException
	 */
	@GetMapping("/get/users/log/list")
	public Map<String, Object> getLogsByUserList() throws JsonProcessingException {

		Map<String, Object> mapAsUser = new HashMap<>();
		
		String currentDate = dateUtil.getCurrentDate("yyyyMMdd");
		
		List<UserVO> Users = userByTickDAO.getUserList();		
		
		for( UserVO user : Users ) {
			
			List<AccessLogVO> accessLogs = userByTickDAO.getUserAccessLogList( user.getName(), currentDate, -1 );
			
			mapAsUser.put( user.getName(), accessLogs );			
		}
		
		String toJson = mapper.writeValueAsString( mapAsUser );
		
		log.info( "[ To JSon ] : {}", toJson );
		
		return mapAsUser;
		
	}
	
	/**
	 * 어제 날짜의 로그를 가져옴
	 * @author 안상균
	 * @return 어제 날짜의 로그(모든 사용자)
	 * @throws JsonProcessingException
	 */
	@GetMapping("/get/users/log/list/yesterday")
	public Map<String, Object> getLogsByUserListYesterday() throws JsonProcessingException {

		Map<String, Object> mapAsUser = new HashMap<>();
		
		String currentDate = dateUtil.getCurrentDate("yyyyMMdd");
		
		List<UserVO> Users = userByTickDAO.getUserList();		
		
		for( UserVO user : Users ) {
			
			List<AccessLogVO> accessLogs = userByTickDAO.getUserAccessLogList( user.getName(), currentDate, 0 );
			
			mapAsUser.put( user.getName(), accessLogs );			
		}
		
		String toJson = mapper.writeValueAsString( mapAsUser );
		
		log.info( "[ To JSon ] : {}", toJson );
		
		return mapAsUser;
		
	}
	
	/**
	 * 지정한 기간만큼의 로그를 가져옴
	 * @author 안상균
	 * @param days - 지정할 기간
	 * @return 지정한 기간만큼의 로그(모든 사용자)
	 * @throws JsonProcessingException
	 */
	@GetMapping("/get/users/log/list/{days}")
	public Map<String, Object> getLogsByUserListPast(@PathVariable int days) throws JsonProcessingException {

		Map<String, Object> mapAsUser = new HashMap<>();
		
		String currentDate = dateUtil.getCurrentDate("yyyyMMdd");
		
		List<UserVO> Users = userByTickDAO.getUserList();		
		
		for( UserVO user : Users ) {
			
			List<AccessLogVO> accessLogs = userByTickDAO.getUserAccessLogList( user.getName(), currentDate, days );
			
			mapAsUser.put( user.getName(), accessLogs );			
		}
		
		String toJson = mapper.writeValueAsString( mapAsUser );
		
		log.info( "[ To JSon ] : {}", toJson );
		
		return mapAsUser;
		
	}
	
	/**
	 * Request 테스트(사용자 ID 및 날짜 받아옴)<br>
	 * date를 오늘 날짜로 기준을 잡음(yyyyMMdd)
	 * @author 안상균
	 * @param name - User ID
	 * @param date - Selected date
	 * @return String - Result of requesting
	 * @throws JsonProcessingException
	 */
	@GetMapping("/request_data/{name}/{date}")
	public String request_data( @PathVariable String name, @PathVariable String date ) throws JsonProcessingException {
		
		Map<String, Object> mapAsUser = new HashMap<>();
		
		// 3 달치
		List<AccessLogVO> accessLogs = userByTickDAO.getUserAccessLogList( name, date, 90 );
		// today
		List<AccessLogVO> todayLogs = userByTickDAO.getUserAccessLogList( name, date, 0 );
		
		mapAsUser.put("name", name );
		
		// 다양하게 변경해보세요
		if( todayLogs == null || accessLogs == null ) {
			mapAsUser.put("result", "0");
			return String.format("[ %s logs is empty", name );
		} else {
			mapAsUser.put("result", "1");
		}
		// train 할 log ( 어제부터 3달 )
		mapAsUser.put("logs", accessLogs );
		// predict 할 log ( 오늘 날짜 )		
		mapAsUser.put("today_logs", todayLogs );
		
		String toJson = mapper.writeValueAsString( mapAsUser );
		saveJson.saveToFile(toJson);
		
		String result;
//		log.info("toJson : {}", toJson);
		
		result = requestUtil.restRequest( externalAPI.getUrl_req(), toJson );
		
		try {
			log.info("\n-------------------------------- User --------------------------------\n"
					+ "  id             | " + name + "\n"
					+ "------------------------------ Request data -------------------------\n"
					+ "  req-date       | " + convertJson.paramMap(result).get( "req-date" ) + "\n"
					+ "  seq            | " + convertJson.paramMap(result).get( "seq" ) + "\n"
					+ "  status-code    | " + convertJson.paramMap(result).get( "status-code" ) + "\n"
					+ "---------------------------------------------------------------------");
			String seq =  convertJson.paramMap(result).get( "seq" );
			log.info( "seq : {}", seq );
			
			nameAndData.put(name, new UserStatusVO(seq, "0"));
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
		}
		
		return result;
	}
	
	/**
	 * Request 테스트(사용자 ID 받아옴)
	 * @author 박경일
	 * @param name
	 * @return String - Result of requesting
	 * @throws JsonProcessingException
	 */
	@GetMapping("/request_data/{name}")
	public String request_data( @PathVariable String name ) throws JsonProcessingException {
		
		Map<String, Object> mapAsUser = new HashMap<>();
		
		String currentDate = dateUtil.getCurrentDate("yyyyMMdd");
//		String currentDate = "20230619";
		
		// 3 달치
		List<AccessLogVO> accessLogs = userByTickDAO.getUserAccessLogList( name, currentDate, 90 );
		// today
		List<AccessLogVO> todayLogs = userByTickDAO.getUserAccessLogList( name, currentDate, 0 );
		
		mapAsUser.put("name", name );
		
		// 다양하게 변경해보세요
		if( todayLogs == null || accessLogs == null || accessLogs.size() <= 2 ) {
			mapAsUser.put("result", "0");
			return String.format("[ %s logs is empty", name );
		} else {
			mapAsUser.put("result", "1");
		}
		// train 할 log ( 어제부터 3달 )
		mapAsUser.put("logs", accessLogs );
		// predict 할 log ( 오늘 날짜 )		
		mapAsUser.put("today_logs", todayLogs );
		
		String toJson = mapper.writeValueAsString( mapAsUser );
		saveJson.saveToFile(toJson);
		
		String result;
//		log.info("toJson : {}", toJson);
		
		result = requestUtil.restRequest( externalAPI.getUrl_req(), toJson );
		
		try {
			log.info("\n-------------------------------- User --------------------------------\n"
					+ "  id             | " + name + "\n"
					+ "------------------------------ Request data -------------------------\n"
					+ "  req-date       | " + convertJson.paramMap(result).get( "req-date" ) + "\n"
					+ "  seq            | " + convertJson.paramMap(result).get( "seq" ) + "\n"
					+ "  status-code    | " + convertJson.paramMap(result).get( "status-code" ) + "\n"
					+ "---------------------------------------------------------------------");
			String seq =  convertJson.paramMap(result).get( "seq" );
			log.info( "seq : {}", seq );
			
			nameAndData.put(name, new UserStatusVO(seq, "0"));
			
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
		}
		
		return result;
	}
	
	/**
	 * 선택한 Sequence number로 polling 진행<br>
	 * 2 차 anomal 분석이 끝난 data 요청<br>
	 * status-code 가 1 인 경우 : 분석 완료<br>
	 * status-code 가 0 인 경우 : 분석중
	 * @author 박경일
	 * @param seq - Sequence number
	 * @return String
	 * @throws JsonProcessingException
	 */
	@GetMapping("/polling/{seq}")
	public String polling_get_data( @PathVariable String seq ) throws JsonProcessingException {
		
		Map<String, Object> mapAsUser = new HashMap<>();
		
		mapAsUser.put("seq", seq  );
		
		String toJson = mapper.writeValueAsString( mapAsUser );
		
		String result = "";
//		log.info("{}", toJson);
		
		final long startTime = System.currentTimeMillis();
		final long limitTime = 10 * 60 * 1000; // polling 제한시간(millisecond 단위): 10분
		
		while (true) {
			long presentTime = System.currentTimeMillis() - startTime; // 현재 경과 시간
			// 제한 시간 초과 시 break
			if (presentTime > limitTime) {
				break;
			}
			
			result = requestUtil.restRequest( externalAPI.getUrl_polling(), toJson );
			String status;
			try {
				status = convertJson.paramMap(result).get( "status-code" );
				if (status.equals("1")) {
					break;
				}
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
//		log.info("Result - polling: {}", result);
		
		try {
			String status =  convertJson.paramMap(result).get( "status-code" );
//			String anomal;
			String username = "";
			for (String key : nameAndData.keySet()) {
				if (nameAndData.get(key).getSeq().equals(seq)) {
					username = key;
					break;
				}
			}
//			final String date = "20230620";
			String date = dateUtil.getCurrentDate("yyyyMMdd");
			UserDetailVO userDetail = pollingDAO.getUserDetail(username, date).get(0);
			
			RuleTblVO rule = pollingDAO.getRule(5500).get(0);
//			log.info( "status : {}", status );
			if (Integer.valueOf( status ) == 0) {
				log.info("\n-------------------------------- User --------------------------------\n"
						+ "  id                 | " + username + "\n"
						+ "-------------------------------- Polling ----------------------------\n"
						+ "  req-date           | " + convertJson.paramMap(result).get( "req-date" ) + "\n"
						+ "  seq                | " + convertJson.paramMap(result).get( "seq" ) + "\n"
						+ "  status-code        | " + convertJson.paramMap(result).get( "status-code" ) + "\n"
						+ "---------------------------------------------------------------------");
			}
			if( Integer.valueOf( status ) != 0 ) {
//				anomal =  convertJson.paramMap(result).get( "anomal" );
				JSONArray anomal = convertJson.getAnomals(result);
//				log.info( "anomal : {}", anomal );
				log.info("\n-------------------------------- User --------------------------------\n"
						+ "  id                 | " + username + "\n"
						+ "-------------------------------- Polling ----------------------------\n"
						+ "  req-date           | " + convertJson.paramMap(result).get( "req-date" ) + "\n"
						+ "  seq                | " + convertJson.paramMap(result).get( "seq" ) + "\n"
						+ "  status-code        | " + convertJson.paramMap(result).get( "status-code" ) + "\n"
						+ "  completion-date    | " + convertJson.paramMap(result).get( "completion-date" ) + "\n"
						+ "  anomal             | " + anomal + "\n"
						+ "---------------------------------------------------------------------");
				
//				if (!anomal.equals("[]")) {
				if (anomal.length() != 0) {
//					anomal = anomal.replace("[", "").replace("]", "");
//					String[] anomalArr = anomal.split(",");
					
//					for (String ano : anomalArr) {
					for (int i = 0; i < anomal.length(); i++) {
						String ano = anomal.getString(i);
//						String ano = anoObj.toString();
						log.info("anomal: {}", ano);
						Integer count = pollingDAO.getAnormalLogsCount(ano, username);
						List<BizLogSummaryVO> anormalLogs = pollingDAO.getAnormalLogs(ano, username);
						
						pollingDAO.insertEmpDetail(userDetail, rule, count);
						
						EmpDetailVO empDetail = pollingDAO.getEmpDetail(username, 5500).get(0);
						
						for (BizLogSummaryVO anormalLog : anormalLogs) {
							pollingDAO.insertRuleBiz(anormalLog, empDetail, username);
						}
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
		}
		
		return result;
	}
	
	/**
	 * 모든 사용자에 대해 Request ~ polling 진행<br>
	 * 인증 심사 시 아래의 execute2 사용 
	 * @author 안상균
	 * @return String
	 */
	@GetMapping("/execute_old")
	public String execute() {
		return testScheduler.doExecute();
	}
	
	/**
	 * 모든 사용자에 대해 Request ~ polling 진행<br>
	 * 한 user의 request 및 polling 후 status code가 1이 될 시 다음 user 진행
	 * @author 안상균
	 * @return String
	 */
	@GetMapping("/execute")
	public String execute2() {
		// 로그 있는 모든 사용자 ID 가져오기
		List<String> userNames = new ArrayList<>();
		List<UserVO> userLogs = userByTickDAO.getUserList();
		
		for (UserVO vo : userLogs) {
			userNames.add(vo.getName());
		}
		
		// 모든 user에 대해서 request 및 polling
		for (String userName : userNames) {
			try {
				String requestResult = request_data(userName);
				String seq = convertJson.paramMap(requestResult).get( "seq" );
				
				// status code가 1이 될 때 까지 polling
				log.info("User {} is started. (Sequence number: {})", userName, seq);
				String pollingResult = polling_get_data(seq);
				log.info("User {} is completed.", userName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return "All users completed!";
	}
	
	/**
	 * 모든 사용자에 대해 Request ~ polling 진행<br>
	 * date를 오늘 날짜로 기준을 잡음(yyyyMMdd)
	 * 한 user의 request 및 polling 후 status code가 1이 될 시 다음 user 진행
	 * @author 안상균
	 * @return String
	 */
	@GetMapping("/execute/{date}")
	public String execute2(@PathVariable String date) {
		// 로그 있는 모든 사용자 ID 가져오기
		List<String> userNames = new ArrayList<>();
		List<UserVO> userLogs = userByTickDAO.getUserList();
		
		for (UserVO vo : userLogs) {
			userNames.add(vo.getName());
		}
		
		// 모든 user에 대해서 request 및 polling
		for (String userName : userNames) {
			try {
				String requestResult = request_data(userName, date);
				String seq = convertJson.paramMap(requestResult).get( "seq" );
				
				// status code가 1이 될 때 까지 polling
				log.info("User {} is started. (Sequence number: {})", userName, seq);
				String pollingResult = polling_get_data(seq);
				log.info("User {} is completed.", userName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return "All users completed!";
	}
	
	/**
	 * 지정된 임의의 사용자에 대해 Request ~ polling 진행<br>
	 * date를 오늘 날짜로 기준을 잡음(yyyyMMdd)
	 * 한 user의 request 및 polling 후 status code가 1이 될 시 다음 user 진행
	 * @author 박경일
	 * @return String
	 */
	@GetMapping("/execute/{name}/{date}")
	public String execute3( @PathVariable String name, @PathVariable String date) {
		// 로그 있는 모든 사용자 ID 가져오기
		List<String> userNames = new ArrayList<>();
		List<UserVO> userLogs = userByTickDAO.getUserList();
		
		for (UserVO vo : userLogs) {
			userNames.add(vo.getName());
		}
		
		try {
			String requestResult = request_data(name, date);
			String seq = convertJson.paramMap(requestResult).get( "seq" );
			
			// status code가 1이 될 때 까지 polling
			log.info("User {} is started. (Sequence number: {})", name, seq);
			String pollingResult = polling_get_data(seq);
			log.info("User {} is completed.", name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return String.format( " %s user completed!", name );
	}
	
}
