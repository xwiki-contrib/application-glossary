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
import java.util.Locale;

import javax.inject.Provider;

import org.junit.Rule;
import org.junit.runner.RunWith;
import org.xwiki.contrib.glossary.GlossaryCache;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.rendering.configuration.RenderingConfiguration;
import org.xwiki.rendering.macro.MacroContentParser;
import org.xwiki.rendering.parser.StreamParser;
import org.xwiki.rendering.test.integration.RenderingTestSuite;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.mockito.MockitoComponentManager;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import com.xpn.xwiki.XWikiContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Run all tests found in {@code *.test} files located in the classpath. These {@code *.test} files must follow the
 * conventions described in {@link org.xwiki.rendering.test.integration.TestDataParser}.
 *
 * @version $Id$
 */
@AllComponents
@RunWith(RenderingTestSuite.class)
public class GlossaryReferenceMacroTest
{
    @RenderingTestSuite.Initialized
    public void initialize(MockitoComponentManager componentManager) throws Exception
    {
        // By default the GlossaryTransformation will be registered since DefaultRenderingConfiguration's
        // implementation registers all transformation in the classpath for the test. However, in this test we don't
        // want the "glossary" transformation since we're testing a macro that is supposed to be used especially when
        // the transformation is not enabled. Also, enabling the transformation would require to setup more mocks which
        // is unnecessary.
        RenderingConfiguration configuration = componentManager.registerMockComponent(RenderingConfiguration.class);
        when(configuration.getTransformationNames()).thenReturn(Arrays.asList("macro"));

        DocumentReference worldDocumentReference = new DocumentReference("xwiki", "Glossary", "world");
        DocumentReference world2DocumentReference = new DocumentReference("xwiki", "myglossary", "world2");
        DocumentReference testDocumentReference = new DocumentReference("xwiki", "myglossary", "test");
        DocumentReference test2DocumentReference = new DocumentReference("xwiki", "Glossary", "test2");
        GlossaryCache glossaryCache = componentManager.registerMockComponent(GlossaryCache.class);
        when(glossaryCache.get("world")).thenReturn(worldDocumentReference);
        when(glossaryCache.get("world2")).thenReturn(world2DocumentReference);
        when(glossaryCache.get("test")).thenReturn(testDocumentReference);
        when(glossaryCache.get("test2")).thenReturn(test2DocumentReference);
    }
}
