
BUILD=build

UNAME=$(shell uname)

CXXFLAGS+=-std=c++11
CXXFLAGS+=-stdlib=libc++
CXXFLAGS+=-MMD -fPIC -W -g -Wall -Wextra
CXXFLAGS+=-O3

MAVENCLASS=$(BUILD)/mavenclass.mach-o
MAVENCLASS_BUILD=$(BUILD)/mavenclass
MAVENCLASS_SRC=src-mavenclass
MAVENCLASS_SRCS=$(wildcard $(MAVENCLASS_SRC)/*.cpp)
MAVENCLASS_OBJS=$(MAVENCLASS_SRCS:$(MAVENCLASS_SRC)/%=$(MAVENCLASS_BUILD)/%.o)
JNIF=jnif/build/libjnif.a

MAVEN_INDEX_DB=out/mavenindex.sqlite3
MAVEN_REPO=cache/repo
SELECT_ARTS="select max(idate), * from artifact_jar group by groupid, artifactid"
MAVEN_CLASS_DB=out/mavenclass.sqlite3

.PHONY: all run db clean $(MAVEN_CLASS_DB)

all: $(MAVENCLASS)

run: $(MAVENCLASS) $(MAVEN_CLASS_DB)
	$(MAVENCLASS) $(MAVEN_INDEX_DB) $(MAVEN_REPO) $(SELECT_ARTS) $(MAVEN_CLASS_DB)

db: $(MAVEN_CLASS_DB)

clean:
	-rm -r $(BUILD)

$(MAVEN_CLASS_DB): sql/mavenclass.sql
	rm -f $@
	cat $^ | sqlite3 -bail $@

$(MAVENCLASS): LDFLAGS+=-lz -lsqlite3 -O3
$(MAVENCLASS): $(MAVENCLASS_OBJS) $(JNIF)
	$(CXX) $(LDFLAGS) -o $@ $^

$(MAVENCLASS_BUILD)/%.cpp.o: $(MAVENCLASS_SRC)/%.cpp | $(MAVENCLASS_BUILD)
	$(CXX) $(CXXFLAGS) -Ijnif/src -c -o $@ $<

# -include $(LIBJNIF_BUILD)/*.cpp.d

$(MAVENCLASS_BUILD):
	mkdir -p $@

$(JNIF):
	make -C jnif/
