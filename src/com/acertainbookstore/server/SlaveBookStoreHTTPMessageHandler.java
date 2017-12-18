package com.acertainbookstore.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedList;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.acertainbookstore.business.ReplicationRequest;
import com.acertainbookstore.business.ReplicationResult;
import com.acertainbookstore.business.SlaveCertainBookStore;
import com.acertainbookstore.interfaces.BookStoreSerializer;
import com.acertainbookstore.utils.BookStoreConstants;
import com.acertainbookstore.utils.BookStoreException;
import com.acertainbookstore.utils.BookStoreKryoSerializer;
import com.acertainbookstore.utils.BookStoreMessageTag;
import com.acertainbookstore.utils.BookStoreRequest;
import com.acertainbookstore.utils.BookStoreResponse;
import com.acertainbookstore.utils.BookStoreResult;
import com.acertainbookstore.utils.BookStoreUtility;
import com.acertainbookstore.utils.BookStoreXStreamSerializer;
import com.esotericsoftware.kryo.io.Input;

/**
 * {@link SlaveBookStoreHTTPMessageHandler} implements the message handler class
 * which is invoked to handle messages received by the slave book store HTTP
 * server It decodes the HTTP message and invokes the
 * {@link SlaveCertainBookStore} API.
 */
public class SlaveBookStoreHTTPMessageHandler extends AbstractHandler {

	/** The book store. */
	private SlaveCertainBookStore myBookStore = null;

	/** The serializer. */
	private static ThreadLocal<BookStoreSerializer> serializer;

	/**
	 * Instantiates a new slave book store HTTP message handler.
	 *
	 * @param bookStore
	 *            the book store
	 */
	public SlaveBookStoreHTTPMessageHandler(SlaveCertainBookStore bookStore) {
		myBookStore = bookStore;

		// Setup the type of serializer.
		if (BookStoreConstants.BINARY_SERIALIZATION) {
			serializer = ThreadLocal.withInitial(BookStoreKryoSerializer::new);
		} else {
			serializer = ThreadLocal.withInitial(BookStoreXStreamSerializer::new);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jetty.server.Handler#handle(java.lang.String,
	 * org.eclipse.jetty.server.Request, javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse)
	 */
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		BookStoreMessageTag messageTag;
		String requestURI;

		response.setStatus(HttpServletResponse.SC_OK);
		requestURI = request.getRequestURI();

		// Need to do request multiplexing
		if (!BookStoreUtility.isEmpty(requestURI) && requestURI.toLowerCase().startsWith("/stock")) {
			// The request is from the store manager; more sophisticated.
			// security features could be added here.
			messageTag = BookStoreUtility.convertURItoMessageTag(requestURI.substring(6));
		} else {
			messageTag = BookStoreUtility.convertURItoMessageTag(requestURI);
		}

		// The RequestURI before the switch.
		if (messageTag == null) {
			System.err.println("No message tag.");
		} else {
			switch (messageTag) {
			case ADDBOOKS:
				replicateHandler(request, response, messageTag);
				break;

			/** The tag for the add copies message. */
			case ADDCOPIES:
				replicateHandler(request,response, messageTag);
				break;

			/** The tag for the buy books message. */
			case BUYBOOKS:
				replicateHandler(request, response, messageTag);
				break;

			/** The tag for the update editor picks message. */
			case UPDATEEDITORPICKS:
				replicateHandler(request,response, messageTag);
				break;
			/** The tag for the remove all books message. */
			case REMOVEALLBOOKS:
				replicateHandler(request,response, messageTag);
				break;
			/** The tag for the remove books message. */
			case REMOVEBOOKS:
				replicateHandler(request,response, messageTag);
				break;
				
			case LISTBOOKS:
				listBooks(response);
				break;

			case GETBOOKS:
				getBooks(request, response);
				break;

			case GETEDITORPICKS:
				getEditorPicks(request, response);
				break;

			case GETSTOCKBOOKSBYISBN:
				getStockBooksByISBN(request, response);
				break;

			case DIE:
				handleDying(request, response);
				System.out.println("Goodbye cruel world");
				System.exit(1);
				break;

			default:
				System.err.println("Unsupported message tag.");
				break;
			}
		}

		// Mark the request as handled so that the HTTP response can be sent
		baseRequest.setHandled(true);
	}

	private void handleDying(HttpServletRequest request, HttpServletResponse response) throws IOException {
		BookStoreResponse bookStoreResponse = new BookStoreResponse();
		BookStoreResult bookStoreResult = new BookStoreResult(new LinkedList<>(), -1);
		bookStoreResponse.setResult(bookStoreResult);
		byte[] serializedResponseContent = serializer.get().serialize(bookStoreResponse);
		response.getOutputStream().write(serializedResponseContent);
		response.flushBuffer();
		response.getOutputStream().close();
	}


	private void replicateHandler(HttpServletRequest request, HttpServletResponse response, BookStoreMessageTag messageTag) throws IOException {
		byte[] serializedRequestContent = getSerializedRequestContent(request);
		Set<?> dataSet = (Set<?>) serializer.get().deserialize(serializedRequestContent);
		
		ReplicationRequest req = new ReplicationRequest(dataSet, messageTag);
		BookStoreResponse bookStoreResult = new BookStoreResponse();
		try {
			ReplicationResult res = myBookStore.replicate(req);
			
			if (!res.isReplicationSuccessful()) {
				bookStoreResult.setException(new BookStoreException());
			}
			
		} catch (BookStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		byte[] serializedResponseContent = serializer.get().serialize(bookStoreResult);
		response.getOutputStream().write(serializedResponseContent);
	}

	

	/**
	 * Gets the stock books by ISBN.
	 *
	 * @param request
	 *            the request
	 * @param response
	 *            the response
	 * @return the stock books by ISBN
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@SuppressWarnings("unchecked")
	private void getStockBooksByISBN(HttpServletRequest request, HttpServletResponse response) throws IOException {
		byte[] serializedRequestContent = getSerializedRequestContent(request);

		Set<Integer> isbnSet = (Set<Integer>) serializer.get().deserialize(serializedRequestContent);
		BookStoreResponse bookStoreResponse = new BookStoreResponse();

		try {
			BookStoreResult bookStoreResult = myBookStore.getBooksByISBN(isbnSet);
			bookStoreResponse.setResult(bookStoreResult);
		} catch (BookStoreException ex) {
			bookStoreResponse.setException(ex);
		}

		byte[] serializedResponseContent = serializer.get().serialize(bookStoreResponse);
		response.getOutputStream().write(serializedResponseContent);
	}

	/**
	 * Gets the editor picks.
	 *
	 * @param request
	 *            the request
	 * @param response
	 *            the response
	 * @return the editor picks
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void getEditorPicks(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String numBooksString = URLDecoder.decode(request.getParameter(BookStoreConstants.BOOK_NUM_PARAM), "UTF-8");
		BookStoreResponse bookStoreResponse = new BookStoreResponse();

		try {
			int numBooks = BookStoreUtility.convertStringToInt(numBooksString);
			BookStoreResult bookStoreResult = myBookStore.getEditorPicks(numBooks);
			bookStoreResponse.setResult(bookStoreResult);
		} catch (BookStoreException ex) {
			bookStoreResponse.setException(ex);
		}

		byte[] serializedResponseContent = serializer.get().serialize(bookStoreResponse);
		response.getOutputStream().write(serializedResponseContent);
	}

	/**
	 * Gets the books.
	 *
	 * @param request
	 *            the request
	 * @param response
	 *            the response
	 * @return the books
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	@SuppressWarnings("unchecked")
	private void getBooks(HttpServletRequest request, HttpServletResponse response) throws IOException {
		byte[] serializedRequestContent = getSerializedRequestContent(request);

		Set<Integer> isbnSet = (Set<Integer>) serializer.get().deserialize(serializedRequestContent);
		BookStoreResponse bookStoreResponse = new BookStoreResponse();

		try {
			BookStoreResult bookStoreResult = myBookStore.getBooks(isbnSet);
			bookStoreResponse.setResult(bookStoreResult);
		} catch (BookStoreException ex) {
			bookStoreResponse.setException(ex);
		}

		byte[] serializedResponseContent = serializer.get().serialize(bookStoreResponse);
		response.getOutputStream().write(serializedResponseContent);
	}

	/**
	 * Lists the books.
	 *
	 * @param response
	 *            the response
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void listBooks(HttpServletResponse response) throws IOException {
		BookStoreResponse bookStoreResponse = new BookStoreResponse();

		try {
			BookStoreResult bookStoreResult = myBookStore.getBooks();
			bookStoreResponse.setResult(bookStoreResult);
		} catch (BookStoreException e) {
			bookStoreResponse.setException(e);
		}

		byte[] serializedResponseContent = serializer.get().serialize(bookStoreResponse);
		response.getOutputStream().write(serializedResponseContent);
	}

	/**
	 * Gets the serialized request content.
	 *
	 * @param request
	 *            the request
	 * @return the serialized request content
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private byte[] getSerializedRequestContent(HttpServletRequest request) throws IOException {
		Input in = new Input(request.getInputStream());
		byte[] serializedRequestContent = in.readBytes(request.getContentLength());
		in.close();
		return serializedRequestContent;
	}
}
