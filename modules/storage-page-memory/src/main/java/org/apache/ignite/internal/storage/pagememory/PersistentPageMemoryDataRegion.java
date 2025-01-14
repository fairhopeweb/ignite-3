/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.internal.storage.pagememory;

import static org.apache.ignite.internal.util.Constants.GiB;
import static org.apache.ignite.internal.util.Constants.MiB;

import java.util.Arrays;
import org.apache.ignite.internal.pagememory.DataRegion;
import org.apache.ignite.internal.pagememory.configuration.schema.PersistentPageMemoryDataRegionConfiguration;
import org.apache.ignite.internal.pagememory.configuration.schema.PersistentPageMemoryDataRegionView;
import org.apache.ignite.internal.pagememory.io.PageIoRegistry;
import org.apache.ignite.internal.pagememory.persistence.PartitionMetaManager;
import org.apache.ignite.internal.pagememory.persistence.PersistentPageMemory;
import org.apache.ignite.internal.pagememory.persistence.checkpoint.CheckpointManager;
import org.apache.ignite.internal.pagememory.persistence.store.FilePageStoreManager;
import org.apache.ignite.internal.storage.StorageException;

/**
 * Implementation of {@link DataRegion} for persistent case.
 */
class PersistentPageMemoryDataRegion implements DataRegion<PersistentPageMemory> {
    private final PersistentPageMemoryDataRegionConfiguration cfg;

    private final PageIoRegistry ioRegistry;

    private final int pageSize;

    private final FilePageStoreManager filePageStoreManager;

    private final PartitionMetaManager partitionMetaManager;

    private final CheckpointManager checkpointManager;

    private volatile PersistentPageMemory pageMemory;

    /**
     * Constructor.
     *
     * @param cfg Data region configuration.
     * @param ioRegistry IO registry.
     * @param filePageStoreManager File page store manager.
     * @param partitionMetaManager Partition meta information manager.
     * @param checkpointManager Checkpoint manager.
     * @param pageSize Page size in bytes.
     */
    public PersistentPageMemoryDataRegion(
            PersistentPageMemoryDataRegionConfiguration cfg,
            PageIoRegistry ioRegistry,
            FilePageStoreManager filePageStoreManager,
            PartitionMetaManager partitionMetaManager,
            CheckpointManager checkpointManager,
            int pageSize
    ) {
        this.cfg = cfg;
        this.ioRegistry = ioRegistry;
        this.pageSize = pageSize;

        this.filePageStoreManager = filePageStoreManager;
        this.partitionMetaManager = partitionMetaManager;
        this.checkpointManager = checkpointManager;
    }

    /**
     * Starts a persistent data region.
     */
    public void start() {
        PersistentPageMemoryDataRegionView dataRegionConfigView = cfg.value();

        PersistentPageMemory pageMemoryImpl = new PersistentPageMemory(
                cfg,
                ioRegistry,
                calculateSegmentSizes(dataRegionConfigView, Runtime.getRuntime().availableProcessors()),
                calculateCheckpointBufferSize(dataRegionConfigView),
                filePageStoreManager,
                null,
                (pageMemory, fullPageId, buf) -> checkpointManager.writePageToDeltaFilePageStore(pageMemory, fullPageId, buf, true),
                checkpointManager.checkpointTimeoutLock(),
                pageSize
        );

        pageMemoryImpl.start();

        pageMemory = pageMemoryImpl;
    }

    /**
     * Stops a persistent data region.
     */
    public void stop() throws Exception {
        if (pageMemory != null) {
            pageMemory.stop(true);
        }
    }

    /** {@inheritDoc} */
    @Override
    public PersistentPageMemory pageMemory() {
        checkDataRegionStarted();

        return pageMemory;
    }

    /**
     * Returns file page store manager.
     */
    public FilePageStoreManager filePageStoreManager() {
        return filePageStoreManager;
    }

    /**
     * Returns partition meta information manager.
     */
    public PartitionMetaManager partitionMetaManager() {
        return partitionMetaManager;
    }

    /**
     * Returns checkpoint manager.
     */
    public CheckpointManager checkpointManager() {
        return checkpointManager;
    }

    /**
     * Calculates the size of segments in bytes.
     *
     * @param dataRegionConfigView Data region configuration.
     * @param concurrencyLevel Number of concurrent segments in Ignite internal page mapping tables, must be greater than 0.
     */
    // TODO: IGNITE-16350 Add more and more detailed description
    static long[] calculateSegmentSizes(PersistentPageMemoryDataRegionView dataRegionConfigView, int concurrencyLevel) {
        assert concurrencyLevel > 0 : concurrencyLevel;

        long maxSize = dataRegionConfigView.size();

        long fragmentSize = Math.max(maxSize / concurrencyLevel, MiB);

        long[] sizes = new long[concurrencyLevel];

        Arrays.fill(sizes, fragmentSize);

        return sizes;
    }

    /**
     * Calculates the size of the checkpoint buffer in bytes.
     *
     * @param dataRegionConfigView Data region configuration.
     */
    // TODO: IGNITE-16350 Add more and more detailed description
    static long calculateCheckpointBufferSize(PersistentPageMemoryDataRegionView dataRegionConfigView) {
        long maxSize = dataRegionConfigView.size();

        if (maxSize < GiB) {
            return Math.min(GiB / 4L, maxSize);
        }

        if (maxSize < 8L * GiB) {
            return maxSize / 4L;
        }

        return 2L * GiB;
    }

    /**
     * Checks that the data region has started.
     *
     * @throws StorageException If the data region did not start.
     */
    private void checkDataRegionStarted() {
        if (pageMemory == null) {
            throw new StorageException("Data region not started");
        }
    }
}
