package com.acertainbookstore.client.tests;

import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.utils.BookStoreException;
import org.junit.Test;

import java.util.List;
import java.util.Set;

public class Test4Client2 extends Thread{

    private final Integer test2Number;
    private final Integer wholeSet;

    public Test4Client2(Integer test2Number, Integer wholeSet) {
        this.test2Number = test2Number;
        this.wholeSet = wholeSet;
    }

    @Test
    public void run() {
        for (int i = 0; i < test2Number; i++ ){
            try {
                List<StockBook> received = ConcurrencyTest.storeManager.getBooks();
                if (received.size() != 0 && received.size() != wholeSet) ConcurrencyTest.testAddRemoveBooksConcurrentResult = false;
            } catch (BookStoreException e) {
                e.printStackTrace();
            }
        }
    }
}
