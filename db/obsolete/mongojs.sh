#!/bin/sh
# assuming mongo bin is in your path

host=localhost
port=27017
db=playtest12



#mongoimport --host $host --port $port -d $db --collection $1 --file $1.json

#load("test/my.js")
mongo $host:$port/$db $1


