package com.acertainbookstore.business;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.acertainbookstore.interfaces.ReplicatedReadOnlyBookStore;
import com.acertainbookstore.interfaces.ReplicatedReadOnlyStockManager;
import com.acertainbookstore.interfaces.Replication;
import com.acertainbookstore.utils.BookStoreException;
import com.acertainbookstore.utils.BookStoreMessageTag;
import com.acertainbookstore.utils.BookStoreUtility;

/**
 * {@link SlaveCertainBookStore} is a wrapper over the CertainBookStore class
 * and supports the ReplicatedReadOnlyBookStore and
 * ReplicatedReadOnlyStockManager interfaces.
 * 
 * This class must also handle replication requests sent by the master.
 */
public class SlaveCertainBookStore extends ReadOnlyCertainBookStore
		implements ReplicatedReadOnlyBookStore, ReplicatedReadOnlyStockManager, Replication {

	/**
	 * Instantiates a new slave certain book store.
	 */
	public SlaveCertainBookStore() {
		bookStore = new CertainBookStore();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.acertainbookstore.interfaces.Replication#replicate(com.
	 * acertainbookstore.business.ReplicationRequest)
	 */
	@Override
	public synchronized ReplicationResult replicate(ReplicationRequest req) throws BookStoreException {
		BookStoreMessageTag messageTag;
		String requestURI;
		this.snapshotId++;
		
		messageTag = req.getMessageType();
		boolean replRes = false;
		if (messageTag == null) {
			System.err.println("No message tag.");
		} else {
			switch (messageTag) {
			case ADDBOOKS:
				replRes = addBooks(req);
				break;

			/** The tag for the add copies message. */
			case ADDCOPIES:
				replRes = addCopies(req);
				break;

			/** The tag for the buy books message. */
			case BUYBOOKS:
				replRes = buyBooks(req);
				break;

			/** The tag for the update editor picks message. */
			case UPDATEEDITORPICKS:
				replRes = updateEditorPicks(req);
				break;
			/** The tag for the remove all books message. */
			case REMOVEALLBOOKS:
				replRes = removeAllBooks(req);
				break;
			/** The tag for the remove books message. */
			case REMOVEBOOKS:
				replRes = removeBooks(req);
				break;
			case DIE:
				System.exit(1);

			break;

			default:
				System.err.println("Unsupported message tag.");
				break;
			}
		}
		return new ReplicationResult("" ,replRes); // TODO: find address
		
		//throw new BookStoreException("This method needs to be implemented.");
	}
	
	private boolean removeBooks(ReplicationRequest req) {
		try {
			this.bookStore.removeBooks((Set<Integer>)req.getDataSet());
			return true;
		} catch (BookStoreException e) {
			return false;
		}
	}

	private boolean removeAllBooks(ReplicationRequest req) {
		try {
			this.bookStore.removeAllBooks();
			return true;
		} catch (BookStoreException e) {
			return false;
		}
	}

	private boolean updateEditorPicks(ReplicationRequest req) {
		try {
			this.bookStore.updateEditorPicks((Set<BookEditorPick>) req.getDataSet());
			return true;
		} catch (BookStoreException e) {
			return false;
		}
	}

	private boolean buyBooks(ReplicationRequest req) {
		try {
			this.bookStore.buyBooks((Set<BookCopy>)req.getDataSet());
			return true;
		} catch (BookStoreException e) {
			return false;
		}
	}

	private boolean addCopies(ReplicationRequest req) {
		try {
			this.bookStore.addCopies((Set<BookCopy>) req.getDataSet());
			return true;
		} catch (BookStoreException e) {
			return false;
		}
	}

	private boolean addBooks(ReplicationRequest req) {
		try {
			this.bookStore.addBooks((Set<StockBook>) req.getDataSet());
			return true;
		} catch (BookStoreException e) {
			return false;
		}
	}
}