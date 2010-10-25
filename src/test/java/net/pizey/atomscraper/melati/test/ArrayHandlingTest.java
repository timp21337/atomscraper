/**
 * 
 */
package net.pizey.atomscraper.melati.test;

import junit.framework.TestCase;

/**
 * @author timp
 *
 */
public class ArrayHandlingTest extends TestCase {

  /**
   * @param name
   */
  public ArrayHandlingTest(String name) {
    super(name);
  }

  /* (non-Javadoc)
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    super.setUp();
  }

  /* (non-Javadoc)
   * @see junit.framework.TestCase#tearDown()
   */
  protected void tearDown() throws Exception {
    super.tearDown();
  }
  Object[] extras;
  int current = 0;
  synchronized Object[] extras() {
    if (extras == null) {
      System.err.println("Creating extras length " + current);
      extras = new Object[current];
    } else if (extras.length < current ) {
      System.err.println("Growing from " + extras.length + " to " + current);
      Object[] newExtras = new Object[current];
      System.arraycopy(extras, 0, newExtras, 0, extras.length);
      extras = newExtras;
    }
    return extras;
  }
  
  public void testExtras() { 
    current++;
    extras()[current -1] = "one";
    dumpExtras();
    current++;
    extras()[current -1] = "two";
    dumpExtras();
    current++;
    extras()[current -1] = "three";
    dumpExtras();
    current++;
    extras()[current -1] = "four";
    dumpExtras();
  }

  private void dumpExtras() {
    for (int i = 0; i < extras.length; i++) { 
      System.err.println(extras[i]);
    }
    
  }

}
