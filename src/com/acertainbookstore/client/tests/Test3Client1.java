package com.acertainbookstore.client.tests;

import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.business.ImmutableStockBook;
import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.utils.BookStoreException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Test3Client1 extends Thread {

    private int nrOfBooks;
    private int isbn;


    public Test3Client1(int nrOfBooks, int isbn) {
        this.nrOfBooks = nrOfBooks;
        this.isbn = isbn;
    }

    public StockBook getBookToAdd() {
        return new ImmutableStockBook(isbn, "Harry Potter and JUnit", "JK Unit", (float) 10, 5, 0, 0, 0,
                false);
    }

    public void run() {

        for(int i = 0; i<nrOfBooks ;i ++ )
        {

            try {

                Set<StockBook> booksToAdd = new HashSet<StockBook>();
                booksToAdd.add(getBookToAdd());
                ConcurrencyTest.storeManager.addBooks(booksToAdd);
                isbn ++;

            }
            catch (BookStoreException ex) {
                ex.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }

    }
}
