package ai.core;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.logging.log4j.Logger;
import org.jpl7.Query;
import org.olat.core.commons.services.scheduler.JobWithDB;
import org.olat.core.logging.Tracing;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@DisallowConcurrentExecution
public class RefreshPrologJob extends JobWithDB {
	
	private static final Logger log = Tracing.createLoggerFor(RefreshPrologJob.class);

	@Override
	public void executeWithDB(JobExecutionContext arg0) throws JobExecutionException {
		log.info("Refresh Prolog Job Started");
		
		try {
			PrologEngine.getInstance();
			PrologEngine.resetFile();
			DBScraper.PrepareProlog();
			Query.oneSolution("make.");
		}
		catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		log.info("Refresh Prolog Job Finished");
	}
	
}
