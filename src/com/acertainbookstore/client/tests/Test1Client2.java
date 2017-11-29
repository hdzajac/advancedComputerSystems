package com.acertainbookstore.client.tests;

import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreException;

import java.util.Set;

public class Test1Client2 extends Thread {

    private int nrOfOp;
    private Set<BookCopy> bookCopiesSet;

    public Test1Client2(int nrOfOp, Set<BookCopy> bookCopiesSet) {
        this.nrOfOp = nrOfOp;
        this.bookCopiesSet = bookCopiesSet;
    }

    public void run() {

        for(int i = 0; i<nrOfOp ;i ++ )
        {
            try {
                ConcurrencyTest.storeManager.addCopies(bookCopiesSet);
                System.out.println("Add copies ");
            }
            catch (BookStoreException ex) {
                ;
            }
        }

    }
}