
#cmake会将so打包进apk中，需要用的在apk找出相应的so导入到别的项目
cmake_minimum_required(VERSION 3.4.1)

#对于互相独立的cpp文件应该分开生成so文件，如果相互依赖的就可以用file()(上面注释掉的语句)一起生成同一个so文件

 set(CAMERA_SOURCE    src/main/cpp/lang.h
                      src/main/cpp/log.h
                      src/main/cpp/camera_util.h
                      src/main/cpp/camera_util.cpp )

#添加头文件目录
#INCLUDE_DIRECTORIES()是用来定义非标准库头文件要搜索的路径

add_library(camera_util SHARED ${CAMERA_SOURCE})

#添加外部动态链接库
target_link_libraries(camera_util  log jnigraphics )
