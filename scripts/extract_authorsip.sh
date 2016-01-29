#!/bin/bash

releases=("v2.6.12" "v2.6.13" "v2.6.14" "v2.6.15" "v2.6.16" "v2.6.17" "v2.6.18" "v2.6.19" "v2.6.20" "v2.6.21" "v2.6.22" "v2.6.23" "v2.6.24" "v2.6.25" "v2.6.26" "v2.6.27" "v2.6.28" "v2.6.29" "v2.6.30" "v2.6.31" "v2.6.32" "v2.6.33" "v2.6.34" "v2.6.35" "v2.6.36" "v2.6.37" "v2.6.38" "v2.6.39" "v3.0" "v3.1" "v3.2" "v3.3" "v3.4" "v3.5" "v3.6" "v3.7" "v3.8" "v3.9" "v3.10" "v3.11" "v3.12" "v3.13" "v3.14" "v3.15" "v3.16" "v3.17" "v3.18" "v3.19" "v4.0" "v4.1" "v4.2")
path=$1
repositoryName=$2
currentpath=${PWD}
clear
now=$(date)
echo -e $now: BEGIN test script: $path \\n 


cd $path

for entry in "${releases[@]}"; do
	echo $entry
	git checkout $entry
	cd $currentpath
	./commit_log_script.sh $path 
	./linguist_script.sh $path
	java -jar authorship_analyzer.jar $path $repositoryName $entry >> file-authorship.log
	cd $path	
	
done
	
	
echo -e $now: END test script: $path \\n 
