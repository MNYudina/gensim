java -server -XX:+DoEscapeAnalysis -XX:+AggressiveOpts -Xms2048m -Xmx2048m -jar target/GraphStats-1.2.jar

java -server -XX:+DoEscapeAnalysis -XX:+AggressiveOpts -Xms2048m -Xmx2048m -jar target/GraphStats-1.2.jar -f graphs/p2p-Gnutella08.net -op dr -t 4
java -server -XX:+DoEscapeAnalysis -XX:+AggressiveOpts -Xms2048m -Xmx2048m -jar target/GraphStats-1.2.jar -f graphs/p2p-Gnutella31.net -op dr,3scfe,3scs,4scfe,4scs -r 100000 -t 4
java -server -XX:+DoEscapeAnalysis -XX:+AggressiveOpts -Xms2048m -Xmx2048m -jar target/GraphStats-1.2.jar -f graphs/roadNet-PA.net -op 3scfe,3scs,4scfe,4scs -r 100000 -t 4
java -server -XX:+DoEscapeAnalysis -XX:+AggressiveOpts -Xms2048m -Xmx2048m -jar target/GraphStats-1.2.jar -f graphs/com-dblp.ungraph.net -op 3scfe,3scs,4scfe,4scs -r 100000 -t 4