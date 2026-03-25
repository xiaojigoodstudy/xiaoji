@echo off
setlocal
set "_PROJECT_DIR=%~dp0"
pushd "%_PROJECT_DIR%" >nul

if defined XIAOJI_JAVA8_HOME (
  set "JAVA_HOME=%XIAOJI_JAVA8_HOME%"
) else if defined JAVA8_HOME (
  set "JAVA_HOME=%JAVA8_HOME%"
) else if exist "D:\Program Files\Java\jdk1.8.0_161\bin\java.exe" (
  set "JAVA_HOME=D:\Program Files\Java\jdk1.8.0_161"
) else if exist "C:\Program Files\Java\jdk1.8.0_161\bin\java.exe" (
  set "JAVA_HOME=C:\Program Files\Java\jdk1.8.0_161"
) else (
  echo [ERROR] JDK 8 not found.
  echo Set XIAOJI_JAVA8_HOME or JAVA8_HOME to your JDK 8 path.
  popd >nul
  exit /b 1
)

set "PATH=%JAVA_HOME%\bin;%PATH%"
mvn %*
set "_EXIT_CODE=%ERRORLEVEL%"

popd >nul
endlocal & exit /b %_EXIT_CODE%
