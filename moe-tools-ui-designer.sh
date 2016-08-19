#!/bin/sh

#
# BUILD moe.tools.ui.transformer PROJECT
# 

qualifier="$1"
build_number="$2"
target_repo="$3"
repo_user="$4"
repo_pass="$5"

export MOE_MAVEN_ADDR=$target_repo

export MOE_UI_TRANSFORMER_BUILD_NUMBER=$build_number
export PUBLISH_TARGET_REPO_ADDR=$target_repo
export PUBLISH_TARGET_REPO_USER=$repo_user
export PUBLISH_TARGET_REPO_PASS=$repo_pass

export IDEA_HOME=/Applications/IntelliJ\ IDEA\ 14\ CE.app/Contents/

# replace build version
#cat META-INF/plugin.xml | sed "s/1.0/1.0.$build_number/g" > META-INF/temp.xml
#mv -f META-INF/temp.xml META-INF/plugin.xml

#run build
ant

mkdir -p $target_repo/org/moe/UIPrototyper
cp xrt_UIPrototyper.zip $target_repo/org/moe/UIPrototyper/moe_uiprototyper_plugin.zip