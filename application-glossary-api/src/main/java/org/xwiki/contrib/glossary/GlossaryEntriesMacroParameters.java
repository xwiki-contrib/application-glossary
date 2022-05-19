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
package org.xwiki.contrib.glossary;

import org.xwiki.properties.annotation.PropertyDescription;
import org.xwiki.rendering.listener.HeaderLevel;

/**
 * Available parameters for the Glossary Entries macro.
 *
 * @version $Id$
 * @since 1.3
 */
public class GlossaryEntriesMacroParameters
{
    private String glossaryId;

    private HeaderLevel entryNameHeaderLevel = HeaderLevel.LEVEL2;

    /**
     * @param glossaryId see {@link #getGlossaryId()}
     */
    @PropertyDescription("The glossary id. Defaults to 'Glossary'")
    public void setGlossaryId(String glossaryId)
    {
        this.glossaryId = glossaryId;
    }

    /**
     * @return the id of the glossary (corresponds to the space reference where the glossary is located,
     *         e.g. for a {@code A.B.MyGlossary.WebHome} glossary home page, the id is {@code A.B.MyGlossary}. When not
     *         specified, {@code Glossary} is used.
     */
    public String getGlossaryId()
    {
        return this.glossaryId;
    }

    /**
     * @param entryNameHeaderLevel see {@link #getEntryNameHeaderLevel()}
     */
    @PropertyDescription("The header level used for displaying glossary entry name")
    public void setEntryNameHeaderLevel(HeaderLevel entryNameHeaderLevel)
    {
        this.entryNameHeaderLevel = entryNameHeaderLevel;
    }

    /**
     * @return the header level that will be used when displaying each glossary entry name.
     */
    public HeaderLevel getEntryNameHeaderLevel()
    {
        return this.entryNameHeaderLevel;
    }


}
