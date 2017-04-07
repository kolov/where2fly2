#!/bin/bash

# Exit on any error
set -e

sudo chown -R ubuntu:ubuntu /home/ubuntu/.kube
kubectl patch deployment where2fly2 -p '{"spec":{"template":{"spec":{"containers":[{"name":"where2fly2",
"image":"eu.gcr.io/iconic-setup-91510/where2fly2:latest"}]}}}}'