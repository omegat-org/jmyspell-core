package org.dts.spell.tests;


import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.dts.spell.SpellChecker;
import org.dts.spell.dictionary.OpenOfficeSpellDictionary;
import org.dts.spell.dictionary.SpellDictionary;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BasicTest {

  @Test
  public void testCheckOpenOfficeDictionary() throws IOException {
      InputStream affFile = BasicTest.class.getResourceAsStream("/en_US.aff");
      InputStream dicFile = BasicTest.class.getResourceAsStream("/en_US.dic");
      SpellDictionary dict = new OpenOfficeSpellDictionary(affFile, dicFile) ;
      SpellChecker checker = new SpellChecker(dict) ;
      checker.setCaseSensitive(false) ;

      String word = "Hello world!";
      assertThat(checker.isCorrect(word)).isTrue();
      List<String> suggestions = dict.getSuggestions(word) ;
      assertThat(suggestions.size()).isEqualTo(10);
      assertThat(suggestions).contains("Worldliness's", "Worldlinesses", "Worldliness", "Worldliest", "Afterworlds",
              "Afterworld's", "Afterworld", "Worldwide", "Dreamworlds");
  }
}
