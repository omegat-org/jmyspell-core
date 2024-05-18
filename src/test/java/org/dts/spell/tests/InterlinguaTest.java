package org.dts.spell.tests;


import org.dts.spell.SpellChecker;
import org.dts.spell.dictionary.OpenOfficeSpellDictionary;
import org.dts.spell.dictionary.SpellDictionary;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;

public class InterlinguaTest extends TestBase {

    private static final String DICTIONARY_ZIP = "ia.zip";

    @Override
    protected String getDictionary() {
        return DICTIONARY_ZIP;
    }

    @Test
    public void testInterlinguaZipLoad() throws IOException {
        try (InputStream is = getClass().getResourceAsStream(DICTIONARY_ZIP)) {
            SpellDictionary dict = new OpenOfficeSpellDictionary(is, personalDictionary, false);
            SpellChecker checker = new SpellChecker(dict);
            checker.setCaseSensitive(false);
            String word = "Die";
            assertThat(checker.isCorrect(word)).isTrue();
        }
    }

    @Test
    public void testInterlinguaFileLoad() throws IOException {
        try (InputStream affFile = Files.newInputStream(tempDir.resolve("ia/index.aff"));
             InputStream dicFile = Files.newInputStream(tempDir.resolve("ia/index.dic"))) {
            SpellDictionary dict = new OpenOfficeSpellDictionary(affFile, dicFile, personalDictionary, false);
            SpellChecker checker = new SpellChecker(dict);
            checker.setCaseSensitive(false);
            String word = "Die";
            assertThat(checker.isCorrect(word)).isTrue();
        }
    }

    @Test
    public void testInterlinguaFileLoadBackground() throws IOException {
        try (InputStream affFile = Files.newInputStream(tempDir.resolve("ia/index.aff"));
             InputStream dicFile = Files.newInputStream(tempDir.resolve("ia/index.dic"))) {
            SpellDictionary dict = new OpenOfficeSpellDictionary(affFile, dicFile, personalDictionary, true);
            SpellChecker checker = new SpellChecker(dict);
            checker.setCaseSensitive(false);
            String word = "Die";
            assertThat(checker.isCorrect(word)).isTrue();

        }
    }
}
