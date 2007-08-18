//$Id$
package org.hibernate.search;

import org.hibernate.classic.Session;

/**
 * Extends the Hibernate {@link Session} with Full text search and indexing capabilities
 *
 * @author Emmanuel Bernard
 */
public interface FullTextSession extends Session {
	/**
	 * Create a Query on top of a native Lucene Query returning the matching objects
	 * of type <code>entities</code> and their respective subclasses.
	 * If no entity is provided, no type filtering is done.
	 */
	FullTextQuery createFullTextQuery(org.apache.lucene.search.Query luceneQuery, Class... entities);

	/**
	 * Force the (re)indexing of a given <b>managed</b> object.
	 * Indexation is batched per transaction
	 */
	void index(Object entity);

	/**
	 * return the SearchFactory
	 */
	SearchFactory getSearchFactory();
}