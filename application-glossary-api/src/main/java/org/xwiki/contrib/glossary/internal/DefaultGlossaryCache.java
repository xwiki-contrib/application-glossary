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
import org.xwiki.component.phase.Disposable;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.contrib.glossary.GlossaryCache;
import org.xwiki.contrib.glossary.GlossaryException;
import org.xwiki.contrib.glossary.GlossaryModel;
import org.xwiki.model.reference.DocumentReference;

/**
 * Cache of Glossary data to speed up the transformation.
 *
 * @version $Id$
 */
@Component
@Singleton
public class DefaultGlossaryCache implements GlossaryCache, Initializable, Disposable
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

    @Inject
    private GlossaryModel glossaryModel;

    @Override
    public void initialize() throws InitializationException
    {
        try {
            CacheConfiguration cacheConfiguration = new CacheConfiguration();
            cacheConfiguration.setConfigurationId(NAME);
            LRUEvictionConfiguration lru = new LRUEvictionConfiguration();
            lru.setMaxEntries(1000);
            cacheConfiguration.put(LRUEvictionConfiguration.CONFIGURATIONID, lru);
            this.cache = this.cacheManager.createNewCache(cacheConfiguration);

            Map<String, DocumentReference> glossaryEntries;

            // Load existing Glossary entries in the current wiki and save them in the cache
            glossaryEntries = this.glossaryModel.getGlossaryEntries();
            for (Map.Entry<String, DocumentReference> entry : glossaryEntries.entrySet()) {
                this.cache.set(entry.getKey(), entry.getValue());
            }
        } catch (CacheException | GlossaryException e) {
            throw new InitializationException("Failed to initialize Glossary cache", e);
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

    @Override
    public void remove(String key)
    {
        this.cache.remove(key);
    }

    @Override
    public void dispose()
    {
        if (this.cache != null) {
            this.cache.dispose();
        }
    }
}
