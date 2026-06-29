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

@rem Remove trailing backslash to prevent Java arg parsing issue on Windows
set "PROJECT_DIR=%DIRNAME%"
if "%PROJECT_DIR:~-1%"=="\" set "PROJECT_DIR=%PROJECT_DIR:~0,-1%"

"%JAVA_EXE%" -Dmaven.multiModuleProjectDirectory="%PROJECT_DIR%" -classpath "%MAVEN_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
