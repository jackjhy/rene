#!/bin/bash

MAX_HEAP_SIZE="1024M"
HEAP_NEWSIZE="256M"

if [ "x$RENE_HOME" = "x" ]; then
    RENE_HOME="`dirname "$0"`/.."
    RENE_HOME="`pwd`/$RENE_HOME"
fi

properties="$properties -Drene.home=$RENE_HOME"

# Use JAVA_HOME if set, otherwise look for java in PATH
if [ -x "$JAVA_HOME/bin/java" ]; then
    JAVA="$JAVA_HOME/bin/java"
else
    JAVA="`which java`"
fi

# Determine the sort of JVM we'll be running on.

java_ver_output=`"${JAVA:-java}" -version 2>&1`

jvmver=`echo "$java_ver_output" | awk -F'"' 'NR==1 {print $2}'`
JVM_VERSION=${jvmver%_*}
JVM_PATCH_VERSION=${jvmver#*_}

jvm=`echo "$java_ver_output" | awk 'NR==2 {print $1}'`
case "$jvm" in
    OpenJDK)
        JVM_VENDOR=OpenJDK
        # this will be "64-Bit" or "32-Bit"
        JVM_ARCH=`echo "$java_ver_output" | awk 'NR==3 {print $2}'`
        ;;
    "Java(TM)")
        JVM_VENDOR=Oracle
        # this will be "64-Bit" or "32-Bit"
        JVM_ARCH=`echo "$java_ver_output" | awk 'NR==3 {print $3}'`
        ;;
    *)
        # Help fill in other JVM values
        JVM_VENDOR=other
        JVM_ARCH=unknown
        ;;
esac

for jar in "$RENE_HOME"/lib/*.jar; do
    CLASSPATH="$CLASSPATH:$jar"
done

JVM_OPTS="$JVM_OPTS -ea"

# enable thread priorities, primarily so we can give periodic tasks
# a lower priority to avoid interfering with client workload
JVM_OPTS="$JVM_OPTS -XX:+UseThreadPriorities"
# allows lowering thread priority without being root.  see
# http://tech.stolsvik.com/2010/01/linux-java-thread-priorities-workaround.html
JVM_OPTS="$JVM_OPTS -XX:ThreadPriorityPolicy=42"

# min and max heap sizes should be set to the same value to avoid
# stop-the-world GC pauses during resize, and so that we can lock the
# heap in memory on startup to prevent any of it from being swapped
# out.
JVM_OPTS="$JVM_OPTS -Xms${MAX_HEAP_SIZE}"
JVM_OPTS="$JVM_OPTS -Xmx${MAX_HEAP_SIZE}"
JVM_OPTS="$JVM_OPTS -Xmn${HEAP_NEWSIZE}"
JVM_OPTS="$JVM_OPTS -XX:+HeapDumpOnOutOfMemoryError"

# set jvm HeapDumpPath with CASSANDRA_HEAPDUMP_DIR
if [ "x$RENE_HEAPDUMP_DIR" != "x" ]; then
    JVM_OPTS="$JVM_OPTS -XX:HeapDumpPath=$RENE_HEAPDUMP_DIR/rene-`date +%s`-pid$$.hprof"
fi

# Per-thread stack size.
JVM_OPTS="$JVM_OPTS -Xss256k"

# Larger interned string table, for gossip's benefit (CASSANDRA-6410)
JVM_OPTS="$JVM_OPTS -XX:StringTableSize=1000003"

# GC tuning options
JVM_OPTS="$JVM_OPTS -XX:+UseParNewGC"
JVM_OPTS="$JVM_OPTS -XX:+UseConcMarkSweepGC"
JVM_OPTS="$JVM_OPTS -XX:+CMSParallelRemarkEnabled"
JVM_OPTS="$JVM_OPTS -XX:SurvivorRatio=8"
JVM_OPTS="$JVM_OPTS -XX:MaxTenuringThreshold=1"
JVM_OPTS="$JVM_OPTS -XX:CMSInitiatingOccupancyFraction=75"
JVM_OPTS="$JVM_OPTS -XX:+UseCMSInitiatingOccupancyOnly"
JVM_OPTS="$JVM_OPTS -XX:+UseTLAB"
# note: bash evals '1.7.x' as > '1.7' so this is really a >= 1.7 jvm check
if [ "$JVM_VERSION" \> "1.7" ] && [ "$JVM_ARCH" = "64-Bit" ] ; then
    JVM_OPTS="$JVM_OPTS -XX:+UseCondCardMark"
fi

# Prefer binding to IPv4 network intefaces (when net.ipv6.bindv6only=1). See
# http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6342561 (short version:
# comment out this entry to enable IPv6 support).
JVM_OPTS="$JVM_OPTS -Djava.net.preferIPv4Stack=true"

launch_service()
{
    pidpath="$1"
    foreground="$2"
    props="$3"
    class="$4"
    rene_parms="-Dlog4j.configuration=file:$RENE_HOME/log4j-custom.properties -Dlog4j.defaultInitOverride=true"

    if [ "x$foreground" != "x" ]; then
        exec $NUMACTL "$JAVA" $JVM_OPTS -cp "$CLASSPATH" $props "$class"
    # Startup CassandraDaemon, background it, and write the pid.
    else
        exec $NUMACTL "$JAVA" $JVM_OPTS -cp "$CLASSPATH" $props "$class" <&- &
        [ ! -z "$pidpath" ] && printf "%d" $! > "$pidpath"
        true
    fi

    return $?
}

# Parse any command line options.
args=`getopt vfhp:bD:H:E: "$@"`
eval set -- "$args"

classname="com.suning.rene.ServerMain"

            foreground="yes"
while true; do
    case "$1" in
        -p)
            pidfile="$2"
            shift 2
        ;;
        -b)
            foreground=""
            shift
        ;;
        -h)
            echo "Usage: $0 [-b] [-h] [-p pidfile]"
            exit 0
        ;;
        -D)
            properties="$properties -D$2"
            shift 2
        ;;
        --)
            shift
            break
        ;;
        *)
            echo "Error parsing arguments!" >&2
            exit 1
        ;;
    esac
done

# Start up the service
launch_service "$pidfile" "$foreground" "$properties" "$classname"

exit $?
