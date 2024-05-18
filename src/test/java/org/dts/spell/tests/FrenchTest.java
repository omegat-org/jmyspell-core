package org.dts.spell.tests;


import org.dts.spell.SpellChecker;
import org.dts.spell.dictionary.OpenOfficeSpellDictionary;
import org.dts.spell.dictionary.SpellDictionary;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;

public class FrenchTest extends TestBase {

    private static final String DICTIONARY_ZIP = "fr.zip";

    @Override
    protected String getDictionary() {
        return DICTIONARY_ZIP;
    }

    @Test
    public void testFrenchSpell() throws IOException {
        try (InputStream affFile = Files.newInputStream(tempDir.resolve("fr/index.aff"));
             InputStream dicFile = Files.newInputStream(tempDir.resolve("fr/index.dic"))) {
            SpellDictionary dict = new OpenOfficeSpellDictionary(affFile, dicFile);
            SpellChecker checker = new SpellChecker(dict);
            checker.setCaseSensitive(false);
            String word = "Bonjour le monde!";
            assertThat(checker.isCorrect(word)).isTrue();
        }
    }

}
