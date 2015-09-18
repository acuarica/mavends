
# MavenDS: An Analysis Framework for the Maven Repository


## Introduction

Ever wondered how a certain API was used?
Or what is it its evolution?
If you do research on Mining Software Repositories, *MavenDS* is the right tool for you. 
*MavenDS* allows you selective download and analyze artifacts from the *Maven Central* repository http://search.maven.org/.

The *Maven Central Repository* became the most used JVM repository.
Because not only Java code, but Scala, Groovy, Go and every JVM language can deploy to Maven Central.
Thus making it, the ideal to explore new ideas on how software artifacts are used.


## Requirements

*MavenDS* uses several external tools to function properly.
They need to be installed before running *MavenDS*.
The list of tools are:

- **Git** https://git-scm.com: To clone our repository.
- **Java JDK 7** http://openjdk.java.net: To compile and run our applications.
- **Ant** https://ant.apache.org/: Software tool for automating software build process.
- **Aria2** http://aria2.sourceforge.net/: Tool to automate downloads of a large amount of files.


## Repository Setup

Our tool consists of three main components: a) a Java project to download and extract information for Java archives and POM files from Maven Repository, 
An Eclipse Java project is located at the root of the repository.
Our tool are available online.

    git clone https://bitbucket.org/acuarica/mavends.git


## Getting Started

The build process is managed by an *Ant* script.
You can see the list of targets by running the following command:

    ant -projecthelp

It shows the list of available targets, with a short description of what it does.
The output should be similar to the following:

```
MavenDS: An Analysis Framework for the Maven Repository.
Targets begining with '-' are executed internally by other targets.
To fetch the Nexus Index and Maven Artifacts, *aria2* is used.
Check http://aria2.sourceforge.net/ for more details on how to install *aria2*.
Some targets require a configuration.
Prepend the configuration target before the goal target, e.g., *ant conf-ch urilist*.
Main targets:

 -compile         Compiles all Java files under the src/ directory.
 analysis-stats   Runs Stats analysis. Requires configuration.
 analysis-unsafe  Runs Unsafe analysis. Requires configuration.
 clean            Removes the compilation directory, e.g., *build*.
 conf-all         Configuration using --all-- artifacts. (~254GB).
 conf-ch          Configuration using artifacts from the 'ch' root group (~500MB).
 conf-com         Configuration using artifacts from the 'com' root group (~58GB).
 conf-net         Configuration using artifacts from the 'net' root group (~16GB).
 fetcharts        Fetches all artifacts specified by the URI list. Requires configuration.
 mavenindex       Creates and populates the Maven Index DB from the Nexus Index.
 mavenpom         Extract the information from POM files. Requires configuration.
 nexusindex       Fetches and uncompresses the Nexus Maven Repository Index.
 urilist          Builds the list of artifacts to download. Requires configuration.
Default target: -compile
```

At any point, if you want to start the experiments from scratch, you can run the following command to remove the build folder by running:

    ant clean

The first step in our workflow is to get a representative subset of artifacts to get analyzed.

## Fetching Nexus Index

To download the nexus index, run the following command:

    ant nexusindex

This command will fetch the nexus index.
 

## Build Maven Index

    ant mavenindex

## Selective Fetching and Analysis: Configurations


    conf-all
    ant conf-ch
    ant conf-com
    ant conf-net

## Fetching Artifacts

    ant <conf> fetcharts
    ant <conf> urilist

## Maven POM Dependencies

    ant <conf> mavenpom

## Analysis

    ant <conf>analysis-stats
    
    ant <conf> analysis-unsafe

ASM