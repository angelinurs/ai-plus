#!/bin/sh
nohup java -Djava.net.preferIPv4Stack=true -jar ./aiplus.jar > aiplus_logs/startup.log &
