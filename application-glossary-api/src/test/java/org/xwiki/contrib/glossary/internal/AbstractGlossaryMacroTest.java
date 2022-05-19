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
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Provider;

import org.mockito.stubbing.Answer;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.bridge.DocumentModelBridge;
import org.xwiki.contrib.glossary.GlossaryCache;
import org.xwiki.contrib.glossary.GlossaryConfiguration;
import org.xwiki.contrib.glossary.GlossaryModel;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.SpaceBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.configuration.RenderingConfiguration;
import org.xwiki.rendering.test.integration.RenderingTestSuite;
import org.xwiki.test.mockito.MockitoComponentManager;

import com.xpn.xwiki.XWikiContext;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractGlossaryMacroTest
{
    protected Provider<XWikiContext> xWikiContextProvider;

    protected XWikiContext xWikiContext;

    protected DocumentAccessBridge documentAccessBridge;

    protected GlossaryConfiguration glossaryConfiguration;

    protected GlossaryModel glossaryModel;

    protected GlossaryCache glossaryCache;

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

        documentAccessBridge = componentManager.registerMockComponent(DocumentAccessBridge.class);

        xWikiContextProvider = componentManager.registerMockComponent(XWikiContext.TYPE_PROVIDER);
        xWikiContext = mock(XWikiContext.class);
        when(xWikiContextProvider.get()).thenReturn(xWikiContext);
        when(xWikiContext.getLocale()).thenReturn(Locale.CANADA_FRENCH);

        glossaryConfiguration =
            componentManager.registerMockComponent(GlossaryConfiguration.class);
        when(glossaryConfiguration.defaultGlossaryId()).thenReturn("Glossary");

        glossaryModel = componentManager.registerMockComponent(GlossaryModel.class);
        glossaryCache = componentManager.registerMockComponent(GlossaryCache.class);

        initializeInternal(componentManager);
    }

    protected void initializeInternal(MockitoComponentManager componentManager) throws Exception
    {

    }

    /**
     * Setup the test glossary entries that we use for the macro tests.
     */
    protected void setupTestGlossaryEntries() throws Exception
    {
        DocumentReference worldDocumentReference = setupGlossaryEntry("world", "Glossary", "world",
            Arrays.asList(new WordBlock("World"), new SpaceBlock(), new WordBlock("1")));
        DocumentReference world2DocumentReference = setupGlossaryEntry("world2", "myglossary", "world2",
            Arrays.asList(new WordBlock("World"), new SpaceBlock(), new WordBlock("2")));
        DocumentReference testDocumentReference = setupGlossaryEntry("test", "myglossary", "test",
            Arrays.asList(new WordBlock("Test"), new SpaceBlock(), new WordBlock("1")));
        DocumentReference test2DocumentReference = setupGlossaryEntry("test2", "Glossary", "test2",
            Arrays.asList(new WordBlock("Test"), new SpaceBlock(), new WordBlock("2")));

        when(glossaryModel.getGlossaryEntries("Glossary")).thenReturn(Collections.singletonMap(Locale.CANADA_FRENCH,
            new HashMap<String, DocumentReference>() {
                {
                    put("world", worldDocumentReference);
                    put("test2", test2DocumentReference);
                }
            }
        ));

        when(glossaryModel.getGlossaryEntries("myglossary")).thenReturn(Collections.singletonMap(Locale.CANADA_FRENCH,
            new HashMap<String, DocumentReference>() {
                {
                    put("world2", world2DocumentReference);
                    put("test", testDocumentReference);
                }
            }
        ));

        when(xWikiContext.getOrDefault(eq("glossary-entries-Glossary"), eq(false))).thenReturn(false);
        when(xWikiContext.getOrDefault(eq("glossary-entries-myglossary"), eq(false))).thenReturn(false);
    }

    /**
     * Bootstrap a glossary entry for doing macro tests.
     *
     * @param entryName the name of the glossary entry (the page name)
     * @param glossaryId the ID of the glossary
     * @param entryTitle the title of the entry
     * @param entryContent the content of the entry
     */
    protected DocumentReference setupGlossaryEntry(String entryName, String glossaryId, String entryTitle,
        List<Block> entryContent) throws Exception
    {
        DocumentReference documentReference = new DocumentReference("xwiki", glossaryId, entryName);

        DocumentModelBridge documentModelBridge = mock(DocumentModelBridge.class);
        when(documentAccessBridge.getTranslatedDocumentInstance(documentReference)).thenReturn(documentModelBridge);
        when(documentModelBridge.getTitle()).thenReturn(entryTitle);
        when(documentModelBridge.getXDOM()).thenReturn(new XDOM(entryContent));

        if (glossaryId.equals("Glossary")) {
            when(glossaryCache.get(entryTitle, Locale.CANADA_FRENCH)).thenReturn(documentReference);
        }
        when(glossaryCache.get(entryTitle, Locale.CANADA_FRENCH, glossaryId)).thenReturn(documentReference);

        return documentReference;
    }
}
