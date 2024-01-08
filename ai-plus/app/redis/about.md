# Database 명세

## Install
### [ Cent-OS 7 ]
0. version
	- redis-3.2.12-2.el7.x86_64 
1. ref
	- [ install on centos 7 - personal]( https://sudo-minz.tistory.com/102 )
2. redis package 설치
	- sudo yum install epel-release yum-utils
	- yum install redis
3. redis 서비스 시작 
	- systemctl start redis
	- systemctl enable redis
4. redis 원격 access 설정
	- vi /etc/redis.conf
5. 6379 port Listening 확인
	- ss -an | grep 6379
6. server check
	- redis-cli ping

### [ wsl ]
- ref :     
	- [ install-redis-on-wsl - kotext.tech ]( https://kontext.tech/article/618/install-redis-on-wsl )    

### [ official ]   
- ref :     
	- [ install on centos7 - redis.io ]( https://redis.io/docs/getting-started/installation/install-redis-from-source/ )  


## table -> json

```yml
	key:
	  req_seq : sequence number
	  `seq:[number]` : 
	    desc : 각 데이터 key
	    ex: "{\"seq\": \"1\", \"req-date\": \"20230530 13:44:15\", \"status-code\": \"1\", \"completion-date\": \"20230530 13:44:16\", \"anomal\": [{\"ds\": \"2023-02-03 15:30:00\", \"y\": 3.0}, {\"ds\": \"2023-02-06 09:30:00\", \"y\": 1.0}, {\"ds\": \"2023-02-06 11:00:00\", \"y\": 1.0}]}"

```
```yml
# example 
data:
	seq: 1
	req-data: 230525000157
	res-Data: 230525003011
	status-code: 0
	anomal-data:
	  - data1
	  - data2
```

