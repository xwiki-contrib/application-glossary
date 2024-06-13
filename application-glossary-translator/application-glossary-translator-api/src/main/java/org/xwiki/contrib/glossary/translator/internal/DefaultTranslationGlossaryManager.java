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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.contrib.glossary.translator.TranslationGlossary;
import org.xwiki.contrib.glossary.translator.TranslationGlossaryManager;
import org.xwiki.contrib.translator.TranslatorConfiguration;

@Component
@Singleton
public class DefaultTranslationGlossaryManager implements TranslationGlossaryManager
{
    @Inject
    private ComponentManager componentManager;

    @Inject
    private TranslatorConfiguration translatorConfiguration;

    @Inject
    private Logger logger;

    @Override
    public TranslationGlossary getTranslationGlossary()
    {
        return getTranslationGlossary(this.translatorConfiguration.getTranslator());
    }

    @Override
    public TranslationGlossary getTranslationGlossary(String hint)
    {
        try {
            return this.componentManager.getInstance(TranslationGlossary.class, hint);
        } catch (ComponentLookupException e) {
            this.logger.error("Error while getting the TranslationGlossary with hint [{}]", hint, e);
            return null;
        }
    }
}
