import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.LinkedList;

import java.util.ArrayList;
import java.util.Collections;

public class BPlusTest {

    public static void main(String[] args) {
        AttributeSchema attr1 = new AttributeSchema("num", "integer", false, false, true, Integer.BYTES);

        BPlusTree BPlusTest = new BPlusTree(attr1, 0);

        ArrayList<Byte> nullBitMap = new ArrayList<>(Collections.nCopies(2, (byte) 0));
        int int1 = 18;
        String string1 = "texty";
        ArrayList<Object> recTuple1 = new ArrayList<>();
        recTuple1.add(int1);
        recTuple1.add(string1);
        Record record1 = new Record(recTuple1, Integer.BYTES + 20, nullBitMap);

        for (int i = 1; i <= 28; i++) {
            BPlusTest.insert(record1, i, 0);
        }
        BPlusTest.insert(record1, 3, 0);
        
        //Create a BPlusTree instance with appropriate attribute schema and order
        AttributeSchema attr = new AttributeSchema("num", "integer", false, false, true, Integer.BYTES);
        BPlusTree bPlusTree = new BPlusTree(attr, 0);
        ArrayList<Byte> nullBitMap1 = new ArrayList<>(Collections.nCopies(2, (byte) 0));
        int int2 = 19;
        String string2 = "texty";
        ArrayList<Object> recTuple2 = new ArrayList<>();
        recTuple2.add(int2);
        recTuple2.add(string2);
        Record record = new Record(recTuple2, Integer.BYTES + 20, nullBitMap1);

        BPlusTest.display();
        System.out.println();
        BPlusTest.delete(17);

        BPlusTest.display();
        System.out.println();
        BPlusTest.delete(13);

        BPlusTest.display();
        System.out.println();
        BPlusTest.delete(5);

        BPlusTest.display();
        System.out.println();
        BPlusTest.delete(15);

        BPlusTest.display();
        System.out.println();
        BPlusTest.delete(3);

        BPlusTest.display();
        System.out.println();
        BPlusTest.delete(4);

        BPlusTest.display();
        System.out.println();



        // Test basic insertion
         //testBasicInsertion(bPlusTree, record);
 
         // Test insertion into non-empty tree
         //testInsertionIntoNonEmptyTree(bPlusTree, record);
 
         // Test insertion of duplicates
         //testInsertionOfDuplicates(bPlusTree, record);
 
         // Test insertion into full leaf nodes
         //testInsertionIntoFullLeafNodes(bPlusTree, record);
 
         // Test insertion into full internal nodes
         //testInsertionIntoFullInternalNodes(bPlusTree, record);
 
         // Test insertion at different levels
         //testInsertionAtDifferentLevels(bPlusTree, record);
 
         
 
         // Test insertion with large data sets
        //testInsertionWithLargeDataSets(bPlusTree, record);
 
         // Test insertion and search
         //testInsertionAndSearch(bPlusTree, record);
 
         // Test edge cases and boundary conditions
         //testEdgeCasesAndBoundaryConditions(bPlusTree);

        
     }

     // Test basic insertion
     private static void testBasicInsertion(BPlusTree bPlusTree, Record record) {

         System.out.println("Testing Basic Insertion:");
         bPlusTree.insert(record, 10, 0);
         bPlusTree.insert(record, 20, 1);
         bPlusTree.display();
     }

     // Test insertion into non-empty tree
     private static void testInsertionIntoNonEmptyTree(BPlusTree bPlusTree, Record record) {

         System.out.println("\nTesting Insertion into Non-empty Tree:");
         bPlusTree.insert(record, 30, 2);
         bPlusTree.insert(record, 15, 3);
         bPlusTree.display();
     }

     // Test insertion of duplicates
     private static void testInsertionOfDuplicates(BPlusTree bPlusTree, Record record) {

         System.out.println("\nTesting Insertion of Duplicates:");
         bPlusTree.insert(record, 20, 4); // Attempt to insert a duplicate key
         bPlusTree.display();
     }

     // Test insertion into full leaf nodes
     private static void testInsertionIntoFullLeafNodes(BPlusTree bPlusTree, Record record) {

         System.out.println("\nTesting Insertion into Full Leaf Nodes:");
         for (int i = 4; i <= 10; i++) {
             bPlusTree.insert(record, i * 10, i);
         }
         bPlusTree.display();
     }

     // Test insertion into full internal nodes
     private static void testInsertionIntoFullInternalNodes(BPlusTree bPlusTree, Record record) {

         System.out.println("\nTesting Insertion into Full Internal Nodes:");
         for (int i = 11; i <= 15; i++) {
             bPlusTree.insert(record, i * 10, i);
         }
         bPlusTree.display();
     }

     // Test insertion at different levels
     private static void testInsertionAtDifferentLevels(BPlusTree bPlusTree, Record record) {

         System.out.println("\nTesting Insertion at Different Levels:");
         bPlusTree.insert(record, 5, 16); // Insert into leaf node
         bPlusTree.insert(record, 25, 17); // Insert into internal node
         bPlusTree.display();
     }

     // Test insertion with large data sets
     private static void testInsertionWithLargeDataSets(BPlusTree bPlusTree, Record record) {

         System.out.println("\nTesting Insertion with Large Data Sets:");
         // Insert a large number of records into the tree and measure performance
         System.out.println("Inserting large data sets:");
         for (int i = 0; i < 500; i++) {
             bPlusTree.insert(record, i, i);
         }
         bPlusTree.display();
     }

     // Test insertion and search
     private static void testInsertionAndSearch(BPlusTree bPlusTree, Record record) {

         System.out.println("\nTesting Insertion and Search:");
         // Insert records into the tree and perform searches for inserted keys
         System.out.println("Inserting records and searching for keys:");
         for (int i = 0; i < 10; i++) {
             bPlusTree.insert(record, i, i);
         }
         System.out.println("Searching for keys:");
         for (int i = 0; i < 10; i++) {
             System.out.println("Search for key " + i + ": " + bPlusTree.search(i));
         }
     }

     // Test edge cases and boundary conditions
     private static void testEdgeCasesAndBoundaryConditions(BPlusTree bPlusTree) {
        System.out.println("\nTesting Edge Cases and Boundary Conditions:");
        // Test various edge cases such as inserting into an empty tree, root node splitting, etc.
        System.out.println("Testing edge cases and boundary conditions:");

        // Check if the root node is null before displaying
        if (bPlusTree == null) {
            System.out.println("Empty tree. Nothing to display.");
        } else {
            bPlusTree.display();
        }
     }




    }
