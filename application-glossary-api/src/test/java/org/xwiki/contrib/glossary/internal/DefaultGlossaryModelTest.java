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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Provider;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.contrib.glossary.GlossaryConfiguration;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.query.Query;
import org.xwiki.query.QueryManager;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultGlossaryModel}.
 *
 * @version $Id$
 * @since 1.1
 */
@ComponentList({
    QueryManager.class,
    GlossaryConfiguration.class
})
public class DefaultGlossaryModelTest
{
    @Rule
    public MockitoComponentMockingRule<DefaultGlossaryModel> mocker =
        new MockitoComponentMockingRule<>(DefaultGlossaryModel.class);

    private QueryManager queryManager;

    private XWikiContext xWikiContext;

    private XWiki xwiki;

    private DocumentReferenceResolver<String> documentResolver;

    @Before
    public void setUp() throws Exception
    {
        queryManager = mocker.getInstance(QueryManager.class);

        Provider<XWikiContext> xWikiContextProvider = mocker.registerMockComponent(XWikiContext.TYPE_PROVIDER);
        xWikiContext = mock(XWikiContext.class);
        when(xWikiContextProvider.get()).thenReturn(xWikiContext);
        xwiki = mock(XWiki.class);
        when(xWikiContext.getWiki()).thenReturn(xwiki);

        documentResolver = mocker.registerMockComponent(DocumentReferenceResolver.TYPE_STRING);
    }

    @Test
    public void getGlossaryEntries() throws Exception
    {
        Query query = mock(Query.class);
        when(queryManager.createQuery(any(String.class), eq(Query.HQL))).thenReturn(query);

        DocumentReference documentReference = new DocumentReference("xwiki", "test", "fullName1");
        Object[] entry1 = { "test.fullName1", "title1", "en", "en", 0 };
        Object[] entry2 = { "test.fullName1", "title2", "fr", "en", 1 };
        List<Object> queryResults = Arrays.asList(entry1, entry2);
        when(query.bindValue(any(String.class), any(String.class))).thenReturn(query);
        when(query.execute()).thenReturn(queryResults);
        when(documentResolver.resolve("test.fullName1")).thenReturn(documentReference);

        Map<Locale, Map<String, DocumentReference>> expected = new HashMap<Locale, Map<String, DocumentReference>>() {{
            put(Locale.ENGLISH, new HashMap<String, DocumentReference>() {{
                put("title1", documentReference);
            }});
            put(Locale.FRENCH, new HashMap<String, DocumentReference>() {{
                put("title2", documentReference);
            }});
        }};

        assertEquals(mocker.getComponentUnderTest().getGlossaryEntries(), expected);
    }

    @Test
    public void getGlossaryContent() throws Exception
    {
        DocumentReference documentReference = mock(DocumentReference.class);
        XWikiDocument document = mock(XWikiDocument.class);
        XWikiDocument translatedDocument = mock(XWikiDocument.class);
        XDOM xdom = mock(XDOM.class);

        when(xwiki.getDocument(documentReference, xWikiContext)).thenReturn(document);
        when(document.getDefaultLocale()).thenReturn(Locale.ROOT);
        when(document.getTranslatedDocument(Locale.ENGLISH, xWikiContext)).thenReturn(translatedDocument);
        when(translatedDocument.getXDOM()).thenReturn(xdom);

        assertEquals(mocker.getComponentUnderTest().getGlossaryContent(documentReference, Locale.ENGLISH), xdom);
    }
}
