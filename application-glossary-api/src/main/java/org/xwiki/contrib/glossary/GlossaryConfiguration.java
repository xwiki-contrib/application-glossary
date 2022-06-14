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
package org.xwiki.contrib.glossary;

import java.util.List;

import org.xwiki.component.annotation.Role;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.SpaceReference;

/**
 * Configuration for the Glossary application.
 *
 * @version $Id$
 * @since 1.1
 */
@Role
public interface GlossaryConfiguration
{
    /**
     * @return true if documents should be updated at save time if they contain words referring to the glossary.
     */
    boolean updateDocumentsOnSave();

    /**
     * @return a list of class references which should be excluded from document update on save.
     */
    List<EntityReference> excludedClassesFromTransformations();

    /**
     * @return the default glossary ID to be used when not specified.
     */
    String defaultGlossaryId();

    /**
     * @return true if the periodic transformation job should be active.
     */
    boolean isActivateTransformationJob();

    /**
     * @return true if page version should be incremented when a link to a glossary entry is added or removed by the
     * periodic transformation job.
     */
    boolean isIncrementVersionOnTransformationJob();

    /**
     * @return list of SpaceReferences where the periodic transformation job should perform transformations
     */
    List<SpaceReference> getTransformationJobIncludeSpaces();

}
