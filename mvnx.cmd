@echo off
setlocal EnableDelayedExpansion

if "%~1"=="" goto usage
set "_JDK=%~1"
shift

set "_MAVEN_ARGS="
:collect
if "%~1"=="" goto dispatch
set "_MAVEN_ARGS=!_MAVEN_ARGS! "%~1""
shift
goto collect

:dispatch
if /I "%_JDK%"=="8" (
  call "%~dp0mvn8.cmd" !_MAVEN_ARGS!
  exit /b !ERRORLEVEL!
)

if /I "%_JDK%"=="17" (
  call "%~dp0mvn17.cmd" !_MAVEN_ARGS!
  exit /b !ERRORLEVEL!
)

goto usage

:usage
echo Usage: mvnx.cmd ^<8^|17^> [maven args...]
echo Example: mvnx.cmd 17 clean compile
echo Example: mvnx.cmd 8 -v
exit /b 1
