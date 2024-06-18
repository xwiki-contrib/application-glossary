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

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.glossary.GlossaryConstants;
import org.xwiki.contrib.glossary.GlossaryException;
import org.xwiki.contrib.glossary.GlossaryModel;
import org.xwiki.localization.LocaleUtils;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;
import org.xwiki.rendering.block.XDOM;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

/**
 * Default implementation using the Query Manager to find all Glossary entries in the database, for the current wiki.
 *
 * @version $Id$
 */
@Component
@Singleton
public class DefaultGlossaryModel implements GlossaryModel
{
    @Inject
    private QueryManager queryManager;

    @Inject
    @Named("currentmixed")
    private DocumentReferenceResolver<String> defaultDocumentReferenceResolver;

    @Inject
    private Provider<XWikiContext> xwikiContextProvider;

    @Inject
    @Named("local")
    private EntityReferenceSerializer<String> entityReferenceSerializer;

    @Override
    public Map<Locale, Map<String, DocumentReference>> getGlossaryEntries() throws GlossaryException
    {
        return this.getGlossaryEntries(null);
    }

    @Override
    public Map<Locale, Map<String, DocumentReference>> getGlossaryEntries(String glossaryId) throws GlossaryException
    {
        // Find all existing glossary entries and save them in a cache for fast transformation execution.
        Map<Locale, Map<String, DocumentReference>> glossaryMap = new HashMap<>();

        glossaryId = (StringUtils.isBlank(glossaryId)) ? "%" : String.format("%s%%", glossaryId);

        try {
            Query query = this.queryManager.createQuery("select doc.fullName, doc.title, doc.language, "
                + "doc.defaultLanguage, doc.translation from XWikiDocument doc, BaseObject obj where "
                + "obj.className = :glossaryClassRef and obj.name = doc.fullName "
                + "and doc.fullName like :glossaryId", Query.HQL);
            List<Object[]> documents = query.bindValue("glossaryId", glossaryId)
                .bindValue("glossaryClassRef",
                    entityReferenceSerializer.serialize(GlossaryConstants.GLOSSARY_XCLASS_REFERENCE)).execute();

            for (Object[] document : documents) {
                DocumentReference reference = this.defaultDocumentReferenceResolver.resolve((String) document[0]);
                String title = (String) document[1];
                String language = (String) document[2];
                String defaultLanguage = (String) document[3];
                Integer isTranslation = (Integer) document[4];

                // Compute the locale of the document
                Locale locale = Locale.getDefault();
                if (StringUtils.isNotBlank(language)) {
                    locale = LocaleUtils.toLocale(language);
                } else if (StringUtils.isNotBlank(defaultLanguage) && isTranslation == 0) {
                    locale = LocaleUtils.toLocale(defaultLanguage);
                }

                Map<String, DocumentReference> map;
                if (glossaryMap.containsKey(locale)) {
                    map = glossaryMap.get(locale);
                } else {
                    map = new HashMap<>();
                    glossaryMap.put(locale, map);
                }

                map.put(title, reference);
            }
        } catch (QueryException e) {
            throw new GlossaryException("Failed to retrieve Glossary entries", e);
        }

        return glossaryMap;
    }

    @Override
    public XDOM getGlossaryContent(DocumentReference reference, Locale locale) throws GlossaryException
    {
        XWikiContext xwikiContext = this.xwikiContextProvider.get();
        XWikiDocument xwikiDocument;
        try {
            xwikiDocument = xwikiContext.getWiki().getDocument(reference, xwikiContext);
            if (!xwikiDocument.getDefaultLocale().equals(locale)) {
                xwikiDocument = xwikiDocument.getTranslatedDocument(locale, xwikiContext);
            }
            // Parse the content, using the syntax of the document
            return xwikiDocument.getXDOM();
        } catch (XWikiException e) {
            throw new GlossaryException(String.format("Failed to get glossary content for [%s]", reference), e);
        }
    }

    @Override
    public String getGlossaryId(DocumentReference entryReference)
    {
        // See the GlossaryReferenceMacroParameters#getGlossaryId() for a definition of the glossary ID
        return entityReferenceSerializer.serialize(entryReference.getLastSpaceReference());
    }
}
