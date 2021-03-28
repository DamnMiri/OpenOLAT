package ai.core;

import org.jpl7.Query;
import org.olat.core.util.WebappHelper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

public class PrologEngine {
	
	private static File f;
	
	private static PrologEngine instance = null;
	
	private PrologEngine()
	{
		System.out.println("Inizializzazione Prolog");
        String style_singleton = "style_check(-singleton).";
        Query.hasSolution(style_singleton);
        Query.hasSolution("consult('"+getUserDataPath()+"/fatti.pl')");
        Query.hasSolution("consult('"+getUserDataPath()+"/predicati.pl')");
        Query.hasSolution("consult('"+getUserDataPath()+"/relazioni.pl')");
		System.out.println("Inizializzazione Prolog Terminata");
	}
	
	public static PrologEngine getInstance()
	{
		if(instance==null)
		{
			instance = new PrologEngine();
		}
		return instance;
	}
	
	public static String getUserDataPath()
	{
		return WebappHelper.getUserDataRoot().replace("\\", "/");
	}
	
	public static void resetFile() throws IOException
	{
		f = new File(getUserDataPath()+"/fatti.pl");
        f.createNewFile();
		FileWriter fw = new FileWriter(f, false);
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter pw = new PrintWriter(bw);
        pw.println();
        pw.close();
        bw.close();
        fw.close();
	}
    
    public static void generaInserisci(String predicato, String[] variabili) throws IOException
    {
		FileWriter fw = new FileWriter(f, true);
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter pw = new PrintWriter(bw);
        pw.println(generaFatto(predicato, variabili));
        pw.close();
        bw.close();
        fw.close();
    }
    
    public static void insertEmptyLine() throws IOException
    {
		FileWriter fw = new FileWriter(f, true);
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter pw = new PrintWriter(bw);
        pw.println("");
        pw.close();
        bw.close();
        fw.close();
    }
    
    public static String generaFatto(String predicato, String[] variabili)
    {
        String vars="";
        for (String s: variabili) {
            vars = vars.concat(s+",");
        }
        vars = vars.substring(0, vars.length() - 1);
        return predicato.concat("(").concat(vars).concat(").");
    }
    
    public static String ToJavaString(String prologString)
    {
    	if(prologString.charAt(0) == '\'' && prologString.charAt(prologString.length()-1) == '\'')
    	{
    		prologString = prologString.substring(1,prologString.length()-1);
    	}
    	return prologString;
    }
    
}