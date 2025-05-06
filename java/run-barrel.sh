cd target/
java -Djava.rmi.server.hostname=127.0.0.1 -cp "./lib/jsoup-1.18.3.jar:." googol.Barrel 8002 127.0.0.1 8001
cd ..