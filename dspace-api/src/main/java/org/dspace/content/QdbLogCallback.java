package org.dspace.content;

import org.apache.log4j.*;

import org.rosuda.JRI.*;

public class QdbLogCallback implements RMainLoopCallbacks {

	@Override
	public void rBusy(Rengine re, int status){
	}

	@Override
	public void rShowMessage(Rengine re, String message){
		logger.warn(message);
	}

	@Override
	public String rReadConsole(Rengine re, String prompt, int options){
	    throw new UnsupportedOperationException();
    }

	@Override
	public void rWriteConsole(Rengine re, String message, int options){
		logger.info(message);
	}

	@Override
	public void rFlushConsole(Rengine re){
    }

	@Override
	public String rChooseFile(Rengine re, int options){
	    throw new UnsupportedOperationException();
    }

	@Override
	public void rLoadHistory(Rengine re, String path){
		throw new UnsupportedOperationException();
	}

	@Override
	public void rSaveHistory(Rengine re, String path){
		throw new UnsupportedOperationException();
	}

	private static final Logger logger = Logger.getLogger(QdbLogCallback.class);
}