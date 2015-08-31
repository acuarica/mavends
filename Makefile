

all: nexus.cpp.o
	time ./nexus.cpp.o

%.cpp.o: %.cpp
	$(CXX) $(CXXFLAGS) -O3 -o $@ $<

