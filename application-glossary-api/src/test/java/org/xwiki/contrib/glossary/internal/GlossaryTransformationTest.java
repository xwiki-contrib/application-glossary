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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.contrib.glossary.GlossaryCache;
import org.xwiki.contrib.glossary.GlossaryModel;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.rendering.block.LinkBlock;
import org.xwiki.rendering.block.MacroMarkerBlock;
import org.xwiki.rendering.block.ParagraphBlock;
import org.xwiki.rendering.block.SpaceBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.internal.renderer.event.EventBlockRenderer;
import org.xwiki.rendering.internal.renderer.event.EventRenderer;
import org.xwiki.rendering.internal.renderer.event.EventRendererFactory;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.transformation.TransformationContext;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link GlossaryTransformation}.
 *
 * @version $Id$
 */
@ComponentList({
    EventBlockRenderer.class,
    EventRendererFactory.class,
    EventRenderer.class
})
public class GlossaryTransformationTest
{
    @Rule
    public MockitoComponentMockingRule<GlossaryTransformation> mocker =
        new MockitoComponentMockingRule<>(GlossaryTransformation.class);

    @Before
    public void setUp() throws Exception
    {
        GlossaryCache glossaryCache = this.mocker.getInstance(GlossaryCache.class);

        DocumentReference reference1 = new DocumentReference("wiki", "space", "foo");
        DocumentReference reference2 = new DocumentReference("wiki", "space", "XWiki");

        when(glossaryCache.get("foo")).thenReturn(reference1);
        when(glossaryCache.get("XWiki")).thenReturn(reference2);

        EntityReferenceSerializer<String> serializer = this.mocker.getInstance(EntityReferenceSerializer.TYPE_STRING);
        when(serializer.serialize(reference1)).thenReturn("wiki:space.foo");
        when(serializer.serialize(reference2)).thenReturn("wiki:space.XWiki");
    }

    @Test
    public void transform() throws Exception
    {
        // - The "Hello" word shouldn't be changed since there's no glossary entry for it
        // - There's one glossary word to change to a link: "foo"
        // - The "XWiki" word shouldn't be changed since it's inside a link
        //   (even though there's a glossary entry for it)
        // - The "protected" word shouldn't be changed since it's inside a protected block (code macro here)
        XDOM xdom = new XDOM(Arrays.asList(new ParagraphBlock(Arrays.asList(
            new WordBlock("Hello"), new SpaceBlock(), new WordBlock("foo"), new SpaceBlock(),
            new LinkBlock(Arrays.asList(new WordBlock("XWiki")),
                new DocumentResourceReference("XWiki.WebHome"), false),
            new MacroMarkerBlock("code", Collections.emptyMap(), Arrays.asList(new WordBlock("protected")), true)))));

        this.mocker.getComponentUnderTest().transform(xdom, new TransformationContext());

        WikiPrinter printer = new DefaultWikiPrinter();
        BlockRenderer xwikiBlockRenderer = this.mocker.getInstance(BlockRenderer.class, "event/1.0");
        xwikiBlockRenderer.render(xdom, printer);
        assertEquals("beginDocument\n"
            + "beginParagraph\n"
            + "onWord [Hello]\n"
            + "onSpace\n"
            + "beginLink [Typed = [true] Type = [doc] Reference = [wiki:space.foo]] [false]\n"
            + "endLink [Typed = [true] Type = [doc] Reference = [wiki:space.foo]] [false]\n"
            + "onSpace\n"
            + "beginLink [Typed = [true] Type = [doc] Reference = [XWiki.WebHome]] [false]\n"
            + "onWord [XWiki]\n"
            + "endLink [Typed = [true] Type = [doc] Reference = [XWiki.WebHome]] [false]\n"
            + "beginMacroMarkerInline [code] []\n"
            + "onWord [protected]\n"
            + "endMacroMarkerInline [code] []\n"
            + "endParagraph\n"
            + "endDocument", printer.toString());
    }
}
