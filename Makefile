CC=gcc
CFLAGS=-I.
DEPS = catalog.h record.h attribute.h bufferpool.h constants.h page.h storagemanager.h table.h
OBJ = main.o catalog.o record.o attribute.o bufferpool.o constants.o page.o storagemanager.o table.o

%.o: %.c $(DEPS)
	$(CC) -c -o $@ $< $(CFLAGS)

main: $(OBJ)
	$(CC) -o $@ $^ $(CFLAGS)

clean:
	rm -f *.o main

