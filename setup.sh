#!/bin/sh
libpath="/usr/lib"
confpath="/usr/conf"
shpath="/usr/bin/"
echo -n  "do you really want to setup all the cluster? you must first config node in the nodes[y or n]:"
read var
if [ "$var" != "y" ];
then
exit
fi
echo "start to distribute all files using nodes:"
if [ -s nodes ];
then
for node in `cat nodes`;
do
echo "start to config $node."

ssh $node  '
libpath="/usr/lib"
confpath="/usr/conf"
shpath="/usr/bin/"
if [ ! -d "$libpath" ];then
mkdir -p $libpath
fi
if [ ! -d "$confpath" ];then
mkdir -p $confpath
fi
if [ ! -d "$shpath" ];then 
mkdir -p $shpath 
fi
'
if [ "0" ==  "$?" ];then
echo "configured!"
else
echo "error in configuration,please check $node,exit!"
exit
fi
echo "start to copy files to $node.. "
scp cluster.jar sigar.jar $node:/usr/lib/
scp Monitor_conf.xml nodes master $node:/usr/conf/
scp ClusterServer.sh Node.sh Admin.sh $node:/usr/bin/
ssh $node chmod 700 /usr/bin/Admin.sh /usr/bin/Node.sh /usr/bin/ClusterServer.sh
echo "OK!,please then use Startup.sh to start the cluster!"
done
else
echo "no nodes is configed in file :nodes,exit..."
fi
