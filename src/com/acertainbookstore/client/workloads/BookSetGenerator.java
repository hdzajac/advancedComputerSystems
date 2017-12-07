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


	private Random rnd;
	private StringBuilder sb ;

	public BookSetGenerator() {
		// TODO Auto-generated constructor stub
		rnd = new Random();
		sb = new StringBuilder();
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
		List<StockBook> l = new ArrayList<StockBook>();
		int isbn, numCopies;
		long numSaleMisses, numTimesRated, totalRating;
		float price;
		boolean editorPick;

		for(int i = 0; i< num; i++) {
			isbn = rnd.nextInt(5000)+i;;
			String title = rndString(30);
			String author = rndString(15);
			price = rnd.nextFloat()*50;
			numCopies = rnd.nextInt(5)+1;
			numSaleMisses = rndLong(0, 100);
			numTimesRated = rndLong(0, 100);
			totalRating = rndLong(0, 5);
			editorPick = rnd.nextBoolean();
			ImmutableStockBook isb = new ImmutableStockBook(isbn,title,author,price,numCopies,numSaleMisses,numTimesRated, totalRating, editorPick);
			l.add(isb);
		}
		
		return new HashSet<StockBook>(l);
	}
	
	// Generates a random long number between min and max
	private long rndLong(long min, long max) {


		 long rndlong = min + (long)(rnd.nextDouble()*(max - min));
		 return rndlong;
	}
	
	// Generates a random string of lower-case characters of length 'len'
	private String rndString(int len) {
		char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
		char c;
		for (int i = 0; i < len; i++) {
			c = chars[rnd.nextInt(chars.length)];
		    sb.append(c);
		}
		String output = sb.toString();
		return output;
	}

}
