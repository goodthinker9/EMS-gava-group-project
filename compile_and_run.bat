@echo off
REM ─────────────────────────────────────────────────────────────
REM  Build & run script for the Employee Management System
REM  Requires: JDK 17+, mysql-connector-j jar in lib\
REM ─────────────────────────────────────────────────────────────

set JAR=lib\mysql-connector-j-8.3.0.jar
set SRC_DIR=src
set OUT_DIR=out

echo Compiling...
if not exist %OUT_DIR% mkdir %OUT_DIR%

REM Collect all .java files
for /r %SRC_DIR% %%f in (*.java) do echo %%f >> sources.txt
javac -cp %JAR% -d %OUT_DIR% @sources.txt
del sources.txt

echo Launching EMS...
java -cp "%OUT_DIR%;%JAR%" Main
