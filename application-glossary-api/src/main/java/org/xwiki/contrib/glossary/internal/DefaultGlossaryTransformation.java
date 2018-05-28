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
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.glossary.GlossaryTransformation;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.LinkBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.internal.block.ProtectedBlockFilter;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.transformation.AbstractTransformation;
import org.xwiki.rendering.transformation.TransformationContext;

/**
 * Create Transformation for Glossary Application.
 * 
 * @version $Id$
 */
@Component
@Named("glossary")
@Singleton
public class DefaultGlossaryTransformation extends AbstractTransformation implements GlossaryTransformation
{
    @Inject
    private QueryManager queryManager;

    @Inject
    private Logger logger;

    @Inject
    private EntityReferenceSerializer<String> serializer;

    @Inject
    private ProtectedBlockFilter filter = new ProtectedBlockFilter();

    /**
     * @return the names of glossary entries.
     * @throws QueryException when no object is found.
     */
    public Map<String, DocumentReference> getGlossaryEntries()
    {
        /*
         * Since 'Glossary entries' will be created as several pages, so firstly we will create a query to find the
         * pages having space name = 'Glossary', and the query will return the 'space' and the 'name' of that page. This
         * 'space' and 'name' will be used to create a Document Reference that will be passed to the map<String,
         * Document Reference>.
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

    @Override
    public void transform(Block block, TransformationContext context)
    {
        Map<String, DocumentReference> result = getGlossaryEntries();

        for (WordBlock wordBlock : this.filter.getChildrenByType(block, WordBlock.class, true)) {

            String word = wordBlock.getWord();

            // Checking if the map 'result' contains the 'glossary' word.

            if (result.containsKey(word)) {
                // Taking the DocumentReference from the map and converting it to ResourceReference
                // using 'EntityReferenceSerializer' because link block takes 'Resource Reference' as an argument.
                DocumentReference reference = result.get(word);
                String serial = serializer.serialize(reference);
                ResourceReference linkReference = new DocumentResourceReference(serial);
                wordBlock.getParent().replaceChild(new LinkBlock(wordBlock.getChildren(), linkReference, false),
                    wordBlock);

            }

        }

    }
}
