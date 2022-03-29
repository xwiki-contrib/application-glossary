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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.contrib.glossary.GlossaryCache;
import org.xwiki.contrib.glossary.GlossaryConfiguration;
import org.xwiki.contrib.glossary.GlossaryEntriesTransformer;
import org.xwiki.contrib.glossary.GlossaryException;
import org.xwiki.contrib.glossary.GlossaryModel;
import org.xwiki.contrib.xdom.regex.Matcher;
import org.xwiki.contrib.xdom.regex.Pattern;
import org.xwiki.contrib.xdom.regex.PatternBuilder;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.CompositeBlock;
import org.xwiki.rendering.block.LinkBlock;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.block.match.ClassBlockMatcher;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.util.ParserUtils;

/**
 * Default GlossaryEntriesTransformer.
 *
 * @version $Id$
 */
@Component
@Singleton
public class DefaultGlossaryEntriesTransformer implements GlossaryEntriesTransformer
{
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

    @Inject
    @Named("local")
    private EntityReferenceSerializer<String> referenceSerializer;

    @Override
    public boolean transformGlossaryEntries(XDOM xdom, Syntax syntax, Locale locale) throws GlossaryException
    {
        boolean xdomModified = removeGlossaryReferenceMacroBlocks(xdom, syntax, locale);
        xdomModified = addGlossaryReferenceMacroBlocks(xdom, locale) || xdomModified;
        return xdomModified;
    }

    private boolean removeGlossaryReferenceMacroBlocks(XDOM xdom, Syntax syntax, Locale locale)
    {
        GlossaryCache glossaryCache = glossaryCacheProvider.get();
        boolean modified = false;

        // Try to load a parser for the current document syntax. If it fails, we won't be able to remove glossary
        // entries form the document (we can only add new ones) as removing entries means generating a set of blocks
        // corresponding to the macro content to be used as a replacement of the glossary macro.
        try {
            Parser syntaxParser = componentManager.getInstance(Parser.class, syntax.toIdString());
            ParserUtils parserUtils = new ParserUtils();

            // Check the document content for any entry that could be removed as they are not part of the glossary cache
            // anymore
            // We'll also check if the entry is part of a LinkBlock, in which case we explicitly want to remove the
            // glossary entry (if it was added previously)
            for (Block block : xdom.getBlocks(new ClassBlockMatcher(MacroBlock.class), Block.Axes.DESCENDANT_OR_SELF)) {
                MacroBlock macroBlock = (MacroBlock) block;

                if (macroBlock.getId().equals(GlossaryReferenceMacro.MACRO_NAME)) {
                    String macroGlossaryId = StringUtils.isBlank(macroBlock.getParameter(GLOSSARY_ID))
                        ? glossaryConfiguration.defaultGlossaryId() : macroBlock.getParameter(GLOSSARY_ID);
                    String macroEntryId = macroBlock.getParameter(ENTRY_ID);

                    if (glossaryCache.get(macroEntryId, locale, macroGlossaryId) == null || hasParentLink(macroBlock)) {
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
            logger.error("Failed to load syntax parser for syntax [{}].", syntax.toIdString(), e);
        }

        return modified;
    }

    private boolean addGlossaryReferenceMacroBlocks(XDOM xdom, Locale locale) throws GlossaryException
    {
        AtomicBoolean modified = new AtomicBoolean(false);
        Map<Locale, Map<String, DocumentReference>> entries = glossaryModel.getGlossaryEntries();
        Map<String, DocumentReference> localeEntries = entries.get(locale);
        if (localeEntries != null) {
            // Order entries by descending length to match first the longest ones eg "Workers Committee",
            // then only "Committee"
            Set<Map.Entry<String, DocumentReference>> set = localeEntries.entrySet();
            Comparator<Map.Entry<String, DocumentReference>> entryLengthComparator =
                (h1, h2) -> h2.getKey().length() - h1.getKey().length();
            Stream<Map.Entry<String, DocumentReference>> sortedStream =
                set.stream().sorted(entryLengthComparator);

            sortedStream.forEach((Map.Entry<String, DocumentReference> entry) -> {
                PatternBuilder builder = new PatternBuilder();
                String title = entry.getKey();

                // Surround all words with signs "^" at the start and "$" at the end
                String regex = title.replaceAll("([a-zA-Z0-9]+)", "^$1\\$");
                regex = regex.replaceAll("\\?", "\\?");
                Pattern pattern = builder.build(regex);
                Class<? extends Block> primaryBlockClass = pattern.getPrimaryBlockPattern().getBlockClass();
                ClassBlockMatcher classBlockMatcher = new ClassBlockMatcher(primaryBlockClass);

                for (Block block : xdom.getBlocks(classBlockMatcher, Block.Axes.DESCENDANT)) {
                    Matcher matcher = pattern.getMatcher(block);
                    if (matcher.matches() && !hasParentLink(block)) {
                        Map<String, String> parameters = new HashMap<>();
                        // Currently a glossary entry is necessarily a terminal page, and the glossary it belongs to is
                        // its direct parent reference.
                        EntityReference glossaryReference = entry.getValue().getParent();
                        // TODO: check if the EntityReferenceSerializer should be used instead
                        parameters.put(ENTRY_ID, entry.getValue().getName());
                        parameters.put(GLOSSARY_ID, referenceSerializer.serialize(glossaryReference));
                        MacroBlock glossaryEntryMacroBlock =
                            new MacroBlock("glossaryReference", parameters, entry.getKey(),
                                true);
                        matcher.replace(glossaryEntryMacroBlock);
                        modified.set(true);
                    }
                }
            });
        }
        return modified.get();
    }

    private boolean hasParentLink(Block block)
    {
        boolean hasParentLink = false;
        Block currentBlock = block;
        while (!hasParentLink && currentBlock.getParent() != null) {
            if (currentBlock instanceof LinkBlock) {
                hasParentLink = true;
            } else {
                currentBlock = currentBlock.getParent();
            }
        }

        return hasParentLink;
    }
}
