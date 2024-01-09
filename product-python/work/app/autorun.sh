#!/bin/bash

APP_NAME=gunicorn

if [ ! -d logs ];then

    mkdir logs

fi


FLASK_PID=`ps -aux | grep gunicorn | grep -v grep | awk '{print $2}'`
# FLASK_PID=`ps -au | grep gunicorn | grep -v grep | awk '{print $2}'`


if [ "${FLASK_PID}" ];then

    kill $FLASK_PID

    echo "restart ..."

    echo "gunicorn restart" >> ./logs/$APP_NAME.log

    sleep 1

else

    echo "start ..."

    echo "gunicorn start ..." >> ./logs/$APP_NAME.log

fi


sleep 1


python -m gunicorn app:app -b 0.0.0.0:5000 -w 6 --threads=10 --timeout=360 -k gevent >> ./logs/$APP_NAME.log &
