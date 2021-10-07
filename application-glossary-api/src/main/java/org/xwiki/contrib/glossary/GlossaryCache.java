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
import org.xwiki.model.reference.DocumentReference;

/**
 * @version $Id$
 */
@Role
public interface GlossaryCache
{
    /**
     * Get the value associated with the provided key in the cache.
     * When called, the cache will look for the entry corresponding to the current locale, in the default
     * glossary ID.
     *
     * @param key of type String
     * @return the DocumentReference
     */
    DocumentReference get(String key);

    /**
     * Get the value associated with the provided key in the cache.
     *
     * @param key of type String
     * @param locale the locale to use when searching for the entry
     * @return the DocumentReference
     */
    DocumentReference get(String key, Locale locale);

    /**
     * Get the value associated with the provided key and glossary id in the cache.
     *
     * @param key of type String
     * @param locale the locale to use when searching for the entry
     * @param glossaryId the ID of the glossary, which corresponds to the space the glossary entry is in
     * @return the DocumentReference
     */
    DocumentReference get(String key, Locale locale, String glossaryId);

    /**
     * Sets the key and value in the cache. If the provided document reference has a locale, the locale will be used
     * in the cache key. Else, the locale will be taken from the current context.
     *
     * @param key represents the glossaryItem.
     * @param value representing the reference to the glossary Document.
     */
    void set(String key, DocumentReference value);

    /**
     * Sets the key and value in the cache.
     *
     * @param key represents the glossaryItem.
     * @param locale the locale to use for the entry
     * @param value representing the reference to the glossary Document.
     */
    void set(String key, Locale locale, DocumentReference value);

    /**
     * Remove the key and value from the cache.
     *
     * @param key represents the glossaryItem.
     */
    void remove(String key);

    /**
     * Remove the key and value from the cache.
     *
     * @param key represents the glossaryItem.
     * @param locale represents the locale for which the item should be removed
     */
    void remove(String key, Locale locale);

    /**
     * Remove the key and value from the cache.
     *
     * @param key represents the glossaryItem.
     * @param locale represents the locale for which the item should be removed
     * @param glossaryId the ID of the glossary that should be used
     */
    void remove(String key, Locale locale, String glossaryId);
}
