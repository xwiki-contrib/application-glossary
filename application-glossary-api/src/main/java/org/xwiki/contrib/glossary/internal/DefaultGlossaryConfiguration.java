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
package org.xwiki.contrib.glossary.internal;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.contrib.glossary.GlossaryConfiguration;
import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * Default implementation for {@link GlossaryConfiguration}.
 *
 * @version $Id$
 * @since 1.1
 */
@Component
@Singleton
public class DefaultGlossaryConfiguration implements GlossaryConfiguration
{
    private static final String CONFIGURATION_PREFIX = "glossary.";

    private static final String UPDATE_DOCUMENTS_ON_SAVE = "updateDocumentsOnSave";

    private static final String DEFAULT_GLOSSARY_ID = "defaultGlossaryId";

    private static final String GLOSSARY = "Glossary";

    private static final List<String> GLOSSARY_CODE_SPACE = Arrays.asList(GLOSSARY, "Code");

    @Inject
    private ConfigurationSource configurationSource;

    @Inject
    private Provider<XWikiContext> xWikiContextProvider;

    @Override
    public boolean updateDocumentsOnSave()
    {
        boolean defaultValue = configurationSource.getProperty(CONFIGURATION_PREFIX + UPDATE_DOCUMENTS_ON_SAVE, true);

        try {
            BaseObject baseObject = getConfigurationObject();

            if (baseObject != null) {
                int defaultIntValue = (defaultValue) ? 1 : 0;
                return (baseObject.getIntValue(UPDATE_DOCUMENTS_ON_SAVE, defaultIntValue) == 1);
            }
        } catch (Exception e) {
            // Fail silently
        }

        return defaultValue;
    }

    @Override
    public String defaultGlossaryId()
    {
        String defaultValue = configurationSource.getProperty(CONFIGURATION_PREFIX + DEFAULT_GLOSSARY_ID, GLOSSARY);

        try {
            BaseObject baseObject = getConfigurationObject();

            if (baseObject != null) {
                String propertyValue = baseObject.getStringValue(DEFAULT_GLOSSARY_ID);
                if (StringUtils.isNotBlank(propertyValue)) {
                    return propertyValue;
                }
            }
        } catch (Exception e) {
            // Fail silently
        }

        return defaultValue;
    }

    private BaseObject getConfigurationObject() throws XWikiException
    {
        XWikiContext xWikiContext = xWikiContextProvider.get();
        XWiki xWiki = xWikiContext.getWiki();

        XWikiDocument configurationDoc = xWiki.getDocument(new DocumentReference(
            xWikiContext.getWikiId(), GLOSSARY_CODE_SPACE, "GlossaryConfiguration"), xWikiContext);
        return configurationDoc.getXObject(new DocumentReference(
            xWikiContext.getWikiId(), GLOSSARY_CODE_SPACE, "GlossaryConfigurationClass"));
    }
}
