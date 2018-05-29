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

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.transformation.TransformationContext;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

/**
 * unit tests for {@link DefaultGlossaryTransformation}.
 *
 * @version $Id$
 */
@AllComponents
public class DefaultGlossaryTransformationTest
{

    @Rule
    public final MockitoComponentMockingRule<GlossaryTransformation> mocker =
        new MockitoComponentMockingRule<GlossaryTransformation>(GlossaryTransformation.class);

    // private QueryManager queryManager;

    @Before
    public void setUp() throws ComponentLookupException
    {
        DocumentReference documentReference1 = mock(DocumentReference.class);
        DocumentReference documentReference2 = mock(DocumentReference.class);
        DocumentReference documentReference3 = mock(DocumentReference.class);

        String str1 = "HP";
        String str2 = "Samsung";
        String str3 = "XWiki";

        Map<String, DocumentReference> glossaryMap = new HashMap<String, DocumentReference>();
        glossaryMap.put(str1, documentReference1);
        glossaryMap.put(str2, documentReference2);
        glossaryMap.put(str3, documentReference3);

        // when(this.mocker.getComponentUnderTest().getGlossaryEntries()).thenReturn(glossaryMap);

    }

    //

    // @Test
    // void testGetGlossaryEntries() {
    // fail("Not yet implemented");
    // }

    @Test
    @Ignore
    public void testTransform() throws Exception
    {

        // This testInput will contain some of the glossary words. It represents a
        // paragraph on a wiki page.
        // Some glossary entries to be checked are "HP", "Hello", "Samsung", "XWiki".
        String testInput = "Hello, there are some great companies like HP, Samsung and XWiki";

        Parser parser = this.mocker.getInstance(Parser.class, "xwiki/2.1");
        XDOM xdom = parser.parse(new StringReader(testInput));
        this.mocker.getComponentUnderTest().transform(xdom, new TransformationContext());
        WikiPrinter printer = new DefaultWikiPrinter();
        BlockRenderer xwiki21BlockRenderer = this.mocker.getInstance(BlockRenderer.class, "xwiki/2.1");
        xwiki21BlockRenderer.render(xdom, printer);
        assertEquals(
            "[[doc:Glossary.Hello]], there are some great companies like [[doc:Glossary.HP]], [[doc:Glossary.Samsung]] and [[doc:Glossary.XWiki]]",
            printer.toString());

    }

}
