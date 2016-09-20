/*
 * IndexedFileWordMap.java
 *
 * Created on 19 de marzo de 2007, 06:13 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.dts.spell.dictionary.myspell.wordmaps;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.Collator;
import java.util.Iterator;
import java.util.Locale;
import org.dts.spell.dictionary.myspell.HEntry;
import org.dts.spell.dictionary.myspell.Utils;
import org.dts.spell.dictionary.myspell.Utils.IndexComparator;
import org.dts.spell.dictionary.myspell.Utils.IndexSearcher;

/**
 *
 * @author DreamTangerine
 */
public class IndexedFileWordMap extends WordMap
{
  
  /** Creates a new instance of IndexedFileWordMap */
  public IndexedFileWordMap()
  {
  }

  public void init(int nEntries) throws IOException
  {
    wordFile = File.createTempFile("words","dic") ;
    indexes = new long[nEntries] ;
    nIndex = 0 ;
    
    words = new RandomAccessFile(wordFile, "rw") ;
    wordFile.deleteOnExit() ;
  }
  
  public void finish(int nEntries) throws IOException 
  {
    // Time to sort
    Utils.heapSort(indexes, new IndexComparator()
    {
      public boolean isLess(long index1, long index2)
      {
        return getEntry(index1).compareTo(getEntry(index2)) < 0 ;
      }

      public boolean isGreater(long index1, long index2)
      {
        return getEntry(index1).compareTo(getEntry(index2)) > 0 ;
      }
    }) ;
  }
  
  public HEntry get(String word)
  {
    try
    {
      int index = Utils.binarySearch(word, indexes.length, new IndexSearcher<String>()
      {
        public int compare(int index, String obj)
        {
          return getWord(index).compareTo(obj) ;
        }
      }) ;
      
      if (index >= 0)
        return getEntry(index) ;
      else
        return null ;
    }
    catch(Exception ex)
    {
      return null ;
    }
  }
  
  public void add(HEntry entry) throws IOException
  {
    indexes[nIndex] = words.length() ;
    ++nIndex ;
    
    words.writeUTF(entry.word) ;
    words.writeUTF(entry.astr) ;
  }
  
  protected String getWord(int index)
  {
    try
    {
      words.seek(indexes[index]) ;
      
      return words.readUTF() ;
    }
    catch (IOException ex)
    {
      return null ;
    }
  }
  
  protected HEntry getEntry(long position)
  {
    try
    {
      words.seek(position) ;
    
      return new HEntry(words.readUTF(), words.readUTF()) ;
    }
    catch (IOException ex)
    {
      return null ;
    }
  }

  protected HEntry getEntry(int index)
  {
    return getEntry(indexes[index]) ;
  }
  
  public void addCustomWord(String word) throws IOException
  {
    //add(new HEntry(word)) ;
  }
  
  public Iterator<HEntry> iterator()
  {
    return new Iterator<HEntry>()
    {
      public void remove()
      {
        throw new UnsupportedOperationException() ;
      }
      
      public HEntry next()
      {
        ++next ;
        
        return nextEntry ;
      }
      
      public boolean hasNext()
      {
        if (next < indexes.length)
            nextEntry = getEntry(next) ;
        else
          nextEntry = null ;
        
        return null != nextEntry ;
      }
      
      private HEntry nextEntry ;
      private int next = 0 ;
    } ;
  }
  
  private RandomAccessFile words ;
  private File wordFile ;
  private long[] indexes ;
  private int nIndex ;
}
