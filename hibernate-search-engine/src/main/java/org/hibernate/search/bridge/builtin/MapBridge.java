/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.hibernate.search.bridge.builtin;

import java.util.Map;

import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;

/**
 * Each entry ({@code null included}) of the values in a {@link Map} is indexed using the specified {@link FieldBridge}.
 * <br>
 * A {@code null} array is not indexed.
 *
 * @author Davide D'Alto
 */
public class MapBridge implements FieldBridge {

	private final FieldBridge bridge;

	/**
	 * @param bridge
	 *            the {@link FieldBridge} used for each entry of a {@link java.util.Map} object.
	 */
	public MapBridge(FieldBridge bridge) {
		this.bridge = bridge;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.hibernate.search.bridge.FieldBridge#set(java.lang.String, java.lang.Object, org.apache.lucene.document.Document, org.hibernate.search.bridge.LuceneOptions)
	 */
	@Override
	public void set(String fieldName, Object value, Document document, LuceneOptions luceneOptions) {
		if ( value != null ) {
			indexNotNullMap( fieldName, value, document, luceneOptions );
		}
	}

	protected void indexNotNullMap(String name, Object value, Document document, LuceneOptions luceneOptions) {
		Iterable<?> collection = ((Map<?,?>) value).values();
		for ( Object entry : collection ) {
			indexEntry( name, entry, document, luceneOptions );
		}
	}

	protected void indexEntry(String fieldName, Object entry, Document document, LuceneOptions luceneOptions) {
		bridge.set( fieldName, entry, document, luceneOptions );
	}

}
