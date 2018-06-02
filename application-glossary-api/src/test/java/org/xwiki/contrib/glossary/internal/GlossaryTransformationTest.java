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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.contrib.glossary.EntryRetrieval;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.internal.parser.plain.PlainTextBlockParser;
import org.xwiki.rendering.internal.parser.plain.PlainTextStreamParser;
import org.xwiki.rendering.internal.parser.reference.type.URLResourceReferenceTypeParser;
import org.xwiki.rendering.internal.renderer.plain.PlainTextBlockRenderer;
import org.xwiki.rendering.internal.renderer.plain.PlainTextRenderer;
import org.xwiki.rendering.internal.renderer.plain.PlainTextRendererFactory;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.transformation.Transformation;
import org.xwiki.rendering.transformation.TransformationContext;
import org.xwiki.test.annotation.BeforeComponent;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.mockito.MockitoComponentMockingRule;
import org.xwiki.test.page.XWikiSyntax21ComponentList;

/**
 * @version $Id$
 */
@XWikiSyntax21ComponentList
@ComponentList({
    URLResourceReferenceTypeParser.class,
    PlainTextRendererFactory.class
})
public class GlossaryTransformationTest
{
    @Rule
    public MockitoComponentMockingRule<GlossaryTransformation> mocker =
        new MockitoComponentMockingRule<>(GlossaryTransformation.class);

    private EntryRetrieval defaultEntryRetrieval;

    @Before
    public void setUp() throws Exception
    {
        this.defaultEntryRetrieval = this.mocker.getInstance(EntryRetrieval.class);

        DocumentReference documentReference1 = mock(DocumentReference.class);
        DocumentReference documentReference2 = mock(DocumentReference.class);
        DocumentReference documentReference3 = mock(DocumentReference.class);

        String str1 = "foo";
        String str2 = "bar";
        String str3 = "XWiki";

        Map<String, DocumentReference> glossaryMap = new HashMap<>();
        glossaryMap.put(str1, documentReference1);
        glossaryMap.put(str2, documentReference2);
        glossaryMap.put(str3, documentReference3);

        when(this.defaultEntryRetrieval.getGlossaryEntries()).thenReturn(glossaryMap);
    }

    @Test
    public void transform() throws Exception
    {
        // This testInput will contain some of the glossary words. It represents a
        // paragraph on a wiki page.
        // Some glossary entries to be checked are "foo", "bar", "XWiki".
        // Test Fixture..
        String testInput = "Hello, there are some great companies like foo, bar and XWiki";

        // Mocks the Parser Class
        Parser parser = this.mocker.getInstance(Parser.class, "xwiki/2.1");
        // Parses the "String" into XDOM
        XDOM xdom = parser.parse(new StringReader(testInput));
        // Glossary Transformation executes.
        this.mocker.getComponentUnderTest().transform(xdom, new TransformationContext());
        WikiPrinter printer = new DefaultWikiPrinter();
        BlockRenderer xwikiBlockRenderer = this.mocker.getInstance(BlockRenderer.class, "xwiki/2.1");
        xwikiBlockRenderer.render(xdom, printer);
        assertEquals("Hello, there are some great companies like [[doc:Glossary.HP]], [[doc:Glossary.Samsung]] "
            + "and [[doc:Glossary.XWiki]]", printer.toString());
    }
}
