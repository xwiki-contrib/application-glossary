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

import org.xwiki.component.annotation.Role;
import org.xwiki.model.reference.DocumentReference;

/**
 * @version $Id$
 */
@Role
public interface GlossaryCache
{

    /**
     * Get the value associated with the provided key in the cache.
     *
     * @param key of type String
     * @return the DocumentReference
     */
    DocumentReference get(String key);

    /**
     * Sets the key and value in the cache.
     * 
     * @param key represents the glossaryItem.
     * @param value representing the DocumentReference of glossary Document.
     */
    void set(String key, DocumentReference value);

    /**
     * Remove the key and value pair from the cache.
     * 
     * @param key represents the glossaryItem.
     */
    void remove(String key);
}
