package org.dts.spell.tests;


import org.dts.spell.SpellChecker;
import org.dts.spell.dictionary.OpenOfficeSpellDictionary;
import org.dts.spell.dictionary.SpellDictionary;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SpanishTest extends TestBase {

    private static final String DICTIONARY_ZIP = "es.zip";

    @Override
    protected String getDictionary() {
        return DICTIONARY_ZIP;
    }

    @Test
    public void testSpell() throws IOException {
        try (InputStream affFile = Files.newInputStream(tempDir.resolve("es/index.aff"));
             InputStream dicFile = Files.newInputStream(tempDir.resolve("es/index.dic"))) {
            SpellDictionary dict = new OpenOfficeSpellDictionary(affFile, dicFile);
            SpellChecker checker = new SpellChecker(dict);
            checker.setCaseSensitive(false);

            String word = "Hola mundo!";
            assertThat(checker.isCorrect(word)).isTrue();
            List<String> suggestions = dict.getSuggestions(word);
            assertThat(suggestions.size()).isEqualTo(10);
            assertThat(suggestions).contains("Inframundo", "Juzgamundos", "Inframundos", "Vagamundos",
                    "Vagamundo", "Trotamundos", "Trasmundos", "Trasmundo", "Inmundos", "Submundos");
        }
    }

    @Test
    public void testOpenOfficeDictionaryZipDictionary() throws IOException {
        try (InputStream is = getClass().getResourceAsStream(DICTIONARY_ZIP)) {
            SpellDictionary dict = new OpenOfficeSpellDictionary(is, personalDictionary, false);
            SpellChecker checker = new SpellChecker(dict);
            checker.setCaseSensitive(false);

            String word = "Hola mundo!";
            assertThat(checker.isCorrect(word)).isTrue();
        }
    }

}
