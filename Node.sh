# chkconfig: 2345 08 92 
# 
# description: Automates a packet filtering firewall withipchains. 
#
#!/bin/sh
CNAME=${1%.java}
HIVE_HOME=/usr/lib/hive
HADOOP_HOME=/usr/lib/hadoop
JARNAME=$CNAME.jar
JARDIR=/home/yaboo/$CNAME
HBASE_HOME=/usr/lib/hbase
CLASSPATH=/home/partition.jar:$(ls $HIVE_HOME/lib/hive-serde-*.jar):$(ls $HIVE_HOME/lib/hive-exec-*.jar):/usr/lib/hbase/conf:/usr/lib/zookeeper/conf:/usr/lib/hadoop/conf

for file in `ls /usr/lib/hbase/*jar`; do
    CLASSPATH=$CLASSPATH:$file
done

for file in `ls $HADOOP_HOME/lib/*.jar`; do
    CLASSPATH=$CLASSPATH:$file
done
for file in `ls /usr/lib/hbase/lib/*jar`; do
    CLASSPATH=$CLASSPATH:$file
done


echo $CLASSPATH

export CLASSPATH=$CLASSPATH
export LD_LIBRARY_PATH=/usr/lib
echo $LD_LIBRARY_PATH
java -cp $CLASSPATH:/usr/lib/cluster.jar:/usr/lib/sigar.jar com.intel.fangpei.terminal.Node 

