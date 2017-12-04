package com.acertainbookstore.client.tests;

import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.utils.BookStoreException;

import java.util.Set;

public class Test1Client1 extends Thread {

private int nrOfOp;
private Set<BookCopy> booksToBuy;

public Test1Client1(int nrOfOp, Set<BookCopy> booksToBuy) {
        this.nrOfOp = nrOfOp;
        this.booksToBuy = booksToBuy;
        }

public void run() {

        for(int i = 0; i<nrOfOp ;i ++ )
        {
        try {
        ConcurrencyTest.client.buyBooks(booksToBuy);
        }
        catch (BookStoreException ex) {
        ex.printStackTrace();
        }
        }

        }
        }
