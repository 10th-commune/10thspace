#!/bin/bash


build() {
	export CPLUS_INCLUDE_PATH=$PWD/slog
	export LD_LIBRARY_PATH=$PWD/slog
	export LIBRARY_PATH=$PWD/slog:$LIBRARY_PATH


    sudo apt-get -y install cmake
    sudo apt-get -y install libuu-dev 
    sudo apt-get -y install libcurl4-openssl-dev
#    apt-get -y install openssl-devel
    sudo apt-get -y  install libcurl-dev 
    sudo apt-get -y  install liblog4cxx-dev libprotobuf-lite libhiredis-dev protobuf-compiler cmake g++  libprotobuf-dev

	echo "#ifndef __VERSION_H__" > base/version.h
	echo "#define __VERSION_H__" >> base/version.h
	echo "#define VERSION \"$1\"" >> base/version.h
	echo "#endif" >> base/version.h

   	CURPWD=$PWD	


	for i in base slog; 
	do     
		cd $CURPWD/$i
		cmake .
		make
    		if [ $? -eq 0 ]; then
    		    echo "make base and slog successed";
  		  else
       		 echo "make base and slog failed";
       		 exit;
    		fi
	done
	
	cd $CURPWD

    cp base/libbase.a lib/
	mkdir base/slog/lib
    cp slog/slog_api.h base/slog/
    cp slog/libslog.so base/slog/lib/

	for i in route_server msg_server tools db_proxy_server login_server ;
    do     
		cd $CURPWD/$i
		cmake .
		make
    		if [ $? -eq 0 ]; then
    		    echo "make all successed";
  		  else
       		 echo "make failed";
       		 exit;
    		fi
	done

	cd $CURPWD

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
    tar zcvf    $build_name $build_version

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
