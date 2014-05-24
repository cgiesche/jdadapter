@echo off

REM ############################ S E T T I N G S ###################################
set username=username
set password=password
set synoUri=http://192.168.178.30:5000/download/download_redirector.cgi
REM ################################################################################

java -DsynoUri="%synoUri%" -Dpassword="%password%" -Dusername="%username%" -jar winstone-0.9.10.jar --httpPort=9666 --warfile=JdAdapter.war