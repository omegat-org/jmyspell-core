/*
 * Created on 27/12/2004
 *
 */
package org.dts.spell.dictionary.myspell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.dts.spell.dictionary.myspell.wordmaps.WordMap;

/**
 * Initial version
 *
 * @author DreamTangerine
 *
 * Some changes in affix tables
 *
 * @author Alex Buloichik <alex73mail@gmail.com>
 */
public class AffixMgr
{
  /** Map of prefixes by prefix text. */
  private final Map<String, List<PfxEntry>> prefixesByData = new TreeMap<>();
  
  /** Map of suffixes by suffix text. */
  private final Map<String, List<SfxEntry>> suffixesByData = new TreeMap<>();
  
  /** Map of prefixes by prefix name. */
  private final Map<Character, List<PfxEntry>> prefixesByName = new TreeMap<>();
  
  /** Map of suffixes by suffix name. */
  private final Map<Character, List<SfxEntry>> suffixesByName = new TreeMap<>();
  
  /** List of words. */
  private final WordMap pHMgr ;
  
  private String trystring = null ;
  private String compound  = null ;
  private int cpdmin = 3 ;
  private ReplEntry[] reptable = null ;
  private MapEntry[] maptable = null ;
  private boolean nosplitsugs = false ;
  
  private String encoding = null;
  
  
  /**
   * Initialize affix manager from .aff stream.
   *
   * @param affStream
   *            .aff stream
   * @param encoding
   *            encoding
   * @param ptr
   *            words list
   */
  public AffixMgr(InputStream affStream, String encoding, WordMap ptr) throws IOException
  {
    pHMgr = ptr ;
    
    parse_file(affStream, encoding) ;
  }
  
  
  /**
   * Check word by all affixes.
   *
   * @param word
   *            word
   * @return word entry if found or null if not exist
   */
  public HEntry affix_check(String word)
  {
    // check all prefixes (also crossed with suffixes if allowed)
    HEntry rv = prefix_check(word) ;
    
    // if still not found, check all suffixes
    if (null == rv) {
        rv = suffix_check(word, 0, null) ;
    }
    
    return rv ;
  }
  
  /**
   * Check word against prefixes.
   *
   * @param word
   *            word
   * @return word entry if found or null if not exist
   */
  public HEntry prefix_check(String  word)
  {
    // check start of word against prefixes
    for (int i = 0; i <= word.length(); i++)
    {
      // NOTE : i begins at 0 because "" is a valid prefix.
      final String currPrefix = word.substring(0, i) ;
      final List<PfxEntry> list = prefixesByData.get(currPrefix) ;
      
      if (null != list)
        for (PfxEntry pe : list)
        {
          HEntry rv = pe.check(word);
          
          if (rv != null)
            return rv;
        }
    }
    
    return null;
  }
  
  /**
   * Check word against suffixes.
   *
   * @param word
   *            word
   * @return word entry if found or null if not exist
   */
  public HEntry suffix_check(String word, int sfxopts, PfxEntry ppfx)
  {
    // check end of word against prefixes
    for (int i = word.length() ; i >= 0; i--)
    {
      // NOTE : i begins at word.length() because "" is a valid suffix.
      final String currSuffix = word.substring(i) ;
      final List<SfxEntry> list = suffixesByData.get(currSuffix) ;
      
      if (null != list)
      {
        for (SfxEntry se : list)
        {
          HEntry rv = se.check(word, sfxopts, ppfx);
          
          if (rv != null)
            return rv;
        }
      }
    }
    
    return null;
  }
  
  private void expand_suffixes(String ts, String ap, List<GuessWord> wlst)
  {
    int al = ap.length() ;
    
    for (int i = 0 ; i < al ; i++)
    {
      char c = ap.charAt(i) ;
      final List<SfxEntry> list = suffixesByName.get(c);
      
      if (null != list)
        for (SfxEntry sptr : list)
        {
          String newword = sptr.add(ts);
          
          if (newword != null)
            wlst.add(new GuessWord(newword, sptr.allowCross()));
        }
    }
  }
  
  private void expand_prefixes_suffixes(String ap, List<GuessWord> wlst)
  {
    int al = ap.length() ;
    
    ListIterator<GuessWord> it = wlst.listIterator() ; it.next() ;
    
    // handle cross products of prefixes and suffixes
    while (it.hasNext())
    {
      GuessWord wlstJ = it.next() ;
      
      if (wlstJ.allow)
      {
        for (int k = 0 ; k < al ; k++)
        {
          char c = ap.charAt(k) ;
          final List<PfxEntry> list = prefixesByName.get(c);
          
          if (null != list)
            for (PfxEntry cptr : list)
            {
              if (cptr.allowCross())
              {
                String newword = cptr.add(wlstJ.word);
                
                if (newword != null)
                  it.add(new GuessWord(newword, cptr.allowCross()));
              }
            }
        }
      }
    }
  }
  
  private void expand_prefixes(String ts, String ap, List<GuessWord> wlst)
  {
    int al = ap.length() ;
    
    for (int m = 0 ; m < al ; m++)
    {
      char c = ap.charAt(m) ;
      final List<PfxEntry> list = prefixesByName.get(c) ;
      
      if (null != list)
        for (PfxEntry ptr : list)
        {
          String newword = ptr.add(ts);
          
          if (newword != null)
            wlst.add(new GuessWord(newword, ptr.allowCross()));
        }
    }
  }
  
  public List<GuessWord> expand_rootword(String ts, String ap)
  {
    List<GuessWord> wlst = new LinkedList<>() ;
    
    // first add root word to list
    wlst.add(new GuessWord(ts, false)) ;
    
    // handle suffixes
    expand_suffixes(ts, ap, wlst) ;
    
    // handle cross products of prefixes and suffixes
    expand_prefixes_suffixes(ap, wlst) ;
    
    // now handle pure prefixes
    expand_prefixes(ts, ap, wlst) ;
    
    return wlst ;
  }
  
  public HEntry compound_check(String word, char compound_flag)
  {
    int len = word.length() ;
    
    int i ;
    HEntry rv;
    String st ;
    String wordI ;
    
    // handle case of string too short to be a piece of a compound word
    if (len < cpdmin)
      return null ;
    
    for (i = cpdmin ; i < (len - (cpdmin - 1)) ; i++)
    {
      st = word.substring(0, i) ;
      rv = lookup(st) ;
      
      if (rv == null)
        rv = affix_check(st) ;
      
      if ((rv != null) && Utils.TestAff(rv.astr, compound_flag, rv.astr.length()))
      {
        wordI = word.substring(i) ;
        rv = lookup(wordI) ;
        
        if ((rv != null) && Utils.TestAff(rv.astr, compound_flag, rv.astr.length()))
          return rv ;
        
        rv = affix_check(wordI) ;
        
        if ((rv != null) && Utils.TestAff(rv.astr, compound_flag, rv.astr.length()))
          return rv ;
        
        rv = compound_check(wordI, compound_flag) ;
        
        if (rv != null)
          return rv ;
      }
    }
    
    return null ;
  }
  
  public HEntry lookup(String word)
  {
    if (pHMgr == null)
      return null ;
    
    return pHMgr.get(word) ;
  }
  
  public int get_numrep()
  {
    if (reptable != null)
      return reptable.length ;
    else
      return 0 ;
  }
  
  public ReplEntry[] get_reptable()
  {
    return reptable ;
  }
  
  public int get_nummap()
  {
    if (maptable != null)
      return maptable.length ;
    else
      return 0 ;
  }
  
  public MapEntry[] get_maptable()
  {
    return maptable ;
  }

  @SuppressWarnings("unused")
  public String get_encoding()
  {
    if (encoding == null)
      encoding = "ISO8859-1" ;
    
    return encoding ;
  }
  
  public String get_try_string()
  {
    return trystring ;
  }
  
  public String get_compound()
  {
    return compound ;
  }
  
  public boolean get_nosplitsugs()
  {
    return nosplitsugs;
  }

  private static String readLine(InputStream affStream) throws IOException
  {
    StringBuilder builder = new StringBuilder(20) ;
    
    int r = affStream.read() ;
    
    while (-1 != r && '\n' != r)
    {
      builder.append((char) r) ;
      r = affStream.read() ;
    }
    
    if (r != -1 || builder.length() > 0)
      return builder.toString() ;
    else
      return null ;
  }
  
  private static boolean canSkip(String line)
  {
    line = line.trim() ;
    
    return line.isEmpty() || line.startsWith("#");
  }
  
  public static String readEncoding(InputStream affStream) throws IOException
  {
    // we suppose that to the first line with is no a comment is in US-ASCII
    String line = readLine(affStream);

    while (null != line && (canSkip(line) || !line.startsWith("SET")))
      line = readLine(affStream);

    return parseEncoding(line) ;
  }
  
  
  private void parse_file(InputStream affStream, String encoding) throws IOException
  {
    BufferedReader rd = new BufferedReader(new InputStreamReader(affStream, encoding)) ;
    
    this.encoding = encoding ;
    
    String line ;
    
    try
    {
      Conditions.beginRead() ;
      
      while ((line = rd.readLine()) != null)
      {
        if (!canSkip(line))
        {
          // parse this affix: P - prefix, S - suffix
          if (line.startsWith("PFX"))
            parse_pfxaffix(line, rd) ;
          else if (line.startsWith("SFX"))
            parse_sfxaffix(line, rd) ;
          else if (line.startsWith("TRY"))
            parse_try(line) ;
          else if (line.startsWith("SET"))
            parse_set(line) ;
          else if (line.startsWith("COMPOUNDFLAG"))
            parse_cpdflag(line) ;
          else if (line.startsWith("COMPOUNDMIN"))
            parse_cpdmin(line) ;
          else if (line.startsWith("REP"))
            parse_reptable(line, rd) ;
          else if (line.startsWith("MAP"))
            parse_maptable(line, rd) ;
          else if (line.startsWith("NOSPLITSUGS")) // handle NOSPLITSUGS
            nosplitsugs = true ;
        }
      }
      
      trim_affixs() ;
    }
    finally
    {
      Conditions.endRead() ;
    }
  }
  
  private void trim_affixs()
  {
    trim_affix_collection(prefixesByData.values()) ;
    trim_affix_collection(prefixesByName.values()) ;
    trim_affix_collection(suffixesByData.values()) ;
    trim_affix_collection(suffixesByName.values()) ;    
  }
  
  private <T> void trim_affix_collection(Collection<List<T>> values)
  {
    for (List<T> value : values)
      ((ArrayList<T>) value).trimToSize() ;
  }
  
  private void parse_try(String line) throws IOException
  {
    if (trystring != null) {
        throw new IOException(Utils.getString("ERROR_DUPLICATE_TRY"));
    }
    
    StringTokenizer tp = new StringTokenizer(line, " ") ;
    String piece ;
    int i = 0 ;
    int np = 0 ;
    
    while (tp.hasMoreTokens())
    {
      piece = tp.nextToken() ;
      
      if (!piece.isEmpty())
      {
        switch (i)
        {
          case 0:
            np++ ;
            break ;
            
          case 1:
            trystring = piece ;
            np++ ;
            break ;
            
          default:
            break ;
        }
        
        i++ ;
      }
    }
    
    if (np != 2) {
        throw new IOException(Utils.getString("ERROR_MISSING_TRY"));
    }
  }
  
  private static final Pattern ENCODING_MICROSOFT = Pattern.compile("microsoft-cp(\\d+)");

  private static String parseEncoding(String line) throws IOException {
    if (line == null) {
      throw new IOException(Utils.getString("ERROR_MISSING_SET"));
    }
    StringTokenizer tp = new StringTokenizer(line, " ");
    String piece;
    int i = 0;
    int np = 0;
    String result = null;

    while (tp.hasMoreTokens()) {
      piece = tp.nextToken();

      if (!piece.isEmpty()) {
        switch (i) {
          case 0:
            np++;
            break;

          case 1:
            result = piece;
            np++;
            break;

          default:
            break;
        }

        i++;
      }
    }

    if (np != 2) {
        throw new IOException(Utils.getString("ERROR_MISSING_SET"));
    }

    if (result != null) {
      result = result.trim();
      Matcher m = ENCODING_MICROSOFT.matcher(result);
      if (m.matches()) {
        result = "windows-" + m.group(1);
      }
    }
    return result;
  }
  
  private void parse_set(String line) throws IOException
  {
    if (encoding != null) {
        throw new IOException(Utils.getString("ERROR_DUPLICATE_SET"));
    }
    encoding = parseEncoding(line) ;
  }
  
  private void parse_cpdflag(String line) throws IOException
  {
    if (compound != null)
      throw new IOException(Utils.getString("ERROR_DUPLICATE_COMPOUND_FLAGS"));
    
    StringTokenizer tp = new StringTokenizer(line, " ") ;
    String piece ;
    int i = 0 ;
    int np = 0 ;
    
    while (tp.hasMoreTokens())
    {
      piece = tp.nextToken() ;
      
      if (!piece.isEmpty())
      {
        switch (i)
        {
          case 0:
            np++ ;
            break ;
            
          case 1:
            compound = piece ;
            np++ ;
            break ;
            
          default:
            break ;
        }
        
        i++ ;
      }
    }
    if (np != 2)
      throw new IOException(Utils.getString("ERROR_MISSING_COMPOUND_FLAG"));
  }
  
  private void parse_cpdmin(String line) throws IOException
  {
    StringTokenizer tp = new StringTokenizer(line, " ") ;
    String piece ;
    int i = 0 ;
    int np = 0 ;
    
    while (tp.hasMoreTokens())
    {
      piece = tp.nextToken() ;
      
      if (!piece.isEmpty())
      {
        switch (i)
        {
          case 0:
            np++ ;
            break ;
            
          case 1:
            cpdmin = Integer.parseInt(piece) ;
            np++ ;
            break ;
            
          default:
            break ;
        }
        
        i++ ;
      }
    }
    
    if (np != 2)
      throw new IOException(Utils.getString("ERROR_MISSING_COMPOUND_MIN"));
    
    if ((cpdmin < 1) || (cpdmin > 50))
      cpdmin = 3 ;
  }
  
  private void parse_reptable(String line, BufferedReader af) throws IOException
  {
    int numrep = get_numrep() ;
    
    if (numrep != 0)
      throw new IOException(Utils.getString("ERROR_DUPLICATE_REP"));
    
    StringTokenizer tp = new StringTokenizer(line, " ") ;
    String piece ;
    int i = 0 ;
    int np = 0 ;
    
    while (tp.hasMoreTokens())
    {
      piece = tp.nextToken() ;
      
      if (!piece.isEmpty())
      {
        switch (i)
        {
          case 0:
            np++ ;
            break ;
            
          case 1:
            numrep = Integer.parseInt(piece) ;
            
            if (numrep < 1)
              throw new IOException(Utils.getString("INCORRECT_NUMBER_OF_ENTRIES_REP_TABLE"));
            
            reptable = new ReplEntry[numrep] ;
            np++ ;
            break ;
            
          default:
            break ;
        }
        
        i++ ;
      }
    }
    
    if (np != 2)
      throw new IOException(Utils.getString("ERROR_MISSING_REP_TABLE"));
    
    /* now parse the numrep lines to read in the remainder of the table */
    for (int j = 0 ; j < numrep ; j++)
    {
      tp = new StringTokenizer(af.readLine(), " ") ;
      i = 0 ;
      
      reptable[j] = new ReplEntry() ;
      reptable[j].pattern = null ;
      reptable[j].replacement = null ;
      
      while (tp.hasMoreTokens())
      {
        piece = tp.nextToken() ;
        
        if (!piece.isEmpty())
        {
          switch (i)
          {
            case 0:
              if (!piece.startsWith("REP"))
                throw new IOException(Utils.getString("ERROR_REP_TABLE_CORRUPT"));
              break ;
              
            case 1:
              reptable[j].pattern = piece ;
              break ;
              
            case 2:
              reptable[j].replacement = piece ;
              break ;
              
            default:
              break ;
          }
          
          i++ ;
        }
      }
      
      if ((reptable[j].pattern == null) || (reptable[j].replacement == null))
        throw new IOException(Utils.getString("ERROR_REP_TABLE_CORRUPT"));
    }
    
  }
  
  private void parse_maptable(String line, BufferedReader af) throws IOException
  {
    int nummap = get_nummap() ;
    
    if (nummap != 0)
      throw new IOException(Utils.getString("ERROR_DUPLICATE_MAP"));
    
    StringTokenizer tp = new StringTokenizer(line, " ") ;
    String piece ;
    int i = 0 ;
    int np = 0 ;
    
    while (tp.hasMoreTokens())
    {
      piece = tp.nextToken() ;
      
      if (!piece.isEmpty())
      {
        switch (i)
        {
          case 0:
            np++ ;
            break ;
            
          case 1:
            nummap = Integer.parseInt(piece) ;
            
            if (nummap < 1)
              throw new IOException(Utils.getString("ERROR_NUMBER_ENTRIES_MAP"));
            
            maptable = new MapEntry[nummap] ;
            np++ ;
            break ;
            
          default:
            break ;
        }
        
        i++ ;
      }
    }
    
    if (np != 2)
      throw new IOException(Utils.getString("ERROR_MISSING_MAP"));
    
    /* now parse the nummap lines to read in the remainder of the table */
    for (int j = 0 ; j < nummap ; j++)
    {
      line = af.readLine() ;
      tp = new StringTokenizer(line, " ") ;
      i = 0 ;
      
      maptable[j] = new MapEntry() ;
      maptable[j].set = null ;
      
      while (tp.hasMoreTokens())
      {
        piece = tp.nextToken() ;
        
        if (!piece.isEmpty())
        {
          switch (i)
          {
            case 0:
              if (!piece.startsWith("MAP"))
                throw new IOException(Utils.getString("ERROR_MAP_CORRUPT"));
              
              break ;
              
            case 1:
              maptable[j].set = piece ;
              break ;
              
            default:
              break ;
          }
          
          i++ ;
        }
      }
      
      if ((maptable[j].set == null) || (maptable[j].set.isEmpty()))
        throw new IOException(Utils.getString("ERROR_MAP_CORRUPT"));
    }
  }
  
  private AffixHeader readAffxHeader(String line) throws IOException
  {
    AffixHeader result = new AffixHeader() ;
    StringTokenizer tp = new StringTokenizer(line, " ") ;
    
    try
    {
      // piece 1 - is type of affix
      result.type = tp.nextToken().charAt(0) ;
      
      // piece 2 - is affix char
      result.achar = tp.nextToken().charAt(0) ;
      
      // piece 3 - is cross product indicator
      if (tp.nextToken().charAt(0) == 'Y')
        result.ff = (short) Utils.XPRODUCT ;
      
      // piece 4 - is number of affentries
      result.numents = Integer.parseInt(tp.nextToken()) ;
    }
    catch (NoSuchElementException ex)
    {
      Utils.throwIOException("ERROR_AFFIX_HEADER", result.achar, line) ;
    }
    
    return result ;
  }
  
  private void parse_pfxaffix(String line, BufferedReader af) throws IOException
  {
    // split affix header line into pieces
    AffixHeader header = readAffxHeader(line) ;

    // now parse numents affentries for this affix
    for (int j = 0 ; j < header.numents ; j++)
      new PfxEntry(this, header, af.readLine()) ;
  }
  
  private void parse_sfxaffix(String line, BufferedReader af) throws IOException
  {
    // split affix header line into pieces
    AffixHeader header = readAffxHeader(line) ;

    // now parse numents affentries for this affix
    for (int j = 0 ; j < header.numents ; j++)
      new SfxEntry(this, header, af.readLine()) ;
  }
  
  void build_pfxlist(PfxEntry pfxptr)
  {
    prefixesByData.computeIfAbsent(pfxptr.appnd, k -> new ArrayList<>()).add(pfxptr);
    prefixesByName.computeIfAbsent(pfxptr.achar, k -> new ArrayList<>()).add(pfxptr) ;
  }
  
  void build_sfxlist(SfxEntry sfxptr)
  {
    suffixesByData.computeIfAbsent(sfxptr.appnd, k -> new ArrayList<>()).add(sfxptr);
    suffixesByName.computeIfAbsent(sfxptr.achar, k -> new ArrayList<>()).add(sfxptr);
  }
}
