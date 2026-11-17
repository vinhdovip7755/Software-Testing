package com.bookstore.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ExcelUtilTest {
    @Test
    void identifiesWindowsFileLockErrors() {
        IOException lockedFile = new IOException(
                "The process cannot access the file because it is being used by another process");
        IOException unrelatedError = new IOException("Disk is full");

        assertTrue(ExcelUtil.isFileInUse(lockedFile));
        assertFalse(ExcelUtil.isFileInUse(unrelatedError));
        assertTrue(ExcelUtil.isFileInUse(new IOException("Export failed", lockedFile)));
    }
}
