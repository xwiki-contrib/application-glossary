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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.glossary.GlossaryCache;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.xpn.xwiki.doc.XWikiDocument;

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
    private static final List<Event> EVENTS = Arrays.asList(new DocumentCreatedEvent(), new DocumentDeletedEvent());

    private static final EntityReference GLOSSARY_XCLASS_REFERENCE =
        new EntityReference("GlossaryClass", EntityType.DOCUMENT,
            new EntityReference("Code", EntityType.SPACE,
                new EntityReference("Glossary", EntityType.SPACE)));

    @Inject
    private Provider<GlossaryCache> cache;

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
        // Note:
        // - if the doc is deleted then document.getOriginalDocument() is the doc before the deletion and thus
        //   containing the xclass
        // - if the doc is created then document.getOriginalDocument() is the same as document
        if ((event instanceof DocumentCreatedEvent && document.getXObject(GLOSSARY_XCLASS_REFERENCE) != null)
            || (event instanceof DocumentDeletedEvent
                && document.getOriginalDocument().getXObject(GLOSSARY_XCLASS_REFERENCE) != null))
        {
            DocumentReference glossaryDocumentReference = document.getDocumentReference();
            String glossaryName = getGlossaryName(glossaryDocumentReference);
            if (event instanceof DocumentCreatedEvent) {
                this.cache.get().set(glossaryName, glossaryDocumentReference);
            } else if (event instanceof DocumentDeletedEvent) {
                this.cache.get().remove(glossaryName);
            }
        }
    }

    private String getGlossaryName(DocumentReference reference)
    {
        String name;
        if ("WebHome".equals(reference.getName())) {
            name = reference.getParent().getName();
        } else {
            name = reference.getName();
        }
        return name;
    }
}
