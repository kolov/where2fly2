#!/usr/bin/env bash


export PROJECT_ROOT=~/projects/wherecanigo 

if [ -z ${PROJECT_ROOT+x} ]; then
  echo "PROJECT_ROOT is unset"
  exit 1;
else
  echo "PROJECT_ROOT is set to $PROJECT_ROOT";
fi


kubectl delete -f $PROJECT_ROOT/docker/kube/mongo-d.yml
kubectl apply -f $PROJECT_ROOT/docker/kube/mongo-d.yml
kubectl apply -f $PROJECT_ROOT/docker/kube/mongo-s.yml

kubectl delete -f $PROJECT_ROOT/docker/kube/w2f2-d.yml
kubectl apply -f $PROJECT_ROOT/docker/kube/w2f2-d.yml


