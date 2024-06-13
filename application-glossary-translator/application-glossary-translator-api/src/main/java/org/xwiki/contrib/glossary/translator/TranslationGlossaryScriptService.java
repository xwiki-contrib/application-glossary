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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.glossary.translator.model.TranslationGlossaryInfo;
import org.xwiki.contrib.glossary.translator.model.TranslationGlossaryLanguagePairs;
import org.xwiki.script.service.ScriptService;
import org.xwiki.security.authorization.ContextualAuthorizationManager;
import org.xwiki.security.authorization.Right;

/**
 * @version $Id$
 */
@Component
@Named(TranslationGlossaryScriptService.HINT)
@Singleton
public class TranslationGlossaryScriptService implements ScriptService
{
    static final String HINT = "translator";

    @Inject
    private ContextualAuthorizationManager authorizationManager;

    @Inject
    private TranslationGlossaryManager translationGlossaryManager;

    public void synchronizeGlossaries()
    {
        TranslationGlossary translationGlossary = translationGlossaryManager.getTranslationGlossary();
        if (this.authorizationManager.hasAccess(Right.PROGRAM)) {
            translationGlossary.synchronizeGlossaries();
        }
    }

    public List<TranslationGlossaryInfo> getGlossaries()
    {
        TranslationGlossary translationGlossary = translationGlossaryManager.getTranslationGlossary();
        if (this.authorizationManager.hasAccess(Right.PROGRAM)) {
            return translationGlossary.getGlossaries();
        } else {
            return new ArrayList<>(0);
        }
    }

    public Map<String, String> getGlossaryEntryDetails(String id)
    {
        TranslationGlossary translationGlossary = translationGlossaryManager.getTranslationGlossary();
        if (this.authorizationManager.hasAccess(Right.PROGRAM)) {
            return translationGlossary.getGlossaryEntryDetails(id);
        } else {
            return new HashMap<>(0);
        }
    }

    public List<TranslationGlossaryLanguagePairs> getGlossaryLanguagePairs()
    {
        TranslationGlossary translationGlossary = translationGlossaryManager.getTranslationGlossary();
        if (this.authorizationManager.hasAccess(Right.PROGRAM)) {
            return translationGlossary.getGlossaryLanguagePairs();
        } else {
            return new ArrayList<>(0);
        }
    }
}
