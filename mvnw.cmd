@echo off
if "%JAVA_HOME%"=="" (
  for /f "skip=2 tokens=2*" %%a in ('reg query "HKLM\SOFTWARE\JavaSoft\JDK" /v CurrentVersion 2^>nul') do set "JV=%%b"
  for /f "skip=2 tokens=2*" %%a in ('reg query "HKLM\SOFTWARE\JavaSoft\JDK\%JV%" /v JavaHome 2^>nul') do set "JAVA_HOME=%%b"
)
if "%JAVA_HOME%"=="" if exist "C:\Program Files\Java\jdk-24\bin\java.exe" set "JAVA_HOME=C:\Program Files\Java\jdk-24"
if "%JAVA_HOME%"=="" (
  echo Set JAVA_HOME to your JDK path, e.g. set JAVA_HOME=C:\Program Files\Java\jdk-24
  exit /b 1
)

set "MAVEN_PROJECTBASEDIR=%~dp0"
set "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar"

if not exist "%WRAPPER_JAR%" (
  echo Maven wrapper jar not found at %WRAPPER_JAR%
  exit /b 1
)

"%JAVA_HOME%\bin\java" -classpath "%WRAPPER_JAR%" "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" org.apache.maven.wrapper.MavenWrapperMain %*
exit /b %ERRORLEVEL%
