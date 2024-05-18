package org.dts.spell.tests;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class TestBase {
    protected Path tempDir;
    protected File personalDictionary;

    @Before
    public void setUp() throws Exception {
        tempDir = Files.createTempDirectory("jmyspell");
        extractZip(getClass().getResourceAsStream(getDictionary()), tempDir);
        personalDictionary = Files.createTempFile(tempDir, "personal", ".dic").toFile();
    }

    protected abstract String getDictionary();

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(tempDir.toFile());
    }


    protected void extractZip(InputStream inputStream, Path extractDirectory) throws IOException, ArchiveException {
        ArchiveStreamFactory archiveStreamFactory = new ArchiveStreamFactory();
        ArchiveInputStream archiveInputStream = archiveStreamFactory
                .createArchiveInputStream(ArchiveStreamFactory.ZIP, inputStream);
        ArchiveEntry archiveEntry;
        while ((archiveEntry = archiveInputStream.getNextEntry()) != null) {
            Path path = extractDirectory.resolve(archiveEntry.getName());
            File file = path.toFile();
            if (archiveEntry.isDirectory()) {
                if (!file.isDirectory()) {
                    var ignore = file.mkdirs();
                }
            } else {
                File parent = file.getParentFile();
                if (!parent.isDirectory()) {
                    var ignore = parent.mkdirs();
                }
                try (OutputStream outputStream = Files.newOutputStream(path)) {
                    IOUtils.copy(archiveInputStream, outputStream);
                }
            }
        }
    }
}
