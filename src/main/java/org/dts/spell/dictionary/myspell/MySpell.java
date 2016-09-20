/*
 * Created on 31/12/2004
 *
 */
package org.dts.spell.dictionary.myspell ;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream ;
import java.io.IOException ;
import java.io.BufferedReader ;
import java.io.InputStream;
import java.io.InputStreamReader ;
import java.util.Enumeration;
import java.util.HashMap ;
import java.util.List ;
import java.util.Collections ;
import java.util.LinkedList ;
import java.util.ListIterator ;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import org.dts.spell.dictionary.myspell.wordmaps.WordMap;

/**
 * @author DreamTangerine
 *  
 */
public class MySpell
{
  public static final int NOCAP = 0 ;

  public static final int INITCAP = 1 ;

  public static final int ALLCAP = 2 ;

  public static final int HUHCAP = 3 ;

  private AffixMgr pAMgr ;

  private WordMap pHMgr ;

  private SuggestMgr pSMgr ;

  private String encoding ;

  private int maxSug ;

  public MySpell(String zipFile) throws IOException
  {
   this(new ZipFile(zipFile)) ; 
  }
  
  public MySpell(ZipFile zipFile) throws IOException
  {
    Enumeration<? extends ZipEntry> entries = zipFile.entries() ;
    InputStream affStream = null ;
    InputStream dStream = null ;    
    
    while (entries.hasMoreElements() && (null == affStream || null == dStream))
    {
      ZipEntry entry = entries.nextElement() ;
      
      if (entry.getName().endsWith(".aff"))
        affStream = zipFile.getInputStream(entry) ;
      else if (entry.getName().endsWith(".dic"))
        dStream = zipFile.getInputStream(entry) ;
    }
      
    initFromStreams(affStream, dStream) ;    
    
    affStream.close() ;
    dStream.close() ;
  }

  public MySpell(InputStream zipStream) throws IOException
  {
    this(new ZipInputStream(zipStream)) ;
  }

  /**
   * Read the dictionary from a ZipInputStream.
   * Use with care because the format and dependencies make a memory Stream.  
   * @param zipStream
   * @throws IOException
   */
  public MySpell(ZipInputStream zipStream) throws IOException
  {
    ZipEntry entry = zipStream.getNextEntry() ;

    InputStream affStream = null ;
    InputStream dStream = null ;
    
    // Optimize and only one Stream in memory.
    // In fact, It is better that the first Entry was Affix, because normally is
    // smaller than dictionary.
    while (null != entry)
    {
      if (entry.getName().endsWith(".aff"))
      {
        if (null == dStream)
          affStream = createFromZipEntry(zipStream, entry) ;
        else
        {
          affStream = zipStream ;
          break ;
        }
      }
      else if (entry.getName().endsWith(".dic"))
      {
        if (null == affStream)
          dStream = createFromZipEntry(zipStream, entry) ;
        else
        {
          dStream = zipStream ;
          break ;
        }
      }
      
      entry = zipStream.getNextEntry() ;      
    }
      
    initFromStreams(affStream, dStream) ;
    
    // No need close.
  }
  
  
  public MySpell(InputStream affStream, InputStream dStream) throws IOException
  {
    initFromStreams(affStream, dStream) ;
  }

  public MySpell(String affpath, String dpath) throws IOException
  {
    InputStream affStream = null ;
    InputStream dStream = null ;    
    
    try
    {
      affStream = new FileInputStream(affpath) ; 
      dStream = new FileInputStream(dpath) ;      
      
      initFromStreams(affStream, dStream) ;
    }
    finally
    {
      Utils.close(affStream) ;
      Utils.close(dStream) ;      
    }
  }

  
  private InputStream createFromZipEntry(
      ZipInputStream zipStream,
      ZipEntry entry) throws IOException
  {
    int size = (int) entry.getSize() ; // we expect no file longer than int
    byte[] data = new byte[size] ; 
    int cReaded = zipStream.read(data) ;
    int current = cReaded ;
    
    while (cReaded > 0)
    {
      cReaded = zipStream.read(data, current, size - current) ;
      current += cReaded ; 
    }
    
    return new ByteArrayInputStream(data) ;
  }
  
  
  private void initFromStreams(InputStream affStream, InputStream dStream)  throws IOException
  {
    encoding = AffixMgr.readEncoding(affStream) ;

    /* first set up the hash manager */
    pHMgr = load_tables(dStream) ;

    /* next set up the affix manager */
    /* it needs access to the hash manager lookup methods */
    pAMgr = new AffixMgr(affStream, encoding, pHMgr) ;

    /* get the preferred try string and the dictionary */
    /* encoding from the Affix Manager for that dictionary */
    String try_string = pAMgr.get_try_string() ;
    //encoding = pAMgr.get_encoding();

    /* and finally set up the suggestion manager */
    maxSug = 15 ;
    pSMgr = new SuggestMgr(try_string, maxSug, pAMgr) ;
  }

  public List<String> suggest(String word)
  {
    return suggest(word, maxSug) ;
  }
    
  public List<String> suggest(String word, int nMax)
  {
    String wspace ;

    if (pSMgr == null)
      return Collections.emptyList() ;

    int[] captype = new int[1] ;
    boolean[] abbv = new boolean[1] ;
    String cw = cleanword(word, captype, abbv) ;
    int wl = cw.length() ;

    if (wl == 0)
      return Collections.emptyList() ;

    //int ns = 0 ;
    List<String> wlst = new LinkedList<String>() ;

    switch (captype[0])
    {
      case NOCAP:
      {
        wlst = pSMgr.suggest(wlst, cw, nMax) ;
        break ;
      }

      case INITCAP:
      {
        wspace = cw.toLowerCase() ;

        pSMgr.suggest(wlst, wspace, nMax) ;

        ListIterator<String> it = wlst.listIterator() ;

        while (it.hasNext())
          it.set(Utils.mkInitCap(it.next())) ;

        pSMgr.suggest(wlst, cw, nMax) ;
        break ;
      }

      case HUHCAP:
      {
        pSMgr.suggest(wlst, cw, nMax) ;
        wspace = cw.toLowerCase() ;
        pSMgr.suggest(wlst, wspace, nMax) ;
        break ;
      }

      case ALLCAP:
      {
        wspace = cw.toLowerCase() ;
        pSMgr.suggest(wlst, wspace, nMax) ;

        ListIterator<String> it = wlst.listIterator() ;

        while (it.hasNext())
          it.set((it.next()).toUpperCase()) ;

        pSMgr.suggest(wlst, cw, nMax) ;
        break ;
      }
    }

    if (!wlst.isEmpty())
      return wlst ;

    // try ngram approach since found nothing
    pSMgr.ngsuggest(wlst, cw, pHMgr, nMax) ;

    if (!wlst.isEmpty())
    {
      switch (captype[0])
      {
        case NOCAP:
          break ;

        case HUHCAP:
          break ;

        case INITCAP:
        {
          ListIterator<String> it = wlst.listIterator() ;

          while (it.hasNext())
            it.set(Utils.mkInitCap(it.next())) ;
        }
          break ;

        case ALLCAP:
        {
          ListIterator<String> it = wlst.listIterator() ;

          while (it.hasNext())
            it.set((it.next()).toUpperCase()) ;
        }
          break ;
      }
    }

    return wlst ;
  }

  public boolean spell(String word)
  {
    String rv = null ;

    String cw ;
    String wspace ;

    //int wl = word.length();
    //if (wl > (MAXWORDLEN - 1)) return 0;
    int[] captype = new int[1] ;
    boolean[] abbv = new boolean[1] ;

    cw = cleanword(word, captype, abbv) ;
    int wl = cw.length() ;

    if (wl == 0)
      return true ;

    switch (captype[0])
    {
      case HUHCAP:
      case NOCAP:
      {
        rv = check(cw) ;
        if ((abbv[0]) && (rv == null))
        {
          cw += '.' ;
          rv = check(cw) ;
        }
        break ;
      }

      case ALLCAP:
      {
        wspace = cw.toLowerCase() ;
        rv = check(wspace) ;

        if (rv == null)
        {
          rv = check(Utils.mkInitCap(wspace)) ;
        }
        if (rv == null)
          rv = check(cw) ;

        if (abbv[0] && (rv == null))
        {
          wspace = cw ;
          wspace += '.' ;
          rv = check(wspace) ;
        }
        break ;
      }
      case INITCAP:
      {
        wspace = cw.toLowerCase() ;
        rv = check(wspace) ;
        if (rv == null)
          rv = check(cw) ;

        if (abbv[0] && (rv == null))
        {
          wspace = cw ;
          wspace += '.' ;
          rv = check(wspace) ;
        }
        break ;
      }
    }

    return rv != null ;
  }

  public String get_dic_encoding()
  {
    return encoding ;
  }

  private static int[] index ;
  
  private WordMap load_tables(InputStream tStream) throws IOException
  {
    WordMap result = null ;
    int tablesize ;

    // raw dictionary - munched file
    BufferedReader rawdict = null ;

    try
    {
      rawdict = new BufferedReader(new InputStreamReader(tStream, encoding)) ;

      // first read the first line of file to get hash table size
      String ts = rawdict.readLine() ;

      if (ts == null)
        throw new IOException(Utils.getString("ERROR_HASH_MANAGER_2")) ;

      tablesize = Integer.parseInt(ts) ;

      if (tablesize == 0)
        throw new IOException(Utils.getString("ERROR_HASH_MANAGER_4")) ;

      // allocate the hash table
      result = WordMap.create() ;
      result.init(tablesize) ;
      
      //index = new int[tablesize] ;
      
      // loop through all words on much list and add to hash
      // table and create word and affix strings
      while ((ts = rawdict.readLine()) != null)
      {
        ts = ts.trim() ;

        // split each line into word and affix char strings
        int ap = ts.indexOf('/') ;
        HEntry en ;

        if (ap != -1)
          en = new HEntry(ts.substring(0, ap), ts.substring(ap + 1)) ;
        else
          en = new HEntry(ts, "") ;

        result.add(en) ;
      }
      
      result.finish(tablesize) ;
      //System.out.println("Ya lo ley�") ;
      
    }
    finally
    {
      Utils.close(rawdict) ;
    }

    return result ;
  }

  private String cleanword(String src, int[] pcaptype, boolean[] pabbrev)
  {
    int p = 0 ;
    int q = 0 ;

    // first skip over any leading special characters
    while ((q < src.length()) && !Character.isLetterOrDigit(src.charAt(q)))
      q++ ;

    // now strip off any trailing special characters
    // if a period comes after a normal char record its presence
    pabbrev[0] = false ;

    int nl = src.substring(q).length() ;

    while ((nl > 0) && !Character.isLetterOrDigit(src.charAt(q + nl - 1)))
      nl-- ;

    if ((q + nl) < src.length() && src.charAt(q + nl) == '.')
      pabbrev[0] = true ;

    // if no characters are left it can't be an abbreviation and can't be
    // capitalized
    if (nl <= 0)
    {
      pcaptype[0] = NOCAP ;
      pabbrev[0] = false ;

      return "" ;
    }

    // now determine the capitalization type of the first nl letters
    int ncap = 0 ;
    int nneutral = 0 ;
    int nc = 0 ;

    p = q ;

    while (nl > 0)
    {
      nc++ ;
      char c = src.charAt(q) ;

      if (Character.isUpperCase(c))
        ncap++ ;

      if (!Character.isUpperCase(c) && !Character.isLowerCase(c))
        nneutral++ ;

      q++ ;
      nl-- ;
    }

    // now finally set the captype
    if (ncap == 0)
      pcaptype[0] = NOCAP ;
    else if ((ncap == 1) && Character.isUpperCase(src.charAt(p)))
      pcaptype[0] = INITCAP ;
    else if ((ncap == nc) || ((ncap + nneutral) == nc))
      pcaptype[0] = ALLCAP ;
    else
      pcaptype[0] = HUHCAP ;

    return src.substring(p, q) ;
  }

  private String check(String word)
  {
    HEntry he = null ;

    if (pHMgr != null)
      he = pHMgr.get(word) ;

    if ((he == null) && (pAMgr != null))
    {
      // try stripping off affixes */
      he = pAMgr.affix_check(word) ;

      // try check compound word
      if ((he == null) && (pAMgr.get_compound() != null))
        he = pAMgr.compound_check(word, pAMgr.get_compound().charAt(0)) ;
    }

    if (he != null)
      return he.word ;

    return null ;
  }
  
  /**
   * This function add a new word to the current WordManager, but this word is not
   * add permanet, that is, is not save in file.
   * 
   * @param word The word to add.
   */
  public void addCustomWord(String word) throws IOException
  {
    pHMgr.addCustomWord(word) ;
  }
}
