package org.dspace.gwt.client;

import java.math.*;
import java.util.*;

import com.google.gwt.cell.client.*;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.*;

import org.dspace.gwt.rpc.*;

public class CompoundDataGrid extends DataGrid<Compound> {

	public CompoundDataGrid(QdbTable table){
		super(50);

		Resolver resolver = new Resolver(table);

		IdColumn id = table.getColumn(IdColumn.class);
		addColumn(new IdTextColumn(id), "Id");

		NameColumn name = table.getColumn(NameColumn.class);
		addColumn(new NameTextColumn(resolver, name), "Name");

		CasColumn cas = table.getColumn(CasColumn.class);
		if(cas != null){
			addColumn(new CasTextColumn(cas), "CAS");
		}

		PropertyColumn property = table.getColumn(PropertyColumn.class);
		addColumn(new PropertyTextColumn(property), new PropertyTextHeader(property));

		List<PredictionColumn> predictions = table.getAllColumns(PredictionColumn.class);
		for(PredictionColumn prediction : predictions){
			addColumn(new PredictionTextColumn(prediction), new PredictionTextHeader(prediction));
			addColumn(new PredictionErrorTextColumn(prediction), "Error");
		}

		List<DescriptorColumn> descriptors = table.getAllColumns(DescriptorColumn.class);
		for(DescriptorColumn descriptor : descriptors){
			addColumn(new DescriptorTextColumn(descriptor), new DescriptorTextHeader(descriptor));
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

		setColumnWidth(column, 20, Unit.PX);
	}

	@Override
	public void insertColumn(int index, Column<Compound, ?> column, Header<?> header, Header<?> footer){
		super.insertColumn(index, column, header, footer);

		if(column instanceof CompoundTextColumn){
			CompoundTextColumn<?, ?> sortableColumn = (CompoundTextColumn<?, ?>)column;

			int length = sortableColumn.getLength();

			if(length > 0){
				length = Math.min(Math.max(length, 2), 60);

				// XXX: Cut the horizontal padding from 15 px to 5 px
				setColumnWidth(column, 15 + (length * 6) + 15, Unit.PX);
			}
		}
	}

	public Comparator<Compound> getComparator(Column<?, ?> column){

		if(column instanceof CompoundTextColumn){
			CompoundTextColumn<?, ?> sortableColumn = (CompoundTextColumn<?, ?>)column;

			return sortableColumn.getComparator();
		}

		return null;
	}

	static
	abstract
	public class CompoundTextColumn <C extends QdbColumn<V>, V> extends Column<Compound, String> {

		private C column = null;


		protected CompoundTextColumn(Cell<String> cell, C column){
			super(cell);

			setColumn(column);

			setSortable(true);
		}

		public int getLength(){
			return getColumn().getLength();
		}

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

		public C getColumn(){
			return this.column;
		}

		private void setColumn(C column){
			this.column = column;
		}
	}

	static
	public class IdTextColumn extends AttributeTextColumn<IdColumn> {


		public IdTextColumn(IdColumn column){
			super(new TextCell(), column);
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
	public class AttributeTextColumn <C extends AttributeColumn> extends CompoundTextColumn<C, String> {

		public AttributeTextColumn(Cell<String> cell, C column){
			super(cell, column);
		}

		@Override
		public String getValue(Compound compound){
			return getColumn().getValue(compound.getId());
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
	}

	static
	public class NameTextColumn extends AttributeTextColumn<NameColumn> {

		public NameTextColumn(Resolver resolver, NameColumn column){
			super(new ResolverTooltipCell(resolver), column);
		}
	}

	static
	public class CasTextColumn extends AttributeTextColumn<CasColumn> {

		public CasTextColumn(CasColumn column){
			super(new TextCell(), column);
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
	public class ParameterTextColumn <C extends ParameterColumn> extends CompoundTextColumn<C, Object> {

		public ParameterTextColumn(Cell<String> cell, C column){
			super(cell, column);
		}

		@Override
		public String getValue(Compound compound){
			Object object = getObject(compound);

			return (object != null ? object.toString() : null);
		}

		public Object getObject(Compound compound){
			return getColumn().getValue(compound.getId());
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
	}

	static
	public class PropertyTextColumn extends ParameterTextColumn<PropertyColumn> {

		public PropertyTextColumn(PropertyColumn column){
			super(new TextCell(), column);
		}
	}

	static
	public class PredictionTextColumn extends ParameterTextColumn<PredictionColumn> {

		public PredictionTextColumn(PredictionColumn column){
			super(new TextCell(), column);
		}
	}

	static
	public class PredictionErrorTextColumn extends CompoundTextColumn<PredictionColumn, Object> {

		public PredictionErrorTextColumn(PredictionColumn column){
			super(new TextCell(), column);
		}

		@Override
		public String getValue(Compound compound){
			BigDecimal error = getError(compound);

			return (error != null ? error.toString() : null);
		}

		public BigDecimal getError(Compound compound){
			return getColumn().getError(compound.getId());
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
	}

	static
	public class DescriptorTextColumn extends ParameterTextColumn<DescriptorColumn> {

		public DescriptorTextColumn(DescriptorColumn column){
			super(new TextCell(), column);
		}
	}

	static
	abstract
	public class ParameterTextHeader extends Header<String> {

		private ParameterColumn parameter = null;


		public ParameterTextHeader(ParameterColumn parameter){
			super(new ParameterTooltipCell(parameter));

			setParameter(parameter);
		}

		@Override
		public String getValue(){
			ParameterColumn parameter = getParameter();

			return parameter.getName();
		}

		public ParameterColumn getParameter(){
			return this.parameter;
		}

		private void setParameter(ParameterColumn parameter){
			this.parameter = parameter;
		}
	}

	static
	public class PropertyTextHeader extends ParameterTextHeader {

		public PropertyTextHeader(PropertyColumn property){
			super(property);
		}
	}

	static
	public class PredictionTextHeader extends ParameterTextHeader {

		public PredictionTextHeader(PredictionColumn prediction){
			super(prediction);
		}
	}

	static
	public class DescriptorTextHeader extends ParameterTextHeader {

		public DescriptorTextHeader(DescriptorColumn descriptor){
			super(descriptor);
		}
	}
}