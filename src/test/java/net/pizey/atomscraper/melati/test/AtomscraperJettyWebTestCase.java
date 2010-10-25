/*
 * $Source: /usr/cvsroot/melati/melati-archetype/src/main/resources/archetype-resources/src/test/java/melati/test/JettyWebTestCase.java,v $
 * $Revision: 1.2 $
 *
 * Copyright (C) 2010 Tim Pizey
 *
 *
 * Contact details for copyright holder:
 *
 *     Tim Pizey <timp At paneris.org>
 *     http://paneris.org/~timp
 */

package net.pizey.atomscraper.melati.test;

import org.melati.JettyWebTestCase;

/**
 * @author timp
 * @since  3 Mar 2009
 *
 */



public class AtomscraperJettyWebTestCase extends JettyWebTestCase {

  public AtomscraperJettyWebTestCase(String name) {
    super(name);
    webAppDirName = "src/main/webapp";
    contextName = "atomscraper";
  }

  /**
   * {@inheritDoc}
   * @see org.melati.JettyWebTestCase#setUp()
   */
  protected void setUp() throws Exception {
    // Port 0 means "assign arbitrarily port number"
    actualPort = startServer(8080);
    getTestContext().setBaseUrl("http://localhost:" + actualPort + "/" );
    
    // Delete hsqldb files
    //File script = new File("db/atomscraper.script");
    //script.delete();
    //File properties = new File("db/atomscraper.properties");
    //properties.delete();
  }

  /**
   * {@inheritDoc}
   * @see org.melati.JettyWebTestCase#tearDown()
   */
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  /**
   * If you don't know by now.
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    contextName = "";
    webAppDirName = "src/main/webapp";
    startServer(8080);
  }
  
  /**
   * Just to say hello.
   */
  public void testIndex() {
    beginAt("/");
    assertTextPresent("Admin");
  }
  public void testTestData() {
    beginAt("/testdata/study-info.xml");
    assertTextPresent("Study info - why no title? ");
  }
  
  public void testEntrySchlurp() { 
    setScriptingEnabled(false);
    beginAt("/Login/atomscraper?continuationURL=" + 
        "/atomscraper/Entry/Flat?uri=http://localhost:" + getActualPort() + "/atomscraper/testdata/studies/KHDXJ.atom");
    setTextField("field_login", "_administrator_");
    setTextField("field_password", "FIXME");
    checkCheckbox("rememberme");
    submit("action");
    System.err.println(getPageSource());
    //assertTextPresent("Study info - why no title? ");   
  }
}
