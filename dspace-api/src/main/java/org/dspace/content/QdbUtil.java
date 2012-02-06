package org.dspace.content;

import java.sql.*;

import org.dspace.core.*;

public class QdbUtil {

	private QdbUtil(){
	}

	static
	public BitstreamFormat getBitstreamFormat(Context context) throws SQLException {
		BitstreamFormat format = BitstreamFormat.findByShortDescription(context, "QsarDB");
		if(format == null){
			throw new QdbConfigurationException("QsarDB bitstream format not found");
		}

		return format;
	}
}