#!/usr/bin/env bash

license='L01234567890123P'
secret='ABCDEFGHIJKLMNOP'
time=$(date '+%s')

# Step 1 : Obtain proxy IP
# Important: the ip addresses in the obtained ip:port list belong to TTProxy central server, NOT the proxy node ip which finally communicate with the target server.

sign=$(echo -e "$license$time$secret\c" | md5sum | awk '{print $1}')

url="https://api.ttproxy.com/v1/obtain?license=$license&time=$time&sign=$sign"
# url="$url&cnt=1" # Get number of proxies (optional)

proxies=`curl $url`

# Step 2 : Use proxy IP
targetUrl='https://httpbin.org/get'
proxy=$(echo $proxies|jq .data | jq .proxies[1] | sed "s/\"//g" )

echo $proxy
curl -x $proxy $targetUrl