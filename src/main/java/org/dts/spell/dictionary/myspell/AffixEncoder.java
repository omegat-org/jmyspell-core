/*
 * AffixEncoder.java
 *
 * Created on 4 de enero de 2007, 02:27 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.dts.spell.dictionary.myspell;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

/**
 *
 * @author DreamTangerine
 */
public final class AffixEncoder
{
  // The encoder
  private CharsetEncoder encoder = null ;

  // Array to hold the char to convert
  private char[] charArray = new char[1] ;
  private CharBuffer charBuffer = CharBuffer.wrap(charArray) ;
  
  // Array to hold the index. max two bytes one short UTF16
  private ByteBuffer buffer = ByteBuffer.allocate(2) ;
  
  /** Creates a new instance of AffixEncoder */
  public AffixEncoder()  
  {
    this("ISO8859-1") ;
  }
  
  public AffixEncoder(String charsetName)
  {
    Charset charset = Charset.forName(charsetName) ;
    encoder = charset.newEncoder() ;
    buffer.order(ByteOrder.LITTLE_ENDIAN) ;
  }
  
  final int getSetSize()
  {
    return (int) (encoder.maxBytesPerChar() * 256.0) ;
  }
  
  final int getCharIndex(char c)
  {
    int result ;
    
    if (encoder.canEncode(c))
    {
      buffer.rewind() ;
      charBuffer.rewind() ;
      charArray[0] = c ;
      
      encoder.reset() ;
      encoder.encode(charBuffer, buffer, true) ;
      encoder.flush(buffer) ;
      
      result = buffer.getShort(0) & 0x0ffff ;
    }
    else
    {
      assert false ;
      result = 0 ;
    }
    
    assert result < getSetSize() ;
    
    return result ;
  }
  
  public String getName()
  {
    return encoder.charset().displayName() ;
  }
  
  public Charset getCharset()
  {
    return encoder.charset() ;
  }
}
