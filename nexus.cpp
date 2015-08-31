
#include <iostream>
#include <fstream>
#include <sys/mman.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>

using namespace std;

class Header {
		
};

class StreamReader {
	public:

		StreamReader(const char* path) :
			buffer(path, ios::in | ios::binary)
		{
		}

		unsigned char read1() {
				char b;
				buffer.read(&b, 1);
				return b;
		}

		unsigned short read2() {
			unsigned short b;
			buffer.read((char*)&b, 2);
			return ntohs(b);
		}

		unsigned int read4() {
			unsigned int b;
			buffer.read((char*)&b, 4);
			return ntohl(b);
		}

		unsigned long read8() {
				unsigned long b;
				buffer.read((char*)&b, 8);
				b = ((long)ntohl(0xffffffff & b) << 32) | ( ntohl( b >> 32) );
				return b;
		}

		void ignore(int size) {
			buffer.ignore(size);
		}

		operator bool() const {
			return buffer;
		}

	private:

		ifstream buffer;

};

class MapReader {

	public:
		
		MapReader(const char* path) : pos(0) {
		
			   int fd = open(path, O_RDONLY);

  struct stat sb;



   fstat(fd, &sb);

	 size = sb.st_size;
//cerr << "file size: " << sb.st_size << endl;

		mm = (unsigned char*)mmap(NULL, sb.st_size, PROT_READ, MAP_SHARED, fd, 0);
		omm = mm;
		}

		long size;

		unsigned char read1() {
				char b = mm[0];
				mm += 1;
				return b; 
		}

		unsigned short read2() {
			unsigned short b = *(unsigned short*)mm;
			mm += 2;
			return ntohs(b);
		}

		unsigned int read4() {
			unsigned int b = *(unsigned int*)mm;
			mm += 4;
			return ntohl(b);
		}

		unsigned long read8() {
				unsigned long b = *(unsigned long*)mm;
				mm += 8;
				b = ((long)ntohl(0xffffffff & b) << 32) | ( ntohl( b >> 32) );
				return b;
		}

		void ignore(int size) {
			mm += size;
			//buffer.ignore(size);
		}

		operator bool() const {
			return mm - (unsigned char*)omm < size;
		}

		void* omm;
	
	public:

		unsigned char* mm;

		long pos;

};

/**
 *
 */
template <typename TReader, typename TVisitor>
void nexusIndexParser(TReader& reader, TVisitor& visitor) {
	int headerByte = reader.read1();
	long headerDate = reader.read8();
	
	visitor.visitHeader(headerByte, headerDate);
	
	int n = 0;
	while (reader) {
		int nf = reader.read4();
		
		printf("{ %d ", nf);
		for (int i = 0; i < nf; i++) {
			int b = reader.read1();
			
			int keylen = reader.read2();
			unsigned char* keyp = reader.mm;
			reader.ignore(keylen);

			int valuelen = reader.read4();
			unsigned char* valuep = reader.mm;
			reader.ignore(valuelen);

			printf("%d:%.*s=%.*s ", b, keylen, keyp, valuelen, valuep);
//			visitor.v(b, keylen, keyp, valuelen, valuep);
		}

		printf("}\n");
	}
	
	n++;
	
	cout << "Number of records: " << n << endl;
}

class NexusIndexPrinter{

	public:

		NexusIndexPrinter(ostream& os) : os(os) {
		}

		void visitHeader(int hb, long d) {
			os << hb << endl;
			os << d << endl;
		}

	private:

		ostream& os;
};

int main(int argc, const char* argv[]) {
	NexusIndexPrinter p(cout); 
	MapReader mr("cache/nexus-maven-repository-index");
	
	nexusIndexParser(mr, p);

	cout << "hola" << endl;
}

