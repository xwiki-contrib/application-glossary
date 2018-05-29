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
package org.xwiki.contrib.glossary.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.glossary.EntryRetrieval;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

/**
 * @version $Id$
 */
@Component
@Singleton
public class DefaultEntryRetrieval implements EntryRetrieval
{
    @Inject
    private QueryManager queryManager;

    @Inject
    private Logger logger;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.contrib.glossary.EntryRetrieval#getGlossaryEntries()
     */

    @Override
    public Map<String, DocumentReference> getGlossaryEntries()
    {
        /*
         * Since 'Glossary entries' will be created as several pages, so firstly we will create a query to find the
         * pages having space name = 'Glossary', and the query will return the 'space' and the 'name' of that page. This
         * 'space' and 'name' will be used to create a Document Reference that will be passed to the
         * map<String,DocumentReference>. This map will be used in transformations.
         */

        Map<String, DocumentReference> glossaryMap = new HashMap<String, DocumentReference>();
        try {
            Query query = this.queryManager.createQuery("select doc.space, doc.name where doc.space like 'Glossary.%'",
                Query.XWQL);
            List<Object[]> glossaryList = (List<Object[]>) (List) query.execute();
            for (Object[] glossaryData : glossaryList) {
                String space = (String) glossaryData[0];
                String name = (String) glossaryData[1];
                DocumentReference reference = new DocumentReference("wiki", space, name);
                glossaryMap.put(name, reference);
            }

            return glossaryMap;

        } catch (QueryException e) {
            this.logger.error("Failure in getting entries", e);
            return null;
        }
    }

}
