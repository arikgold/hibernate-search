//$Id$
package org.hibernate.search.backend.impl;

import java.util.Properties;

import javax.transaction.Synchronization;

import org.hibernate.search.backend.QueueingProcessor;
import org.hibernate.search.backend.Work;
import org.hibernate.search.backend.WorkQueue;
import org.hibernate.search.backend.Worker;
import org.hibernate.search.backend.TransactionContext;
import org.hibernate.search.engine.SearchFactoryImplementor;
import org.hibernate.search.util.LoggerFactory;
import org.hibernate.search.util.WeakIdentityHashMap;
import org.slf4j.Logger;

/**
 * Queue works per transaction.
 * If out of transaction, the work is executed right away
 * <p/>
 * When <code>hibernate.search.worker.type</code> is set to <code>async</code>
 * the work is done in a separate thread (threads are pooled)
 * 
 * @author Emmanuel Bernard
 */
public class TransactionalWorker implements Worker {
	
	//note: there is one Worker instance per SearchFactory, reused concurrently for all sessions.
	
	private static final Logger log = LoggerFactory.make();
	
	//FIXME: discuss the next line! it looks like there actually is concurrent access
	//not a synchronized map since for a given transaction, we have not concurrent access
	protected final WeakIdentityHashMap<Object, Synchronization> synchronizationPerTransaction = new WeakIdentityHashMap<Object, Synchronization>();
	private QueueingProcessor queueingProcessor;

	public void performWork(Work work, TransactionContext transactionContext) {
		if ( transactionContext.isTransactionInProgress() ) {
			Object transactionIdentifier = transactionContext.getTransactionIdentifier();
			PostTransactionWorkQueueSynchronization txSync = ( PostTransactionWorkQueueSynchronization )
					synchronizationPerTransaction.get( transactionIdentifier );
			if ( txSync == null || txSync.isConsumed() ) {
				txSync = new PostTransactionWorkQueueSynchronization(
						queueingProcessor, synchronizationPerTransaction
				);
				transactionContext.registerSynchronization( txSync );
				synchronizationPerTransaction.put( transactionIdentifier, txSync );
			}
			txSync.add( work );
		}
		else {
			// this is a workaround: isTransactionInProgress should return "true"
			// for correct configurations.
			log.warn( "It appears changes are being pushed to the index out of a transaction. " +
					"Register the IndexWorkFlushEventListener listener on flush to correctly manage Collections!" );
			WorkQueue queue = new WorkQueue( 2 ); //one work can be split
			queueingProcessor.add( work, queue );
			queueingProcessor.prepareWorks( queue );
			queueingProcessor.performWorks( queue );
		}
	}

	public void initialize(Properties props, SearchFactoryImplementor searchFactory) {
		this.queueingProcessor = new BatchedQueueingProcessor( searchFactory, props );
	}

	public void close() {
		queueingProcessor.close();
	}

	public void flushWorks(TransactionContext transactionContext) {
		if ( transactionContext.isTransactionInProgress() ) {
			Object transaction = transactionContext.getTransactionIdentifier();
			PostTransactionWorkQueueSynchronization txSync = ( PostTransactionWorkQueueSynchronization )
					synchronizationPerTransaction.get( transaction );
			if ( txSync != null && !txSync.isConsumed() ) {
				txSync.flushWorks();
			}
		}
	}

}
