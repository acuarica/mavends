
# MavenDS: The Maven Repository on a Silver Platter

## Introduction

*MavenDS* is a dataset and analysis framework for the Maven Central Repository.
*MavenDS* is filesystem.
In short, with *MavenDS*: Everything you wanted to know about the Maven Repository.

https://www.sqlite.org/intern-v-extern-blob.html

## Getting Started

The build process is managed by an *Ant* script.
You can see the list of targets by running the following command:
 
    ant -projecthelp

Targets beginning with '-' (minus) are internal targets not to be used directly by the user.
Instead they are used by other targets.

### Fetching Nexus Index

    ant nexusindex

    ant mavenindex

 
    ant urilist-dev

 
    ant fetcharts

 
    ant maveninode-dev

    ant mavenpom-dev

 
    ant clean

 
 