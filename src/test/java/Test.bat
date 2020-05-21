@ECHO OFF
@ECHO STARTUP App
@ECHO 设置环境变量,循环当前目录下的lib目录下所有jar文件,并设置CLASSPATH

FOR %%F IN (lib\*.jar) DO call :addcp %%F
goto extlibe
:addcp
SET CLASSPATH=%CLASSPATH%;%1
goto :eof
:extlibe

@ECHO 当要运行的jar设置到CLASSPATH中
SET CLASSPATH=%CLASSPATH%;XXXX-1.0.0.jar
@ECHO 显示CLASSPATH
SET CLASSPATH

@ECHO 运行应用程序
java -server XxxxxMain

pause