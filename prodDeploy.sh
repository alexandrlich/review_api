#!/bin/bash
 
REMOTE1=app1
#REMOTE2=app2

REMOTE_APP=/home/alex/appstage
PORT=5555
echo "start upload process"



upload() {

echo "test" + $1

	ssh -p $PORT $1 "cd $REMOTE_APP; rm -rf *";
	 
	#sbt stage || exit 1;
		
	rsync -vRra app/ -e "ssh -p $PORT" $1:$REMOTE_APP/;
	rsync -vRra conf/ -e "ssh -p $PORT" $1:$REMOTE_APP/;
	rsync -va build.sbt -e "ssh -p $PORT" $1:$REMOTE_APP;
	rsync -vRa project/build.properties -e "ssh -p $PORT" $1:$REMOTE_APP/;
	rsync -vRa project/plugins.sbt -e "ssh -p $PORT" $1:$REMOTE_APP/; 

	echo "app upload completed to server: $1";
}
 
upload $REMOTE1
#upload $REMOTE2


exit 1

#ssh $REMOTE1 "cd $REMOTE_APP; play compile";
#ssh $REMOTE1 "cd $REMOTE_APP; play stage";


