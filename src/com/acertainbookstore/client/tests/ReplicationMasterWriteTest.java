package com.acertainbookstore.client.tests;

import com.acertainbookstore.business.Book;
import com.acertainbookstore.business.CertainBookStore;
import com.acertainbookstore.business.ImmutableStockBook;
import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.client.BookStoreClientConstants;
import com.acertainbookstore.client.ReplicationAwareBookStoreHTTPProxy;
import com.acertainbookstore.client.ReplicationAwareStockManagerHTTPProxy;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.interfaces.BookStoreSerializer;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.*;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.junit.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static junit.framework.TestCase.assertTrue;

public class ReplicationMasterWriteTest {

    /** The Constant TEST_ISBN. */
    private static final Integer TEST_ISBN = 30345650;

    /** The Constant NUM_COPIES. */
    private static final Integer NUM_COPIES = 5;

    /** The local test. */
    private static boolean localTest = false;

    /** The store manager. */
    private static StockManager storeManager;

    /** The serializer. */
    private static ThreadLocal<BookStoreSerializer> serializer;

    private static BookStore bookStoreClient;

    /** The client. */
    private static HttpClient client;

    /** The slave addresses. */
    private static Set<String> slaveAddresses;

    /** The master address. */
    private static String masterAddress;

    /** The file path. */
    private static String filePath = "./proxy.properties";

    /**
     * Initializes a new instance.
     */
    @BeforeClass
    public static void setUpBeforeClass() throws IOException {
        initializeReplicationAwareMappings();

        // Setup the type of serializer.
        if (BookStoreConstants.BINARY_SERIALIZATION) {
            serializer = ThreadLocal.withInitial(BookStoreKryoSerializer::new);
        } else {
            serializer = ThreadLocal.withInitial(BookStoreXStreamSerializer::new);
        }

        client = new HttpClient();

        // Max concurrent connections to every address.
        client.setMaxConnectionsPerDestination(BookStoreClientConstants.CLIENT_MAX_CONNECTION_ADDRESS);

        // Max number of threads.
        client.setExecutor(new QueuedThreadPool(BookStoreClientConstants.CLIENT_MAX_THREADSPOOL_THREADS));

        // Seconds timeout; if no server reply, the request expires.
        client.setConnectTimeout(BookStoreClientConstants.CLIENT_MAX_TIMEOUT_MILLISECS);

        try {
            client.start();
        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            String localTestProperty = System.getProperty(BookStoreConstants.PROPERTY_KEY_LOCAL_TEST);
            localTest = (localTestProperty != null) ? Boolean.parseBoolean(localTestProperty) : localTest;

            if (localTest) {
                CertainBookStore store = new CertainBookStore();
                storeManager = store;
                bookStoreClient = store;
            } else {
                storeManager = new ReplicationAwareStockManagerHTTPProxy();
                bookStoreClient = new ReplicationAwareBookStoreHTTPProxy();
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
     * Checks whether writing is not possible after master server fail
     *
     * Master server after receiving DIE request, responds with an empty list and snapshot id == -1
     * and dies immediately afterwards.
     *
     * After checking for those values in the response the second part of the test is run:
     *      Testing for successful read.
     */
    @Test
    public void testFailedMaster() {
        String urlString = getMasterServerAddress() + "/" + BookStoreMessageTag.DIE;
        BookStoreRequest bookStoreRequest = BookStoreRequest.newPostRequest(urlString, "bye bye");
        BookStoreResponse bookStoreResponse;
        try {
            bookStoreResponse = BookStoreUtility.performHttpExchange(client, bookStoreRequest,
                    serializer.get());
            BookStoreResult bookStoreResult = bookStoreResponse.getResult();

            assertTrue(bookStoreResult.getSnapshotId() == -1);
            assertTrue(bookStoreResult.getList().isEmpty());

            List<StockBook> receivedBooks  = storeManager.getBooks();

            assertTrue(receivedBooks.size() == 1);

            StockBook book = getDefaultBook();
            List<StockBook> booksThatShouldBeThere = new LinkedList<>();
            booksThatShouldBeThere.add(book);

            assertTrue(receivedBooks.containsAll(booksThatShouldBeThere));

        } catch (BookStoreException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialize replication aware mappings.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static void initializeReplicationAwareMappings() throws IOException {
        final String httpProtocol = "http://";
        final String stock = "/stock";

        Properties props = new Properties();
        slaveAddresses = new HashSet<>();

        props.load(new FileInputStream(filePath));
        masterAddress = props.getProperty(BookStoreConstants.KEY_MASTER);

        if (!masterAddress.toLowerCase().startsWith(httpProtocol)) {
            masterAddress = httpProtocol + masterAddress;
        }

        if (!masterAddress.endsWith(stock)) {
            masterAddress = masterAddress + stock;
        }

        String slaveAddressesInternal = props.getProperty(BookStoreConstants.KEY_SLAVE);

        for (String slave : slaveAddressesInternal.split(BookStoreConstants.SPLIT_SLAVE_REGEX)) {
            if (!slave.toLowerCase().startsWith(httpProtocol)) {
                slave = httpProtocol + slave;
            }

            if (!slave.endsWith(stock)) {
                slave = slave + stock;
            }

            slaveAddresses.add(slave);
        }
    }

    /**
     * Gets the master server address.
     *
     * @return the master server address
     */
    private String getMasterServerAddress() {
        return masterAddress;
    }


}
