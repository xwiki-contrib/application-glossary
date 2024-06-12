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
package org.xwiki.contrib.glossary.translator;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.xwiki.component.annotation.Role;
import org.xwiki.contrib.glossary.translator.model.TranslatorGlossaryInfo;
import org.xwiki.contrib.glossary.translator.model.TranslatorGlossaryLanguagePairs;

@Role
public interface TranslationGlossary
{
    /**
     * Force synchronisation of the glossaries with translation provider
     */
    void synchronizeGlossaries();

    /**
     * @return a list of glossaries available on translator service
     */
    List<TranslatorGlossaryInfo> getGlossaries();

    /**
     * @param id glossary id
     * @return a map with source lang, target lang
     */
    Map<String, String> getGlossaryEntryDetails(String id);

    /**
     * @return a list of all available language pair
     */
    List<TranslatorGlossaryLanguagePairs> getGlossaryLanguagePairs();

    /**
     * Used by the translator extension to pass the correct glossary ID on translation
     * @param source language
     * @param destination language
     * @return the string of the glossary if found
     */
    Optional<String> getGlossaryIdForLanguages(Locale source, Locale destination);
}
