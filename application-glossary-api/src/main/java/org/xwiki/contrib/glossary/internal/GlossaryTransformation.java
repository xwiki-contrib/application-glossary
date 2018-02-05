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

import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.LinkBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.internal.block.ProtectedBlockFilter;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.transformation.AbstractTransformation;
import org.xwiki.rendering.transformation.TransformationContext;
import org.xwiki.rendering.transformation.TransformationException;

/**
 * Create Transformation for Glossary Application.
 * 
 * @version $Id$
 */
@Component
@Named("glossaryTansformation")
@Singleton
public class GlossaryTransformation extends AbstractTransformation
{
    @Inject
    private QueryManager queryManager;

    @Inject
    private Logger logger;

    @Inject
    @Named("current")
    private DocumentReferenceResolver<String> resolver;

    @Inject
    private EntityReferenceSerializer<String> serializer;

    private ProtectedBlockFilter filter = new ProtectedBlockFilter();

    /**
     * @return the names of glossary entries.
     * @throws QueryException when no object is found.
     */
    public Map<String, String> getGlossaryEntries()
    {

        Query query;
        try {
            query = this.queryManager.createQuery("where doc.space like 'Glossary.%'", Query.XWQL);
            List<String> glossaryList = new ArrayList<String>();
            glossaryList = query.execute(); 
            Map<String, String> glossaryMap = new HashMap<String, String>();
            for(String str : glossaryList) {
                glossaryMap.put(str, null);
            }
            return glossaryMap;
            
        } catch (QueryException e) {
            this.logger.error("Failure in getting entries", e);
            return null;
        }

    }

    @Override
    public void transform(Block block, TransformationContext context) throws TransformationException
    {
        Map<String, String> result = getGlossaryEntries();
        Set<Map.Entry<String, String>> entrySet = result.entrySet();

        
            for (WordBlock wordBlock : this.filter.getChildrenByType(block, WordBlock.class, true)) {
                for (Map.Entry<String, String> temp : entrySet) {
                    String entry = temp.getKey();
                
                if (entry.equals(wordBlock.getWord())) {
                    String page = "Glossary." + result;
                    DocumentReference reference = resolver.resolve(page);
                    String serial = serializer.serialize(reference);
                    ResourceReference linkReference = new DocumentResourceReference(serial);
                    wordBlock.getParent().replaceChild(new LinkBlock(wordBlock.getChildren(), linkReference, false),
                        wordBlock);

                }
                }
            }

        }

    }
}
