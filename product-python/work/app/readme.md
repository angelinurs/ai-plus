# status check redis
## [ Cent-OS 7 ]
0. version redis-3.2.12-2.el7.x86_64 
1. redis package 설치
  - sudo yum install epel-release yum-utils
  - yum install redis
3. redis 서비스 시작 
   - systemctl start redis
  - systemctl enable redis
5. redis 원격 access 설정
   - vi /etc/redis.conf
6. 6379 port Listening 확인
   - ss -an | grep 6379
7. server check
   - redis-cli ping
   output >> pong

# How to install pip packages
> `$ python -m pip install -r ./requirements.txt`

# How to run
> `$ ./autorun.sh`
