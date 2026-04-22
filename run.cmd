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

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0run-with-ai.ps1"
if errorlevel 1 (
  echo.
  echo run-with-ai.ps1 failed.
  pause
  exit /b 1
)
