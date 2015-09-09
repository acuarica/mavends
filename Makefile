

CMD=time java -cp build/ ch.usi.inf.mavends.CheckNexusIndex --nexusindex=cache/nexus-maven-repository-index

all:
	ant
	$(CMD)
	$(CMD)
	$(CMD)
	$(CMD)
	$(CMD)

