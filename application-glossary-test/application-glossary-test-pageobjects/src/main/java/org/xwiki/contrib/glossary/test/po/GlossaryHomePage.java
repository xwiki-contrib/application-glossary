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
package org.xwiki.contrib.glossary.test.po;

import java.util.Arrays;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.test.ui.po.LiveTableElement;
import org.xwiki.test.ui.po.ViewPage;

/**
 * Represents actions that can be done on a Glossary home page.
 *
 * @version $Id$
 */
public class GlossaryHomePage extends ViewPage
{
    /**
     * Main wiki id.
     */
    public static final String MAIN_WIKI = "xwiki";

    /**
     * Glossary home page document reference.
     */
    public static final GlossaryHomePage DEFAULT_GLOSSARY_HOME_PAGE =
        new GlossaryHomePage(new DocumentReference(MAIN_WIKI, Arrays.asList("Glossary"), "WebHome"));

    private EntityReference homeReference;

    /**
     * @param homeReference the reference to the home page where the Glossary app is installed (several versions of the
     *            Glossary app can be installed in the same wiki)
     */
    public GlossaryHomePage(EntityReference homeReference)
    {
        this.homeReference = homeReference;
    }

    /**
     * Opens the home page.
     */
    public void gotoPage()
    {
        getUtil().gotoPage(this.homeReference);
    }

    /**
     * @return the String reference to the space where the Glossary app is installed (e.g. "{@code Space1.Space2})
     * @since 7.2RC1
     */
    public String getSpaces()
    {
        return getUtil().serializeReference(
            this.homeReference.extractReference(EntityType.SPACE).removeParent(new WikiReference(MAIN_WIKI)));
    }

    /**
     * @return the name of the home page where the Glossary app is installed (e.g. "{@code WebHome})
     */
    public String getPage()
    {
        return this.homeReference.getName();
    }

    /**
     * @param glossaryName the name of the Glossary entry to add
     * @return the new Glossary entry page
     */
    public GlossaryEntryEditPage addGlossaryEntry(String glossaryName)
    {
        WebElement glossaryNameField = getDriver().findElement(By.name("glossaryItem"));
        WebElement glossaryNameButton = getDriver().findElement(By.xpath(
            "//div[contains(@class, 'glossary-link add-glossary')]//input[contains(@class, 'btn btn-success')]"));
        glossaryNameField.clear();
        glossaryNameField.sendKeys(glossaryName);
        glossaryNameButton.click();
        return new GlossaryEntryEditPage();
    }

    /**
     * @return the Glossary livetable element
     */
    public LiveTableElement getGlossaryLiveTable()
    {
        LiveTableElement lt = new LiveTableElement("glossary");
        lt.waitUntilReady();
        return lt;
    }
}
