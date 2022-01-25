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

import java.util.Locale;

import org.xwiki.component.annotation.Role;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Transforms an XDOM by replacing glossary entry strings into glossary macro calls.
 *
 *  @version $Id$
 */
@Role
public interface GlossaryEntriesTransformer
{
    /**
     * Transforms an XDOM by replacing glossary entry strings into glossary entry macro calls.
     * @param xdom an XDOM
     * @param syntax wiki syntax
     * @param locale document locale
     * @return true if the XDOM was modified, false otherwise
     */
    boolean transformGlossaryEntries(XDOM xdom, Syntax syntax, Locale locale) throws GlossaryException;
}
