#!/usr/bin/bash

javac *.java
for i in `ls | grep .class$ | sed 's/.class//g'`
do
    echo $i
    java $i
done
mv *.json ../assets/langpacks/
rm -f *.class
