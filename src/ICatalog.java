import java.io.IOException;

public interface ICatalog {
    public void addTable(Catalog c, TableSchema table);
    public void dropTable(Catalog c, String name);
    public void addPage(Page newPage);
    public void addAttribute(Catalog c, String name, String type);
    public void displayCatalog(Catalog c);
    public int tableExists(Catalog c, String name);
    public int findTableDisplay(Catalog c, String name);
    public void writeCatalogToFile(Catalog c, String pathname) throws IOException;
    public void readCatalogFromFile(Catalog c, String pathname) throws IOException;
}
