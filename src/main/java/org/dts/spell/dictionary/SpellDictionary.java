/*
 * Created on 04/02/2005
 *
 */
package org.dts.spell.dictionary ;

import java.util.List ;

/**
 * An interface for all dictionary implementations. It defines the most basic
 * operations on a dictionary: adding words, checking if a word is correct, and
 * getting a list of suggestions for misspelled words.
 * 
 * @author DreamTangerine
 *  
 */
public interface SpellDictionary
{
  /**
   * Add a word permanently to the dictionary. In general, to the personal
   * dictionary associated with this main dictionary.
   * 
   * @param word
   *          a word to add.
   *  
   */
  public void addWord(String word) throws SpellDictionaryException ;

  /**
   * Returns true if the word is correctly spelled against the dictionary.
   * 
   * @param word
   *          a word to check.
   */
  public boolean isCorrect(String word) ;

  /**
   * Returns a list of String that are the suggestions to any word. If the word
   * is correctly spelled, then this method could return just that one word, or
   * it could still return a list of words with similar spellings.
   * 
   * @param word
   *          the word that we want to get a list of spelling suggestions for.
   * 
   * @return List a List of suggested words. It can't be null. An empty list must be return no suggestions.
   */
  public List<String> getSuggestions(String word) ;
  
  /**
   * Returns a list of String that are the suggestions to any word. If the word
   * is correctly spelled, then this method could return just that one word, or
   * it could still return a list of words with similar spellings.
   * 
   * @param word
   *          the word that we want to get a list of spelling suggestions for.
   *
   * @param nMax
   *          the maximun number of word to return
   * 
   * @return List a List of suggested words. It can't be null. An empty list must be return no suggestions.
   */
  public List<String> getSuggestions(String word, int nMax) ;  
}
