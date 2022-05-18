echo off
echo "begin core compile"

set PATH=%JAVA_HOME%\bin;%PATH%
if not exist bin mkdir bin

for /r %%a in (*.java) do (
javac -sourcepath src -d bin -Xlint:unchecked -Xlint:deprecation %%a
if errorlevel 1 goto ERROR
)
pause

jar cf core.jar bin\*.class

@echo "end core compile"

:ERROR
pause
exit
