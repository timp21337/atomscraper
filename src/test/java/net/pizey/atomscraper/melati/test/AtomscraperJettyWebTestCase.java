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

  private int unlikelyPortNo = 8089;
  
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
    actualPort = startServer(unlikelyPortNo);
    getTestContext().setBaseUrl("http://localhost:" + actualPort + "/" );
    
    //Delete hsqldb files
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
    contextName = "atomscraper";
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
     schlurp("/Login/atomscraper?continuationURL=" + 
       "/atomscraper/Entry/Flat?uri=http://localhost:" + 
       getActualPort() + "/atomscraper/testdata/studies/KHDXJ.atom");
  }

  public void testTree() { 
    System.err.println(schlurp("/Login/atomscraper?continuationURL=" + 
        "/atomscraper/Entry/Flat?uri=http://localhost:" + 
        getActualPort() + "/atomscraper/testdata/test.xml"));
  }
  
  public void testEmpty() { 
    System.err.println(schlurp("/Empty/atomscraper"));
  }

  private String schlurp(String relativeUrl) {
    setScriptingEnabled(false);
    beginAt(relativeUrl);
    setTextField("field_login", "_administrator_");
    setTextField("field_password", "FIXME");
    checkCheckbox("rememberme");
    submit("action");
    return getPageSource();
  }
  
  public void testWalker() { 
    setScriptingEnabled(true);
    beginAt("/walker.html");
  }


}
