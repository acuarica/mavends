
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
# SELECT_ARTS="select max(idate), * from artifact_jar group by groupid, artifactid"
# MAVEN_CLASS_DB=out/mavenclass.sqlite3

.PHONY: all run-ch-last _run clean

all: $(MAVENCLASS)

run-all-last: WHERE_ARTS=1=1
run-all-last: MAVEN_CLASS_DB=out/mavenclass-all-last.sqlite3
run-all-last: _run

run-ch-last: WHERE_ARTS=rootgroup='ch'
run-ch-last: MAVEN_CLASS_DB=out/mavenclass-ch-last.sqlite3
run-ch-last: _run

_run: SELECT_ARTS="select max(idate), * from artifact_jar where $(WHERE_ARTS) group by groupid, artifactid"
_run: $(MAVENCLASS) sql/mavenclass.sql
	rm -f $(MAVEN_CLASS_DB)
	cat sql/mavenclass.sql | sqlite3 -bail $(MAVEN_CLASS_DB)
	$(MAVENCLASS) $(MAVEN_INDEX_DB) $(MAVEN_REPO) $(SELECT_ARTS) $(MAVEN_CLASS_DB)

clean:
	rm -rf $(BUILD)
	make -C jnif/ clean

$(MAVENCLASS): LDFLAGS+=-lz -lsqlite3 -O3
$(MAVENCLASS): $(MAVENCLASS_OBJS) $(JNIF)
	$(CXX) $(LDFLAGS) -o $@ $^

$(MAVENCLASS_BUILD)/%.cpp.o: $(MAVENCLASS_SRC)/%.cpp | $(MAVENCLASS_BUILD)
	$(CXX) $(CXXFLAGS) -Ijnif/src -c -o $@ $<

-include $(MAVENCLASS_BUILD)/*.cpp.d

$(MAVENCLASS_BUILD):
	mkdir -p $@

$(JNIF):
	make -C jnif/
