package net.pizey.atomscraper.test;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.pizey.atomscraper.DomUtils;

import org.melati.util.test.StringInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import junit.framework.TestCase;

public class DomUtilsTest extends TestCase {

  public DomUtilsTest(String name) {
    super(name);
  }

  protected void setUp() throws Exception {
    super.setUp();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  
  public void testWhitespaceStripper() throws Exception { 
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    documentBuilderFactory.setIgnoringComments(true);
    
    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    String fragment = "<e1> <e2>e2v</e2> <e3/> <e4>e4v</e4> </e1>";
    StringInputStream in = new StringInputStream(fragment); 
    Document dom = documentBuilder.parse(in);
    NodeList nl = dom.getChildNodes();
    for (int i = 0; i < nl.getLength(); i++) { 
      Node n = nl.item(i);
      DomUtils.dumpNode(n);
    }
    
    DomUtils.stripWhitespace(dom, dom.getDocumentElement());

  }
}
