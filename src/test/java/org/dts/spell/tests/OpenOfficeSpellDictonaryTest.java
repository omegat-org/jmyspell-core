package org.dts.spell.tests;


import org.dts.spell.SpellChecker;
import org.dts.spell.dictionary.OpenOfficeSpellDictionary;
import org.dts.spell.dictionary.SpellDictionary;
import org.dts.spell.dictionary.SpellDictionaryException;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipFile;

import static org.assertj.core.api.Assertions.assertThat;

public class OpenOfficeSpellDictonaryTest extends TestBase {

    private static final String DICTIONARY_ZIP = "en_US.zip";

    @Override
    protected String getDictionary() {
        return DICTIONARY_ZIP;
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
    public void testOpenOfficeDictionaryZipDictionaryStream() throws IOException {
        try (InputStream is = getClass().getResourceAsStream(DICTIONARY_ZIP)) {
            SpellDictionary dict = new OpenOfficeSpellDictionary(is, personalDictionary, false);
            SpellChecker checker = new SpellChecker(dict);
            checker.setCaseSensitive(false);

            String word = "Hello world!";
            assertThat(checker.isCorrect(word)).isTrue();
        }
    }

    @Test
    public void testOpenOfficeDictionaryUnknownWord() throws IOException {
        try (InputStream is = getClass().getResourceAsStream(DICTIONARY_ZIP)) {
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
    public void testOpenOfficeDictionaryZipFile() throws IOException {
        URL zipURL = getClass().getResource(DICTIONARY_ZIP);
        ZipFile zipFile = new ZipFile(Objects.requireNonNull(zipURL).getFile());
        SpellDictionary dict = new OpenOfficeSpellDictionary(zipFile);
        SpellChecker checker = new SpellChecker(dict);
        checker.setCaseSensitive(false);
        String word = "Hello world!";
        assertThat(checker.isCorrect(word)).isTrue();
    }

    @Test
    public void testOpenOfficeDictionaryStreamBackground() throws IOException {
        try (InputStream is = getClass().getResourceAsStream(DICTIONARY_ZIP)) {
            SpellDictionary dict = new OpenOfficeSpellDictionary(is, personalDictionary, true);
            SpellChecker checker = new SpellChecker(dict);
            checker.setCaseSensitive(false);
            String word = "Hello world!";
            assertThat(checker.isCorrect(word)).isTrue();
        }
    }

    @Test
    public void testOpenOfficeDictionaryCharSequence() throws IOException {
        try (InputStream is = getClass().getResourceAsStream(DICTIONARY_ZIP)) {
            SpellDictionary dict = new OpenOfficeSpellDictionary(is, personalDictionary, true);
            SpellChecker checker = new SpellChecker(dict);
            checker.setCaseSensitive(false);
            String word = "Hello world!";
            assertThat(checker.checkSpell(word)).isNullOrEmpty();
        }
    }

    @Test
    public void testOpenOfficeDictionaryAddWord() throws IOException, SpellDictionaryException {
        try (InputStream is = getClass().getResourceAsStream(DICTIONARY_ZIP)) {
            SpellDictionary dict = new OpenOfficeSpellDictionary(is, personalDictionary, false);
            dict.addWord("Chao");
            SpellChecker checker = new SpellChecker(dict);
            checker.setCaseSensitive(false);

            String word = "Chao world!";
            assertThat(checker.isCorrect(word)).isTrue();
        }
    }

}
