package com.acertainbookstore.client.tests;

import com.acertainbookstore.business.CertainBookStore;
import com.acertainbookstore.business.ImmutableStockBook;
import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.client.ReplicationAwareBookStoreHTTPProxy;
import com.acertainbookstore.client.ReplicationAwareStockManagerHTTPProxy;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreConstants;
import com.acertainbookstore.utils.BookStoreException;
import org.junit.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static junit.framework.TestCase.assertTrue;

public class ReplicationTest {

    /** The Constant TEST_ISBN. */
    private static final Integer TEST_ISBN = 30345650;

    /** The Constant NUM_COPIES. */
    private static final Integer NUM_COPIES = 5;

    /** The local test. */
    private static boolean localTest = false;

    /** The store manager. */
    private static StockManager storeManager;

    /** The client. */
    private static BookStore client;

    /**
     * Initializes a new instance.
     */
    @BeforeClass
    public static void setUpBeforeClass() {
        try {
            String localTestProperty = System.getProperty(BookStoreConstants.PROPERTY_KEY_LOCAL_TEST);
            localTest = (localTestProperty != null) ? Boolean.parseBoolean(localTestProperty) : localTest;

            if (localTest) {
                CertainBookStore store = new CertainBookStore();
                storeManager = store;
                client = store;
            } else {
                storeManager = new ReplicationAwareStockManagerHTTPProxy();
                client = new ReplicationAwareBookStoreHTTPProxy();
            }

            storeManager.removeAllBooks();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper method to get the default book used by initializeBooks.
     *
     * @return the default book
     */
    public StockBook getDefaultBook() {
        return new ImmutableStockBook(TEST_ISBN, "Harry Potter and JUnit", "JK Unit", (float) 10, NUM_COPIES, 0, 0, 0,
                false);
    }

    /**
     * Method to add a book, executed before every test case is run.
     *
     * @throws BookStoreException
     *             the book store exception
     */
    @Before
    public void initializeBooks() throws BookStoreException {
        Set<StockBook> booksToAdd = new HashSet<StockBook>();
        booksToAdd.add(getDefaultBook());

        storeManager.addBooks(booksToAdd);
    }

    /**
     * Method to clean up the book store, execute after every test case is run.
     *
     * @throws BookStoreException
     *             the book store exception
     */
    @After
    public void cleanupBooks() throws BookStoreException {
        storeManager.removeAllBooks();
    }

    /**
     * Checks whether the insertion of a books with initialize books worked.
     *
     * @throws BookStoreException
     *             the book store exception
     */
    @Test
    public void testInitializeBooks() throws BookStoreException {
        List<StockBook> addedBooks = new ArrayList<StockBook>();
        addedBooks.add(getDefaultBook());

        List<StockBook> listBooks = null;
        listBooks = storeManager.getBooks();

        assertTrue(addedBooks.containsAll(listBooks) && addedBooks.size() == listBooks.size());
    }




    /**
     * Tear down after class.
     *
     * @throws BookStoreException
     *             the book store exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws BookStoreException {
        storeManager.removeAllBooks();

        if (!localTest) {
            ((ReplicationAwareBookStoreHTTPProxy) client).stop();
            ((ReplicationAwareStockManagerHTTPProxy) storeManager).stop();
        }

    }


}
