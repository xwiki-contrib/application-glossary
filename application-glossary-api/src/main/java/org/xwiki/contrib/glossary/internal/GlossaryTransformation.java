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

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.contrib.glossary.EntryRetrieval;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.LinkBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.internal.block.ProtectedBlockFilter;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.rendering.transformation.AbstractTransformation;
import org.xwiki.rendering.transformation.TransformationContext;
import org.xwiki.rendering.transformation.TransformationException;

/**
 * Create Transformation for Glossary Application.
 * 
 * @version $Id$
 */
@Component
@Singleton
@Named("glossary")
public class GlossaryTransformation extends AbstractTransformation implements Initializable
{
    @Inject
    private EntryRetrieval entryRetrieval;

    private ProtectedBlockFilter filter = new ProtectedBlockFilter();

    private Map<String, DocumentReference> glossaryEntries;

    // TO DO: Inject the GlossaryCache object here.
    @Override
    public void initialize()
    {
        // Initialize the cache here.
        // How to initialize?:
        // See:
        // xwiki-platform-oldcore/src/main/java/com/xpn/xwiki/internal/cache/rendering/DefaultRenderingCache.java
        // initialise the cache with getGlossaryEntries by setting key-value pairs.
        this.glossaryEntries = this.entryRetrieval.getGlossaryEntries();
    }

    @Override
    public void transform(Block block, TransformationContext context) throws TransformationException
    {
        for (WordBlock wordBlock : this.filter.getChildrenByType(block, WordBlock.class, true)) {

            String word = wordBlock.getWord();

            // Checking if the map 'result' contains the 'glossary' word. For now, it only supports single strings.
            if (this.glossaryEntries.containsKey(word)) {
                // Taking the DocumentReference from the map and converting it to ResourceReference
                // because link block takes 'Resource Reference' as an argument.
                DocumentReference reference = this.glossaryEntries.get(word);
                DocumentResourceReference linkReference = new DocumentResourceReference(reference.toString());
                wordBlock.getParent().replaceChild(new LinkBlock(wordBlock.getChildren(), linkReference, false),
                    wordBlock);

            }
        }
    }

}
