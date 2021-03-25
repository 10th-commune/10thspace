#!/bin/bash
# author: amirline

REDIS_CONF_PATH=/usr/local/etc
REDIS_CONF=redis.conf
REDIS_SERVER_PATH=/usr/local/bin
REDIS_SERVER=redis-server

IM_SERVER_PATH=./im-server-2
CRU_DIR=$(pwd)

function restart_a() {
    cd $IM_SERVER_PATH/$1
    if [ ! -e *.conf  ]
    then
        echo "no config file"
        return
    fi

    if [ -e server.pid  ]; then
        pid=`cat server.pid`
        echo "kill pid=$pid"
        kill $pid
        while true
        do
            oldpid=`ps -ef|grep $1|grep $pid`;
            if [ $oldpid" " == $pid" " ]; then
                echo $oldpid" "$pid
                sleep 1
            else
                break
            fi
        done
        ../daeml ./$1
    else 
        ../daeml ./$1
    fi
}

run_redis() {
	PROCESS=$(pgrep redis)
	if [ -z "$PROCESS" ]; then 
		echo "no redis is running..." 
	else 
		echo "Warning: redis is running"
		return 0
	fi

	$REDIS_SERVER_PATH/$REDIS_SERVER $REDIS_CONF_PATH/$REDIS_CONF
}

run_mariadb() {
    systemctl stop mariadb.service
    systemctl start mariadb.service
}

run_nginx() {
      killall nginx
     /usr/local/nginx/nginx
}

run_php5() {
    killall php-fpm
    /usr/local/php5/sbin/php-fpm
}

run_db_proxy_server() {
        cd $CRU_DIR
        restart_a  db_proxy_server
        sleep 3

	PROCESS=$(pgrep db_proxy_server)
	if [ -z "$PROCESS" ]; then 
	   echo "Try to start db_proxy_server again " 
 	   cd $CRU_DIR
           restart_a  db_proxy_server
	fi
}

run_mariadb
run_redis
run_php5
run_nginx

cd $CRU_DIR
restart_a  login_server
cd $CRU_DIR
restart_a  route_server
cd $CRU_DIR
restart_a  msg_server

#db_proxy_server need more care
run_db_proxy_server

netstat -ntlp





