cd target
java -Djava.rmi.server.hostname=172.20.10.3 -cp "./lib/jsoup-1.18.3.jar;." googol.Barrel 8003 172.20.10.3 8001 172.20.10.2 
cd ..
