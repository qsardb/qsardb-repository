package org.dspace.service.http;

import java.util.*;

import org.qsardb.model.*;

import org.dspace.content.*;

abstract
public class QdbContentCallable<X> implements QdbCallable<X> {

	private String path = null;


	public QdbContentCallable(String path){
		setPath(path);
	}

	abstract
	public X call(Object object) throws Exception;

	@Override
	public X call(Qdb qdb) throws Exception {
		StringTokenizer st = new StringTokenizer(getPath(), "/");

		Object object = getObject(qdb, st);

		return call(object);
	}

	public String getPath(){
		return this.path;
	}

	private void setPath(String path){
		this.path = path;
	}

	static
	private Object getObject(Qdb qdb, StringTokenizer st){

		if(st.hasMoreTokens()){
			ContainerRegistry<?, ?> containerRegistry = getContainerRegistry(qdb, st.nextToken());

			if(st.hasMoreTokens()){
				Container<?, ?> container = getContainer(containerRegistry, st.nextToken());

				if(st.hasMoreTokens()){
					Cargo<?> cargo = getCargo(container, st.nextToken());

					if(st.hasMoreTokens()){
						throw new IllegalArgumentException();
					}

					return cargo;
				}

				return container;
			}

			return containerRegistry;
		}

		return null;
	}

	static
	private ContainerRegistry<?, ?> getContainerRegistry(Qdb qdb, String id){

		if("compounds".equals(id)){
			return qdb.getCompoundRegistry();
		} else

		if("properties".equals(id)){
			return qdb.getPropertyRegistry();
		} else

		if("descriptors".equals(id)){
			return qdb.getDescriptorRegistry();
		} else

		if("models".equals(id)){
			return qdb.getModelRegistry();
		} else

		if("predictions".equals(id)){
			return qdb.getPredictionRegistry();
		}

		throw new IllegalArgumentException(id);
	}

	static
	private Container<?, ?> getContainer(ContainerRegistry<?, ?> containerRegistry, String id){
		Container<?, ?> container = containerRegistry.get(id);

		if(container != null){
			return container;
		}

		throw new IllegalArgumentException(id);
	}

	static
	private Cargo<?> getCargo(Container<?, ?> container, String id){

		if(container.hasCargo(id)){
			return container.getCargo(id);
		}

		throw new IllegalArgumentException(id);
	}
}