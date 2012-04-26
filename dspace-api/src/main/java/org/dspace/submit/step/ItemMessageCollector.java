package org.dspace.submit.step;

import java.io.*;
import java.util.*;

import org.qsardb.validation.*;

import org.dspace.content.*;
import org.dspace.core.*;

public class ItemMessageCollector implements MessageCollector, Serializable {

	private List<Message> messages = new ArrayList<Message>();


	@Override
	public void add(Message message){
		this.messages.add(message);
	}

	public boolean hasErrors(){

		for(Message message : this.messages){
			Message.Level level = message.getLevel();

			if((Message.Level.ERROR).equals(level)){
				return true;
			}
		}

		return false;
	}

	public List<Message> getMessages(){
		return Collections.unmodifiableList(this.messages);
	}

	static
	public ItemMessageCollector load(Item item) throws IOException, ClassNotFoundException {
		File file = getFile(item);
		if(!file.isFile()){
			return null;
		}

		InputStream is = new FileInputStream(file);

		try {
			ObjectInputStream ois = new ObjectInputStream(is);

			try {
				return (ItemMessageCollector)ois.readObject();
			} finally {
				ois.close();
			}
		} finally {
			is.close();
		}
	}

	static
	public void store(Item item, ItemMessageCollector collector) throws IOException {
		File file = getFile(item);

		OutputStream os = new FileOutputStream(file);

		try {
			ObjectOutputStream oos = new ObjectOutputStream(os);

			try {
				oos.writeObject(collector);
			} finally {
				oos.close();
			}
		} finally {
			os.close();
		}
	}

	static
	public File getFile(Item item){
		File tempDir = new File(ConfigurationManager.getProperty("upload.temp.dir"));

		return new File(tempDir, ItemMessageCollector.class.getSimpleName() + "-" + String.valueOf(item.getID()) + ".ser");
	}
}