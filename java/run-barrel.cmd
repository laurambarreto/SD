cd target
java -Djava.rmi.server.hostname=192.168.0.174 -cp "./lib/jsoup-1.18.3.jar;." googol.Barrel 8002 192.168.0.174 8000 192.168.0.174
cd ..
