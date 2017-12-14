package com.acertainbookstore.server;

import com.acertainbookstore.business.ReplicationRequest;
import com.acertainbookstore.business.ReplicationResult;
import com.acertainbookstore.client.BookStoreClientConstants;
import com.acertainbookstore.interfaces.BookStoreSerializer;
import com.acertainbookstore.interfaces.Replication;
import com.acertainbookstore.utils.*;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

/**
 * {@link ReplicationAwareServerHTTPProxy} implements the client side code for
 * replicate RPC, invoked by the master bookstore to propagate updates to
 * slaves, there is one proxy for each destination slave server.
 */
public class ReplicationAwareServerHTTPProxy implements Replication {

	private static ThreadLocal<BookStoreSerializer> serializer;

	/** The client. */
	protected HttpClient client;

	protected String destinationServerAddress;

	/**
	 * Instantiates a new replication aware server HTTP proxy.
	 *
	 * @param destinationServerAddress
	 *            the destination server address
	 */
	public ReplicationAwareServerHTTPProxy(String destinationServerAddress) throws Exception {
		// Setup the type of serializer.
		if (BookStoreConstants.BINARY_SERIALIZATION) {
			serializer = ThreadLocal.withInitial(BookStoreKryoSerializer::new);
		} else {
			serializer = ThreadLocal.withInitial(BookStoreXStreamSerializer::new);
		}

		this.destinationServerAddress = destinationServerAddress;
		client = new HttpClient();

		// Max concurrent connections to every address.
		client.setMaxConnectionsPerDestination(BookStoreClientConstants.CLIENT_MAX_CONNECTION_ADDRESS);

		// Max number of threads.
		client.setExecutor(new QueuedThreadPool(BookStoreClientConstants.CLIENT_MAX_THREADSPOOL_THREADS));

		// Seconds timeout; if no server reply, the request expires.
		client.setConnectTimeout(BookStoreClientConstants.CLIENT_MAX_TIMEOUT_MILLISECS);

		client.start();
	}

	/*
	 * (non-Javadoc)g
	 * 
	 * @see com.acertainbookstore.interfaces.Replication#replicate(com.
	 * acertainbookstore.business.ReplicationRequest)
	 */
	@Override
	public ReplicationResult replicate(ReplicationRequest req) throws BookStoreException {
		String urlString = destinationServerAddress + "/" + req.getMessageType();
		BookStoreRequest bookStoreRequest = BookStoreRequest.newPostRequest(urlString, req.getDataSet());
		BookStoreResponse response = BookStoreUtility.performHttpExchange(client, bookStoreRequest, serializer.get());
		if (response.getException() == null)
			return new ReplicationResult(destinationServerAddress,true);
		else
			return new ReplicationResult(destinationServerAddress,false);
	}

	/**
	 * Stop.
	 */
	public void stop() throws Exception {
		client.stop();
	}
}
