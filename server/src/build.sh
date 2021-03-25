#!/bin/bash

build() {
    yum -y install cmake
    yum -y install libuuid-devel
    yum -y install openssl-devel
    yum -y install curl-devel

	echo "#ifndef __VERSION_H__" > base/version.h
	echo "#define __VERSION_H__" >> base/version.h
	echo "#define VERSION \"$1\"" >> base/version.h
	echo "#endif" >> base/version.h

    cd ./third
    chmod u+x *.sh
    ./make_hiredis.sh
    ./make_log4cxx.sh
    ./make_protobuf.sh
    cd ../
    if [ ! -d lib ]
    then
        mkdir lib
    fi

	cd base
    cmake .
	make
    if [ $? -eq 0 ]; then
        echo "make base successed";
    else
        echo "make base failed";
        exit;
    fi
    if [ -f libbase.a ]
    then
        cp libbase.a ../lib/
    fi
    cd ../slog
    cmake .
    make
    if [ $? -eq 0 ]; then
        echo "make slog successed";
    else
        echo "make slog failed";
        exit;
    fi
    mkdir ../base/slog/lib
    cp slog_api.h ../base/slog/
    cp libslog.so ../base/slog/lib/
    cp -a lib/liblog4cxx* ../base/slog/lib/

    cd ../login_server
    cmake .
	make
    if [ $? -eq 0 ]; then
        echo "make login_server successed";
    else
        echo "make login_server failed";
        exit;
    fi

	cd ../route_server
    cmake .
	make
    if [ $? -eq 0 ]; then
        echo "make route_server successed";
    else
        echo "make route_server failed";
        exit;
    fi

	cd ../msg_server
    cmake .
	make
    if [ $? -eq 0 ]; then
        echo "make msg_server successed";
    else
        echo "make msg_server failed";
        exit;
    fi



    cd ../tools
    make
    if [ $? -eq 0 ]; then
        echo "make tools successed";
    else
        echo "make tools failed";
        exit;
    fi

    cd ../db_proxy_server
    cmake .
    make
    if [ $? -eq 0 ]; then
        echo "make db_proxy_server successed";
    else
        echo "make db_proxy_server failed";
        exit;
    fi


	cd ../

    mkdir -p ../run/login_server
    mkdir -p ../run/route_server
    mkdir -p ../run/msg_server
    mkdir -p ../run/db_proxy_server

	#copy executables to run/ dir
	cp bin/login_server ../run/login_server/

	cp bin/route_server ../run/route_server/

	cp bin/msg_server ../run/msg_server/

    cp bin/db_proxy_server ../run/db_proxy_server/

    cp bin/daeml ../run/

    build_version=im-server-$1
    build_name=$build_version.tar.gz
	if [ -e "$build_name" ]; then
		rm $build_name
	fi
    mkdir -p ../$build_version
    mkdir -p ../$build_version/login_server
    mkdir -p ../$build_version/route_server
    mkdir -p ../$build_version/msg_server
    mkdir -p ../$build_version/db_proxy_server
    mkdir -p ../$build_version/lib

    cp login_server/loginserver.conf ../$build_version/login_server/
    cp bin/login_server ../$build_version/login_server/

    cp bin/route_server ../$build_version/route_server/
    cp route_server/routeserver.conf ../$build_version/route_server/

    cp bin/msg_server ../$build_version/msg_server/
    cp msg_server/msgserver.conf ../$build_version/msg_server/

    cp bin/db_proxy_server ../$build_version/db_proxy_server/
    cp db_proxy_server/dbproxyserver.conf ../$build_version/db_proxy_server/

    cp slog/log4cxx.properties ../$build_version/lib/
    cp slog/libslog.so  ../$build_version/lib/
    cp -a slog/lib/liblog4cxx.so* ../$build_version/lib/
    cp sync_lib_for_zip.sh ../$build_version/

    cp bin/daeml ../$build_version/
    cp ../run/restart.sh ../$build_version/

    cd ../
    tar zcvf $build_name $build_version
    rm -rf $build_version
    tar zxvf $build_name -C run/
    cd run/$build_version
    chmod u+x sync_lib_for_zip.sh
    ./sync_lib_for_zip.sh
    cd ../
    rm -rf db_proxy_server
    rm -rf msg_server
    rm -rf login_server
    rm -rf route_server


}

clean() {
	cd base
	make clean
	cd ../login_server
	make clean
	cd ../route_server
	make clean
	cd ../msg_server
	make clean
	cd ../db_proxy_server
	make clean
}

print_help() {
	echo "Usage: "
	echo "  $0 clean --- clean all build"
	echo "  $0 version version_str --- build a version"
}

case $1 in
	clean)
		echo "clean all build..."
		clean
		;;
	version)
		if [ $# != 2 ]; then 
			echo $#
			print_help
			exit
		fi

		echo $2
		echo "build..."
		build $2
		;;
	*)
		print_help
		;;
esac
