#ifndef catalog.h
#define catalog.h

#define MAX_PAGE_SIZE 4096  // bytes
#define MAX_NUM_RECORDS 100  // Can change
#define MAX_NUM_ATTRIBUTES 50  // Can also change
#define MAX_NAME_SIZE 50  // "   "

typedef struct Catalog {
    struct Table* tables;
    int tableCount;
    struct BufferPool* bufferPool;
} Catalog;

typedef struct Table {
    struct Attribute* attributes[MAX_NUM_ATTRIBUTES];
    char name[MAX_NAME_SIZE];
    int recordCount;
    int currentPage;
} Table;

typedef struct Record {
    int id;
    char name[MAX_NAME_SIZE];

} Record;

typedef struct BufferPool{
    struct Page* pages;
    int pageCount;
} BufferPool;

typedef struct Page {
    struct Record records[MAX_NUM_RECORDS];
    char data[MAX_PAGE_SIZE];
} Page;

typedef struct Attribute {
    char name[MAX_NAME_SIZE];
    char type[20];
    // Add constraints or other things. Maybe size
} Attribute;


// Initialization Functions:

void initializeCatalog(Catalog* c);

void initializeTable(Table* table);

// void initializeRecord(Record* record);

void initializeBufferPool(BufferPool* bufferPool);

void initializePage(Page* page, int pageNumber);

void initializeAttribute(Attribute* attribute);


// Insertion Functions:

void addTable(Catalog* c, char name[MAX_NAME_SIZE]);  // recordCount and currentPage initialized as 0

void addRecord(Catalog* c, int pageNumber, int id, char name[MAX_NAME_SIZE]);  // Can we auto-initialize id to the next id instead of hard-coding it in?
                                                                               // Access the Page from the provided Catalog

void addPage(Catalog* c);  // How do we deal with this one?

void addAttribute(Catalog* c, char name[MAX_NAME_SIZE], char type[20]);  // Add more constraints as needed


// Search functions:

void findPage(BufferPool* bufferPool, int pageNumber);  // Do we want to implement this to search for data instead?

#endif
