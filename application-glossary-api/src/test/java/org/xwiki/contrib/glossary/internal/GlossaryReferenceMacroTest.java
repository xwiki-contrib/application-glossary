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
import org.xwiki.contrib.glossary.GlossaryConfiguration;
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
 * Run all glossary reference tests found in the classpath. These {@code *.test} files must follow the
 * conventions described in {@link org.xwiki.rendering.test.integration.TestDataParser}.
 *
 * @version $Id$
 */
@AllComponents
@RunWith(RenderingTestSuite.class)
@RenderingTestSuite.Scope(pattern = "glossaryReferenceMacro\\d+\\.test")
public class GlossaryReferenceMacroTest extends AbstractGlossaryMacroTest
{
    public void initializeInternal(MockitoComponentManager componentManager) throws Exception
    {
        setupTestGlossaryEntries();

        when(xWikiContext.getOrDefault(eq("glossary-anchors-Glossary"), eq(false))).thenReturn(false);
        when(xWikiContext.getOrDefault(eq("glossary-anchors-myglossary"), eq(false))).thenReturn(false);
    }
}
