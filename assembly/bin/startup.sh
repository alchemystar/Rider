#!/bin/sh
usage() {
	echo "Usage:"
	echo "    startup.sh [-c config_folder] [-l log_folder] [-d debug mode] [-h]"
	echo "Description:"
	echo "    config_folder - config folder path, not must,if empty,default classpath"
	echo "    log_folder - rider server's logs base folder, /home/work  log path: /home/work/logs"
	echo "    debug_mode - 1|0  1 means debug port is open, 0 close ,default 0"
	echo "    -h - show this help"
	exit -1
}

source ~/.bash_profile

LOG_BASE_DIR=""
CONFIG_DIR=""
DEBUG_MODE="0";

while getopts "h:l:c:d:" arg
do
	case $arg in
	    l) LOG_BASE_DIR=$OPTARG;;
		c) CONFIG_DIR=$OPTARG;;
		d) DEBUG_MODE=$OPTARG;;
		h) usage;;
		?) usage;;
	esac
done

echo "Rider"
echo 'Copyright (c) 2000-2017 All Rights Reserved.'
echo 'Distributed'
echo "alchemystar@163.com"

SOFT_DIR="${HOME}/soft"

if [ -d ${SOFT_DIR}/java ]; then
	export JAVA_HOME=${SOFT_DIR}/java
fi

#==============================================================================
#set HOME
CURR_DIR=`pwd`
cd `dirname "$0"`/..

RIDER_HOME=`pwd`
if [ ! -z "$LOG_BASE_DIR" ] ; then
  if [ ! -d $LOG_BASE_DIR/logs ] ; then
     mkdir -p $LOG_BASE_DIR/logs
  fi
fi
cd $CURR_DIR
if [ -z "RIDER_HOME" ] ; then
    echo
    echo "Error: RIDER_HOME environment variable is not defined correctly."
    echo
    exit 1
fi

LOG_HOME="RIDER_HOME/logs"
if [ ! -z "$LOG_BASE_DIR" ] ; then
  LOG_HOME="$LOG_BASE_DIR/logs"
fi

#set JAVA_OPTS
JAVA_OPTS="-server -Xms2048m -Xmx2048m -Xmn768m -Xss512k -XX:PermSize=256m -XX:MaxPermSize=256m"
#performance Options
#JAVA_OPTS="$JAVA_OPTS -XX:+AggressiveOpts"
#JAVA_OPTS="$JAVA_OPTS -XX:+UseBiasedLocking"
#JAVA_OPTS="$JAVA_OPTS -XX:+UseFastAccessorMethods"
JAVA_OPTS="$JAVA_OPTS -XX:+CMSClassUnloadingEnabled -XX:+ExplicitGCInvokesConcurrent"
JAVA_OPTS="$JAVA_OPTS -XX:+UseParNewGC"
JAVA_OPTS="$JAVA_OPTS -XX:+UseConcMarkSweepGC"
JAVA_OPTS="$JAVA_OPTS -XX:+UseCMSCompactAtFullCollection"
JAVA_OPTS="$JAVA_OPTS -XX:+UseCMSInitiatingOccupancyOnly"
JAVA_OPTS="$JAVA_OPTS -XX:CMSInitiatingOccupancyFraction=75"
JAVA_OPTS="$JAVA_OPTS -Dsun.net.inetaddr.ttl=5"
#GC Log Options
JAVA_OPTS="$JAVA_OPTS -Xloggc:$LOG_HOME/gc.log"
JAVA_OPTS="$JAVA_OPTS -XX:+PrintGCApplicationStoppedTime"
JAVA_OPTS="$JAVA_OPTS -XX:+PrintGCDateStamps"
JAVA_OPTS="$JAVA_OPTS -XX:+PrintGCDetails"
#debug Options
if [ "$DEBUG_MODE"x == "1"x ] ; then
JAVA_OPTS="$JAVA_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,address=8065,server=y,suspend=n"
fi
#==============================================================================


#=============================================================================
#set CLASSPATH
RIDER_CLASSPATH="$RIDER_HOME/conf:$RIDER_HOME/lib"
for i in "$RIDER_HOME"/lib/*.jar
do
    RIDER_CLASSPATH="$RIDER_CLASSPATH:$i"
done

#startup Server
RUN_CMD="$JAVA_HOME/bin/java"
RUN_CMD="$RUN_CMD -Drider.home=$RIDER_HOME"
if [ ! -z "$LOG_BASE_DIR" ] ; then
  RUN_CMD="$RUN_CMD -Drider.log.home=$LOG_BASE_DIR"
fi

RUN_CMD="$RUN_CMD -classpath $RIDER_CLASSPATH"
RUN_CMD="$RUN_CMD $JAVA_OPTS"
RUN_CMD="$RUN_CMD alchemystar.engine.server.RiderServer"
if [ ! -z "$CONFIG_DIR" ] ; then
  RUN_CMD="$RUN_CMD -conf $CONFIG_DIR"
fi
RUN_CMD="$RUN_CMD $@"
RUN_CMD="$RUN_CMD >> $RIDER_HOME/logs/console.log 2>&1 &"
echo $RUN_CMD
eval $RUN_CMD
echo -e "\033[33m Rider startup OK! \033[0m"
#==============================================================================