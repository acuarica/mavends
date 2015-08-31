
#include <iostream>
#include <fstream>


using namespace std;

class Header {
		
};

class NexusIndex {
	
	public:
		
		/**
		 *
		 */
		NexusIndex(const char* indexPath) : 
			buffer(indexPath, ios::in | ios::binary)
		{
			headerByte = read1();
			headerDate = read8();

			cerr << headerByte << endl;
			cerr << headerDate << endl;
		}

		void all() {
			int n = 0;
			while (buffer) {
				hola();
				n++;
			}

			cout << "Number of records: " << n << endl;
		}

		void hola() {
			int nf = read4();
			
//			cout << nf << endl;
			
			for (int i = 0; i < nf; i++) {
				read1();
				
				int keylen = read2();
				buffer.ignore(keylen);

				int valuelen = read4();
				buffer.ignore(valuelen);

			}
		}

	private:
		
		ifstream buffer;

		int headerByte;

		unsigned long headerDate;

		inline unsigned char read1() {
				char b;
				buffer.read(&b, 1);
				return b;
		}

		inline unsigned short read2() {
			unsigned short b;
			buffer.read((char*)&b, 2);
			return ntohs(b);
		}

		inline unsigned int read4() {
			unsigned int b;
			buffer.read((char*)&b, 4);
			return ntohl(b);
		}

		inline unsigned long read8() {
				unsigned long b;
				buffer.read((char*)&b, 8);
				b = ((long)ntohl(0xffffffff & b) << 32) | ( ntohl( b >> 32) );
				return b;
		}

};

int main(int argc, const char* argv[]) {
	NexusIndex ni("cache/nexus-maven-repository-index");

	ni.all();

}

