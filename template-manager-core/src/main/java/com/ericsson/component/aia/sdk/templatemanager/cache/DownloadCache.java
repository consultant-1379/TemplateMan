/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.sdk.templatemanager.cache;

import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.sdk.templatemanager.TemplateManager;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * This is a cache which is used when templates are downloaded. It is implemented using the Google Guava Cache libraries.
 */
public class DownloadCache {

    private static final Logger Log = LoggerFactory.getLogger(DownloadCache.class);

    private final LoadingCache<String, Path> cache;

    /**
     * An instance of the TemplateManager needs to be passed into the the DownloadCache class. When an item is not found in the cache, then it is
     * downloaded afresh using the TemplateManager.
     *
     * @param templateManager
     *            to download templates which are not found in the cache.
     * @param expiryTime
     *            is the number of minutes before an item in the download cache will expire.
     */
    public DownloadCache(final TemplateManager templateManager, final int expiryTime) {

        CacheLoader<String, Path> loader;

        loader = new CacheLoader<String, Path>() {

            @Override
            public Path load(final String pbaId) {
                Log.info("Template not found in the download cache for pbaId:{} Getting it from TemplateManager.", pbaId);
                return templateManager.downloadTemplate(pbaId);
            }
        };

        Log.info("Configuring the template download cache to use an expiry time of {} minutes.", expiryTime);

        cache = CacheBuilder.newBuilder().maximumSize(100).expireAfterWrite(expiryTime, TimeUnit.MINUTES).build(loader);
    }

    /**
     * Gets the downloaded template file. The cache is checked firstly to see if it is available there. If not then the load() function above gets
     * invoked to download the template file.
     *
     * @param pbaId
     *            the unique id.
     * @return the path to the archive stored in the cache.
     */
    public Path get(final String pbaId) {
        try {
            return cache.get(pbaId);

        } catch (final ExecutionException ex) {
            Log.error("Unable to download a template.", ex);
        }
        return null;
    }

    /**
     * Clears the template download cache. There are no items in the cache after this call.
     */
    public void clearCache() {
        Log.info("Clearing the contents of the cache which stores downloaded templates.");
        cache.invalidateAll();
    }
}
