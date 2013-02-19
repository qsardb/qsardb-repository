package org.dspace.app.util;

import javax.servlet.*;

import org.dspace.content.*;
import org.dspace.core.*;

import org.qsardb.cargo.rds.Context;

import org.apache.log4j.*;

import org.rosuda.JRI.*;

public class RDSContextListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent event){

		try {
			logger.info("Starting the R engine");

			Context.startEngine(new QdbLogCallback());

			logger.info("The R engine was successfully started");
		} catch(Exception e){
			logger.error("Failed to start the R engine", e);

			return;
		}

		try {
			String scriptPath = ConfigurationManager.getProperty("dspace.dir") + "/config/qsardb.R";
			if(scriptPath.indexOf('\\') > -1){
				scriptPath = scriptPath.replace('\\', '/');
			}

			logger.info("Loading QsarDB R script file " + scriptPath);

			Rengine engine = Context.getEngine();

			engine.eval("source(\'" + scriptPath + "\')");

			logger.info("QsarDB R script was successfully loaded");
		} catch(Exception e){
			logger.error("Failed to load QsarDB R script file", e);
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