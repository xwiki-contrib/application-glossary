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
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.bridge.event.DocumentUpdatingEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.glossary.GlossaryConfiguration;
import org.xwiki.contrib.glossary.GlossaryModel;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;
import org.xwiki.rendering.block.XDOM;

import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

/**
 * Scan any document at save time and look for glossary words in the document content. If words are found,
 * update them to insert a glossary macro.
 *
 * @version $Id$
 * @since 1.1
 */
@Component
@Named(GlossaryDocumentSaveEventListener.LISTENER_NAME)
@Singleton
public class GlossaryDocumentSaveEventListener implements EventListener
{
    /**
     * The name of the listener.
     */
    public static final String LISTENER_NAME = "glossaryDocumentSaveEventListener";

    @Inject
    private Logger logger;

    @Inject
    private GlossaryModel glossaryModel;

    @Inject
    private GlossaryConfiguration glossaryConfiguration;

    @Override
    public String getName()
    {
        return LISTENER_NAME;
    }

    @Override
    public List<Event> getEvents()
    {
        return Arrays.asList(new DocumentUpdatingEvent());
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        if (glossaryConfiguration.updateDocumentsOnSave()) {
            XWikiDocument document = (XWikiDocument) source;

            try {
                XDOM xdom = document.getXDOM();
                if (glossaryModel.transformGlossaryEntries(xdom, document.getLocale())) {
                    document.setContent(xdom);
                }
            } catch (XWikiException e) {
                logger.error("Failed to transform content for document [{}] to look for Glossary entries.",
                    document.getDocumentReference(), e);
            }
        }
    }
}
