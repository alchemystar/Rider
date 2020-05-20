 @echo off
 echo ======================================startup==================================
 set OPTS=-Xms512M -Xmx512M -Xss128k -XX:+AggressiveOpts -XX:+UseParallelGC -XX:NewSize=64M
 set LIBPATH=%Rider_HOME_LIB%
 @ECHO 当要运行的jar设置到CLASSPATH中
 SET CLASSPATH=%CLASSPATH%;%Rider_HOME_LIB%\Rider-1.0-SNAPSHOT.jar
 set ENGINE=%Rider_HOME_LIB%\Rider-1.0-SNAPSHOT.jar
 set CP=%ENGINE%

 echo 输出jar的路径
 echo %CLASSPATH%
 echo ENGINE:%ENGINE%
 set MAIN=alchemystar.engine.server.RiderServer

 set CP=%CP%;%LIBPATH%/*;%CLASSPATH%
 echo ===============================================================================
 echo.
 echo   Engine Startup Environment
 echo   CLASSPATH: %CP%
 echo.
 echo ===============================================================================
 echo.

 @ECHO 运行应用程序
 rem java -cp %CP% %MAIN% %OPTS%  -Drider.home=%Rider_HOME% -Drider.log.home=%Rider_HOME_LOG%  -conf %Rider_HOME_CONFIG%  >> %Rider_HOME_LOG%\console.log

 rem java -cp %CP% %MAIN% %OPTS%  -Drider.home=%Rider_HOME% -Drider.log.home=%Rider_HOME_LOG%  -conf %Rider_HOME_CONFIG%

 @ECHO 配置文件目录
 ECHO %Rider_HOME_CONFIG%
 java -cp %CP% %MAIN% %OPTS%  -Drider.home=%Rider_HOME% -Drider.log.home=%Rider_HOME_LOG% -conf %Rider_HOME_CONFIG%

 pause