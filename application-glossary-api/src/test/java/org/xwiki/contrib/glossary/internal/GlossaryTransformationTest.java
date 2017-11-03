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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.transformation.TransformationContext;

import static org.mockito.Mockito.*;

import java.io.StringReader;
import java.util.ArrayList;
import static org.junit.Assert.*;

/**
 * unit tests for {@link GlossaryTransformation}.
 *
 * @version $Id$
 */
public class GlossaryTransformationTest
{
    @Rule
    public MockitoComponentMockingRule<GlossaryTransformation> mocker =
        new MockitoComponentMockingRule<GlossaryTransformation>(GlossaryTransformation.class);

    private GlossaryTransformation glossaryTransformation;

    @Mock
    private ArrayList<String> mockArrayList;

    @Before
    public void setUp() throws Exception
    {
        this.mockArrayList = new ArrayList<String>();
        mockArrayList.add("glossaryEntry1");
        mockArrayList.add("glossaryEntry2");

    }

    @Test
    public void transformWhenOk() throws Exception
    {
        glossaryTransformation = this.mocker.getComponentUnderTest();
        when(this.glossaryTransformation.getGlossaryEntries()).thenReturn(mockArrayList);
        String testInput = "This is a glossaryEntry1, AnotherglossaryEntry1, XWikiEnterprise glossaryEntry2.";

        Parser parser = this.mocker.getInstance(Parser.class, "xwiki/2.1");
        XDOM xdom = parser.parse(new StringReader(testInput));
        this.glossaryTransformation.transform(xdom, new TransformationContext());
        WikiPrinter printer = new DefaultWikiPrinter();
        BlockRenderer xwiki21BlockRenderer = this.mocker.getInstance(BlockRenderer.class, "xwiki/2.1");
        xwiki21BlockRenderer.render(xdom, printer);
        assertEquals("This is a [[doc:glossaryEntry1]], AnotherglossaryEntry1, XWikiEnterprise [[doc:glossaryEntry2]].",
            printer.toString());

    }
}
