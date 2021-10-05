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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.glossary.GlossaryCache;
import org.xwiki.contrib.glossary.GlossaryConstants;
import org.xwiki.contrib.glossary.GlossaryReferenceMacroParameters;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.LinkBlock;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroContentParser;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.descriptor.DefaultContentDescriptor;
import org.xwiki.rendering.transformation.MacroTransformationContext;

/**
 * References a glosssary entry by creating a link to that glossary definition. The entry is defined either by
 * passing an id parameter to the macro and if no such id is passed, then, by using the macro content as the glossary
 * id.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Named("glossaryReference")
@Singleton
public class GlossaryReferenceMacro extends AbstractMacro<GlossaryReferenceMacroParameters>
{
    /**
     * The description of the macro.
     */
    private static final String DESCRIPTION = "References a glossary entry";

    /**
     * The description of the macro content.
     */
    private static final String CONTENT_DESCRIPTION = "The glossary entry id";

    @Inject
    private MacroContentParser contentParser;

    @Inject
    private EntityReferenceSerializer<String> serializer;

    @Inject
    private GlossaryCache glossaryCache;

    /**
     * Create and initialize the descriptor of the macro.
     */
    public GlossaryReferenceMacro()
    {
        super("GlossaryReference", DESCRIPTION, new DefaultContentDescriptor(CONTENT_DESCRIPTION),
            GlossaryReferenceMacroParameters.class);
        setDefaultCategory(DEFAULT_CATEGORY_NAVIGATION);
    }

    @Override
    public List<Block> execute(GlossaryReferenceMacroParameters parameters, String content,
        MacroTransformationContext macroContext) throws MacroExecutionException
    {
        if (content == null) {
            throw new MacroExecutionException("You must specify some content which will be used as the label off "
                + "the glossary reference link");
        }

        String entryId = parameters.getEntryId() != null ? parameters.getEntryId() : content.trim();

        List<Block> children = this.contentParser.parse(content, macroContext, false, true).getChildren();

        // Generate a link to the Glossary entry
        ResourceReference resourceReference = getGlossaryEntryReference(entryId);
        LinkBlock block = new LinkBlock(children, resourceReference, !macroContext.isInline());
        block.setParameter(GlossaryConstants.CSS_CLASS_ATTRIBUTE_NAME, GlossaryConstants.GLOSSARY_ENTRY_CSS_CLASS);
        List<Block> result = Arrays.asList(block);

        return result;
    }

    @Override
    public boolean supportsInlineMode()
    {
        return true;
    }

    private ResourceReference getGlossaryEntryReference(String entryId)
    {
        String documentReferenceString = this.serializer.serialize(glossaryCache.get(entryId));
        return new DocumentResourceReference(documentReferenceString);
    }
}
