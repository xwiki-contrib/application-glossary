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

import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.xwiki.glossary.test.po.GlossaryEntryEditPage;
import org.xwiki.glossary.test.po.GlossaryHomePage;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.panels.test.po.ApplicationsPanel;
import org.xwiki.test.ui.AbstractTest;
import org.xwiki.test.ui.SuperAdminAuthenticationRule;
import org.xwiki.test.ui.po.LiveTableElement;
import org.xwiki.test.ui.po.ViewPage;
import org.junit.Assert;

/**
 * UI tests for the Glossary application.
 *
 * @version $Id$
 * @since 4.3M2
 */
public class GlossaryTest extends AbstractTest
{
    // Login as superadmin to have delete rights.
    @Rule
    public SuperAdminAuthenticationRule authenticationRule = new SuperAdminAuthenticationRule(getUtil());

    @Test
    public void testGlossary() throws Exception
    {
        // Note: we use a dot in the page name to verify it's supported by the Glossary application and we use an accent
        // to
        // verify encoding.
        String glossaryTestPage = "Test.entrée de Glossary";

        // Delete pages that we create in the test
        getUtil().rest().deletePage(getTestClassName(), glossaryTestPage);

        // Navigate to the Glossary app by clicking in the Application Panel.
        // This verifies that the Glossary application is registered in the Applications Panel.
        // It also verifies that the Translation is registered properly.
        ApplicationsPanel applicationPanel = ApplicationsPanel.gotoPage();
        ViewPage vp = applicationPanel.clickApplication("Glossary");

        // Verify we're on the right page!
        GlossaryHomePage homePage = GlossaryHomePage.DEFAULT_GLOSSARY_HOME_PAGE;
        Assert.assertEquals(homePage.getSpaces(), vp.getMetaDataValue("space"));
        Assert.assertEquals(homePage.getPage(), vp.getMetaDataValue("page"));

        // Add Glossary entry
        GlossaryEntryEditPage entryPage = homePage.addGlossaryEntry(glossaryTestPage);
        entryPage.setAnswer("content");
        vp = entryPage.clickSaveAndView();

        // Go back to the home page by clicking in the breadcrumb (this verifies that the new entry has the Glossary
        // home
        // specified in the breadcrumb).
        vp.clickBreadcrumbLink("Glossary");

        // Assert Livetable:
        // - verify that the Translation has been applied by checking the Translated livetable column name
        // - verify that the Livetable contains our new Glossary entry
        LiveTableElement lt = homePage.getGlossaryLiveTable();
        Assert.assertTrue(lt.hasRow("Glossary Items", glossaryTestPage));
    }

    /**
     * Verify that it's possible to add a new Glossary altogether, in a different space. Also make sure it works when
     * creating that new Glossary in a Nested Space.
     * 
     * @throws Exception
     */
    @Test
    public void testNewGlossaryAndInNestedSpace() throws Exception
    {
        String glossaryTestPage = "NewGlossaryEntry";
        DocumentReference homeReference =
            new DocumentReference("xwiki", Arrays.asList(getTestClassName(), "Nested"), "WebHome");

        // Delete pages that we create in the test
        getUtil().rest().delete(homeReference);

        // Create a new Glossary home page
        getUtil().addObject(homeReference, "GlossaryCode.GlossaryHomeClass", "description", "new Glossary");
        // Note: AddObject stays in edit mode so we need to navigate again
        GlossaryHomePage homePage = new GlossaryHomePage(homeReference);
        homePage.gotoPage();

        // Add Glossary entry
        GlossaryEntryEditPage entryPage = homePage.addGlossaryEntry(glossaryTestPage);
        entryPage.setAnswer("new content");
        ViewPage vp = entryPage.clickSaveAndView();

        // Go back to the home page by clicking in the breadcrumb (this verifies that the new entry has the Glossary
        // home
        // specified in the breadcrumb).
        vp.clickBreadcrumbLink("Nested");

        // Assert Livetable:
        // - verify that the Translation has been applied by checking the Translated livetable column name
        // - verify that the Livetable contains our new Glossary entry
        LiveTableElement lt = homePage.getGlossaryLiveTable();
        Assert.assertTrue(lt.hasRow("Glossary Items", glossaryTestPage));
    }
}
