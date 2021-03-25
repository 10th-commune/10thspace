#!/bin/bash
# author: luoning
# date: 03/24/2015

cp slog/log4cxx.properties ../run/login_server/
cp slog/libslog.so  ../run/login_server/
cp slog/lib/liblog4cxx.so.* ../run/login_server/

cp slog/log4cxx.properties ../run/route_server/
cp slog/libslog.so  ../run/route_server/
cp slog/lib/liblog4cxx.so.* ../run/route_server/

cp slog/log4cxx.properties ../run/msg_server/
cp slog/libslog.so  ../run/msg_server/
cp slog/lib/liblog4cxx.so.* ../run/msg_server/


cp slog/log4cxx.properties ../run/db_proxy_server/
cp slog/libslog.so  ../run/db_proxy_server/
cp slog/lib/liblog4cxx.so.* ../run/db_proxy_server/
