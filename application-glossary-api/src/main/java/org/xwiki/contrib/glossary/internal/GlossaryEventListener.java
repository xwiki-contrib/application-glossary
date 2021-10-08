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
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.glossary.GlossaryCache;
import org.xwiki.contrib.glossary.GlossaryModel;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * Update the Glossary cache when documents containing {@code Glossary.Code.GlossaryClass} xobjects are modified.
 *
 * @version $Id$
 */
@Component
@Named("glossaryEventListener")
@Singleton
public class GlossaryEventListener implements EventListener
{
    private static final List<Event> EVENTS = Arrays.asList(new DocumentCreatedEvent(),
        new DocumentUpdatedEvent(), new DocumentDeletedEvent());

    private static final EntityReference GLOSSARY_XCLASS_REFERENCE =
        new EntityReference("GlossaryClass", EntityType.DOCUMENT,
            new EntityReference("Code", EntityType.SPACE,
                new EntityReference("Glossary", EntityType.SPACE)));

    @Inject
    private Provider<GlossaryCache> cacheProvider;

    @Inject
    private GlossaryModel glossaryModel;

    @Inject
    private Logger logger;

    @Override
    public List<Event> getEvents()
    {
        return EVENTS;
    }

    @Override
    public String getName()
    {
        return "glossaryEventListener";
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        XWikiDocument document = (XWikiDocument) source;
        XWikiContext xWikiContext = (XWikiContext) data;

        try {
            // We'll need to decide based on the current document status if it is a glossary entry or not. For each
            // supported event, the event can target either a document in its default language, or a translation of
            // the document.

            // Check if a document with the same document reference exists in the XWiki database with a glossary object
            // This can return a non-null object in 3 cases :
            // * We are working on an "original" (non-translated document), and we are just re-loading the same
            // document as we have in parameter of the event
            // * We are working with a translation of the document, and we are loading the non-translated version of
            // this document
            // * We are working with a translation document that just got deleted, however the original document
            // remains in the database.
            BaseObject glossaryObject = xWikiContext.getWiki().getDocument(document.getDocumentReference(),
                xWikiContext).getXObject(GLOSSARY_XCLASS_REFERENCE);
            boolean hasObject = glossaryObject != null;
            boolean isCreateOrUpdate = (event instanceof DocumentCreatedEvent || event instanceof DocumentUpdatedEvent);
            boolean hasOrHadObject =
                (hasObject || document.getOriginalDocument().getXObject(GLOSSARY_XCLASS_REFERENCE) != null);

            // Note: if the original doc is deleted then document.getOriginalDocument() is the doc before the
            // deletion and thus containing the xclass
            if ((isCreateOrUpdate && hasObject) || (event instanceof DocumentDeletedEvent && hasOrHadObject)) {
                handleEvent(document, event);
            }

        } catch (XWikiException e) {
            logger.error("Failed to update the glossary cache.", e);
        }
    }

    private void handleEvent(XWikiDocument document, Event event)
    {
        DocumentReference glossaryDocumentReference;
        String glossaryTitle;
        GlossaryCache cache = this.cacheProvider.get();

        if (event instanceof DocumentCreatedEvent) {
            glossaryDocumentReference = getLocalizedDocumentReference(document);
            glossaryTitle = document.getTitle();

            cache.set(glossaryTitle, glossaryDocumentReference);
        } else if (event instanceof DocumentUpdatedEvent) {
            glossaryDocumentReference = getLocalizedDocumentReference(document);
            glossaryTitle = document.getTitle();

            String glossaryOldTitle = document.getOriginalDocument().getTitle();
            String glossaryId = glossaryModel.getGlossaryId(glossaryDocumentReference);

            cache.remove(glossaryOldTitle, glossaryDocumentReference.getLocale(), glossaryId);
            cache.set(glossaryTitle, glossaryDocumentReference);

        } else if (event instanceof DocumentDeletedEvent) {
            glossaryDocumentReference = getLocalizedDocumentReference(document.getOriginalDocument());
            glossaryTitle = document.getOriginalDocument().getTitle();

            cache.remove(glossaryTitle, glossaryDocumentReference.getLocale(),
                glossaryModel.getGlossaryId(glossaryDocumentReference));
        }
    }

    private DocumentReference getLocalizedDocumentReference(XWikiDocument document)
    {
        DocumentReference glossaryDocumentReference = document.getDocumentReferenceWithLocale();

        // We want to avoid storing glossary entries with empty locales, as the XWiki Context will never return
        // an empty locale, which will prevent us afterwards to properly search for glossary entries in the cache.
        if (glossaryDocumentReference.getLocale().equals(Locale.ROOT)) {
            return new DocumentReference(glossaryDocumentReference, document.getDefaultLocale());
        }

        return glossaryDocumentReference;
    }
}
