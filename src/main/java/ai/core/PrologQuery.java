package ai.core;

import org.jpl7.Query;
import org.jpl7.Term;

import java.sql.SQLException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

public class PrologQuery {
	
	public static Map<String, Term>[] SuggestMostVisited(String username)  throws IOException, SQLException
	{
		String query = "orderedScoreUserObject('"+username+"',X,Y).";
        Map<String, Term>[] res = Query.allSolutions(query);
        printResults(res, query);
		return res;
	}
	
	public static Map<String, Term>[] SuggestMostVisitedRepeat(String username)
	{
		String query = "orderedScoreUserObjectRepeat('"+username+"',X,Y).";
        Map<String, Term>[] res = Query.allSolutions(query);
        printResults(res, query);
		return res;
	}
	
	public static Map<String, Term>[] SuggestSimilarObjects(String lo) throws IOException, SQLException
	{
		String query = "orderedScoreObjectObject('"+lo+"',X,Y).";
        Map<String, Term>[] res = Query.allSolutions(query);
        printResults(res, query);
		return res;
	}
	
	public static String getLOCourse(String lo)
	{
		String query = "hasLearningObject(X,'"+lo+"').";
        Map<String, Term> res = Query.oneSolution(query);
        return PrologEngine.ToJavaString(res.get("X").toString());
	}
	
	public static String getCourseId(String course)
	{
        String query = "courseId('"+course+"',X).";
        Map<String, Term> res = Query.oneSolution(query);
        return PrologEngine.ToJavaString(res.get("X").toString());
	}

	public static String getLOId(String lo)
	{
		String query = "learningObjectId('"+lo+"',X).";
        Map<String, Term> res = Query.oneSolution(query);
        return PrologEngine.ToJavaString(res.get("X").toString());
	}
	
	public static String getLONameFromId(String id)
	{
        String query = "learningObjectId(X,"+id+").";
        Map<String, Term> res = Query.oneSolution(query);
        return PrologEngine.ToJavaString(res.get("X").toString());
	}
	
	public static String visto(String username, String lo)
	{
        String query = "(hasRead('"+username+"','"+lo+"') -> X = true; X = false).";
        Map<String, Term> res = Query.oneSolution(query);
        return PrologEngine.ToJavaString(res.get("X").toString());
	}

    public static void printResults(Map<String, Term>[] map, String query)
    {
        if(map.length==0)
        {
            System.out.println("Nessuna soluzione per "+ query);
        }
        else{
            System.out.println(Arrays.toString(map));
        }
    }

}