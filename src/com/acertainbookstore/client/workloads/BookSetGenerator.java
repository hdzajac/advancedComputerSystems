package com.acertainbookstore.client.workloads;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.acertainbookstore.business.ImmutableStockBook;
import com.acertainbookstore.business.StockBook;

/**
 * Helper class to generate stockbooks and isbns modelled similar to Random
 * class
 */
public class BookSetGenerator {

	public BookSetGenerator() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Returns num randomly selected isbns from the input set
	 * 
	 * @param num
	 * @return
	 */
	public Set<Integer> sampleFromSetOfISBNs(Set<Integer> isbns, int num) {
		List<Integer> l = new ArrayList<>();
		l.addAll(isbns);
		Collections.shuffle(l);
		return new HashSet<Integer>(l.subList(0, num));
	}

	/**
	 * Return num stock books. For now return an ImmutableStockBook
	 * 
	 * @param num
	 * @return
	 */
	public Set<StockBook> nextSetOfStockBooks(int num) {
		Random rnd = new Random();
		List<StockBook> l = new ArrayList<StockBook>();
		
		for(int i = 0; i< num; i++) {
			int isbn = i;
			int isbn = rnd.nextInt(5000)+i;;
			String title = rndString(30);
			String author = rndString(15);
			float price = rnd.nextFloat()*50;
			int numCopies = rnd.nextInt(5)+1;
			long numSaleMisses = rndLong(0, 100);
			long numTimesRated = rndLong(0, 100);
			long totalRating = rndLong(0, 5);
			boolean editorPick = rnd.nextBoolean();
			
			ImmutableStockBook isb = new ImmutableStockBook(isbn,title,author,price,numCopies,numSaleMisses,numTimesRated, totalRating, editorPick);
			l.add(isb);
		}
		
		return new HashSet<StockBook>(l);
	}
	
	// Generates a random long number between min and max
	private long rndLong(long min, long max) {
		 Random rnd = new Random();

		 long rndlong = min + (long)(rnd.nextDouble()*(max - min));
		 return rndlong;
	}
	
	// Generates a random string of lower-case characters of length 'len'
	private String rndString(int len) {
		char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
		StringBuilder sb = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < len; i++) {
		    char c = chars[random.nextInt(chars.length)];
		    sb.append(c);
		}
		String output = sb.toString();
		return output;
	}

}
