@echo off
setlocal
set "_PROJECT_DIR=%~dp0"
pushd "%_PROJECT_DIR%" >nul

if defined XIAOJI_JAVA17_HOME (
  set "JAVA_HOME=%XIAOJI_JAVA17_HOME%"
) else if defined JAVA17_HOME (
  set "JAVA_HOME=%JAVA17_HOME%"
) else if exist "D:\program\java17\bin\java.exe" (
  set "JAVA_HOME=D:\program\java17"
) else if exist "D:\Java\jdk-17\bin\java.exe" (
  set "JAVA_HOME=D:\Java\jdk-17"
) else if exist "C:\Program Files\Java\jdk-17\bin\java.exe" (
  set "JAVA_HOME=C:\Program Files\Java\jdk-17"
) else (
  echo [ERROR] JDK 17 not found.
  echo Set XIAOJI_JAVA17_HOME or JAVA17_HOME to your JDK 17 path.
  popd >nul
  exit /b 1
)

set "PATH=%JAVA_HOME%\bin;%PATH%"
mvn %*
set "_EXIT_CODE=%ERRORLEVEL%"

popd >nul
endlocal & exit /b %_EXIT_CODE%
