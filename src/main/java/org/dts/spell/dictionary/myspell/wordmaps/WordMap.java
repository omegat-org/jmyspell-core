/*
 * WordMap.java
 *
 * Created on 16 de marzo de 2007, 08:39 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.dts.spell.dictionary.myspell.wordmaps;

import java.io.IOException;
import java.util.Iterator;
import org.dts.spell.dictionary.myspell.HEntry;

/**
 *
 * @author DreamTangerine
 */
public abstract class WordMap
{
  public static WordMap create() throws IOException
  {
    // TODO : Time to change implementation.
    WordMap result = 
                //new IndexedFileWordMap() ;
                //new AllLinealInMemoryWordMap() ;
                new AllInMemeoryWordMap() ;
    
    return result ;
  }
  
  /**
   * Called to notice the begin of a add series of calls
   */
  public abstract void init(int nEntries) throws IOException ;
  
  /**
   * Called when all no custom word wre added.
   */
  public abstract void finish(int nEntries) throws IOException ;  
  
  public abstract HEntry get(String word) ;
  
  /**
   * Add a new entry to WordMap. <code>init</code> must be called first
   */
  public abstract void add(HEntry entry) throws IOException ;
  public abstract void addCustomWord(String word) throws IOException ;
  
  public abstract Iterator<HEntry> iterator() ;
}
