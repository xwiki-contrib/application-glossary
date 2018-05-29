/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.contrib.glossary;

import java.util.Map;

import org.xwiki.model.reference.DocumentReference;
import org.xwiki.query.QueryException;

/**
 * The model for retrieving glossary entries from the 'Glossary Space'. The entries received will be used in
 * transformations.
 * 
 * @version $Id$
 */
public interface EntryRetrieval
{
    /**
     * Map to be used to retrieve glossary entries.
     * 
     * @throws QueryException when no object is found.
     * @return a map containing the glossary entries in String form along with it's DocumentReference.
     */
    Map<String, DocumentReference> getGlossaryEntries();
}
