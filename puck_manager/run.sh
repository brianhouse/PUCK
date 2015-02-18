echo "Compiling..."
javac -Xlint:deprecation -g -classpath "lib/log4j.jar:lib/RXTXcomm.jar:lib/jedis-2.0.0.jar:lib/commons-pool-1.6.jar:lib/snakeyaml-1.10.jar" -sourcepath "src/" PuckManager.java -d "build/"
rc=$?
if [[ $rc != 0 ]] ; then
    exit $rc
fi
echo "done."
java -classpath "lib/log4j.jar:lib/RXTXcomm.jar:lib/jedis-2.0.0.jar:lib/commons-pool-1.6.jar:build:lib/snakeyaml-1.10.jar" PuckManager
