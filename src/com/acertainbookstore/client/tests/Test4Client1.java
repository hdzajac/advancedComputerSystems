package com.acertainbookstore.client.tests;

import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.utils.BookStoreException;

import java.util.Set;

public class Test4Client1 extends Thread {

    private final Integer test2Number;
    private final Set<StockBook> booksToAdd;
    private final Set<Integer> removeBooks;

    public Test4Client1(Integer test2Number, Set<StockBook> booksToAdd, Set<Integer>removeBooks) {
        this.test2Number = test2Number;
        this.booksToAdd = booksToAdd;
        this.removeBooks = removeBooks;
    }

    public void  run(){
        for (int i = 0; i < test2Number; i++){
            try {
                ConcurrencyTest.storeManager.removeBooks(removeBooks);
                ConcurrencyTest.storeManager.addBooks(booksToAdd);
            } catch (BookStoreException e) {
                e.printStackTrace();
            }
        }
    }
}
