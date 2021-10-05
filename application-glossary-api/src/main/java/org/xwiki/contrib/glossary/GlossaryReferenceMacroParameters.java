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

/**
 * The available parameters for the Glossary Reference Macro.
 *
 * @version $Id$
 * @since 1.0
 */
public class GlossaryReferenceMacroParameters
{
    private String glossaryId;

    private String entryId;

    /**
     * @param entryId see {@link #getEntryId()}
     */
    @PropertyDescription("The glossary entry id")
    public void setEntryId(String entryId)
    {
        this.entryId = entryId;
    }

    /**
     * @return the id of the glossary entry (corresponds to the page title where the glossary entry is stored
     */
    public String getEntryId()
    {
        return this.entryId;
    }

    /**
     * @param glossaryId see {@link #getEntryId()}
     */
    @PropertyDescription("The glossary id (there can be several glossaries). Defaults to 'Glossary'")
    public void setGlossaryId(String glossaryId)
    {
        this.glossaryId = glossaryId;
    }

    /**
     * @deprecated since 1.1, the glossary ID is not needed anymore as the macro uses the glossary cache
     * @return the id of the glossary (corresponds to the space reference where the glossary is located,
     *         e.g. for a {@code A.B.MyGlossary.WebHome} glossary home page, the id is {@code A.B.MyGlossary}. When not
     *         specified, {@code Glossary} is used.
     */
    public String getGlossaryId()
    {
        return this.glossaryId;
    }
}
