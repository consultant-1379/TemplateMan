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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.nio.file.Path;

import org.junit.Test;

import com.ericsson.component.aia.sdk.templatemanager.cache.ArchivePathCache;

/**
 * The Class SimpleCacheTest.
 */
public class ArchivePathCacheTest {

    private static final String TEST_TEMPLATE_PBA_JSON = "src/test/resources/pba-good.json";
    private ArchivePathCache simpleCache = new ArchivePathCache();
    private Path path = new File(TEST_TEMPLATE_PBA_JSON).toPath();

    /**
     * Should be able to retrieve data added into the cache.
     */
    @Test
    public void shouldBeAbleToRetrieveDataAddedIntoTheCache() {
        final String uuid = simpleCache.add(path);
        assertNotNull(uuid);
        assertEquals(path, simpleCache.get(uuid));
    }

    /**
     * Should remove old entries from cache when max entries are reached.
     */
    @Test
    public void shouldRemoveOldEntriesFromCacheWhenMaxEntriesAreReached() {
        final String uuid = simpleCache.add(path);

        for (int index = 0; index < 1024; index++) {
            simpleCache.add(path);
        }

        assertNull(simpleCache.get(uuid));
    }
}
