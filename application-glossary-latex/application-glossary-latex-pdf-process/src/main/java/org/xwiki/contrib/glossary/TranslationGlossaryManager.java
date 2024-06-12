package org.xwiki.contrib.glossary;

public interface TranslationGlossaryManager
{
    List<GlossaryInfo> GetDeeplGlossariesEntries();

    List<GlossaryLanguagePair> GetDeeplGlossaryiesLanguesPairs();

    GlossaryEntries GetDeeplGlossariesEntriesDetails(String id);

    // Used by the translator deepl extension to pass the correct glossary ID on translation
    String GetGlossaryIdForLanguages(String srcLanguage, String dstLanguage);
}
