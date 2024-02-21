CC=gcc
CFLAGS=-I.
DEPS = catalog.h record.h attribute.h constants.h page.h storagemanager.h table.h buffer.h parse.h
OBJ = main.o catalog.o record.o attribute.o constants.o page.o storagemanager.o table.o buffer.o page.o parse.o

%.o: %.c $(DEPS)
	$(CC) -c -o $@ $< $(CFLAGS)

main: $(OBJ)
	$(CC) -o $@ $^ $(CFLAGS)

clean:
	rm -f *.o main

