@echo off
setlocal
set "DIRNAME=%~dp0"
set "APP_BASE_NAME=%~n0"
set "JAVA_EXE=java.exe"
if defined JAVA_HOME set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"

set "MAVEN_JAR=%DIRNAME%.mvn\wrapper\maven-wrapper.jar"
if not exist "%MAVEN_JAR%" (
    echo Downloading Maven Wrapper...
    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; (New-Object Net.WebClient).DownloadFile('https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar', '%MAVEN_JAR%')"
)

"%JAVA_EXE%" -Dmaven.multiModuleProjectDirectory="%DIRNAME%" -jar "%MAVEN_JAR%" %*
