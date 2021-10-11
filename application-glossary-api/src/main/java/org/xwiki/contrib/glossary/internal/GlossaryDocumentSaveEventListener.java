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

import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.xwiki.bridge.event.DocumentUpdatingEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.contrib.glossary.GlossaryCache;
import org.xwiki.contrib.glossary.GlossaryConfiguration;
import org.xwiki.contrib.glossary.GlossaryModel;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.CompositeBlock;
import org.xwiki.rendering.block.LinkBlock;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.block.match.ClassBlockMatcher;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.util.ParserUtils;

import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

/**
 * Scan any document at save time and look for glossary words in the document content. If words are found,
 * update them to insert a glossary macro.
 *
 * @version $Id$
 * @since 1.1
 */
@Component
@Named(GlossaryDocumentSaveEventListener.LISTENER_NAME)
@Singleton
public class GlossaryDocumentSaveEventListener implements EventListener
{
    /**
     * The name of the listener.
     */
    public static final String LISTENER_NAME = "glossaryDocumentSaveEventListener";

    private static final String ENTRY_ID = "entryId";

    private static final String GLOSSARY_ID = "glossaryId";

    @Inject
    private Logger logger;

    @Inject
    private GlossaryModel glossaryModel;

    @Inject
    private GlossaryConfiguration glossaryConfiguration;

    @Inject
    private Provider<GlossaryCache> glossaryCacheProvider;

    @Inject
    private ComponentManager componentManager;

    @Override
    public String getName()
    {
        return LISTENER_NAME;
    }

    @Override
    public List<Event> getEvents()
    {
        return Arrays.asList(new DocumentUpdatingEvent());
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        if (glossaryConfiguration.updateDocumentsOnSave()) {
            XWikiDocument document = (XWikiDocument) source;

            // Compute the locale of the document that should be used to resolve glossary entries
            Locale locale = (Locale.ROOT.equals(document.getLocale()))
                ? document.getDefaultLocale() : document.getLocale();

            try {
                XDOM xdom = document.getXDOM();
                if (transformGlossaryEntries(xdom, document, locale)) {
                    document.setContent(xdom);
                }
            } catch (XWikiException e) {
                logger.error("Failed to transform content for document [{}] to look for Glossary entries.",
                    document.getDocumentReference(), e);
            }
        }
    }

    private boolean transformGlossaryEntries(XDOM xdom, XWikiDocument document, Locale locale)
    {
        GlossaryCache glossaryCache = glossaryCacheProvider.get();

        boolean xdomModified = removeGlossaryEntries(xdom, document, locale, glossaryCache);
        xdomModified = addGlossaryEntries(xdom, locale, glossaryCache) || xdomModified;

        return xdomModified;
    }

    private boolean removeGlossaryEntries(XDOM xdom, XWikiDocument document, Locale locale, GlossaryCache glossaryCache)
    {
        boolean modified = false;

        // Try to load a parser for the current document syntax. If it fails, we won't be able to remove glossary
        // entries form the document (we can only add new ones) as removing entries means generating a set of blocks
        // corresponding to the macro content to be used as a replacement of the glossary macro.
        try {
            Parser syntaxParser = componentManager.getInstance(Parser.class, document.getSyntax().toIdString());
            ParserUtils parserUtils = new ParserUtils();

            // Check the document content for any entry that could be removed as they are not part of the glossary cache
            // anymore
            for (Block block : xdom.getBlocks(new ClassBlockMatcher(MacroBlock.class), Block.Axes.DESCENDANT_OR_SELF)) {
                MacroBlock macroBlock = (MacroBlock) block;

                if (macroBlock.getId().equals(GlossaryReferenceMacro.MACRO_NAME)) {
                    String macroGlossaryId = StringUtils.isBlank(macroBlock.getParameter(GLOSSARY_ID))
                        ? glossaryConfiguration.defaultGlossaryId() : macroBlock.getParameter(GLOSSARY_ID);
                    String macroEntryId = macroBlock.getParameter(ENTRY_ID);

                    if (glossaryCache.get(macroEntryId, locale, macroGlossaryId) == null) {
                        // Replace the block by a set of blocks ; for this, we need to parse the content of the macro
                        try {
                            XDOM replacementXDOM = syntaxParser.parse(new StringReader(macroBlock.getContent()));

                            List<Block> children = replacementXDOM.getChildren();
                            if (macroBlock.isInline()) {
                                parserUtils.removeTopLevelParagraph(children);
                            }

                            Block replacementBlock = new CompositeBlock(children);

                            macroBlock.getParent().replaceChild(replacementBlock, macroBlock);

                            modified = true;
                        } catch (ParseException e) {
                            logger.error("Failed to parse macro content", e);
                        }
                    }
                }
            }
        } catch (ComponentLookupException e) {
            logger.error("Failed to load syntax parser for syntax [{}].", document.getSyntax().toIdString(), e);
        }

        return modified;
    }

    private boolean addGlossaryEntries(XDOM xdom, Locale locale, GlossaryCache glossaryCache)
    {
        boolean modified = false;

        // Look in the document content for any glossary entries that could be added.
        for (Block block : xdom.getBlocks(new ClassBlockMatcher(WordBlock.class), Block.Axes.DESCENDANT_OR_SELF)) {
            WordBlock wordBlock = (WordBlock) block;

            DocumentReference glossaryEntryReference = glossaryCache.get(wordBlock.getWord(), locale,
                glossaryConfiguration.defaultGlossaryId());

            if (glossaryEntryReference != null && !(block.getParent() instanceof LinkBlock)) {
                // Update the block element to put a glossary macro
                Map<String, String> macroParameters = new HashMap<>();
                macroParameters.put(ENTRY_ID, glossaryEntryReference.getName());
                macroParameters.put(GLOSSARY_ID, glossaryModel.getGlossaryId(glossaryEntryReference));

                MacroBlock macroBlock = new MacroBlock(GlossaryReferenceMacro.MACRO_NAME, macroParameters,
                    wordBlock.getWord(), true);

                wordBlock.getParent().replaceChild(macroBlock, wordBlock);

                modified = true;
            }
        }

        return modified;
    }
}
