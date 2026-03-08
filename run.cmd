@echo off
REM Set JAVA_HOME if not set (adjust path to your JDK)
if "%JAVA_HOME%"=="" (
  for /f "skip=2 tokens=2*" %%a in ('reg query "HKLM\SOFTWARE\JavaSoft\JDK" /v CurrentVersion 2^>nul') do set "JV=%%b"
  for /f "skip=2 tokens=2*" %%a in ('reg query "HKLM\SOFTWARE\JavaSoft\JDK\%JV%" /v JavaHome 2^>nul') do set "JAVA_HOME=%%b"
)
if "%JAVA_HOME%"=="" (
  echo Please set JAVA_HOME to your JDK path.
  pause
  exit /b 1
)

cd /d "%~dp0"
if not exist .mvn\wrapper\maven-wrapper.jar (
  echo Run: mvn -N wrapper:wrapper
  pause
  exit /b 1
)

"%JAVA_HOME%\bin\java" -classpath ".mvn\wrapper\maven-wrapper.jar" "-Dmaven.multiModuleProjectDirectory=%CD%" org.apache.maven.wrapper.MavenWrapperMain cargo:run
