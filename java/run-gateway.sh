cd target/
java -Djava.rmi.server.hostname=127.0.0.1 -cp "./lib/jsoup-1.18.3.jar:." googol.Gateway 8001
cd ..