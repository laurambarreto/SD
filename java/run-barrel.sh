cd target/
java -Djava.rmi.server.hostname=192.168.0.192 -cp "./lib/jsoup-1.18.3.jar:." googol.Barrel 8002 192.168.0.192 8001 192.168.0.192
cd ..