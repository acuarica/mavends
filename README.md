
# MavenDS: An Analysis Framework for the Maven Repository

## Introduction

Ever wondered how a certain API was used?
Or what is it its evolution?
If you do research on Mining Software Repositories, *MavenDS* is the right tool for you. 
*MavenDS* allows you selective download and analyze artifacts from the *Maven Central* repository http://search.maven.org/.

The *Maven Central Repository* became the most used JVM repository.
Because not only Java code, but Scala, Groovy, Go and every JVM language can deploy to Maven Central.
Thus making it the ideal tool to explore new ideas on how software artifacts are used.


## Requirements

*MavenDS* uses several external tools to function properly.
They need to be installed before running *MavenDS*.
The list of tools are:

- **Git** https://git-scm.com/: To clone our repository.
- **Java JDK 7** http://openjdk.java.net/: To compile and run our applications.
- **Ant** https://ant.apache.org/: Software tool for automating software build process.
- **Aria2** http://aria2.sourceforge.net/: Tool to automate downloads of a large amount of files.

Optionally, **SQLite** http://sqlite.org/ can be installed to inspect the Maven Index and Maven POM Depedencies databases. 


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


## Fetching Nexus Index

The first step in our workflow is to get a representative subset of artifacts to get analyzed.
To begin with, it is needed an index of all Maven artifacts.
We get the maven index from a mirror and then uncompress it.
This step requires an active Internet connection.
To download the nexus index, run the following command:

    ant nexusindex

This command will fetch the nexus index.
 

## Build Maven Index

Once the Nexus Index is downloaded, you need to run:

    ant mavenindex

This builds the maven index database.
From this database artifacts can be filtered using plain SQL.
Refer to [sql/mavenindex.sql](sql/mavenindex.sql) for more details on the tables and views of the Maven Index DB.


## Selective Fetching and Analysis: Configurations


    conf-all
    conf-ch
    conf-com
    conf-net

## Fetching Artifacts

    ant <conf> fetcharts
    ant <conf> urilist

## Maven POM Dependencies

    ant <conf> mavenpom

## Analysis

    ant <conf> analysis-stats
    
    ant <conf> analysis-unsafe

ASM


```java
public class BytecodeStatsVisitor extends MavenVisitor {

	private static final Log log = new Log(System.out);

	private long classCount = 0;
	private long methodCount = 0;
	private long callsiteCount = 0;
	private long fielduseCount = 0;
	private long constantCount = 0;

	@Override
	public ClassVisitor visitClass() {
		return new ClassVisitor(Opcodes.ASM5) {

			@Override
			public void visit(int version, int access, String name, String signature, String superName,
					String[] interfaces) {
				super.visit(version, access, name, signature, superName, interfaces);
				classCount++;
			}

			@Override
			public MethodVisitor visitMethod(int access, final String methodName, final String methodDesc,
					String signature, String[] exceptions) {

				methodCount++;

				MethodVisitor mv = new MethodVisitor(Opcodes.ASM5) {

					@Override
					public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
						callsiteCount++;
					}

					@Override
					public void visitFieldInsn(int opcode, String owner, String name, String desc) {
						fielduseCount++;
					};

					@Override
					public void visitLdcInsn(Object cst) {
						constantCount++;
					}
				};

				return mv;
			}
		};
	}

	@Override
	public void close() {
		log.info("Number of classes: %,d", classCount);
		log.info("Number of methods: %,d", methodCount);
		log.info("Number of call sites: %,d", callsiteCount);
		log.info("Number of field uses: %,d", fielduseCount);
		log.info("Number of constants: %,d", constantCount);
	}
}
```





https://www.sqlite.org/intern-v-extern-blob.html

http://dl.acm.org/citation.cfm?id=2501589&CFID=539758608&CFTOKEN=73816535
http://dl.acm.org/citation.cfm?id=2487136&CFID=539758608&CFTOKEN=73816535
http://dl.acm.org/citation.cfm?id=2597123&CFID=539758608&CFTOKEN=73816535
