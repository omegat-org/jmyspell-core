/*
 * Created on 27/12/2004
 *
 */
package org.dts.spell.dictionary.myspell;

/**
 * @author DreamTangerine
 *
 */
public class HEntry implements Comparable<String>
{
  public HEntry(String word)
  {
    this(word, "") ;
  }
  
  public HEntry(String word, String astr)
  {
    this.word = word ;
    this.astr = astr ;
  }

  public int compareTo(String other)
  {
    return word.compareTo(other) ;
  }

  public int compareTo(HEntry other)
  {
    return compareTo(other.word) ;
  }
  
  public String word ;
  public String astr ;
}
