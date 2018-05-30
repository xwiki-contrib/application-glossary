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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.transformation.Transformation;
import org.xwiki.rendering.transformation.TransformationContext;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

/**
 * @version $Id$
 */

@AllComponents
@Ignore
public class GlossaryTransformationTest
{
    @Rule
    public MockitoComponentMockingRule<GlossaryTransformation> mocker =
        new MockitoComponentMockingRule<>(GlossaryTransformation.class);

    private Transformation glossaryTransformation;

    private DefaultEntryRetrieval defaultEntryRetrieval;

    @Before
    public void setUp() throws Exception
    {
        // This creates an instance of the class under test.
        // (Not able to find the java doc of getInstance() method, so supposed this.)
        this.glossaryTransformation = this.mocker.getInstance(Transformation.class, "glossary");
        this.defaultEntryRetrieval = this.mocker.getInstance(DefaultEntryRetrieval.class);

        // Creating mocks for the test.
        // The main implementation requires a "String" and its "DocumentReference".
        DocumentReference documentReference1 = mock(DocumentReference.class);
        DocumentReference documentReference2 = mock(DocumentReference.class);
        DocumentReference documentReference3 = mock(DocumentReference.class);

        String str1 = "foo";
        String str2 = "bar";
        String str3 = "XWiki";

        Map<String, DocumentReference> glossaryMap = new HashMap<String, DocumentReference>();
        glossaryMap.put(str1, documentReference1);
        glossaryMap.put(str2, documentReference2);
        glossaryMap.put(str3, documentReference3);

        // Stub: This will get invoked when getGlossaryEntries will be called in the GlossaryTrasformation component
        // implementation.
        when(defaultEntryRetrieval.getGlossaryEntries()).thenReturn(glossaryMap);
    }

    /**
     * Test method for
     * {@link org.xwiki.contrib.glossary.internal.GlossaryTransformation#transform(org.xwiki.rendering.block.Block, org.xwiki.rendering.transformation.TransformationContext)}.
     */
    @Test
    void testGlossaryTransformation() throws Exception
    {
        // This testInput will contain some of the glossary words. It represents a
        // paragraph on a wiki page.
        // Some glossary entries to be checked are "foo", "bar", "XWiki".
        String testInput = "Hello, there are some great companies like foo, bar and XWiki";

        // Mocks the Parser Class
        Parser parser = this.mocker.getInstance(Parser.class, "xwiki/2.1");
        // Parses the "String" into XDOM
        XDOM xdom = parser.parse(new StringReader(testInput));
        // Glossary Transformation executes.
        this.glossaryTransformation.transform(xdom, new TransformationContext());
        WikiPrinter printer = new DefaultWikiPrinter();
        BlockRenderer xwiki21BlockRenderer = this.mocker.getInstance(BlockRenderer.class, "xwiki/2.1");
        // Store it in a wiki printer.
        xwiki21BlockRenderer.render(xdom, printer);
        assertEquals(
            "Hello, there are some great companies like [[doc:Glossary.HP]], [[doc:Glossary.Samsung]] and [[doc:Glossary.XWiki]]",
            printer.toString());

    }

}
