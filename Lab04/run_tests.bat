@echo off
setlocal
set "JAVA_HOME=C:\Program Files\Java\jre-1.8\jdk-21.0.2"
cd /d d:\Work\WorkSpace\Automatization_with_ProgramProjs_Ivanov\Lab04
call ..\mvnw.cmd test --no-transfer-progress
endlocal
