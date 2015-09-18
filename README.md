
# MavenDS: An Analysis Framework for the Maven Repository


## Introduction

*MavenDS* is a dataset and analysis framework for the Maven Central Repository.
*MavenDS* resembles a filesystem to hold every Maven Central artifact.
In short, *Everything you wanted to know about the Maven Repository* is possible with *MavenDS*.


the Maven Central Repository became the most used JVM repository.
Because not only java code, but scala groovy go and each JVM language can deploy to maven.

## Getting Started

The build process is managed by an *Ant* script [https://ant.apache.org/].
You can see the list of targets by running the following command:

    ant -projecthelp

It shows the list of available targets, with a short description of what it does.
The output should be similar to the following:

    MavenDS: An Analysis Framework for the Maven Repository. Targets begining with '-' are executed internally by other targets. To fetch the Nexus Index and Maven Artifacts, *aria2* is used. Check http://aria2.sourceforge.net/ for how to install *aria2* on your platform. To run targets that requires configuration, prepend the configuration target before the goal target, e.g., *ant conf-ch urilist*
    Main targets:

     -compile         Compiles all Java files under the src/ directory.
     analysis-stats   Runs Stats analysis. Requires configuration.
     analysis-unsafe  Runs Unsafe analysis. Requires configuration.
     clean            Removes the compilation directory, e.g., *build*.
     conf-all         Configuration 'prod' using --all-- artifacts. Circa 254GB of JAR files.
     conf-ch          Configuration 'dev' using artifacts from the 'ch' root group. Circa 500MB of JAR files.
     conf-com         Configuration 'stage' using artifacts from the 'com' root group. Circa 58GB of JAR files.
     conf-net         Configuration 'test' using artifacts from the 'net' root group. Circa 16GB of JAR files.
     fetcharts        Fetches all artifacts specified by the URI list in with *aria2*. Requires configuration.
     mavenindex       Creates and populates the Maven Index DB from the Nexus Index.
     mavenpom         Extract the information from POM files. Requires configuration.
     nexusindex       Fetches and uncompresses the Nexus Maven Repository Index.
     urilist          Builds the list of artifacts to download configuration. Requires configuration.
    Default target: -compile

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
