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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.bridge.event.DocumentUpdatingEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.glossary.GlossaryConfiguration;
import org.xwiki.contrib.glossary.GlossaryEntriesTransformer;
import org.xwiki.contrib.glossary.GlossaryException;
import org.xwiki.model.reference.EntityReference;
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
    private GlossaryConfiguration glossaryConfiguration;

    @Inject
    private GlossaryEntriesTransformer glossaryEntriesTransformer;

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

            // Check if the given document has at least one object from the excluded classes
            boolean hasExcludedObject = false;
            for (Iterator<EntityReference> it = glossaryConfiguration.excludedClassesFromTransformations().iterator();
                it.hasNext() && !hasExcludedObject;) {
                hasExcludedObject = (document.getXObjects(it.next()).size() > 0);
            }

            if (!hasExcludedObject) {
                // Compute the locale of the document that should be used to resolve glossary entries
                Locale locale = (Locale.ROOT.equals(document.getLocale())) ? document.getDefaultLocale() :
                    document.getLocale();

                try {
                    XDOM xdom = document.getXDOM();
                    long start = System.currentTimeMillis();
                    if (glossaryEntriesTransformer.transformGlossaryEntries(xdom, document.getSyntax(), locale)) {
                        long end = System.currentTimeMillis();
                        long duration = end - start;
                        logger.debug("Glossary transformation duration for [{}]: [{}] ",
                            document.getDocumentReference(),
                            duration);
                        document.setContent(xdom);
                    }
                } catch (XWikiException | GlossaryException e) {
                    logger.error("Failed to transform content for document [{}] to look for Glossary entries.",
                        document.getDocumentReference(), e);
                }
            }
        }
    }
}
