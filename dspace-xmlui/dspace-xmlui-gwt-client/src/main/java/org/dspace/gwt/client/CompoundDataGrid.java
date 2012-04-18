package org.dspace.gwt.client;

import java.math.*;
import java.util.*;

import com.google.gwt.cell.client.*;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.*;
import com.google.gwt.user.cellview.client.*;

import org.dspace.gwt.rpc.*;

public class CompoundDataGrid extends DataGrid<Compound> {

	public CompoundDataGrid(QdbTable table){
		super(50);

		TableSectionElement body = getTableBodyElement();
		(body.getStyle()).setProperty("white-space", "nowrap"); // XXX

		Resolver resolver = new Resolver(table);

		addColumn(new IdentifierTextColumn(), "Id");

		NameColumn name = table.getColumn(NameColumn.class);
		addColumn(new NameTextColumn(resolver, name), "Name");

		CasColumn cas = table.getColumn(CasColumn.class);
		if(cas != null){
			addColumn(new CasTextColumn(cas), "CAS");
		}

		PropertyColumn property = table.getColumn(PropertyColumn.class);
		addColumn(new PropertyTextColumn(property), property.getName());

		List<PredictionColumn> predictions = table.getAllColumns(PredictionColumn.class);
		for(PredictionColumn prediction : predictions){
			addColumn(new PredictionTextColumn(prediction), prediction.getName());
			addColumn(new PredictionErrorTextColumn(prediction), "Error");
		}

		List<DescriptorColumn> descriptors = table.getAllColumns(DescriptorColumn.class);
		for(DescriptorColumn descriptor : descriptors){
			addColumn(new DescriptorTextColumn(descriptor), descriptor.getName());
		}

		addEmptyColumn();
	}

	private void addEmptyColumn(){
		TextColumn<Compound> column = new TextColumn<Compound>(){

			@Override
			public String getValue(Compound compound){
				return null;
			}
		};
		addColumn(column, (String)null);

		setColumnWidth(column, 20, Unit.PT);
	}

	@Override
	public void insertColumn(int index, Column<Compound, ?> column, Header<?> header, Header<?> footer){
		super.insertColumn(index, column, header, footer);

		if(column instanceof CompoundTextColumn){
			int length = ((CompoundTextColumn)column).getLength();

			if(length > 0){
				setColumnWidth(column, Math.min(Math.max(length, 2), 60) * 8, Unit.PT);
			}
		}
	}

	public Comparator<Compound> getComparator(Column<?, ?> column){

		if(column instanceof CompoundTextColumn){
			CompoundTextColumn sortableColumn = (CompoundTextColumn)column;

			return sortableColumn.getComparator();
		}

		return null;
	}

	static
	abstract
	public class CompoundTextColumn extends Column<Compound, String> {

		protected CompoundTextColumn(Cell<String> cell){
			super(cell);

			setSortable(true);
		}

		abstract
		public int getLength();

		abstract
		public Comparator<Compound> getComparator();

		@SuppressWarnings (
			value = {"cast", "rawtypes", "unchecked"}
		)
		protected int compare(Object left, Object right){

			if(left == null || right == null){
				return (left != null ? 1 : 0) - (right != null ? 1 : 0);
			} // End if

			if(left instanceof String && right instanceof String){
				return ((String)left).compareToIgnoreCase((String)right);
			}

			return ((Comparable)left).compareTo((Comparable)right);
		}

		static
		protected int getLength(Collection<?> values){
			int length = 0;

			for(Object value : values){

				if(value != null){
					length = Math.max((value.toString()).length(), length);
				}
			}

			return length;
		}
	}

	static
	public class IdentifierTextColumn extends CompoundTextColumn {


		public IdentifierTextColumn(){
			super(new TextCell());
		}

		@Override
		public String getValue(Compound compound){
			return compound.getId();
		}

		@Override
		public int getLength(){
			return 4; // XXX
		}

		@Override
		public Comparator<Compound> getComparator(){
			return new Comparator<Compound>(){

				@Override
				public int compare(Compound left, Compound right){
					return IdentifierTextColumn.this.compare(left.getId(), right.getId());
				}
			};
		}

		@Override
		protected int compare(Object left, Object right){

			if(left instanceof String && right instanceof String){
				int diff = ((String)left).length() - ((String)right).length();

				if(diff != 0){
					return diff;
				}
			}

			return super.compare(left, right);
		}
	}

	static
	abstract
	public class AttributeTextColumn extends CompoundTextColumn {

		private AttributeColumn attribute = null;


		public AttributeTextColumn(Cell<String> cell, AttributeColumn attribute){
			super(cell);

			setAttribute(attribute);
		}

		@Override
		public String getValue(Compound compound){
			return getAttribute().getValue(compound.getId());
		}

		@Override
		public Comparator<Compound> getComparator(){
			return new Comparator<Compound>(){

				@Override
				public int compare(Compound left, Compound right){
					return AttributeTextColumn.this.compare(getValue(left), getValue(right));
				}
			};
		}

		@Override
		public int getLength(){
			Map<String, String> values = getAttribute().getValues();

			return CompoundTextColumn.getLength(values.values());
		}

		public AttributeColumn getAttribute(){
			return this.attribute;
		}

		private void setAttribute(AttributeColumn attribute){
			this.attribute = attribute;
		}
	}

	static
	public class NameTextColumn extends AttributeTextColumn {

		public NameTextColumn(Resolver resolver, NameColumn attribute){
			super(new ResolverTextCell(resolver), attribute);
		}

		@Override
		public int getLength(){
			return (super.getLength() * 2) / 3;
		}
	}

	static
	public class CasTextColumn extends AttributeTextColumn {

		public CasTextColumn(CasColumn attribute){
			super(new TextCell(), attribute);
		}

		@Override
		protected int compare(Object left, Object right){

			if(left instanceof String && right instanceof String){
				int[] leftParts = parse((String)left);
				int[] rightParts = parse((String)right);

				for(int i = 0; i < 3; i++){
					int diff = (leftParts[i] - rightParts[i]);

					if(diff != 0){
						return diff;
					}
				}

				return 0;
			}

			return super.compare(left, right);
		}

		static
		private int[] parse(String string){
			String[] parts = string.split("-");

			if(parts.length != 3){
				throw new IllegalArgumentException(string);
			}

			return new int[]{
				Integer.parseInt(parts[0]),
				Integer.parseInt(parts[1]),
				Integer.parseInt(parts[2]),
			};
		}
	}

	static
	abstract
	public class ParameterTextColumn extends CompoundTextColumn {

		private ParameterColumn parameter = null;


		public ParameterTextColumn(Cell<String> cell, ParameterColumn parameter){
			super(cell);

			setParameter(parameter);
		}

		@Override
		public String getValue(Compound compound){
			Object object = getObject(compound);

			return (object != null ? object.toString() : null);
		}

		public Object getObject(Compound compound){
			return getParameter().getValue(compound.getId());
		}

		@Override
		public int getLength(){
			Map<String, Object> values = getParameter().getValues();

			return ParameterTextColumn.getLength(values.values());
		}

		@Override
		public Comparator<Compound> getComparator(){
			return new Comparator<Compound>(){

				@Override
				public int compare(Compound left, Compound right){
					return ParameterTextColumn.this.compare(getObject(left), getObject(right));
				}
			};
		}

		@Override
		protected int compare(Object left, Object right){

			if(left instanceof BigDecimal && right instanceof String){
				return 1;
			} else

			if(left instanceof String && right instanceof BigDecimal){
				return -1;
			}

			return super.compare(left, right);
		}

		public ParameterColumn getParameter(){
			return this.parameter;
		}

		private void setParameter(ParameterColumn parameter){
			this.parameter = parameter;
		}
	}

	static
	public class PropertyTextColumn extends ParameterTextColumn {

		public PropertyTextColumn(PropertyColumn property){
			super(new TextCell(), property);
		}
	}

	static
	public class PredictionTextColumn extends ParameterTextColumn {

		public PredictionTextColumn(PredictionColumn prediction){
			super(new TextCell(), prediction);
		}
	}

	static
	public class PredictionErrorTextColumn extends CompoundTextColumn {

		private PredictionColumn prediction = null;


		public PredictionErrorTextColumn(PredictionColumn prediction){
			super(new TextCell());

			setPrediction(prediction);
		}

		@Override
		public String getValue(Compound compound){
			BigDecimal error = getError(compound);

			return (error != null ? error.toString() : null);
		}

		public BigDecimal getError(Compound compound){
			return getPrediction().getError(compound.getId());
		}

		@Override
		public int getLength(){
			Map<String, BigDecimal> values = getPrediction().getErrors();

			return ParameterTextColumn.getLength(values.values());
		}

		@Override
		public Comparator<Compound> getComparator(){
			return new Comparator<Compound>(){

				@Override
				public int compare(Compound left, Compound right){
					return PredictionErrorTextColumn.this.compare(getError(left), getError(right));
				}
			};
		}

		public PredictionColumn getPrediction(){
			return this.prediction;
		}

		private void setPrediction(PredictionColumn prediction){
			this.prediction = prediction;
		}
	}

	static
	public class DescriptorTextColumn extends ParameterTextColumn {

		public DescriptorTextColumn(DescriptorColumn descriptor){
			super(new TextCell(), descriptor);
		}
	}
}