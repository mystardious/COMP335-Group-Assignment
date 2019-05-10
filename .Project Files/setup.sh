#!/bin/bash
# Installs required files for client to run

sudo apt-get update && sudo apt-get upgrade -y
sudo apt install openjdk-8-jre-headless
