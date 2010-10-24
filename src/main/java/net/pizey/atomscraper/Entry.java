/**
 * 
 */
package net.pizey.atomscraper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.melati.Melati;
import org.melati.PoemContext;
import org.melati.admin.AdminUtils;
import org.melati.poem.ColumnInfo;
import org.melati.poem.Database;
import org.melati.poem.DisplayLevel;
import org.melati.poem.Field;
import org.melati.poem.NoSuchColumnPoemException;
import org.melati.poem.NoSuchTablePoemException;
import org.melati.poem.Persistent;
import org.melati.poem.PoemThread;
import org.melati.poem.PoemTypeFactory;
import org.melati.poem.Searchability;
import org.melati.poem.Table;
import org.melati.poem.TableCategoryTable;
import org.melati.poem.TableInfo;
import org.melati.poem.Treeable;
import org.melati.servlet.PathInfoException;
import org.melati.template.ServletTemplateContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author timp
 * 
 */
public class Entry extends AtomscraperServlet {

  private static final long serialVersionUID = 5928430778760420797L;

  /**
   * Get and persist an entry.
   */
  public Entry() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * net.pizey.atomscraper.AtomscraperServlet#reallyDoTemplateRequest(org.melati
   * .Melati, org.melati.template.ServletTemplateContext)
   */
  @Override
  protected String reallyDoTemplateRequest(Melati melati,
      ServletTemplateContext templateContext) throws Exception {

    String uri = melati.getRequest().getParameter("uri");
    if (uri == null)
      throw new MissingArgumentException("No uri parameter found");
    populateContext(melati, templateContext, uri);
    return "flat";
  }

  private void populateContext(Melati melati,
      ServletTemplateContext templateContext, String uri) throws Exception {
    URL url = new URL(uri);
    URLConnection yc = url.openConnection();
    BufferedReader in = new BufferedReader(new InputStreamReader(yc
        .getInputStream()));
    String inputLine;
    StringBuffer contents = new StringBuffer();
    while ((inputLine = in.readLine()) != null) {
      contents.append(inputLine);
      contents.append('\n');
      // System.err.println(inputLine);
    }
    in.close();
    templateContext.put("contents", contents.toString());
    templateContext.put("uri", uri);

    Persistent it = parseEntry(melati.getDatabase(), uri);
    PoemThread.commit(); // The last table defined is not yet committed
    if (it == null)
      throw new RuntimeException("Failed");
    melati.setPoemContext(new PoemContext(it, "render"));
    melati.loadTableAndObject();
    List<Tuple> flattenedValues = new ArrayList<Tuple>();
    templateContext.put("flattenedValues", flattenedValues(flattenedValues, it, it.getTable().getName() + ".1."));
    templateContext.put("admin", new AdminUtils(melati));
  }


  private List<Tuple> flattenedValues(List<Tuple> flattenedValues, Persistent it, String prefix) {
    Enumeration<Field> recordDisplayFields = it.getRecordDisplayFields();
    while (recordDisplayFields.hasMoreElements()) {
      Field field =  recordDisplayFields.nextElement();
      if (!(field.getRaw() != null && field.getCooked().equals(it))) {
        if (!(field.getRaw() == null || field.getRaw().toString().equals("")))
          flattenedValues.add(new Tuple(prefix + field.getDisplayName(), 
                            field.getRaw() == null ? "" : field.getRaw().toString()));
      }
    }
    Treeable[] kids = it.getChildren();
    int occurence = 0;
    Persistent previous = null;
    for (Treeable kid : kids) {
      Persistent persistentKid = (Persistent)kid;
      if (previous == null)
        previous = persistentKid;
      if (previous.getTable().equals(persistentKid.getTable()))
        occurence++;
      flattenedValues(flattenedValues, persistentKid, 
          prefix + persistentKid.getTable().getName() + "." + occurence + ".");
    } 
    return flattenedValues;
  }

  private Persistent parseEntry(Database database, String uri) throws Exception {

    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    documentBuilderFactory.setIgnoringComments(true);
    
    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    Document dom = documentBuilder.parse(uri);
    dom = DomUtils.stripWhitespace(dom,dom.getDocumentElement());
    
    Element docElement = dom.getDocumentElement();
    System.err.println(docElement.getNodeName());

    return persist(database, docElement);
  }




  private Persistent persist(Database database, Element element) throws Exception {
    Table table = null;
    try {
      table = database.getTable(cleanName(element.getNodeName()));
    } catch (NoSuchTablePoemException e) {
      table = createTable(database, cleanName(element.getNodeName()));
    }
    Persistent p = table.newPersistent();
    
    setValues(database, p, element, setAttributes(p, element));
    if (p.getTable().getName().equals("atom_link")) {
      System.err.println("Found link with rel " + p.getField("rel").getCookedString());
      if (p.getField("rel").getCookedString().equals("http://www.cggh.org/2010/chassis/terms/studyInfo")
          || p.getField("rel").getCookedString().equals("http://www.cggh.org/2010/chassis/terms/submittedMedia)"))
       addChild(p, parseEntry(database, p.getField("href").getCookedString()));
    }
    
    
    Enumeration<Persistent> existing = p.getTable().selection(p);
    if (existing.hasMoreElements()){
      System.err.println("Returning existing " + cleanName(element.getNodeName()));
      return existing.nextElement();
    } else { 
      System.err.println("Making persistent " + cleanName(element.getNodeName()));
      if (!p.statusExistent())
        p.makePersistent();
      return p;
    }
  }

  private boolean setAttributes(Persistent persistent, Element element) {
    NamedNodeMap attributes = element.getAttributes();
    for (int i = 0; i < attributes.getLength(); i++) {
      Node attribute = attributes.item(i);
      DomUtils.dumpAttribute(attribute);
      setField(persistent, cleanName(attribute.getNodeName()), attribute.getNodeValue(),
          "Attribute");
    }
    return attributes.getLength() > 0;
  }

  private void setValues(Database database, Persistent persistent,
      Element element, boolean hasAttributes) throws Exception {
    NodeList kids = element.getChildNodes();
    if (kids.getLength() == 0) { // empty element eg <region/>
      setField(persistent, 
          cleanName(element.getNodeName()), 
          "", "Flag");
    } else if (kids.getLength() == 1 && kids.item(0).getNodeType() == Node.TEXT_NODE) { 
      // A leaf value node
      Node kid = kids.item(0);
      setField(persistent, 
          cleanName(element.getNodeName()), 
          kid.getNodeValue(), "Value");
    } else { 
      boolean isRepeat = true;
      String nodeName = null;
      for (int i = 0; i < kids.getLength(); i++) {
        if (nodeName == null)
          nodeName = kids.item(i).getNodeName();
        if (!nodeName.equals(kids.item(i).getNodeName()))
          isRepeat = false;
        System.err.println(nodeName + "=" + kids.item(i).getNodeName() + " " + isRepeat);
      }
      //NOTE that a single element within an element is a repeat of size 1
      if (isRepeat){
        System.err.println("Is a repeat: " + nodeName);
        for (int i = 0; i < kids.getLength(); i++) {
          Node kid = kids.item(i);
          DomUtils.dumpNode(kid);
          if (kid.getNodeType() == Node.ELEMENT_NODE) {
                 Persistent child = persist(database, (Element)kid);
                 System.err.println("Created: " + child.displayString());
                 addChild(persistent, child);
          } else throw new RuntimeException("Unexpected node type (expected Element)"
                   + kid.getNodeType());
       } 
      } else {
        for (int i = 0; i < kids.getLength(); i++) {
          Node kid = kids.item(i);
          DomUtils.dumpNode(kid);
          NodeList grandChildren = kid.getChildNodes();
          if (grandChildren.getLength() == 1 && grandChildren.item(0).getNodeType() == Node.TEXT_NODE) {
            setField(persistent, 
                cleanName(kid.getNodeName()), 
                grandChildren.item(0).getNodeValue(), "Value2");
            
          } else if (kid.getNodeType() == Node.ELEMENT_NODE) {
                 Persistent child = persist(database, (Element)kid);
                 System.err.println("Created: " + child.displayString());
                 addChild(persistent, child);
          } else throw new RuntimeException("Unexpected node type"
                   + kid.getNodeType());
       } 
      }
    }
  }

  private void addChild(Persistent persistent, Persistent child) {
    System.err.println("Adding child" + child.getName() + " to " + persistent.getName());
    try { 
      child.getTable().getColumn(persistent.getTable().getName());
    } catch (NoSuchColumnPoemException e) { 
      addColumn(child.getTable(), persistent.getTable().getName(), 
          persistent.getClass(), true, "A reference to a " + persistent.getTable().getName(), 
          persistent.getTable().getInfo().troid().intValue());
      child.getTable().getColumn(persistent.getTable().getName()).setDisplayLevel(DisplayLevel.detail);
    }
    if (!persistent.statusExistent())
      persistent.makePersistent();
    child.setCooked(persistent.getTable().getName(), persistent);
  }

  private String cleanName(String name) { 
    // FIXME Poem has a 50 char max, 
    //we have geneotypingToDistinguishBetweenRecrudescenceAndReinfection
    if (name.equals("study-info"))
        return "studyInfo";
    if (name.equals("serum-finalConcentration"))
        return "serumFinalConcentration";
    if (name.equals("NaHCO3-finalConcentration"))
        return "NaHCO3FinalConcentration";
    if (name.equals("hypoxantine-added"))
        return "hypoxantineAdded";
    if (name.equals("hypoxantine-finalConcentration"))
        return "hypoxantineFinalConcentration";
    if (name.equals("oroticAcid-added"))
        return "oroticAcidAdded";
    if (name.equals("oroticAcid-finalConcentration"))
        return "oroticAcidFinalConcentration";
    if (name.equals("glucose-added"))
        return "glucoseAdded";
    if (name.equals("glucose-finalConcentration"))
        return "glucoseFinalConcentration";


    if (name.length() > 45)
      name = name.substring(0,45);
    name = name.replaceAll(":","_");
    name = name.replaceAll("-","_");
    return name;
  }
  private void setField(Persistent p, String fieldName, String fieldValue,
      String type) {
    try { 
      p.getTable().getColumn(fieldName);
    } catch (NoSuchColumnPoemException e) { 
      addColumn(p.getTable(), fieldName, String.class, true, "A discovered "
          + type, null);
    }
    p.setRawString(fieldName, fieldValue);
  }

  protected PoemContext poemContext(Melati melati) throws PathInfoException {
    return poemContextWithLDB(melati, "atomscraper");
  }

  public static Table createTable(Database db, String name) {
    System.err.println("Creating table " + name);
    TableInfo tableInfo = (TableInfo) db.getTableInfoTable().newPersistent();
    tableInfo.setName(name);
    tableInfo.setDisplayname(name);
    tableInfo.setDescription(name + " element table");
    tableInfo.setDisplayorder(13);
    tableInfo.setSeqcached(Boolean.FALSE);
    tableInfo.setCategory(TableCategoryTable.NORMAL);
    tableInfo.setCachelimit(555);
    tableInfo.makePersistent();
    PoemThread.commit();
    Table created = db.addTableAndCommit(tableInfo, "poemId");
    created.getColumn("poemId").setDisplayLevel(DisplayLevel.detail);
    PoemThread.commit();
    return created;
  }

  private static void addColumn(Table table, String name, Class<?> fieldClass,
      boolean hasSetter, String description, Integer referenceColumnTroid) {
    ColumnInfo columnInfo = (ColumnInfo) table.getDatabase()
        .getColumnInfoTable().newPersistent();
    columnInfo.setTableinfo(table.getInfo());
    columnInfo.setName(name);

    columnInfo.setDisplayname(name);
    columnInfo.setDisplayorder(99);
    columnInfo.setSearchability(Searchability.yes);
    columnInfo.setIndexed(false);
    columnInfo.setUnique(false);
    columnInfo.setDescription(description);
    columnInfo.setUsercreateable(hasSetter);
    columnInfo.setUsereditable(hasSetter);
    columnInfo.setSize(8);
    columnInfo.setWidth(20);
    columnInfo.setHeight(1);
    columnInfo.setPrecision(0);
    columnInfo.setScale(0);
    columnInfo.setNullable(true);
    columnInfo.setDisplaylevel(DisplayLevel.record);
    if (fieldClass == java.lang.Boolean.class) {
      columnInfo.setTypefactory(PoemTypeFactory.BOOLEAN);
      columnInfo.setSize(1);
      columnInfo.setWidth(10);
    } else if (fieldClass == boolean.class) {
      columnInfo.setTypefactory(PoemTypeFactory.BOOLEAN);
      columnInfo.setSize(1);
      columnInfo.setWidth(10);
    } else if (fieldClass == java.lang.Integer.class) {
      columnInfo.setTypefactory(PoemTypeFactory.INTEGER);
    } else if (fieldClass == int.class) {
      columnInfo.setTypefactory(PoemTypeFactory.INTEGER);
    } else if (fieldClass == java.lang.Double.class) {
      columnInfo.setTypefactory(PoemTypeFactory.DOUBLE);
    } else if (fieldClass == double.class) {
      columnInfo.setTypefactory(PoemTypeFactory.DOUBLE);
    } else if (fieldClass == java.lang.Long.class) {
      columnInfo.setTypefactory(PoemTypeFactory.LONG);
    } else if (fieldClass == long.class) {
      columnInfo.setTypefactory(PoemTypeFactory.LONG);
    } else if (fieldClass == java.math.BigDecimal.class) {
      columnInfo.setTypefactory(PoemTypeFactory.BIGDECIMAL);
      columnInfo.setPrecision(22);
      columnInfo.setScale(2);
    } else if (fieldClass == java.lang.String.class) {
      columnInfo.setTypefactory(PoemTypeFactory.STRING);
      columnInfo.setSize(-1);
    } else if (fieldClass == java.sql.Date.class) {
      columnInfo.setTypefactory(PoemTypeFactory.DATE);
    } else if (fieldClass == java.sql.Timestamp.class) {
      columnInfo.setTypefactory(PoemTypeFactory.TIMESTAMP);
    } else if (fieldClass == byte[].class) {
      columnInfo.setTypefactory(PoemTypeFactory.BINARY);
    } else if (Persistent.class.isAssignableFrom(fieldClass)) {
      columnInfo.setTypefactory(PoemTypeFactory.TYPE);
      columnInfo.setTypefactory(PoemTypeFactory.forCode(table.getDatabase(),
      referenceColumnTroid));
    } else {
      throw new RuntimeException("Unexpected class " + fieldClass.getName());
    }
    columnInfo.makePersistent();
    table.addColumnAndCommit(columnInfo);
  }

}
