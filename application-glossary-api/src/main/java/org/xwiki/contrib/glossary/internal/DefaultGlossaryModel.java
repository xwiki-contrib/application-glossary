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

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.contrib.glossary.GlossaryException;
import org.xwiki.contrib.glossary.GlossaryModel;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.syntax.Syntax;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * Default implementation using the Query Manager to find all Glossary entries in the database, for the current wiki.
 *
 * @version $Id$
 */
@Component
@Singleton
public class DefaultGlossaryModel implements GlossaryModel
{
    private static final EntityReference GLOSSARY_XCLASS_REFERENCE =
        new EntityReference("GlossaryClass", EntityType.DOCUMENT,
            new EntityReference("Code", EntityType.SPACE,
                new EntityReference("Glossary", EntityType.SPACE)));

    @Inject
    private QueryManager queryManager;

    @Inject
    private DocumentReferenceResolver<String> defaultDocumentReferenceResolver;

    @Inject
    private Provider<XWikiContext> xwikiContextProvider;

    @Inject
    @Named("context")
    private Provider<ComponentManager> componentManagerProvider;

    @Override
    public Map<String, DocumentReference> getGlossaryEntries() throws GlossaryException
    {
        // Find all existing glossary entries and save them in a cache for fast transformation execution.
        Map<String, DocumentReference> glossaryMap = new HashMap<>();
        try {
            Query query = this.queryManager.createQuery("from doc.object(Glossary.Code.GlossaryClass) as glossary",
                Query.XWQL);
            List<String> documentNames = query.execute();
            for (String documentName : documentNames) {
                DocumentReference reference = this.defaultDocumentReferenceResolver.resolve(documentName);
                // Handle the case of nested page.
                // TODO: When we upgrade the parent POM dependency to a more recent XWiki version, use a
                // PageReferenceResolver instead.
                String name = reference.getName();
                if ("WebHome".equals(name)) {
                    name = reference.getParent().getName();
                }
                glossaryMap.put(name, reference);
            }
        } catch (QueryException e) {
            throw new GlossaryException("Failed to retrieve Glossary entries", e);
        }

        return glossaryMap;
    }

    @Override
    public XDOM getGlossaryContent(DocumentReference reference) throws GlossaryException
    {
        XWikiContext xwikiContext = this.xwikiContextProvider.get();
        XWikiDocument xwikiDocument;
        try {
            xwikiDocument = xwikiContext.getWiki().getDocument(reference, xwikiContext);
            // Get the glossary xproperty for the content
            BaseObject bo = xwikiDocument.getXObject(GLOSSARY_XCLASS_REFERENCE);
            String definition = bo.getLargeStringValue("definition");
            // Parse the content, using the syntax of the document
            return getParser(xwikiDocument.getSyntax()).parse(new StringReader(definition));
        } catch (XWikiException | ParseException e) {
            throw new GlossaryException(String.format("Failed to get glossary content for [%s]", reference), e);
        }
    }

    private Parser getParser(Syntax syntax) throws GlossaryException
    {
        try {
            return this.componentManagerProvider.get().getInstance(Parser.class, syntax.toIdString());
        } catch (ComponentLookupException e) {
            throw new GlossaryException(
                String.format("Failed to find parser for syntax [%s] to parse glossary content", syntax), e);
        }
    }
}
