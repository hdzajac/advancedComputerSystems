package com.acertainbookstore.business;

import java.util.concurrent.Callable;

import com.acertainbookstore.interfaces.Replication;
import com.acertainbookstore.utils.BookStoreException;

/**
 * {@link CertainBookStoreReplicationTask} performs replication to a slave
 * server. It returns the result of the replication on completion using
 * {@link ReplicationResult}.
 */
public class CertainBookStoreReplicationTask implements Callable<ReplicationResult> {

	private Replication replicationClient;
	private ReplicationRequest replicationRequest;

	/**
	 * Instantiates a new certain book store replication task.
	 *
	 * @param replicationClient
	 *            the replication client
	 * @param request
	 *            the request
	 */
	public CertainBookStoreReplicationTask(Replication replicationClient, ReplicationRequest request) {
		this.replicationClient = replicationClient;
		this.replicationRequest = request;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public ReplicationResult call() throws BookStoreException {
		return replicationClient.replicate(replicationRequest);
	}
}
