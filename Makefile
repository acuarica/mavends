

all: nexus.cpp.o
	time ./nexus.cpp.o

%.cpp.o: %.cpp
	$(CXX) $(CXXFLAGS) -O5 -o $@ $<

