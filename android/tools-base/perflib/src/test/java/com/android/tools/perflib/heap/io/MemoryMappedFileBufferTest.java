/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.tools.perflib.heap.io;

import com.android.annotations.NonNull;
import com.android.tools.perflib.heap.Snapshot;
import com.android.tools.perflib.captures.MemoryMappedFileBuffer;

import junit.framework.TestCase;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class MemoryMappedFileBufferTest extends TestCase {

    File file = new File(getClass().getResource("/dialer.android-hprof").getFile());

    public void testSimpleMapping() throws Exception {
        Snapshot snapshot = Snapshot.createSnapshot(new MemoryMappedFileBuffer(file));
        assertSnapshotCorrect(snapshot);
    }

    public void testMultiMapping() throws Exception {
        // Split the file into chunks of 4096 bytes each, leave 128 bytes for padding.
        MemoryMappedFileBuffer shardedBuffer = new MemoryMappedFileBuffer(file, 4096, 128);
        Snapshot snapshot = Snapshot.createSnapshot(shardedBuffer);
        assertSnapshotCorrect(snapshot);
    }

    public void testMultiMappingWrappedRead() throws Exception {
        // Leave just 8 bytes for padding to force wrapped reads.
        MemoryMappedFileBuffer shardedBuffer = new MemoryMappedFileBuffer(file, 9973, 8);
        Snapshot snapshot = Snapshot.createSnapshot(shardedBuffer);
        assertSnapshotCorrect(snapshot);
    }

    public void testMemoryMappingRemoval() throws Exception {
        File tmpFile = File.createTempFile("test_vm", ".tmp");
        System.err.println("vm temp file: " + tmpFile.getAbsolutePath());
        System.err.println("jvm " + System.getProperty("sun.arch.data.model"));

        long n = 500000000L;
        RandomAccessFile raf = new RandomAccessFile(tmpFile, "rw");
        try {
            raf.setLength(n);
            raf.write(1);
            raf.seek(n - 1);
            raf.write(2);
        }
        finally {
            raf.close();
        }

        MemoryMappedFileBuffer buffer = new MemoryMappedFileBuffer(tmpFile);
        assertEquals(1, buffer.readByte());
        buffer.setPosition(n - 1);
        assertEquals(2, buffer.readByte());

        // On Windows, tmpFile can't be deleted without unmapping it first.
        buffer.dispose();
        tmpFile.delete();

        File g = new File(tmpFile.getCanonicalPath());
        assertFalse(g.exists());
    }

    private static void assertSnapshotCorrect(@NonNull Snapshot snapshot) {
        assertEquals(11182, snapshot.getGCRoots().size());
        assertEquals(38, snapshot.getHeap(65).getClasses().size());
        assertEquals(1406, snapshot.getHeap(65).getInstances().size());
        assertEquals(3533, snapshot.getHeap(90).getClasses().size());
        assertEquals(38710, snapshot.getHeap(90).getInstances().size());
    }
}
