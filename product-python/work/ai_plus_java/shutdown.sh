#!/bin/sh
AIPLUS_PATH="./ai_plus_java"
#cd ${AIPLUS_PATH}

pid=`ps -ef | grep "aiplus.jar" | grep -v grep | awk '{print $2}'`

if [ "" == "$pid" ]
then
    echo "NO AI Plus !!"
else
    kill $pid

    echo "KILL AI Plus [$pid]!!!"
fi

