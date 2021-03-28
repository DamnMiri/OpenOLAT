package ai.core;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import java.sql.SQLException;


public class DBScraper {
	
	static DataSource ds = null;
	static Statement statement = null;
	
	public static void setDataSource(DataSource dsrc) throws IOException, SQLException
	{
		ds = dsrc;
		statement = ds.getConnection().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
	}
	
	public static void PrepareProlog() throws IOException, SQLException
	{		
		ScrapeStudents();
		ScrapeCourses();
		ScrapeLearningObjects();
		ScrapeVisto();
		//ScrapeFollows();
	}
	
	private static String addQuotes(String s)
	{
		return  "'"+s+"'";
	}

	private static void ScrapeStudents() throws SQLException, IOException
	{
		ResultSet rs = statement.executeQuery("select authusername from o_bs_authentication where provider = 'OLAT';");
		      while (rs.next()) {
		          String authusername = addQuotes(rs.getString("authusername"));
		          PrologEngine.generaInserisci("student", new String[] {authusername});
		        }
			rs.close();
			PrologEngine.insertEmptyLine();
	}
	
	private static void ScrapeCourses() throws SQLException, IOException
	{
		ResultSet rs = statement.executeQuery("SELECT tb2.name AS nome_corso,tb1.name AS tag_corso,tb2.fk_repoentry as repoentry FROM o_catentry AS tb1 join o_catentry AS tb2 ON tb1.id = tb2.parent_id AND tb2.`type` = 1");
	      while (rs.next()) {
	          String nome_corso = addQuotes(rs.getString("nome_corso"));
	          PrologEngine.generaInserisci("course", new String[] {nome_corso});
	        }
	      /*
	      PrologEngine.insertEmptyLine();
	      rs.beforeFirst();
	      while (rs.next()) {
	          String nome_corso = rs.getString("nome_corso").toLowerCase().replace(" ", "_");
	          String tag_corso = rs.getString("tag_corso").toLowerCase().replace(" ", "_");
	          PrologEngine.generaInserisci("course_type", new String[] {nome_corso,tag_corso});
	        }
	        */
	      PrologEngine.insertEmptyLine();
	      rs.beforeFirst();
	      while (rs.next()) {
	          String nome_corso = addQuotes(rs.getString("nome_corso"));
	          String id = rs.getString("repoentry").toLowerCase().replace(" ", "_");
	          PrologEngine.generaInserisci("courseId", new String[] {nome_corso,id});
	        }
		rs.close();
		PrologEngine.insertEmptyLine();
	}
	
	private static void ScrapeLearningObjects() throws SQLException, IOException
	{
		ResultSet rs = statement.executeQuery("SELECT displayname,resid,repositoryentry_id FROM o_repositoryentry join o_olatresource on fk_olatresource = resource_id WHERE status = 'published'");
	      while (rs.next()) {
	          Long resid = rs.getLong("resid");
		        ICourse corso = CourseFactory.loadCourse(resid);
		        int count = corso.getRunStructure().getRootNode().getChildCount();
		        for (int i = 0; i < count; i++) {
		        	String ident = corso.getRunStructure().getRootNode().getChildAt(i).getIdent();
		            CourseNode chosenNode = corso.getEditorTreeModel().getCourseNode(ident);
		            String lo = addQuotes(chosenNode.getShortTitle());
		            PrologEngine.generaInserisci("learningObject", new String[] {lo});
				}
	        }
	      PrologEngine.insertEmptyLine();
	      rs.beforeFirst();
	      while (rs.next()) {
	          Long resid = rs.getLong("resid");
		        ICourse corso = CourseFactory.loadCourse(resid);
		        int count = corso.getRunStructure().getRootNode().getChildCount();
		        for (int i = 0; i < count; i++) {
		        	String ident = corso.getRunStructure().getRootNode().getChildAt(i).getIdent();
		            CourseNode chosenNode = corso.getEditorTreeModel().getCourseNode(ident);
		            String lo = addQuotes(chosenNode.getShortTitle());
		            String[] taglist = chosenNode.getLearningObjectives().toLowerCase().split("#");
		            String tagz="[";
		            for (int y = 0; y<taglist.length; y++)
		            {
		            	if (y==0) continue;
		            	String cleanTag = Jsoup.clean(taglist[y], Whitelist.none());
		            	tagz+=cleanTag+",";
		            }
		            if(tagz.length()>1)
		            	tagz = tagz.substring(0, tagz.length()-1);
		            tagz = tagz.concat("]");
		            PrologEngine.generaInserisci("learningObjectTaglist", new String[] {lo,tagz}); //al momento solo lezione
				}
	        }
	      PrologEngine.insertEmptyLine();
	      rs.beforeFirst();
	      while (rs.next()) {
	          Long resid = rs.getLong("resid");
		        ICourse corso = CourseFactory.loadCourse(resid);
		        int count = corso.getRunStructure().getRootNode().getChildCount();
		        for (int i = 0; i < count; i++) {
		        	String ident = corso.getRunStructure().getRootNode().getChildAt(i).getIdent();
		            CourseNode chosenNode = corso.getEditorTreeModel().getCourseNode(ident);
		            String lo = addQuotes(chosenNode.getShortTitle());
		            PrologEngine.generaInserisci("learningObjectId", new String[] {lo,ident});
				}
	        }
	      PrologEngine.insertEmptyLine();
	      rs.beforeFirst();
	      while (rs.next()) {
	    	  String displayname = addQuotes(rs.getString("displayname"));
	          Long resid = rs.getLong("resid");
		        ICourse corso = CourseFactory.loadCourse(resid);
		        int count = corso.getRunStructure().getRootNode().getChildCount();
		        for (int i = 0; i < count; i++) {
		        	String ident = corso.getRunStructure().getRootNode().getChildAt(i).getIdent();
		            CourseNode chosenNode = corso.getEditorTreeModel().getCourseNode(ident);
		            String lo = addQuotes(chosenNode.getShortTitle());
		            PrologEngine.generaInserisci("hasLearningObject", new String[] {displayname,lo});
				}
	        }   
		rs.close();
		PrologEngine.insertEmptyLine();
	}

	private static void ScrapeVisto() throws SQLException, IOException
	{
		ResultSet rs = statement.executeQuery("SELECT a_status,a_fully_assessed,a_completion,a_obligation,a_num_visits,fk_entry,a_subident,a_entry_root,authusername,displayname,resid FROM o_as_entry JOIN o_bs_authentication ON fk_identity = identity_fk JOIN o_repositoryentry ON fk_entry = repositoryentry_id JOIN o_olatresource ON fk_olatresource = resource_id WHERE provider = 'OLAT' AND a_entry_root = 0 AND a_fully_assessed = 1");
	    while (rs.next()) {
	        String authusername = addQuotes(rs.getString("authusername"));
	        String a_subident = rs.getString("a_subident").toLowerCase().replace(" ", "_");
	        Long resid = rs.getLong("resid");
	        
	        ICourse asd = CourseFactory.loadCourse(resid);
	        CourseNode chosenNode = asd.getEditorTreeModel().getCourseNode(a_subident);
	        if(chosenNode == null) continue;
	        String lo = addQuotes(chosenNode.getShortTitle());
	        PrologEngine.generaInserisci("hasRead", new String[] {authusername,lo});
	      }
		rs.close();
		PrologEngine.insertEmptyLine();
	}
	
	//non più usato
	private static void ScrapeFollows() throws SQLException, IOException
	{
		ResultSet rs = statement.executeQuery("SELECT u_nickname,displayname from o_bs_group_member JOIN o_user on fk_identity_id = fk_identity join o_re_to_group on o_re_to_group.fk_group_id = o_bs_group_member.fk_group_id join o_repositoryentry on o_repositoryentry.repositoryentry_id = o_re_to_group.fk_entry_id where g_role = 'participant'");
	    while (rs.next()) {
	        String u_nickname = rs.getString("u_nickname").toLowerCase().replace(" ", "_");
	        String displayname = rs.getString("displayname").toLowerCase().replace(" ", "_");
	        PrologEngine.generaInserisci("follows", new String[] {u_nickname,displayname});
	      }
		rs.close();
		PrologEngine.insertEmptyLine();
	}
}