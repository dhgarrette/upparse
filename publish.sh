#!/bin/bash

projectname=upparse
version_len=5

rm -rf target/scala-2.11/${projectname}_2.11-*
sbt publish

for f in target/scala-2.11/${projectname}_2.11-*
do
  echo $f
  len=24+${#projectname}
  version=${f:$len:$version_len}
  dir=public_html/maven-repository/snapshots/net/ponvert/${projectname}/$version-SNAPSHOT/
  ssh k mkdir -p $dir
  fn=${f##*/}
  scp $f k:$dir${fn/_2.11/}
  
  rm -rf ~/.ivy2/cache/net/ponvert/${projectname}_2.11/*-$version*
  rm -rf ~/.ivy2/cache/net/ponvert/${projectname}_2.11/*/*-$version*

done


rm -rf target/scala-2.11/api
sbt doc

scp -r target/scala-2.11/api k:public_html/maven-repository/snapshots/net/ponvert/${projectname}/$version-SNAPSHOT

