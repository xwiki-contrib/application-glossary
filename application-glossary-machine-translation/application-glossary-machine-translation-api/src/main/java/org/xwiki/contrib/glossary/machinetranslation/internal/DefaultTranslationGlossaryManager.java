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
package org.xwiki.contrib.glossary.machinetranslation.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.glossary.GlossaryConstants;
import org.xwiki.contrib.glossary.machinetranslation.TranslationGlossaryManager;
import org.xwiki.contrib.machinetranslation.Translator;
import org.xwiki.contrib.machinetranslation.TranslatorManager;
import org.xwiki.contrib.machinetranslation.model.Glossary;
import org.xwiki.contrib.machinetranslation.model.GlossaryInfo;
import org.xwiki.contrib.machinetranslation.model.LocalePair;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.xpn.xwiki.XWikiContext;

/**
 * @version $Id$
 */
@Component
@Singleton
public class DefaultTranslationGlossaryManager implements TranslationGlossaryManager
{
    @Inject
    private Logger logger;

    @Inject
    private Provider<XWikiContext> xwikiContextProvider;

    @Inject
    private TranslatorManager translatorManager;

    @Inject
    @Named("local")
    private EntityReferenceSerializer<String> serializer;

    @Inject
    private QueryManager queryManager;

    private Map<String, String> getLocalGlossaryEntries(Locale sourceLanguage, Locale targetLanguage)
        throws QueryException
    {
        List<String[]> glossaryRawEntries = queryManager.createQuery("select sourceDoc.title, targetDoc.title "
                    + "from XWikiDocument sourceDoc, XWikiDocument targetDoc, BaseObject glossaryObj "
                    + "where glossaryObj.name = sourceDoc.fullName "
                    + "and glossaryObj.name = targetDoc.fullName "
                    + "and glossaryObj.className = :glossaryClassRef "
                    + "and sourceDoc.space = 'Glossary' "
                    + "and targetDoc.space = 'Glossary' "
                    + "and ((sourceDoc.defaultLanguage = :sourceLanguage and sourceDoc.language = '') "
                    + "     or sourceDoc.language = :sourceLanguage) "
                    + "and ((targetDoc.defaultLanguage = :targetLanguage and targetDoc.language = '') "
                    + "     or targetDoc.language = :targetLanguage)",
                Query.HQL).bindValue("sourceLanguage", sourceLanguage.toString())
            .bindValue("targetLanguage", targetLanguage.toString())
            .bindValue("glossaryClassRef", serializer.serialize(GlossaryConstants.GLOSSARY_XCLASS_REFERENCE)).execute();

        Map<String, String> glossaryEntries = new HashMap<>();
        for (Object[] entry : glossaryRawEntries) {
            String trimmedKey = ((String) entry[0]).trim();
            String trimmedValue = ((String) entry[1]).trim();
            glossaryEntries.put(trimmedKey, trimmedValue);
        }
        return glossaryEntries;
    }

    @Override
    public void synchronizeGlossaries()
    {
        Translator translator = translatorManager.getTranslator();
        XWikiContext context = xwikiContextProvider.get();

        try {
            List<LocalePair> translatorSupportedLocalePairs = translator.getGlossaryLocalePairs();
            logger.debug("Fetched the list of supported glossary language combinations : [{}]",
                translatorSupportedLocalePairs);

            List<Locale> xwikiLanguages = xwikiContextProvider.get().getWiki().getAvailableLocales(context);
            List<Glossary> updateEntries = new ArrayList<>();

            logger.info("Generating glossary entries to register");

            for (Locale sourceLanguage : xwikiLanguages) {
                for (Locale targetLanguage : xwikiLanguages) {
                    String translatorSrcLang =
                        translator.normalizeLocale(sourceLanguage, Translator.NormalisationType.SOURCE_LANG_GLOSSARY);
                    String translatorDstLang =
                        translator.normalizeLocale(targetLanguage, Translator.NormalisationType.TARGET_LANG_GLOSSARY);

                    boolean foundMatchingLocalePairs = translatorSupportedLocalePairs.stream()
                        .anyMatch(entry ->
                            entry.getSourceLocale().toString().equals(translatorSrcLang)
                                && entry.getTargetLocale().toString().equals(translatorDstLang));

                    if (foundMatchingLocalePairs) {
                        Map<String, String> localGlossaryEntries = getLocalGlossaryEntries(sourceLanguage,
                            targetLanguage
                        );
                        if (!localGlossaryEntries.isEmpty()) {
                            updateEntries.add(
                                new Glossary(localGlossaryEntries,
                                    new GlossaryInfo("", "", true, sourceLanguage, targetLanguage, 0)));
                        }
                    }
                }
            }

            logger.info("Updating glossary into the translator");
            translator.updateGlossaries(updateEntries);
            logger.debug("Finished synchronizing glossaries");
        } catch (Exception e) {
            logger.error("Got unexpected error while synchronizing glossaries : [{}]", e.getMessage(), e);
        }
    }
}
