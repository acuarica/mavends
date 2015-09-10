
# MavenDS: The Maven Repository on a Silver Platter


## Introduction

the Maven Central Repository became the most used JVM repository.
Because not only java code, but scala groovy go and each JVM language can deploy to maven.

*MavenDS* is a dataset and analysis framework for the Maven Central Repository.
*MavenDS* resembles a filesystem to hold every Maven Central artifact.
In short, *Everything you wanted to know about the Maven Repository* is possible with *MavenDS*.


## Getting Started

The build process is managed by an *Ant* script.
You can see the list of targets by running the following command:
 
    ant -projecthelp

Targets beginning with '-' (minus) are internal targets not to be used directly by the user.
Instead they are used by other targets.

### Fetching Nexus Index

To download the nexus index, run the following command:

    ant nexusindex

### Check Nexus Index

    ant checknexusindex

    ant mavenindex

 
    ant urilist-dev

 
    ant fetcharts

 
    ant maveninode-dev

    ant mavenpom-dev

 
    ant clean

## Implementation

We take advantage of the following fact:

https://www.sqlite.org/intern-v-extern-blob.html

To store small files in directly in the database.

The files are compressed.
