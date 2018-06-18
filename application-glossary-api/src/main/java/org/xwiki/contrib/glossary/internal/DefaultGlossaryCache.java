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

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.cache.Cache;
import org.xwiki.cache.CacheException;
import org.xwiki.cache.CacheManager;
import org.xwiki.cache.config.CacheConfiguration;
import org.xwiki.cache.eviction.LRUEvictionConfiguration;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.contrib.glossary.EntryRetrieval;
import org.xwiki.contrib.glossary.GlossaryCache;
import org.xwiki.model.reference.DocumentReference;

/**
 * @version $Id$
 */
@Component
@Singleton
public class DefaultGlossaryCache implements GlossaryCache, Initializable
{
    /**
     * Identifier for the glossary cache.
     */
    private static final String NAME = "cache.glossaryCache";

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

    @Inject
    private EntryRetrieval entryRetrieval;

    @Override
    public void initialize() throws InitializationException
    {
        // Initialize the cache here.
        // How to initialize?:
        // See:
        // xwiki-platform-oldcore/src/main/java/com/xpn/xwiki/internal/cache/rendering/DefaultRenderingCache.java
        // initialise the cache with getGlossaryEntries by setting key-value pairs.

        CacheConfiguration cacheConfiguration = new CacheConfiguration();
        cacheConfiguration.setConfigurationId(NAME);
        LRUEvictionConfiguration lru = new LRUEvictionConfiguration();
        lru.setMaxEntries(1000);
        cacheConfiguration.put(LRUEvictionConfiguration.CONFIGURATIONID, lru);
        try {
            this.name = cacheConfiguration.getConfigurationId();
            this.cache = this.cacheManager.createNewCache(cacheConfiguration);

        } catch (CacheException e) {
            throw new InitializationException("Failed to initialize cache", e);
        }

        Map<String, DocumentReference> glossaryEntries;

        glossaryEntries = this.entryRetrieval.getGlossaryEntries();

        for (Map.Entry<String, DocumentReference> entry : glossaryEntries.entrySet()) {
            this.cache.set(entry.getKey(), entry.getValue());
        }

    }

    @Override
    public DocumentReference get(String key)
    {
        return this.cache.get(key);
    }

    @Override
    public void set(String key, DocumentReference value)
    {
        this.cache.set(key, value);
    }

}
