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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.cache.Cache;
import org.xwiki.cache.CacheException;
import org.xwiki.cache.CacheManager;
import org.xwiki.cache.config.CacheConfiguration;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.glossary.GlossaryCache;
import org.xwiki.model.reference.DocumentReference;

/**
 * @version $Id$
 */
@Component
@Singleton
public class DefaultGlossaryCache implements GlossaryCache
{

    /**
     * Used to initialize the actual cache component.
     */
    @Inject
    private CacheManager cacheManager;

    /**
     * The actual cache object.
     */

    private Cache<DocumentReference> cache;

    /**
     * The identifier of the cache.
     */
    private String name;

    @Override
    public void create(CacheConfiguration cacheConfiguration) throws CacheException
    {
        this.name = cacheConfiguration.getConfigurationId();

        this.cache = this.cacheManager.createNewCache(cacheConfiguration);
    }

    @Override
    public DocumentReference get(String key)
    {
        return cache.get(key);
    }

    @Override
    public void set(String key, DocumentReference value)
    {
        this.cache.set(key, value);
    }
}
