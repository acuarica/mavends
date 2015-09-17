
SRCS=$(wildcard *.c)
OBJS=$(SRCS:%.c=%.o)
SQLITEIMPORT=sqliteimport

all: $(SQLITEIMPORT)
	-rm ../out/es.sqlite3
	sqlite3 ../out/es.sqlite3 < ../sql/maveninode.sql
	time ./$(SQLITEIMPORT) 1 2 3

$(SQLITEIMPORT): $(OBJS)
	$(CC) -o $@ $^

%.o: %.c
	$(CC) -c -o $@ $<

