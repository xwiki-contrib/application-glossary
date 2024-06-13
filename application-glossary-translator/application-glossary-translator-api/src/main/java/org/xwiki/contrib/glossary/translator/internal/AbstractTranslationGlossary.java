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
package org.xwiki.contrib.glossary.translator.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.contrib.glossary.translator.model.TranslationGlossaryInfo;
import org.xwiki.contrib.glossary.translator.model.TranslationGlossaryLanguagePairs;
import org.xwiki.contrib.translator.Translator;
import org.xwiki.contrib.translator.TranslatorManager;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.ObjectReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * @version $Id$
 */
public abstract class AbstractTranslationGlossary
{
    @Inject
    protected Logger logger;

    @Inject
    protected Provider<XWikiContext> xwikiContextProvider;

    @Inject
    protected TranslatorManager translatorManager;

    @Inject
    private QueryManager queryManager;

    @Inject
    private ComponentManager componentManager;

    //
    // Public Method used by different pages
    //
    public abstract void synchronizeGlossaries();

    public abstract List<TranslationGlossaryInfo> getGlossaries();

    public abstract List<TranslationGlossaryLanguagePairs> getGlossaryLanguagePairs();

    public abstract Map<String, String> getGlossaryEntryDetails(String id);

    public abstract Optional<String> getGlossaryIdForLanguages(Locale source, Locale destination);

    protected Map<Locale, List<Locale>> getLocaleListMap(
        List<TranslationGlossaryLanguagePairs> translatorSupportedGlossaries) throws XWikiException
    {
        Translator translator = translatorManager.getTranslator();
        XWikiContext context = xwikiContextProvider.get();

        // Get the list of languages that are supported in the XWiki preferences
        DocumentReference reference = new DocumentReference(context.getWikiId(), "XWiki", "XWikiPreferences");
        XWikiDocument preferencesDoc = context.getWiki().getDocument(reference, context);
        BaseObject preferencesObject =
            preferencesDoc.getXObject(new ObjectReference("XWiki.XWikiPreferences", reference));
        String[] missingLanguagesRaw = preferencesObject.getStringValue("languages").split(",");

        Map<Locale, List<Locale>> localeCombinations = new HashMap<>();
        for (String sourceLangStr : missingLanguagesRaw) {
            Locale sourceLanguage = new Locale(sourceLangStr);
            localeCombinations.put(sourceLanguage, new ArrayList<Locale>());
            for (String targetLangStr : missingLanguagesRaw) {
                Locale targetLanguage = new Locale(targetLangStr);
                String deeplSrcLang = translator.normalizeLocale(sourceLanguage);
                String deeplDstLang = translator.normalizeLocale(targetLanguage);

                // Check that src lang dans dstLang are not identical in case of the language is 'fr_FR' to
                // 'fr_CH' which is same for deepL
                if (!deeplSrcLang.equals(deeplDstLang) && AbstractTranslationGlossary.supportsLanguageCombination(
                    translatorSupportedGlossaries, deeplSrcLang, deeplDstLang))
                {
                    localeCombinations.get(sourceLanguage).add(targetLanguage);
                }
            }
        }
        return localeCombinations;
    }

    private static boolean supportsLanguageCombination(
        List<TranslationGlossaryLanguagePairs> translatorSupportedGlossaries, String sourceLanguage,
        String targetLanguage)
    {
        for (TranslationGlossaryLanguagePairs entry : translatorSupportedGlossaries) {
            if (entry.getSourceLanguage().toString().equals(sourceLanguage) && entry.getTargetLanguage().toString()
                .equals(targetLanguage))
            {
                return true;
            }
        }
        return false;
    }

    protected Map<String, String> getLocalGlossaryEntries(Locale targetLanguage, Locale sourceLanguage)
        throws QueryException
    {
        List<String[]> glossaryRawEntries = queryManager.createQuery("select sourceDoc.title, targetDoc.title "
                    + "from XWikiDocument sourceDoc, XWikiDocument targetDoc, BaseObject glossaryObj "
                    + "where glossaryObj.name = sourceDoc.fullName " + "and glossaryObj.name = targetDoc.fullName "
                    + "and glossaryObj.className = 'Glossary.Code.GlossaryClass' " + "and sourceDoc.space = 'Glossary' "
                    + "and targetDoc.space = 'Glossary' "
                    + "and ((sourceDoc.defaultLanguage = :sourceLanguage and sourceDoc.language = '') or sourceDoc.language = :sourceLanguage) "
                    + "and ((targetDoc.defaultLanguage = :targetLanguage and targetDoc.language = '') or targetDoc.language = :targetLanguage)",
                Query.HQL).bindValue("sourceLanguage", sourceLanguage.toString())
            .bindValue("targetLanguage", targetLanguage.toString()).execute();

        Map<String, String> glossaryEntries = new HashMap<>();
        for (String[] entry : glossaryRawEntries) {
            String trimmedKey = entry[0].trim();
            String trimmedValue = entry[1].trim();
            glossaryEntries.put(trimmedKey, trimmedValue);
        }
        return glossaryEntries;
    }

    protected String getGlossaryNamePrefix() throws XWikiException
    {
        XWikiContext context = xwikiContextProvider.get();
        DocumentReference reference =
            new DocumentReference(context.getWikiId(), "Glossary", "TranslatorGlossaryConfiguration");
        XWikiDocument preferencesDoc = context.getWiki().getDocument(reference, context);
        BaseObject preferencesObject = preferencesDoc.getXObject(
            new ObjectReference("Glossary.Code.Translator.TranslatorGlossaryConfigurationClass", reference));
        String glossariesPrefix = preferencesObject.getStringValue("translatorGlossaryNamePrefix");

        String wikiPrefix = context.getWikiId();

        if (StringUtils.isNoneBlank(glossariesPrefix)) {
            return wikiPrefix;
        } else {
            return glossariesPrefix + "-" + wikiPrefix;
        }
    }
}
