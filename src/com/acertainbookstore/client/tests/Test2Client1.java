package com.acertainbookstore.client.tests;

import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreException;

import java.util.Set;

/**
 * Created by huber on 29/11/2017.
 */
public class Test2Client1 extends Thread {


    private final Integer test2Number;
    private final Set<BookCopy> booksToAdd;
    private final Set<BookCopy> booksToRemove;

    public Test2Client1(Integer test2Number, Set<BookCopy> booksToAdd, Set<BookCopy> booksToRemove) {
        this.test2Number = test2Number;
        this.booksToAdd = booksToAdd;
        this.booksToRemove = booksToRemove;

    }

    public void run() {

        for (int i = 0; i < test2Number; i++ ){
            try {
                ConcurrencyTest.client.buyBooks(booksToRemove);
                ConcurrencyTest.storeManager.addCopies(booksToAdd);
            } catch (BookStoreException e) {
                e.printStackTrace();
            }
        }
    }

}
