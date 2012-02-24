package org.dspace.gwt.client;

import java.math.*;
import java.util.*;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.*;
import com.google.gwt.user.cellview.client.*;

import org.dspace.gwt.rpc.*;

public class CompoundDataGrid extends DataGrid<Compound> {

	public CompoundDataGrid(List<QdbColumn<?>> columns){
		super(50);

		TableSectionElement element = getTableBodyElement();
		element.setAttribute("style", "white-space: nowrap;"); // XXX

		addColumn(new IdentifierTextColumn(), "Id");

		NameColumn name = filter(NameColumn.class, columns);
		addColumn(new AttributeTextColumn(name), "Name");

		CasColumn cas = filter(CasColumn.class, columns);
		if(cas != null){
			addColumn(new CasTextColumn(cas), "CAS");
		}

		PropertyColumn property = filter(PropertyColumn.class, columns);
		addColumn(new ParameterTextColumn(property), property.getName());

		List<PredictionColumn> predictions = filterList(PredictionColumn.class, columns);
		for(PredictionColumn prediction : predictions){
			addColumn(new ParameterTextColumn(prediction), prediction.getName());
			addColumn(new ErrorTextColumn(property, prediction), "Error");
		}

		List<DescriptorColumn> descriptors = filterList(DescriptorColumn.class, columns);
		for(DescriptorColumn descriptor : descriptors){
			addColumn(new ParameterTextColumn(descriptor), descriptor.getName());
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
				setColumnWidth(column, length * 6, Unit.PT);
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
	private <C extends QdbColumn<?>> C filter(Class<C> clazz, List<QdbColumn<?>> columns){
		List<C> result = filterList(clazz, columns);

		if(result.size() == 1){
			return result.get(0);
		}

		return null;
	}

	@SuppressWarnings (
		value = {"unchecked"}
	)
	static
	private <C extends QdbColumn<?>> List<C> filterList(Class<C> clazz, List<QdbColumn<?>> columns){
		List<C> result = new ArrayList<C>();

		for(QdbColumn<?> column : columns){

			if((column.getClass()).equals(clazz)){
				result.add((C)column);
			}
		}

		return result;
	}

	static
	abstract
	public class CompoundTextColumn extends TextColumn<Compound> {

		public CompoundTextColumn(){
			setSortable(true);
		}

		abstract
		public int getLength();

		abstract
		public Comparator<Compound> getComparator();

		protected int compare(Object left, Object right){

			if(left == null || right == null){
				return (left != null ? 1 : 0) - (right != null ? 1 : 0);
			}

			return ((Comparable)left).compareTo((Comparable)right);
		}
	}

	static
	public class IdentifierTextColumn extends CompoundTextColumn {

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
	public class AttributeTextColumn extends CompoundTextColumn {

		private AttributeColumn attribute = null;


		public AttributeTextColumn(AttributeColumn attribute){
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
			int length = 0;

			Collection<String> values = (getAttribute().getValues()).values();
			for(String value : values){

				if(value != null){
					length = Math.max(value.length(), length);
				}
			}

			return length;
		}

		public AttributeColumn getAttribute(){
			return this.attribute;
		}

		private void setAttribute(AttributeColumn attribute){
			this.attribute = attribute;
		}
	}

	static
	public class CasTextColumn extends AttributeTextColumn {

		public CasTextColumn(CasColumn attribute){
			super(attribute);
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
	public class ParameterTextColumn extends CompoundTextColumn {

		private ParameterColumn parameter = null;


		public ParameterTextColumn(ParameterColumn parameter){
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
			return 6;
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
	public class ErrorTextColumn extends CompoundTextColumn {

		private PropertyColumn property = null;

		private PredictionColumn prediction = null;


		public ErrorTextColumn(PropertyColumn property, PredictionColumn prediction){
			setProperty(property);
			setPrediction(prediction);
		}

		@Override
		public String getValue(Compound compound){
			BigDecimal error = getError(compound);

			return (error != null ? error.toString() : null);
		}

		public BigDecimal getError(Compound compound){
			Object expValue = getProperty().getValue(compound.getId());

			if(expValue instanceof BigDecimal){
				Object calcValue = getPrediction().getValue(compound.getId());

				if(calcValue instanceof BigDecimal){
					return ((BigDecimal)calcValue).subtract((BigDecimal)expValue);
				}
			}

			return null;
		}

		@Override
		public int getLength(){
			return 6;
		}

		@Override
		public Comparator<Compound> getComparator(){
			return new Comparator<Compound>(){

				@Override
				public int compare(Compound left, Compound right){
					return ErrorTextColumn.this.compare(getError(left), getError(right));
				}
			};
		}

		public PropertyColumn getProperty(){
			return this.property;
		}

		private void setProperty(PropertyColumn property){
			this.property = property;
		}

		public PredictionColumn getPrediction(){
			return this.prediction;
		}

		private void setPrediction(PredictionColumn prediction){
			this.prediction = prediction;
		}
	}
}