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
package org.xwiki.contrib.glossary.latex.script;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.script.ScriptContext;

import org.apache.commons.io.IOUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.glossary.GlossaryCache;
import org.xwiki.contrib.glossary.GlossaryException;
import org.xwiki.contrib.glossary.GlossaryModel;
import org.xwiki.contrib.latex.internal.LaTeXResourceConverter;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.renderer.BlockRenderer;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.script.ScriptContextManager;
import org.xwiki.script.service.ScriptService;

import com.xpn.xwiki.XWikiContext;

/**
 * Script service to help Glossary template perform complex actions such as saving glossary entries both in memory
 * and inside the generated zip file.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Named("latexGlossary")
@Singleton
public class LaTeXGlossaryScriptService implements ScriptService
{
    private static final String NL = "\n";

    private static final String SC_LATEX = "latex";

    private static final String SC_LATEX_GLOSSARY = "glossaryData";

    private static final String GLOSSARY_FILE = "glossary.tex";

    private static final String LATEX_BINDING_RESOURCE_CONVERTER = "resourceConverter";

    @Inject
    private GlossaryCache glossaryCache;

    @Inject
    private GlossaryModel glossaryModel;

    @Inject
    @Named("plain/1.0")
    private BlockRenderer plainBlockRenderer;

    @Inject
    private Provider<XWikiContext> xwikiContextProvider;

    @Inject
    private ScriptContextManager scriptContextManager;

    /**
     * Save the glossary data in the script context so that it can be later accessed when saving it to disk.
     *
     * @param entryId the glossary entry id to save
     * @throws GlossaryException in case an error happens to retrieve the document containing the glossary entry
     */
    public void saveGlossaryEntryInContext(String entryId) throws GlossaryException
    {
        // Algorithm:
        // - Try to find a glossary entry matching the passed id
        // - If found, get the description and save the id + the description in the Velocity Context inside the "latex"
        //   binding (which is a Map<String, Object>). For simplicity, save it as a Map with 2 keys: "id" and
        //   "description".
        DocumentReference reference = this.glossaryCache.get(entryId);
        if (reference != null) {
            saveGlossaryData(entryId, getRenderedDocumentContent(reference));
        }
    }

    /**
     * Save glossary entries to a file ({@code glossary.tex} that is LaTeX-included in the preamble.
     *
     * @throws GlossaryException in case of an error writing to file
     */
    public void saveGlossaryEntriesToFile() throws GlossaryException
    {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Object> glossaryDatum : getGlossaryData().entrySet()) {
            builder.append(String.format("\\newglossaryentry{%s}", glossaryDatum.getKey())).append(NL);
            builder.append("{").append(NL);
            builder.append(String.format("    name=%s,", glossaryDatum.getKey())).append(NL);
            builder.append(String.format("    description={%s}", glossaryDatum.getValue())).append(NL);
            builder.append("}").append(NL);
        }
        LaTeXResourceConverter converter = getLaTeXResourceConverter();
        try {
            try (InputStream inputStream = IOUtils.toInputStream(builder.toString(), "UTF-8")) {
                converter.store(GLOSSARY_FILE, inputStream);
            }
        } catch (IOException e) {
            throw new GlossaryException(String.format("Failed to write glossary entries to [%s]", GLOSSARY_FILE),
                e);
        }
    }

    /**
     * @return the glossary data as a map: the map index is the entry id and the value is the description
     */
    public Map<String, Object> getGlossaryData()
    {
        ScriptContext currentScriptContext = this.scriptContextManager.getCurrentScriptContext();
        Map<String, Object> latexBinding = (Map<String, Object>) currentScriptContext.getAttribute(SC_LATEX);
        // Note: The LaTeX binding should never be null since the export starts by creating it.
        Map<String, Object> glossaryData = (Map<String, Object>) latexBinding.get(SC_LATEX_GLOSSARY);
        if (glossaryData == null) {
            glossaryData = new HashMap<>();
            latexBinding.put(SC_LATEX_GLOSSARY, glossaryData);
        }
        return glossaryData;
    }

    private void saveGlossaryData(String entryId, String description)
    {
        Map<String, Object> glossaryData = getGlossaryData();
        glossaryData.put(entryId, description);
    }

    private LaTeXResourceConverter getLaTeXResourceConverter()
    {
        ScriptContext currentScriptContext = this.scriptContextManager.getCurrentScriptContext();
        Map<String, Object> latexBinding = (Map<String, Object>) currentScriptContext.getAttribute(SC_LATEX);
        return (LaTeXResourceConverter) latexBinding.get(LATEX_BINDING_RESOURCE_CONVERTER);
    }

    private String getRenderedDocumentContent(DocumentReference reference) throws GlossaryException
    {
        Locale locale = xwikiContextProvider.get().getLocale();
        XDOM xdom = this.glossaryModel.getGlossaryContent(reference, locale);
        // TODO: Render it as latex/1.0 content. Note that for this to work we need to implement a new simplified
        // latex/1.0 renderer that doesn't render any preamble.
        WikiPrinter printer = new DefaultWikiPrinter();
        this.plainBlockRenderer.render(xdom, printer);
        return printer.toString();
    }
}
