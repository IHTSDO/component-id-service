package org.snomed.cis.security;

import jakarta.servlet.ReadListener;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class CachedBodyServletInputStreamTest {

    @Test
    void testRead_singleByte() throws IOException {
        byte[] data = "A".getBytes();
        CachedBodyServletInputStream stream = new CachedBodyServletInputStream(data);
        assertEquals('A', stream.read());
        assertEquals(-1, stream.read());
    }

    @Test
    void testIsFinished_shouldBeTrueAfterRead() throws IOException {
        byte[] data = "B".getBytes();
        CachedBodyServletInputStream stream = new CachedBodyServletInputStream(data);

        assertFalse(stream.isFinished());
        stream.read();
        assertTrue(stream.isFinished());
    }

    @Test
    void testIsReady_shouldAlwaysBeTrue() {
        CachedBodyServletInputStream stream = new CachedBodyServletInputStream("abc".getBytes());
        assertTrue(stream.isReady());
    }

    @Test
    void testSetReadListener_shouldThrowUnsupported() {
        CachedBodyServletInputStream stream = new CachedBodyServletInputStream("data".getBytes());

        ReadListener unsupportedListener = new ReadListener() {
            @Override
            public void onDataAvailable() {
                throw new UnsupportedOperationException("Async data reading is not supported.");
            }

            @Override
            public void onAllDataRead() {
                throw new UnsupportedOperationException("Async data reading is not supported.");
            }

            @Override
            public void onError(Throwable t) {
                throw new UnsupportedOperationException("Async data reading is not supported.");
            }
        };

        assertThrows(UnsupportedOperationException.class, () -> stream.setReadListener(unsupportedListener));
    }


    @Test
    void testRead_multipleBytes() throws IOException {
        byte[] data = "Test".getBytes();
        CachedBodyServletInputStream stream = new CachedBodyServletInputStream(data);

        assertEquals('T', stream.read());
        assertEquals('e', stream.read());
        assertEquals('s', stream.read());
        assertEquals('t', stream.read());
        assertEquals(-1, stream.read());
    }

    @Test
    void testRead_emptyInput_shouldReturnMinusOne() throws IOException {
        CachedBodyServletInputStream stream = new CachedBodyServletInputStream(new byte[0]);
        assertEquals(-1, stream.read());
        assertTrue(stream.isFinished());
    }
}
