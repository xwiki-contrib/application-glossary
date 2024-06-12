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
package org.xwiki.contrib.glossary.translator.deepl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.glossary.translator.TranslationGlossary;
import org.xwiki.contrib.glossary.translator.internal.AbstractTranslationGlossary;
import org.xwiki.contrib.glossary.translator.model.TranslatorGlossaryInfo;
import org.xwiki.contrib.glossary.translator.model.TranslatorGlossaryLanguagePairs;
import org.xwiki.contrib.translator.deepl.DeeplTranslator;
import org.xwiki.script.service.ScriptServiceManager;

import com.deepl.api.GlossaryEntries;
import com.deepl.api.GlossaryInfo;
import com.deepl.api.Translator;

@Component
@Named(DeepLTranslationGlossary.HINT)
@Singleton
public class DeepLTranslationGlossary extends AbstractTranslationGlossary
    implements TranslationGlossary
{
    final static String HINT = "deepl";

    final static String NAME = "DeepL";

    @Inject
    private ScriptServiceManager scriptServiceManager;

    private Translator getDeepLTranslator()
    {
        DeeplTranslator translator = (DeeplTranslator) translatorManager.getTranslator();
        return translator.getTranslator();
    }

    private static List<String> getGlossariesWithName(List<GlossaryInfo> deeplGlossaries, String glossaryName)
    {
        List<String> glossaryIds = new ArrayList<>();
        for (GlossaryInfo glossary : deeplGlossaries) {
            if (glossary.getName().equals(glossaryName)) {
                glossaryIds.add(glossary.getGlossaryId());
            }
        }
        return glossaryIds;
    }

    //
    // Public Method used by different pages
    //
    @Override
    public void synchronizeGlossaries()
    {
        DeeplTranslator translator = (DeeplTranslator) translatorManager.getTranslator();
        Translator deepLTranslator = translator.getTranslator();

        try {
            String glossaryNamePrefix = getGlossaryNamePrefix();

            List<GlossaryInfo> deeplGlossaries = deepLTranslator.listGlossaries();
            logger.debug("Fetched the list of registered glossaries : [{}]", deeplGlossaries);

            List<TranslatorGlossaryLanguagePairs> translatorSupportedGlossaries = deepLTranslator.getGlossaryLanguages()
                .stream()
                .map(entry -> new TranslatorGlossaryLanguagePairs(entry.getSourceLanguage(),
                    entry.getTargetLanguage()))
                .collect(Collectors.toList());
            logger.debug("Fetched the list of supported glossary language combinations : [{}]",
                translatorSupportedGlossaries);

            Map<Locale, List<Locale>> localeCombinations = getLocaleListMap(translatorSupportedGlossaries);

            for (Map.Entry<Locale, List<Locale>> e : localeCombinations.entrySet()) {
                Locale sourceLanguage = e.getKey();
                List<Locale> availableTargetLanguages = e.getValue();
                for (Locale targetLanguage : availableTargetLanguages) {// Compute the name of the glossary
                    String glossaryName = glossaryNamePrefix + "-"
                        + sourceLanguage.toString() + "-" + targetLanguage.toString();
                    String deeplSrcLang = translator.normalizeLocale(sourceLanguage);
                    String deeplDstLang = translator.normalizeLocale(targetLanguage);

                    logger.info("Synchronizing glossary [{}]", glossaryName);

                    // Create the list of terms to be added to the glossary
                    GlossaryEntries glossaryEntries = new GlossaryEntries(getLocalGlossaryEntries(targetLanguage,
                        sourceLanguage));

                    // Check if the glossary exists. If it's the case, we need to delete it to re-create it
                    for (String glossaryId : getGlossariesWithName(deeplGlossaries, glossaryName)) {
                        logger.debug("Deleting glossary [{}] with ID [{}]", glossaryName, glossaryId);
                        deepLTranslator.deleteGlossary(glossaryId);
                    }

                    // Create (or re-create the glossary)
                    logger.debug("Creating glossary [{}]", glossaryName);
                    deepLTranslator.createGlossary(glossaryName, deeplSrcLang, deeplDstLang, glossaryEntries);
                }
            }
            logger.debug("Finished synchronizing glossaries");
        } catch (Exception e) {
            logger.error("Got unexpected error while synchronizing glossaries : [{}]", e.getMessage(), e);
        }
    }

    @Override
    public List<TranslatorGlossaryInfo> getGlossaries()
    {
        Translator translator = getDeepLTranslator();
        try {
            String glossaryNamePrefix = getGlossaryNamePrefix();
            return translator.listGlossaries()
                .stream()
                .filter(entry -> entry.getName().startsWith(glossaryNamePrefix))
                .map(item -> new TranslatorGlossaryInfo(item.getGlossaryId(), item.getName(), item.isReady(),
                    item.getSourceLang(), item.getTargetLang(), item.getEntryCount()))
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Got unexpected error while synchronizing glossaries : [{}]", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<TranslatorGlossaryLanguagePairs> getGlossaryLanguagePairs()
    {
        Translator translator = getDeepLTranslator();
        try {
            return translator.getGlossaryLanguages().stream()
                .map(item -> new TranslatorGlossaryLanguagePairs(item.getSourceLanguage(), item.getTargetLanguage()))
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Got unexpected error while synchronizing glossaries : [{}]", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, String> getGlossaryEntryDetails(String id)
    {
        Translator translator = getDeepLTranslator();
        try {
            return translator.getGlossaryEntries(id);
        } catch (Exception e) {
            logger.error("Got unexpected error while synchronizing glossaries : [{}]", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    // Used by the translator extension to pass the correct glossary ID on translation
    @Override
    public Optional<String> getGlossaryIdForLanguages(Locale source, Locale destination)
    {
        Translator translator = getDeepLTranslator();
        try {
            String glossaryNamePrefix = getGlossaryNamePrefix();
            String glossaryName = glossaryNamePrefix + "-" + source.toString() + "-" + destination.toString();
            return translator.listGlossaries().stream()
                .filter(entry -> entry.getName().equals(glossaryName))
                .findFirst().map(GlossaryInfo::getGlossaryId);
        } catch (Exception e) {
            logger.error("Got unexpected error while synchronizing glossaries : [{}]", e.getMessage(), e);
            return Optional.empty();
        }
    }

    public String getName()
    {
        return NAME;
    }
}
