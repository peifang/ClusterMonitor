#!/bin/sh
for master in `cat /usr/conf/master`;
do
echo -n "master is $master,prepare to start..."
ssh $master "nohup /usr/bin/ClusterServer.sh&"
echo ""
echo "started!"
break
done
sleep 3s
for node in `cat /usr/conf/nodes`;
do
echo -n "start to start node demon on $node:"
ssh $node "nohup /usr/bin/Node.sh&"
echo ""
done
echo "all done!"
#if service httpd status >/dev/null 2>&1;
