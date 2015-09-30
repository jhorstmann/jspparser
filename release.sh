#!/usr/bin/env bash

set -e

DEFAULT_VERSION=1.0-SNAPSHOT
RELEASE_VERSION=${1?"Usage $0 release-version [next-version]"}
NEXT_VERSION=${2-$DEFAULT_VERSION}

mvn scm:check-local-modification

# release
mvn versions:set -DnewVersion=$RELEASE_VERSION
git add pom.xml
git commit -m "Release $RELEASE_VERSION"
mvn clean deploy -Prelease
mvn scm:tag

# next development version
mvn versions:set -DnewVersion=$NEXT_VERSION
git add pom.xml
git commit -m "Develop $NEXT_VERSION"
