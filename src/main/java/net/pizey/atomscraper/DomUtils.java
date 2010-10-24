/**
 * 
 */
package net.pizey.atomscraper;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * @author timp
 *
 */
public final class DomUtils {
  public static  Document stripWhitespace(Document dom, Node node) {
    NodeList kids = node.getChildNodes();
    for (int i = 0; i < kids.getLength(); i++) { 
      Node kid = kids.item(i);
      dumpNode(kid);
      if (kid.getNodeType() == Node.TEXT_NODE && ((Text)kid).getData().trim().length() == 0) { 
        node.removeChild(kid);
        //System.err.println("removing " + kid.getNodeName());
        i = i -1;
        kids = node.getChildNodes();
      } else  { 
        //System.err.println("descending into " + kid.getNodeName());
        stripWhitespace(dom, kid);
      }
    }
    return dom;
  }
  
  
  public static  void dumpAttribute(Node attribute) {
    dumpNode("Attribute", attribute);
  }

  public static void dumpNode(Node node) {
    dumpNode("Node", node);
  }

  public static void dumpNode(String nodeType, Node node) {
    System.err.println("{");
    System.err.println(" " + nodeType + " name:  " + node.getNodeName());
    System.err.println(" " + nodeType + " value :" + node.getNodeValue() + ":");
    System.err.println(" " + nodeType + " type:  " + node.getNodeType());
    System.err.println(" " + nodeType + " kids:  " + node.getChildNodes().getLength());
    System.err.println("}");
  }

}
