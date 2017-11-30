package com.acertainbookstore.client.tests;

import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.utils.BookStoreException;

import java.util.List;
import java.util.Set;

public class Test3Client2 extends Thread {

    private List<StockBook> booksInStorePreTest;
    private int nrBooksAdded;

    public Test3Client2(List<StockBook> booksInStorePreTest, int nrBooksAdded) {
        this.booksInStorePreTest = booksInStorePreTest;
        this.nrBooksAdded = nrBooksAdded;
    }

    public void run() {

            try {
                List<StockBook> booksAfter =  ConcurrencyTest.storeManager.getBooks();
                ConcurrencyTest.test3Result = ( booksInStorePreTest.size() + 1000) == booksAfter.size();

            }
            catch (BookStoreException ex) {
                ex.printStackTrace();
            }
    }

}