#!/bin/bash
CURRENT_DIR="${PWD}"

if [ -d "${CURRENT_DIR}/../conf/" ]; then
    pid=`ps -ef | grep rider | grep -v grep | awk '{print $2}'`

    if [ ! -z $pid ]; then
    		echo -e "\033[33m Killing old process ${pid} \033[0m"
            kill $pid
    		sleep 5s
            ps -ef | grep rider | grep -v grep | awk '{print $2}'|while read line
            do
              kill -9 $line
            done
    	fi
    echo -e "\033[33m Starting Rider using ${CURRENT_DIR}/../conf \033[0m"
	mkdir -p ~/var/rider/logs/
	sh ${CURRENT_DIR}/startup.sh -c ${CURRENT_DIR}/../conf -l ~/var/rider/logs
else
	echo -e "\033[31m Config NOT found!! \033[0m"
	exit -1
fi