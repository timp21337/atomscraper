package net.pizey.atomscraper;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.melati.Melati;
import org.melati.poem.AccessPoemException;
import org.melati.poem.AccessToken;
import org.melati.poem.Capability;
import org.melati.poem.Database;
import org.melati.poem.NoSuchTablePoemException;
import org.melati.poem.PoemDatabaseFactory;
import org.melati.poem.PoemThread;
import org.melati.poem.Table;
import org.melati.template.ServletTemplateContext;

/**
 * @author timp
 *
 */
public class Empty extends AtomscraperServlet {

  private static final long serialVersionUID = 141233889705684780L;

  /* (non-Javadoc)
   * @see net.pizey.atomscraper.AtomscraperServlet#reallyDoTemplateRequest(org.melati.Melati, org.melati.template.ServletTemplateContext)
   */
  @Override
  protected String reallyDoTemplateRequest(Melati melati,
      ServletTemplateContext templateContext) throws Exception {
    Database db = melati.getDatabase();
    Capability admin = db.getCanAdminister();
    if(admin==null) {
      db.setCanAdminister();
      admin = db.getCanAdminister();
    }
    AccessToken token = PoemThread.accessToken();
    if (!token.givesCapability(admin))
      throw new AccessPoemException(token, admin);

    System.err.println("Through");
    String name = melati.getDatabaseName();
    List<Table> tables = melati.getDatabase().getTables();
    List<String> tableNames = new ArrayList<String>();
    System.err.println("Through2");
    for (Table t : tables) {
      tableNames.add(t.getName());
      System.err.println("Through3" + t.getName());
    }
    Connection c = melati.getDatabase().getCommittedConnection();
    System.err.println("Through4");
    for (String tableName : tableNames)
    try { 
      System.err.println("Through5:" + tableName);
      Statement s = c.createStatement();
        s.executeUpdate("DROP TABLE " + db.getDbms().getQuotedName(tableName));
      s.close();
      c.commit();
    } catch (NoSuchTablePoemException e) { 
      e = null;
    }
    System.err.println("Through3");
    /*
    for (Table t :  melati.getDatabase().getDisplayTables()) {
      System.err.println("Deleting " + t.getName());
      if (melati.getDatabase().getCommittedConnection() == null)
        throw new RuntimeException("database.getCommittedConnection() null");
      melati.getDatabase().deleteTableAndCommit(t.getInfo());
    }*/
    PoemDatabaseFactory.disconnectDatabase(name);
    PoemDatabaseFactory.getDatabase(name);
    return "emptied";
  }

}
