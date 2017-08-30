package org.dspace.submit.step;

import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.jbibtex.*;

import org.dspace.app.util.*;
import org.dspace.content.*;
import org.dspace.core.*;

public class QdbDescribeStep extends DescribeStep {

	public QdbDescribeStep() throws ServletException {
		super();
	}

	@Override
	public DCInput[] filterInputs(Context context, HttpServletRequest request, HttpServletResponse response, SubmissionInfo submissionInfo, DCInput[] inputs){
		Item item = submissionInfo.getSubmissionItem().getItem();

		return filterBibTeXInput(item, inputs);
	}

	static
	public DCInput[] filterBibTeXInput(Item item, DCInput[] inputs){

		// XXX
		if(!checkPage(inputs)){
			return inputs;
		}

		Key type = BibTeXUtil.getType(item);
		if(type == null){
			throw new IllegalArgumentException();
		}

		List<Key> fields = BibTeXUtil.getFields(type);
		if(fields == null){
			throw new IllegalArgumentException(type.getValue());
		}

		return filterInputs(inputs, fields);
	}

	static
	private boolean checkPage(DCInput[] inputs){

		for(DCInput input : inputs){
			boolean target = (("bibtex").equals(input.getSchema()) && ("entry").equals(input.getElement()) && input.getQualifier() != null);

			if(!target){
				return false;
			}
		}

		return true;
	}

	static
	private DCInput[] filterInputs(DCInput[] inputs, List<Key> fields){
		java.util.List<DCInput> result = new ArrayList<DCInput>();

		KeyMap<DCInput> map = new KeyMap<DCInput>();

		for(DCInput input : inputs){
			map.put(new Key(input.getQualifier()), input);
		}

		for(Key field : fields){
			DCInput input = map.get(field);

			if(input != null){
				result.add(input);
			}
		}

		return result.toArray(new DCInput[result.size()]);
	}
}