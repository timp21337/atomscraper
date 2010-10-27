package net.pizey.atomscraper;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Flattener {



	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// TODO take filename from args
		String fileName = "src/test/resources/donuts.xml";
		
		try {

			String out = flatten(fileName);
			System.out.print(out);
			//items.item1.name1,items.item1.ppu1,items.item1.batters1.batter1,items.item1.batters1.batter2,items.item1.batters1.batter3,items.item1.batters1.batter4,items.item1.topping1,items.item1.topping2,items.item1.topping3,items.item1.topping4,items.item1.topping5,items.item1.topping6,items.item1.topping7,items.item2.name1,items.item2.ppu1,items.item2.batters1.batter1,items.item2.topping1,items.item2.topping2,items.item2.topping3,items.item2.topping4,items.item2.topping5,items.item3.name1,items.item3.ppu1,items.item3.batters1.batter1,items.item3.batters1.batter2,items.item4.name1,items.item4.ppu1,items.item4.batters1.batter1,items.item4.topping1,items.item4.topping2,items.item4.fillings1.filling1.name1,items.item4.fillings1.filling1.addcost1,items.item4.fillings1.filling2.name1,items.item4.fillings1.filling2.addcost1,items.item4.fillings1.filling3.name1,items.item4.fillings1.filling3.addcost1,items.item5.name1,items.item5.ppu1,items.item5.batters1.batter1,items.item5.topping1,items.item5.topping2,items.item6.name1,items.item6.ppu1,items.item6.batters1.batter1,items.item6.topping1,items.item6.topping2,items.item6.topping3,items.item6.topping4,items.item6.fillings1.filling1.name1,items.item6.fillings1.filling1.addcost1,items.item6.fillings1.filling2.name1,items.item6.fillings1.filling2.addcost1,items.item6.fillings1.filling3.name1,items.item6.fillings1.filling3.addcost1,items.item6.fillings1.filling4.name1,items.item6.fillings1.filling4.addcost1,
			//Cake,0.55,Regular,Chocolate,Blueberry,Devil's Food,None,Glazed,Sugar,Powdered Sugar,Chocolate with Sprinkles,Chocolate,Maple,Raised,0.55,Regular,None,Glazed,Sugar,Chocolate,Maple,Buttermilk,0.55,Regular,Chocolate,Bar,0.75,Regular,Chocolate,Maple,None,0,Custard,0.25,Whipped Cream,0.25,Twist,0.65,Regular,Glazed,Sugar,Filled,0.75,Regular,Glazed,Powdered Sugar,Chocolate,Maple,Custard,0,Whipped Cream,0,Strawberry Jelly,0,Rasberry Jelly,0,
			File outFile = new File("src/test/resources/test.csv");
			FileWriter w = new FileWriter(outFile);
			w.write(out);
			w.flush();
			w.close();

		} catch (Exception e) {
			e.printStackTrace();
		} 
		
	}

	
	
	
	private static String flatten(String fileName) throws SAXException, IOException, ParserConfigurationException {

		File file = new File(fileName);
		Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
		
		Element e = d.getDocumentElement();
		String context = e.getTagName();
		
		ArrayList<String> cols = new ArrayList<String>();
		ArrayList<String> vals = new ArrayList<String>();
		
		flatten(context, cols, vals, d.getDocumentElement());

		return serialize(cols, vals);
	}




	private static void flatten(String context, ArrayList<String> cols,	ArrayList<String> vals, Element e) {

		List<Element> kids = getChildElements(e);
		
		if (kids.isEmpty()) {
			
			// e is a leaf
			cols.add(context);
			// If there are no Child Elements then 
			// the first child will be a text one
			Node text = e.getFirstChild();
			if (text.getNodeType() != Node.TEXT_NODE)
			  throw new RuntimeException("Expected a Text node but got " + text.getNodeType());
      vals.add(text.getNodeValue()); 
      // where did getTextContent go?
      //vals.add(e.getTextContent());
			
		}
		else {
			
			// e is not a leaf, recurse
			Map<String,Integer> counts = new HashMap<String,Integer>();
			
			for (Element kid : kids) {
				
				String name = kid.getTagName();
				if (counts.containsKey(name))
					counts.put(name, counts.get(name)+1);
				else counts.put(name, 1);
				
				String newContext = context + "." + name + counts.get(name);
				flatten(newContext, cols, vals, kid);
				
			}
		}
		
	}




	private static List<Element> getChildElements(Element e) {
		List<Element> el = new ArrayList<Element>();
		NodeList nl = e.getChildNodes();
		for (int i=0; i<nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n instanceof Element) {
				el.add((Element)n);
			}
		}
		return el;
	}




	private static String serialize(ArrayList<String> cols,	ArrayList<String> vals) {
		String out = "";
		for (String col : cols) {
			out += col + ",";
		}
		out += "\n";
		for (String val : vals) {
			out += val + ",";
		}
		return out;
	}


	
	
}
