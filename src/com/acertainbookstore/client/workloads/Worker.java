/**
 * 
 */
package com.acertainbookstore.client.workloads;

<<<<<<< HEAD
import java.util.HashSet;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import com.acertainbookstore.business.Book;
import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.interfaces.BookStore;
import com.acertainbookstore.business.BookCopy;
import com.acertainbookstore.business.StockBook;
import com.acertainbookstore.interfaces.StockManager;
import com.acertainbookstore.utils.BookStoreException;

/**
 * 
 * Worker represents the workload runner which runs the workloads with
 * parameters using WorkloadConfiguration and then reports the results
 * 
 */
public class Worker implements Callable<WorkerRunResult> {
    private WorkloadConfiguration configuration = null;
    private int numSuccessfulFrequentBookStoreInteraction = 0;
    private int numTotalFrequentBookStoreInteraction = 0;

    public Worker(WorkloadConfiguration config) {
	configuration = config;
    }

    /**
     * Run the appropriate interaction while trying to maintain the configured
     * distributions
     * 
     * Updates the counts of total runs and successful runs for customer
     * interaction
     * 
     * @param chooseInteraction
     * @return
     */
    private boolean runInteraction(float chooseInteraction) {
	try {
	    float percentRareStockManagerInteraction = configuration.getPercentRareStockManagerInteraction();
	    float percentFrequentStockManagerInteraction = configuration.getPercentFrequentStockManagerInteraction();

	    if (chooseInteraction < percentRareStockManagerInteraction) {
		runRareStockManagerInteraction();
	    } else if (chooseInteraction < percentRareStockManagerInteraction
		    + percentFrequentStockManagerInteraction) {
		runFrequentStockManagerInteraction();
	    } else {
		numTotalFrequentBookStoreInteraction++;
		runFrequentBookStoreInteraction();
		numSuccessfulFrequentBookStoreInteraction++;
	    }
	} catch (BookStoreException ex) {
	    return false;
	}
	return true;
    }

    /**
     * Run the workloads trying to respect the distributions of the interactions
     * and return result in the end
     */
    public WorkerRunResult call() throws Exception {
	int count = 1;
	long startTimeInNanoSecs = 0;
	long endTimeInNanoSecs = 0;
	int successfulInteractions = 0;
	long timeForRunsInNanoSecs = 0;

	Random rand = new Random();
	float chooseInteraction;

	// Perform the warmup runs
	while (count++ <= configuration.getWarmUpRuns()) {
	    chooseInteraction = rand.nextFloat() * 100f;
	    runInteraction(chooseInteraction);
	}

	count = 1;
	numTotalFrequentBookStoreInteraction = 0;
	numSuccessfulFrequentBookStoreInteraction = 0;

	// Perform the actual runs
	startTimeInNanoSecs = System.nanoTime();
	while (count++ <= configuration.getNumActualRuns()) {
	    chooseInteraction = rand.nextFloat() * 100f;
	    if (runInteraction(chooseInteraction)) {
		successfulInteractions++;
	    }
	}
	endTimeInNanoSecs = System.nanoTime();
	timeForRunsInNanoSecs += (endTimeInNanoSecs - startTimeInNanoSecs);
	return new WorkerRunResult(successfulInteractions, timeForRunsInNanoSecs, configuration.getNumActualRuns(),
		numSuccessfulFrequentBookStoreInteraction, numTotalFrequentBookStoreInteraction);
    }

    /**
     * Runs the new stock acquisition interaction
     * 
     * @throws BookStoreException
     */
    private void runRareStockManagerInteraction() throws BookStoreException {
	// TODO: Add code for New Stock Acquisition Interaction
    }

    /**
     * Runs the stock replenishment interaction
     * 
     * @throws BookStoreException
     */
    private void runFrequentStockManagerInteraction() throws BookStoreException {
		StockManager sm = configuration.getStockManager();
		Integer k = configuration.getNumBooksWithLeastCopies();
		Integer numberAddCopies = configuration.getNumAddCopies();

		List<StockBook> kSmallest = sm.getBooks().stream()
				.sorted(Comparator.comparingInt(StockBook::getNumCopies))
				.limit(k)
				.collect(Collectors.toList());

		Set<BookCopy> kCopies = kSmallest.parallelStream()
				.map(b -> new BookCopy(b.getISBN(), numberAddCopies ))
				.collect(Collectors.toSet());

		sm.addCopies(kCopies);
    }

    /**
     * Runs the customer interaction
     * 
     * @throws BookStoreException
     */
    private void runFrequentBookStoreInteraction() throws BookStoreException {
    	StockManager sm = configuration.getStockManager();
    	BookStore bs = configuration.getBookStore();
    	BookSetGenerator bsg = configuration.getBookSetGenerator();
    	
    	// Gets editor picks
    	List<Book> lep = bs.getEditorPicks(configuration.getNumEditorPicksToGet());
    	Set<Integer> isbns = new HashSet<Integer>();
    	for(Book b : lep) {
    		Integer isbn = b.getISBN();
    		isbns.add(isbn);
    	}
    	
    	// Selects a subset of the books returned by calling sampleFromSetOfISBNs
    	Set<Integer> sample = bsg.sampleFromSetOfISBNs(isbns, configuration.getNumBooksToBuy());
    	Set<BookCopy> booksToBuy = new HashSet<BookCopy>();
		for(Integer isbn : sample) {
			booksToBuy.add(new BookCopy(isbn, configuration.getNumBookCopiesToBuy()));
		}
		
		// Buys the books selected by calling buyBooks
		bs.buyBooks(booksToBuy);
    }

}
