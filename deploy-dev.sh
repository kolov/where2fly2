#!/bin/bash

# Exit on any error
set -e
echo Starting deployment
sudo chown -R ubuntu:ubuntu /home/ubuntu/.kube
set GOOGLE_APPLICATION_CREDENTIALS =$HOME/gcloud-service-key.json
#kubectl patch deployment where2fly2 -p '{"spec":{"template":{"spec":{"containers":[{"name":"w2f2", "image":"eu.gcr
#.io/iconic-setup-91510/where2fly2:latest"}]}}}}'
kubectl delete deployment w2f2
kubectl apply -f docker/kube/w2f2-d.yml