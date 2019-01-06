#!/bin/bash

caffeinate &

repeat=10

for ndx in {1..1}
do
  date
  echo "Modeling Pass:" $ndx
  cd ..
  for mode in none discount agglomeration
  do  
    java -jar ForestSim.jar --$mode -repeat $repeat -quiet
  done

  for path in out/none out/discount out/agglomeration 
  do
    echo "Compress and Clean-up $path"
    zip -r $path/gis$ndx.zip $path -x "*.DS_Store" -x "*.csv" -q
    rm -rf $path/*/
  done

  echo "Updating Plots..."
  cd run
  ./analysis.R
done

killall caffeinate

echo "Done!"
date

