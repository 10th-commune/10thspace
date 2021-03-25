# Space3 Server环境配置

**Center OS 7环境配置**
# 按以下步骤安装
- 安装gcc4.9.2
执行目录auto_setup\gcc_setup中的：./gcc_setup.sh
- 安装mariadb
执行目录auto_setup\mariadb中的: ./setup.sh，安装过程中设置数据库默认密码为：12345
- 编译并安装protobuf-3.12.3
源码包位于:src\third\protobuf目录下,执行以下命令：
    >>tar -zxvf protobuf-3.12.3.tar.gz
    >>cd protobuf-3.12.3
    >>./autogen.sh
    >>./configure --prefix=/usr/local/protobuf
    >>make
    >>make install
- 使用protoc编译proto文件，进入./pb目录：
执行：./create.sh ./sync.sh
- 编译源码，进入src目录：
执行：./build.sh version 1 

# 按以下步骤运行
-执行./build.sh version 1之后，直接部署在/run/im-server-1目录下，进入对应的服务目录直接运行进程即可：
