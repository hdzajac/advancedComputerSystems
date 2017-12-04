package com.acertainbookstore.client.tests;

import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.business.BookEditorPick;
import com.acertainbookstore.utils.BookStoreException;

import java.util.Set;

public class Test5Client1 extends Thread {

    private int nrOfOp;
    private  Set<BookEditorPick> editorPicks;

    public Test5Client1(int nrOfOp,  Set<BookEditorPick> editorPicks) {
        this.nrOfOp = nrOfOp;
        this.editorPicks = editorPicks;
    }

    public void run() {

        for(int i = 0; i<nrOfOp ;i ++ )
        {
            try {
                ConcurrencyTest.storeManager.updateEditorPicks(editorPicks);
            }
            catch (BookStoreException ex) {
                ex.printStackTrace();
            }
        }

    }
}