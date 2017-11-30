package com.acertainbookstore.client.tests;

import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.utils.BookStoreException;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class Test2Client2 extends Thread {


    private final Integer test2Number;

    public Test2Client2(Integer test2Number) {
        this.test2Number = test2Number;
    }

    @Test
    public void run() {
        for (int i = 0; i < test2Number; i++ ){
            try {
                List<StockBook> received = ConcurrencyTest.storeManager.getBooks();
                if (!hasCorrectAmounts(received)) ConcurrencyTest.test2Result = false;
            } catch (BookStoreException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean hasCorrectAmounts(List<StockBook> received) {

        Integer reference = received.get(0).getNumCopies();
        StockBook referencedBook = received.get(0);
        if(reference != 0 && reference != 5) {
            System.out.println("Book has " + reference + " copies");
            return false;
        }

        for (StockBook book : received) {
            if(book.getNumCopies() != reference) {
                System.out.println("Reference:  " + reference + " from book: " + referencedBook.toString() +
                        "\nChecked book has: " + book.getNumCopies() + " from book: " + book.toString());
                return false;
            }
        }
        return true;
    }

}
