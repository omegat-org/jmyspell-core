/*
 * ErrorInfo.java
 *
 * Created on 19 de diciembre de 2006, 07:05 PM
 *
 */

package org.dts.spell;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.dts.spell.dictionary.SpellDictionary;
import org.dts.spell.event.SpellCheckEvent;
import org.dts.spell.finder.Word;

/**
 *
 * @author DreamTangerine
 */
public class ErrorInfo
{
  public static final int MAX_SUGGESSTIONS = 4 ;

  private class SuggestionsLoader
  {
    public SuggestionsLoader(SpellCheckEvent event)
    {
      dictionary = event.getDictionary() ;
    }
    
    /**
     * Load the suggestions and return the description.
     */
    public String load()
    {
      List<String> suggentionList = dictionary.getSuggestions(badWord.getText(), MAX_SUGGESSTIONS) ;
      String result ;
      
      if (!suggentionList.isEmpty())
        result = String.format(getString("SPELL_ERROR_INFO"), badWord, suggentionList.get(0)) ;
      else
        result = String.format(getString("SPELL_NO_SUGGESTIONS_ERROR_INFO"), badWord) ;
      
      suggestions = new String[suggentionList.size()] ;
      suggentionList.subList(0, suggestions.length).toArray(suggestions) ;
      
      return result ;
    }
    
    /**
     * Load only description, the first suggestion
     */
    public String loadDescription()
    {
      List<String> suggentionList = dictionary.getSuggestions(badWord.getText(), 1) ;
      String result ;
      
      if (!suggentionList.isEmpty())
        result = String.format(getString("SPELL_ERROR_INFO"), badWord, suggentionList.get(0)) ;
      else
        result = String.format(getString("SPELL_NO_SUGGESTIONS_ERROR_INFO"), badWord) ;
      
      return result ;
    }
    
    private final SpellDictionary dictionary ;
  }
  
  // Lazy load of suggestions
  private SuggestionsLoader loader ;

  /** Creates a new instance of ErrorInfo */
  private ErrorInfo(SpellCheckEvent event)
  {
    this(event, null) ;
    
    // Lazy calculous of suggestions.
    loader = new SuggestionsLoader(event) ;
  }
  
  private ErrorInfo(SpellCheckEvent event, String description)
  {
    this.badWord = event.getCurrentWord() ;
    this.description = description ;
    this.suggestions = null ;
  }
  
  private ErrorInfo(SpellCheckEvent event, String description, String suggestion)
  {
    this.badWord = event.getCurrentWord() ;
    this.description = description ;
    this.suggestions = new String[] { suggestion } ;
  }
  
  private Word badWord ;
  private String description ;
  private String[] suggestions ;
  
  public Word getBadWord()
  {
    return badWord ;
  }
  
  public String getDescription()
  {
    if (null == description)
      description = loader.loadDescription() ;
    
    return description ;
  }
  
  public String[] getSuggestions()
  {
    if (null == suggestions)
    {
      // If loader is null that means we have to delete the word.
      if (null != loader)
      {
        description = loader.load() ;
        loader = null ;
      }
    }
    
    return suggestions ;
  }
  
  static public ErrorInfo getBadCaseErrorInfo(SpellCheckEvent event)
  {
    String word = event.getCurrentWord().getText() ;
    
    return new ErrorInfo(event, String.format(getString("BAD_CASE_ERROR_INFO"), word), Word.getStartSentenceWordCase(word)) ;
  }
  
  static public ErrorInfo getRepeatWordErrorInfo(SpellCheckEvent event)
  {
    return new ErrorInfo(event, String.format(getString("REPEAT_WORD_ERROR_INFO"), event.getCurrentWord())) ;
  }
  
  static public ErrorInfo getSpellingErrorInfo(SpellCheckEvent event)
  {
    return new ErrorInfo(event) ;
  }
  
  private static String getString(String key)
  {
    return ResourceBundle.getBundle("org/dts/spell/messages").getString(key) ;
  }
}
