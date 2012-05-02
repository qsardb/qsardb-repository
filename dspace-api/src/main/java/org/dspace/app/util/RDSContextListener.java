package org.dspace.app.util;

import javax.servlet.*;

import org.qsardb.cargo.rds.*;

import org.apache.log4j.*;

public class RDSContextListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent event){

		try {
			logger.info("Starting the R engine");

			Context.startEngine();

			logger.info("The R engine was successfully started");
		} catch(Exception e){
			logger.error("Failed to start the R engine", e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent event){

		try {
			logger.info("Stopping the R engine");

			Context.stopEngine();

			logger.info("The R engine was successfully stopped");
		} catch(Exception e){
			logger.error("Failed to stop the R engine", e);
		}
	}

	private static final Logger logger = Logger.getLogger(RDSContextListener.class);
}