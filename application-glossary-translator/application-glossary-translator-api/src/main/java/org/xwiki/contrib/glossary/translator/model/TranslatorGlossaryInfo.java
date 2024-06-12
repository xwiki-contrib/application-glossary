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
package org.xwiki.contrib.glossary.translator.model;

import java.util.Date;

public class TranslatorGlossaryInfo
{
    private final String glossaryId;

    private final String name;

    private final boolean ready;

    private final String sourceLang;

    private final String targetLang;

    private final long entryCount;

    public TranslatorGlossaryInfo(String glossaryId, String name, boolean ready, String sourceLang, String targetLang,
        long entryCount)
    {
        this.glossaryId = glossaryId;
        this.name = name;
        this.ready = ready;
        this.sourceLang = sourceLang;
        this.targetLang = targetLang;
        this.entryCount = entryCount;
    }

    public String getGlossaryId()
    {
        return glossaryId;
    }

    public String getName()
    {
        return name;
    }

    public boolean isReady()
    {
        return ready;
    }

    public String getSourceLang()
    {
        return sourceLang;
    }

    public String getTargetLang()
    {
        return targetLang;
    }

    public long getEntryCount()
    {
        return entryCount;
    }
}
