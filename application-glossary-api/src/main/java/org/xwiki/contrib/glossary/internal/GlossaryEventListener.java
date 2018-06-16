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

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

import com.xpn.xwiki.doc.XWikiDocument;

/**
 * An event listener for the Glossary Application.
 * 
 * @version $Id$
 */
@Component
@Named("glossaryEventListener")
@Singleton
public class GlossaryEventListener implements EventListener
{

    // Cache component to be injected here and to be modified below.

    /**
     * Event listened by the component. Two cases: When a Glossary entry is 1)Created 2)Deleted. TODO: When glossary
     * entry is updated.
     */
    private static final List<Event> EVENTS =
        Arrays.<Event>asList(new DocumentCreatedEvent(), new DocumentDeletedEvent());

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
        // Document on which the event is happening
        XWikiDocument document = (XWikiDocument) source;
        // Gives the name of the space in which the document is created.
        String spaceName = document.getDocumentReference().getLastSpaceReference().getName();
        // Check if the document modifies has "Glossary" space.
        if (spaceName.equals(new String("Glossary"))) {
            // Fetch the name of the document
            String glossaryEntry = document.getDocumentReference().getName();
            // Fetch the document reference of the document.
            DocumentReference glossaryDocumentRefernce = document.getDocumentReference();
            // TODO:update the cache object

        }

    }

}
