package org.dts.spell.tests;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.dts.spell.SpellChecker;
import org.dts.spell.dictionary.OpenOfficeSpellDictionary;
import org.dts.spell.dictionary.SpellDictionary;
import org.dts.spell.dictionary.SpellDictionaryException;
import org.dts.spell.filter.DumpFilter;
import org.dts.spell.tokenizer.FilteredTokenizer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JMySpellTest {

    private static final String DICTIONARY_ZIP =  "en_US.zip";
    private Path tempDir;
    private File personalDictionary;

    @Before
    public void setUp() throws Exception {
        tempDir = Files.createTempDirectory("jmyspell");
        extractZip(JMySpellTest.class.getResourceAsStream(DICTIONARY_ZIP), tempDir);
        personalDictionary = Files.createTempFile(tempDir, "personal", ".dic").toFile();
    }

  @Test
  public void testCheckOpenOfficeDictionary() throws IOException {
      try (InputStream affFile = Files.newInputStream(tempDir.resolve("en_US.aff"));
        InputStream dicFile = Files.newInputStream(tempDir.resolve("en_US.dic"))) {
          SpellDictionary dict = new OpenOfficeSpellDictionary(affFile, dicFile);
          SpellChecker checker = new SpellChecker(dict);
          checker.setCaseSensitive(false);

          String word = "Hello world!";
          assertThat(checker.isCorrect(word)).isTrue();
          List<String> suggestions = dict.getSuggestions(word);
          assertThat(suggestions.size()).isEqualTo(10);
          assertThat(suggestions).contains("Worldliness's", "Worldlinesses", "Worldliness", "Worldliest", "Afterworlds",
                  "Afterworld's", "Afterworld", "Worldwide", "Dreamworlds");
      }
  }

  @Test
    public void testOpenOfficeDictionaryZipDictionary() throws IOException, SpellDictionaryException {
      try (InputStream is = JMySpellTest.class.getResourceAsStream(DICTIONARY_ZIP)) {
        SpellDictionary dict = new OpenOfficeSpellDictionary(is, personalDictionary, false);
          SpellChecker checker = new SpellChecker(dict);
          checker.setCaseSensitive(false);

          String word = "Hello world!";
          assertThat(checker.isCorrect(word)).isTrue();
      }
  }

    @Test
    public void testOpenOfficeDictionaryUnknownWord() throws IOException, SpellDictionaryException {
        try (InputStream is = JMySpellTest.class.getResourceAsStream("en_US.zip")) {
            SpellDictionary dict = new OpenOfficeSpellDictionary(is, personalDictionary, false);
            SpellChecker checker = new SpellChecker(dict);
            checker.setCaseSensitive(false);
            String word = "Cha0 world!";
            assertThat(checker.isCorrect(word)).isFalse();
            List<String> suggestions = dict.getSuggestions(word);
            assertThat(suggestions.size()).isEqualTo(9);
        }
    }

    @Test
    public void testOpenOfficeDictionaryAddWord() throws IOException, SpellDictionaryException {
        try (InputStream is = JMySpellTest.class.getResourceAsStream("en_US.zip")) {
            File tempDir = FileUtils.getTempDirectory();
            File personalDictionary = new File(tempDir, "personal.dic");
            SpellDictionary dict = new OpenOfficeSpellDictionary(is, personalDictionary, false);
            dict.addWord("Chao");
            SpellChecker checker = new SpellChecker(dict);
            checker.setCaseSensitive(false);

            String word = "Chao world!";
            assertThat(checker.isCorrect(word)).isTrue();
        }
    }


    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(tempDir.toFile());
    }

    private static void extractZip(InputStream inputStream, Path extractDirectory) throws IOException, ArchiveException {
        ArchiveStreamFactory archiveStreamFactory = new ArchiveStreamFactory();
        ArchiveInputStream archiveInputStream = archiveStreamFactory
                .createArchiveInputStream(ArchiveStreamFactory.ZIP, inputStream);
        ArchiveEntry archiveEntry;
        while ((archiveEntry = archiveInputStream.getNextEntry()) != null) {
            Path path = extractDirectory.resolve(archiveEntry.getName());
            File file = path.toFile();
            if (archiveEntry.isDirectory()) {
                if (!file.isDirectory()) {
                    file.mkdirs();
                }
            } else {
                File parent = file.getParentFile();
                if (!parent.isDirectory()) {
                    parent.mkdirs();
                }
                try (OutputStream outputStream = Files.newOutputStream(path)) {
                    IOUtils.copy(archiveInputStream, outputStream);
                }
            }
        }
    }
}
