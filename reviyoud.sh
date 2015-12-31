#!/bin/sh
#
# Startup script for the application deploy
#
# chkconfig: – 50 50
# description: 
# processname: reviyou
#CONFIG_FILE=/opt/collectd/etc/collectd.conf
PID_FILE=/home/alex/app/target/universal/stage/RUNNING_PID
APP_PATH=/home/alex/app
STAGE_PATH=/home/alex/appstage
#CMD_FILE=/opt/collectd/sbin/collectd

case “$1″ in


#start)
# Starts the collectd deamon
#echo “Starting collectd”
#$CMD_FILE -C $CONFIG_FILE
#;;

#stop)
# stops the daemon bt cat’ing the pidfile
#echo “stopping collectd”
#kill -9 `cat $PID_FILE`
#;;

#restart)
## Stop the service regardless of whether it was
## running or not, start it again.
#echo “Restarting collectd”
#$0 stop
#$0 start
#;;

#reload)
# reloads the config file by sending HUP
#echo “Reloading config”
#kill -HUP `cat $PID_FILE`
#;;



deploy)
#stop play server
echo “Killing server”
kill -9 `cat $PID_FILE`
echo “Compiling app”
cd $STAGE_PATH/
play compile
echo “Staging app”
play stage
rm -rf $APP_PATH/*
;;

*)
echo “Usage: collectd (start|stop|restart|reload|deploy)”
exit 1
;;

esac