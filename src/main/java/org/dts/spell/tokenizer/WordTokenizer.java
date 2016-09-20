/*
 * Created on 29/03/2005
 *
 */
package org.dts.spell.tokenizer;

import org.dts.spell.finder.Word;

/**
 * Este interfaz modeliza un extractor de palabras, sin estado. Es decir debe de ser
 * capaz de extraer las palabras dado un índice.
 * 
 * @author DreamTangerine
 *
 */
public interface WordTokenizer
{
  /**
   * 
   * Obtiene la siguiente palabra, si <em>index</em> está en medio de una palabra 
   * esta se debería de saltar. Este método lo deben de implemtentar todos.
   * 
   * @param index
   * 
   * @return the next word.
   * 
   */
  public Word nextWord(int index) ; 

  /**
   * 
   * Obtiene la palabra actual sobre la que está <em>index</em>, si <em>index</em> 
   * no está en medio de una palabra devolverá null. 
   * Este método puede que no lo implementen todos, con lo que debe de generar una 
   * <em>UnsupportedOperationException</em>.
   * 
   * @param index El índice desde donde buscar.
   * 
   * @return the current word where is index or null.
   * 
   */
  public Word currentWord(int index) ;
  
  /**
   * @param index
   * 
   * @return the previous word where is index or null.
   */
  public Word previousWord(int index) ;
  
  public CharSequence getCharSequence() ; 
  public void setCharSequence(CharSequence sequence) ;  

  
  static int INSERT_CHARS = 0 ; 
  static int DELETE_CHARS = 1 ;  
  static int CHANGE_SEQUENCE = 2 ; 
  
  /**
   * Se llama cuando hay que actualizar la secuencia de carácteres actuales.
   * Debería de ser llamada siempre desde setCharSequence. 
   *
   * @param start El primer carácter desde donde se actualiza.
   * 
   * @param end El último carácter desde donde se actualiza.
   * 
   * @param cause La causa de la actualización. Puede ser cualquiera de las
   * constantes INSERT_CHARS, DELETE_CHARS, CHANGE_SEQUENCE 
   * 
   *
   */
  public void updateCharSequence(int start, int end, int cause) ;  
}
