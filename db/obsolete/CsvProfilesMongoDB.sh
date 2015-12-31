#!/bin/bash

host=localhost
port=27017
db=playtest12

input=$1

[ -z $1 ] && echo "No CSV input file specified" && exit 1
[ ! -e $input ] && echo "Unable to locate $1" && exit 1

read first_line < $input
a=0
headings=`echo $first_line | awk -F, {'print NF'}`
lines=`cat $input | wc -l`
while [ $a -lt $headings ]
do
        head_array[$a]=$(echo $first_line | awk -v x=$(($a + 1)) -F"[,|]" '{print $x}')
        a=$(($a+1))
done

c=0
data="["
while [ $c -lt $lines ]
do
        read each_line
        occupation="\"jobs\" : [{ "
        popular_index=$(($RANDOM%40))
        if [ $c -ne 0 ]; then
                d=0
                data+="{"
                while [ $d -lt $headings ]
                do
                        each_element=$(echo $each_line | awk -v y=$(($d + 1)) -F"[,|]" '{print $y}')

                        #if [[ !$each_element ]]; then
                        #        continue
                        #fi

                        if [[ ${head_array[$d]} == occupation ]]; then
                                occupation+="${head_array[$d]}:\"$each_element\""
                        elif [[ ${head_array[$d]} == company ]]; then
                                if [[ $each_element ]]; then
                                        occupation+=", ${head_array[$d]}:\"$each_element\""
                                fi
                        elif [[ $each_element =~ ^[0-9]+$ || $each_element =~ \".*\" ]]; then
                                data+="${head_array[$d]}:$each_element"
                        else
                                data+="${head_array[$d]}:\"$each_element\""
                        fi

                        if [[ $d -ne $(($headings-1)) && ${head_array[$d]} != occupation && ${head_array[$d]} != company ]]; then
                                data+=","
                        fi
                        d=$(($d+1))
                done
                occupation+=" }]"
                data+=", user_id:\"5378de960b00000b00124b00\", "
                data+="popular_index:$popular_index, "
                data+="theme_name:\"red\", "
                data+="views_count:$popular_index, "
                data+="votes_count:$(($RANDOM%20)), "
                data+="is_deleted:false, "
                data+="$occupation}"
                if [ $c -ne $(($lines-1)) ]; then
                        data+=","
                fi
        fi
        c=$(($c+1))
done < $input
data+="]"

echo "db.profiles.insert(${data})" | mongo $host:$port/$db