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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class ArchivePathCache is used to cache the the path of created project based on a UUID. The cache will maintain a fixed size and delete the
 * oldest entry if more entries are added than allowed.
 */
public class ArchivePathCache {

    /** Logger for ApplicationManagerImpl. */
    private static final Logger Log = LoggerFactory.getLogger(ArchivePathCache.class);

    private static final int MAX_NUMBER_OF_ZIPS_IN_LOCAL_STORAGE = 1024;

    private static final Map<String, Path> cache = Collections
            .synchronizedMap(new LinkedHashMap<String, Path>(MAX_NUMBER_OF_ZIPS_IN_LOCAL_STORAGE, 0.7f, true) {
                private static final long serialVersionUID = -6233551167051192931L;

                @Override
                protected boolean removeEldestEntry(final Map.Entry<String, Path> eldest) {
                    final boolean shouldRemove = size() > MAX_NUMBER_OF_ZIPS_IN_LOCAL_STORAGE;

                    if (shouldRemove) {
                        eldest.getValue().toFile().delete();
                    }

                    Log.trace("Should remove old cache entry? {} oldest entry has id::{}, path:: {}", shouldRemove, eldest.getKey(),
                            eldest.getValue());

                    return shouldRemove;
                }

            });

    /**
     * Adds the path of a created zip file to the cache.
     *
     * @param path
     *            the path
     * @return the uniqueId of the inserted path.
     */
    public String add(final Path path) {
        final String uniqueId = UUID.randomUUID().toString();
        cache.put(uniqueId, path);
        Log.trace("Adding new cache entry with id::" + uniqueId);
        return uniqueId;
    }

    /**
     * Put info into the cache.
     *
     * @param uniqueId
     *            - the ID to be used as key
     * @param path
     *            - the path to be stored
     * @return the id used for storing it
     */
    public String put(final String uniqueId, final Path path) {
        cache.put(uniqueId, path);
        Log.trace("Adding new cache entry with id::" + uniqueId);
        return uniqueId;
    }

    /**
     * @param uniqueId
     *            - unique id
     * @return - old path
     */
    public Path remove(final String uniqueId) {
        Log.trace("Removing cache entry with id::" + uniqueId);
        return cache.remove(uniqueId);
    }

    /**
     * Gets the path of a local zip file from the cache.
     *
     * @param uniqueId
     *            the unique id.
     * @return the path to the archive stored in the cache.
     */
    public Path get(final String uniqueId) {
        return cache.get(uniqueId);
    }
}
