package org.dspace.content;

import java.util.*;

import org.qsardb.cargo.rds.*;
import org.qsardb.evaluation.*;
import org.qsardb.model.*;

import org.apache.log4j.*;

import org.rosuda.JRI.*;

public class QdbRDSEvaluator extends RDSEvaluator {

	public QdbRDSEvaluator(Qdb qdb, RDSObject object){
		super(qdb, object);
	}

	@Override
	public void init() throws Exception {
		super.init();
	}

	@Override
	public Result evaluate(Map<Descriptor, ?> parameters) throws Exception {
		safeEval(".qsardb.beforeEvaluate()");

		try {
			return super.evaluate(parameters);
		} finally {
			safeEval(".qsardb.afterEvaluate()");
		}
	}

	@Override
	public void destroy() throws Exception {
		super.destroy();
	}

	private void safeEval(String string){

		try {
			eval(string);
		} catch(Exception e){
			logger.warn("Evaluation failed", e);
		}
	}

	private void eval(String string) throws Exception {
		Rengine engine = Context.getEngine();

		REXP result = engine.eval(string);

		switch(result.getType()){
			case REXP.XT_STR:
				log(result.asString());
				break;
			case REXP.XT_ARRAY_STR:
				log(result.asStringArray());
				break;
			default:
				break;
		}
	}

	static
	private void log(String... strings){

		if(strings == null){
			return;
		}

		for(String string : strings){
			logger.debug(string);
		}
	}

	private static final Logger logger = Logger.getLogger(QdbRDSEvaluator.class);
}