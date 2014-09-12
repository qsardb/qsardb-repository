package org.dspace.qsardb.client.gwt;

import java.math.*;
import java.util.*;

import org.dspace.qsardb.rpc.gwt.*;

import com.google.gwt.cell.client.*;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.cellview.client.*;

public class CompoundDataGrid extends DataGrid<Compound> implements SeriesDisplayEventHandler {

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
			if (prediction.isNumeric()) {
				addColumn(new PredictionErrorTextColumn(prediction), "Error");
			}
		}

		LeverageColumn leverage = table.getColumn(LeverageColumn.class);
		if(leverage != null){
			addColumn(new LeverageTextColumn(leverage), "Leverage");
		}

		MahalanobisDistanceColumn mahalanobisDistance = table.getColumn(MahalanobisDistanceColumn.class);
		if(mahalanobisDistance != null){
			addColumn(new MahalanobisDistanceTextColumn(mahalanobisDistance), "Mahalanobis distance");
		}

		List<DescriptorColumn> descriptors = table.getAllColumns(DescriptorColumn.class);
		for(DescriptorColumn descriptor : descriptors){
			addColumn(new DescriptorTextColumn(descriptor), new DescriptorTextHeader(descriptor));
		}

		addEmptyColumn(-1);

		// Provides coverage for the right-side scroll bar
		addEmptyColumn(20);
	}

	private void addEmptyColumn(int width){
		TextColumn<Compound> column = new TextColumn<Compound>(){

			@Override
			public String getValue(Compound compound){
				return null;
			}
		};
		addColumn(column, (String)null);

		if(width > -1){
			setColumnWidth(column, width, Unit.PX);
		}
	}

	@Override
	public void onSeriesVisibilityChanged(SeriesDisplayEvent event){
		Set<PredictionColumn> visiblePredictions = event.getValues(Boolean.TRUE);

		for(int i = 0; i < getColumnCount(); i++){
			Column<Compound, ?> column = getColumn(i);

			PredictionColumn prediction = null;

			if(column instanceof PredictionTextColumn){
				prediction = ((PredictionTextColumn)column).getColumn();
			} else

			if(column instanceof PredictionErrorTextColumn){
				prediction = ((PredictionErrorTextColumn)column).getColumn();
			} else

			{
				continue;
			}

			setVisible((CompoundTextColumn<?, ?>)column, visiblePredictions.contains(prediction));
		}
	}

	@Override
	public void insertColumn(int index, Column<Compound, ?> column, Header<?> header, Header<?> footer){
		super.insertColumn(index, column, header, footer);

		if(column instanceof CompoundTextColumn){
			resizeColumn((CompoundTextColumn<?, ?>)column);
		}
	}

	private void resizeColumn(CompoundTextColumn<?, ?> column){
		int length = column.getLength();

		if(length > 0){
			length = Math.min(Math.max(length, 2), 60);

			// XXX: Cut the horizontal padding from 15 px to 5 px
			setColumnWidth(column, 15 + (length * 6) + 15, Unit.PX);
		}
	}

	private void setVisible(CompoundTextColumn<?, ?> column, boolean visible){

		if(column.isVisible() == visible){
			return;
		} // End if

		if(visible){
			resizeColumn(column);
		} else

		{
			setColumnWidth(column, 0, Unit.PX);
		}

		column.setVisible(visible);
	}

	public Comparator<Compound> getComparator(Column<?, ?> column){

		if(column instanceof CompoundTextColumn){
			return getComparator((CompoundTextColumn<?, ?>)column);
		}

		return null;
	}

	private Comparator<Compound> getComparator(CompoundTextColumn<?, ?> column){
		return column.getComparator();
	}

	static
	abstract
	public class CompoundTextColumn <C extends QdbColumn<V>, V> extends Column<Compound, String> {

		private C column = null;

		private boolean visible = true;


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

		private boolean isVisible(){
			return this.visible;
		}

		private void setVisible(boolean visible){
			this.visible = visible;
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
	public class NumericTextColumn <C extends NumericColumn> extends CompoundTextColumn<C, Object> {

		public NumericTextColumn(Cell<String> cell, C column){
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
					return NumericTextColumn.this.compare(getObject(left), getObject(right));
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

	abstract
	static
	public class DistanceTextColumn <C extends DistanceColumn> extends NumericTextColumn<C> {

		public DistanceTextColumn(Cell<String> cell, C column){
			super(cell, column);
		}
	}

	abstract
	static
	public class ParameterTextColumn <C extends ParameterColumn> extends NumericTextColumn<C> {

		public ParameterTextColumn(Cell<String> cell, C column){
			super(cell, column);
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
	public class LeverageTextColumn extends DistanceTextColumn<LeverageColumn> {

		public LeverageTextColumn(LeverageColumn column){
			super(new TextCell(), column);
		}
	}

	static
	public class MahalanobisDistanceTextColumn extends DistanceTextColumn<MahalanobisDistanceColumn> {

		public MahalanobisDistanceTextColumn(MahalanobisDistanceColumn column){
			super(new TextCell(), column);
		}
	}

	static
	public class DescriptorTextColumn extends ParameterTextColumn<DescriptorColumn> {

		public DescriptorTextColumn(DescriptorColumn column){
			super(new TextCell(), column);
		}

		@Override
		public Comparator<Compound> getComparator(){
			ParameterUtil.ensureConverted(getColumn());

			return super.getComparator();
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

			return parameter.getId();
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