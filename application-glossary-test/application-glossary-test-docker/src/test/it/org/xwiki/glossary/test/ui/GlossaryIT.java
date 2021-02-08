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
package org.xwiki.glossary.test.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.xwiki.contrib.glossary.test.po.GlossaryEntryEditPage;
import org.xwiki.contrib.glossary.test.po.GlossaryHomePage;
import org.xwiki.panels.test.po.ApplicationsPanel;
import org.xwiki.test.docker.junit5.UITest;
import org.xwiki.test.ui.TestUtils;
import org.xwiki.test.ui.po.LiveTableElement;
import org.xwiki.test.ui.po.ViewPage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * UI tests for the Glossary application.
 *
 * @version $Id$
 * @since 4.3M2
 */
@UITest
class GlossaryIT
{
    @Test
    void verifyGlossary(TestUtils setup, TestInfo info) throws Exception
    {
        setup.loginAsSuperAdmin();
        verifyGlossaryApplication(setup, info);
        verifyGlossaryReferenceMacro();
        verifyGlossaryTransformation();
        verifyGlossaryLaTeXExport();
    }

    void verifyGlossaryApplication(TestUtils setup, TestInfo info) throws Exception
    {
        // Note: we use a dot in the page name to verify it's supported by the Glossary application and we use an accent
        // to
        // verify encoding.
        String glossaryTestPage = "Test.entrée de Glossary";

        // Delete pages that we create in the test
        setup.rest().deletePage(info.getTestClass().get().getSimpleName(), glossaryTestPage);

        // Navigate to the Glossary app by clicking in the Application Panel.
        // This verifies that the Glossary application is registered in the Applications Panel.
        // It also verifies that the Translation is registered properly.
        ApplicationsPanel applicationPanel = ApplicationsPanel.gotoPage();
        ViewPage vp = applicationPanel.clickApplication("Glossary");

        // Verify we're on the right page!
        GlossaryHomePage homePage = GlossaryHomePage.DEFAULT_GLOSSARY_HOME_PAGE;
        assertEquals(homePage.getSpaces(), vp.getMetaDataValue("space"));
        assertEquals(homePage.getPage(), vp.getMetaDataValue("page"));

        // Add Glossary entry
        GlossaryEntryEditPage entryPage = homePage.addGlossaryEntry(glossaryTestPage);
        entryPage.setDefinition("content");
        vp = entryPage.clickSaveAndView();

        // Go back to the home page by clicking in the breadcrumb (this verifies that the new entry has the Glossary
        // home specified in the breadcrumb).
        vp.clickBreadcrumbLink("Glossary");

        // Assert Livetable:
        // - verify that the Translation has been applied by checking the Translated livetable column name
        // - verify that the Livetable contains our new Glossary entry
        LiveTableElement lt = homePage.getGlossaryLiveTable();
        assertTrue(lt.hasRow("Glossary Items", glossaryTestPage));
    }

    void verifyGlossaryReferenceMacro()
    {
        // TODO
    }

    void verifyGlossaryTransformation()
    {
        // TODO
    }

    void verifyGlossaryLaTeXExport()
    {
        // TODO
    }
}
