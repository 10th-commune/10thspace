#!/bin/bash
# author: luoning
# date: 03/24/2015

cp -a ./lib/log4cxx.properties ./login_server/
cp -a ./lib/libslog.so  ./login_server/
cp -a ./lib/liblog4cxx.so* ./login_server/

cp -a ./lib/log4cxx.properties ./route_server/
cp -a ./lib/libslog.so  ./route_server/
cp -a ./lib/liblog4cxx.so* ./route_server/

cp -a ./lib/log4cxx.properties ./msg_server/
cp -a ./lib/libslog.so  ./msg_server/
cp -a ./lib/liblog4cxx.so* ./msg_server/

cp -a ./lib/log4cxx.properties ./db_proxy_server/
cp -a ./lib/libslog.so  ./db_proxy_server/
cp -a ./lib/liblog4cxx.so* ./db_proxy_server/
