@echo off
setlocal
set "JAVA_HOME=C:\Program Files\Java\jre-1.8\jdk-21.0.2"
cd /d d:\Work\WorkSpace\Automatization_with_ProgramProjs_Ivanov\Lab04
echo Running PIT mutation coverage...
call ..\mvnw.cmd org.pitest:pitest-maven:mutationCoverage --no-transfer-progress
echo.
echo PIT report saved to: target\pit-reports\index.html
endlocal
