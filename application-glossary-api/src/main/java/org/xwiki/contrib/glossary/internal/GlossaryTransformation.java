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

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.glossary.GlossaryCache;
import org.xwiki.contrib.glossary.GlossaryConstants;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.LinkBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.internal.block.ProtectedBlockFilter;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.transformation.AbstractTransformation;
import org.xwiki.rendering.transformation.TransformationContext;

import com.xpn.xwiki.XWikiContext;

/**
 * Find Glossary words in the passed XDOM and replace them with links when a Glossary entries exist for the words.
 *
 * @version $Id$
 * @since 0.3
 */
@Component
@Singleton
@Named("glossary")
public class GlossaryTransformation extends AbstractTransformation
{
    private static final ProtectedBlockFilter PROTECTED_FILTER = new ProtectedBlockFilter();

    @Inject
    private GlossaryCache glossaryCache;

    @Inject
    private EntityReferenceSerializer<String> serializer;

    @Inject
    private Provider<XWikiContext> xWikiContextProvider;

    @Override
    public void transform(Block block, TransformationContext context)
    {
        handleBlocks(block);
    }

    private void handleBlocks(Block currentBlock)
    {
        Block block = currentBlock;

        while (block != null) {
            // Skip LinkBlock since it's already a link...
            if (block instanceof WordBlock) {
                block = replaceWordByLink((WordBlock) block);
            } else if (!(block instanceof LinkBlock)) {
                List<Block> children = block.getChildren();
                for (int i = 0; i < children.size(); i++) {
                    handleBlocks(children.get(i));
                }
            }
            // Make sure to skip protected blocks
            block = PROTECTED_FILTER.getNextSibling(block);
        }
    }

    private Block replaceWordByLink(WordBlock wordBlock)
    {
        Block newBlock = wordBlock;
        String word = wordBlock.getWord();
        Locale locale = xWikiContextProvider.get().getLocale();
        if (this.glossaryCache.get(word, locale) != null) {
            DocumentReference reference = this.glossaryCache.get(word, locale);
            String referenceAsString = serializer.serialize(reference);
            ResourceReference linkReference = new DocumentResourceReference(referenceAsString);
            newBlock = new LinkBlock(Arrays.asList(new WordBlock(word)), linkReference, false);
            newBlock
                .setParameter(GlossaryConstants.CSS_CLASS_ATTRIBUTE_NAME, GlossaryConstants.GLOSSARY_ENTRY_CSS_CLASS);
            wordBlock.getParent().replaceChild(newBlock, wordBlock);
        }
        return newBlock;
    }
}
