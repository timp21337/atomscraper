package net.pizey.atomscraper;

import org.melati.Melati;
import org.melati.poem.PoemDatabaseFactory;
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
    for (Table t :  melati.getDatabase().getDisplayTables()) {
      System.err.println("Deleting " + t.getName());
      melati.getDatabase().deleteTableAndCommit(t.getInfo());
      String name = melati.getDatabaseName();
      PoemDatabaseFactory.disconnectDatabase(name);
      //PoemDatabaseFactory.getDatabase(name);
    }
    return "Emptied";
  }

}
