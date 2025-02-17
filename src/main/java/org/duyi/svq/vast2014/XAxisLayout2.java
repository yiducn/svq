/**
 * 
 */
package org.duyi.svq.vast2014;

import java.awt.geom.Rectangle2D;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Iterator;

import prefuse.Constants;
import prefuse.action.layout.Layout;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.expression.Predicate;
import prefuse.data.query.NumberRangeModel;
import prefuse.data.query.ObjectRangeModel;
import prefuse.data.tuple.TupleSet;
import prefuse.util.DataLib;
import prefuse.util.MathLib;
import prefuse.util.ui.ValuedRangeModel;
import prefuse.visual.VisualItem;

/**
 * @author	duyi
 * @date	2014年6月23日
 */
public class XAxisLayout2 extends Layout {
	public static final int TYPE_TIME = 4;

    private String m_field;
    private int m_scale = Constants.LINEAR_SCALE;
    private int m_axis = Constants.X_AXIS;
    private int m_type = Constants.UNKNOWN;
    
    // visible region of the layout (in item coordinates)
    // if false, the table will be consulted
    private boolean m_modelSet = false;
    private ValuedRangeModel m_model = null;
    private Predicate m_filter = null;
    
    // screen coordinate range
    private double m_min;
    private double m_range;
    
    // value range / distribution
    private double[] m_dist = new double[2];
    
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    /**
     * Create a new AxisLayout. Defaults to using the x-axis.
     * @param group the data group to layout
     * @param field the data field upon which to base the layout
     */
    public XAxisLayout2(String group, String field) {
        super(group);
        m_field = field;
    }
    
    /**
     * Create a new AxisLayout.
     * @param group the data group to layout
     * @param field the data field upon which to base the layout
     * @param axis the axis type, either {@link prefuse.Constants#X_AXIS}
     * or {@link prefuse.Constants#Y_AXIS}.
     */
    public XAxisLayout2(String group, String field, int axis) {
        this(group, field);
        setAxis(axis);
    }
    
    /**
     * Create a new AxisLayout.
     * @param group the data group to layout
     * @param field the data field upon which to base the layout
     * @param axis the axis type, either {@link prefuse.Constants#X_AXIS}
     * or {@link prefuse.Constants#Y_AXIS}.
     * @param filter an optional predicate filter for limiting which items
     * to layout.
     */
    public XAxisLayout2(String group, String field, int axis, Predicate filter) {
        this(group, field, axis);
        setFilter(filter);
    }

    // ------------------------------------------------------------------------
    
    /**
     * Set the data field used by this axis layout action. The values of the
     * data field will determine the position of items along the axis. Note
     * that this method does not affect the other parameters of this action. In
     * particular, clients that have provided a custom range model for
     * setting the axis range may need to appropriately update the model
     * setting for use with the new data field setting.
     * @param field the name of the data field that determines the layout
     */
    public void setDataField(String field) {
        m_field = field;
        if ( !m_modelSet )
            m_model = null;
    }
    
    /**
     * Get the data field used by this axis layout action. The values of the
     * data field determine the position of items along the axis.
     * @return the name of the data field that determines the layout
     */
    public String getDataField() {
        return m_field;
    }
    
    /**
     * Set the range model determing the span of the axis. This model controls
     * the minimum and maximum values of the layout, as provided by the
     * {@link prefuse.util.ui.ValuedRangeModel#getLowValue()} and
     * {@link prefuse.util.ui.ValuedRangeModel#getHighValue()} methods.
     * @param model the range model for the axis.
     */
    public void setRangeModel(ValuedRangeModel model) {
        m_model = model;
        m_modelSet = (model != null);
    }
    
    /**
     * Get the range model determing the span of the axis. This model controls
     * the minimum and maximum values of the layout, as provided by the
     * {@link prefuse.util.ui.ValuedRangeModel#getLowValue()} and
     * {@link prefuse.util.ui.ValuedRangeModel#getHighValue()} methods.
     * @return the range model for the axis.
     */
    public ValuedRangeModel getRangeModel() {
        return m_model;
    }
    
    /**
     * Set a predicate filter to limit which items are considered for layout.
     * Only items for which the predicate returns a true value are included
     * in the layout computation. 
     * @param filter the predicate filter to use. If null, no filtering
     * will be performed.
     */
    public void setFilter(Predicate filter) {
        m_filter = filter;
    }
    
    /**
     * Get the predicate filter to limit which items are considered for layout.
     * Only items for which the predicate returns a true value are included
     * in the layout computation. 
     * @return the predicate filter used by this layout. If null, no filtering
     * is performed.
     */
    public Predicate getFilter() {
        return m_filter;
    }
    
    // ------------------------------------------------------------------------
    
    /**
     * Returns the scale type used for the axis. This setting only applies
     * for numerical data types (i.e., when axis values are from a
     * <code>NumberValuedRange</code>).
     * @return the scale type. One of
     * {@link prefuse.Constants#LINEAR_SCALE}, 
     * {@link prefuse.Constants#SQRT_SCALE}, or
     * {@link Constants#LOG_SCALE}.
     */
    public int getScale() {
        return m_scale;
    }
    
    /**
     * Sets the scale type used for the axis. This setting only applies
     * for numerical data types (i.e., when axis values are from a
     * <code>NumberValuedRange</code>).
     * @param scale the scale type. One of
     * {@link prefuse.Constants#LINEAR_SCALE}, 
     * {@link prefuse.Constants#SQRT_SCALE}, or
     * {@link Constants#LOG_SCALE}.
     */
    public void setScale(int scale) {
        if ( scale < 0 || scale >= Constants.SCALE_COUNT )
            throw new IllegalArgumentException(
                    "Unrecognized scale value: "+scale);
        m_scale = scale;
    }
    
    /**
     * Return the axis type of this layout, either
     * {@link prefuse.Constants#X_AXIS} or {@link prefuse.Constants#Y_AXIS}.
     * @return the axis type of this layout.
     */
    public int getAxis() {
        return m_axis;
    }

    /**
     * Set the axis type of this layout.
     * @param axis the axis type to use for this layout, either
     * {@link prefuse.Constants#X_AXIS} or {@link prefuse.Constants#Y_AXIS}.
     */
    public void setAxis(int axis) {
        if ( axis < 0 || axis >= Constants.AXIS_COUNT )
            throw new IllegalArgumentException(
                    "Unrecognized axis value: "+axis);
        m_axis = axis;
    }
    
    /**
     * Return the data type used by this layout. This value is one of
     * {@link prefuse.Constants#NOMINAL}, {@link prefuse.Constants#ORDINAL},
     * {@link prefuse.Constants#NUMERICAL}, or
     * {@link prefuse.Constants#UNKNOWN}.
     * @return the data type used by this layout
     */
    public int getDataType() {
        return m_type;
    }
    
    /**
     * Set the data type used by this layout.
     * @param type the data type used by this layout, one of
     * {@link prefuse.Constants#NOMINAL}, {@link prefuse.Constants#ORDINAL},
     * {@link prefuse.Constants#NUMERICAL}, or
     * {@link prefuse.Constants#UNKNOWN}.
     */
    public void setDataType(int type) {
        m_type = type;
    }
    
    // ------------------------------------------------------------------------
    
    /**
     * @see prefuse.action.Action#run(double)
     */
    public void run(double frac) {
        TupleSet ts = m_vis.getGroup(m_group);
        setMinMax();
        
        switch ( getDataType(ts) ) {
        case Constants.NUMERICAL:
            numericalLayout(ts);
            break;
        case XAxisLayout2.TYPE_TIME:
        	timeLayout(ts);
        	break;
        default:
            ordinalLayout(ts);
        }
    }
    
    /**
     * 得到所有数值中的最小时间
     * @param tuples
     * @param field
     * @return
     * @throws java.text.ParseException
     */
    public Tuple min(Iterator tuples, String field){
    	try{
	        Tuple t = null, tmp;
	        long min = 0;
//	        System.out.println("::hasnext:"+tuples.hasNext());
	        if ( tuples.hasNext() ) {
	            t = (Tuple)tuples.next();
//	            System.out.println("::"+t.get(field).toString());
	            min = df.parse(t.get(field).toString()).getTime();
	        }
	        while ( tuples.hasNext() ) {
	            tmp = (Tuple)tuples.next();
	            long obj = df.parse(tmp.get(field).toString()).getTime();
	            if (obj < min) {
	                t = tmp;
	                min = obj;
	            }
	        }
//	        System.out.println(":"+t);
	        return t;
    	}catch(Exception e){
    		e.printStackTrace();
    		return null;
    	}
    }
    
    public Tuple max(Iterator tuples, String field){
    	try{
	        Tuple t = null, tmp;
	        long max = 0;
	        if ( tuples.hasNext() ) {
	            t = (Tuple)tuples.next();
	            max = df.parse(t.get(field).toString()).getTime();
	        }
	        while ( tuples.hasNext() ) {
	            tmp = (Tuple)tuples.next();
	            long obj = df.parse(tmp.get(field).toString()).getTime();
	            if (obj > max) {
	                t = tmp;
	                max = obj;
	            }
	        }
	        return t;
    	}catch(Exception e){
    		return null;
    	}
    }
    
    private void timeLayout(TupleSet ts) {
//    	System.out.println("::::"+m_modelSet);
        if ( !m_modelSet ) {        	
//        	Tuple tmin = min(ts.tuples(), m_field);
//        	Tuple tmax = max(ts.tuples(), m_field);
//        	System.out.println(tmax+":"+tmin);
        	
            m_dist[0] = min(ts.tuples(), m_field).getDouble(m_field);
            m_dist[1] = max(ts.tuples(), m_field).getDouble(m_field);
            
            double lo = m_dist[0], hi = m_dist[1];
            if ( m_model == null ) {
                m_model = new NumberRangeModel(lo, hi, lo, hi);
            } else {
                ((NumberRangeModel)m_model).setValueRange(lo, hi, lo, hi);
            }
        } else {
            m_dist[0] = ((Number)m_model.getLowValue()).doubleValue();
            m_dist[1] = ((Number)m_model.getHighValue()).doubleValue();
        }
        
        Iterator iter = m_vis.items(m_group, m_filter);
        while ( iter.hasNext() ) {
            VisualItem item = (VisualItem)iter.next();
            double v = item.getDouble(m_field);
            double f = MathLib.interp(m_scale, v, m_dist);
            set(item, f);
        }
	}

	/**
     * Retrieve the data type.
     */
    protected int getDataType(TupleSet ts) {
        if ( m_type == Constants.UNKNOWN ) {
            boolean numbers = true;
            if ( ts instanceof Table ) {
                numbers = ((Table)ts).canGetDouble(m_field);
            } else {
                for ( Iterator it = ts.tuples(); it.hasNext(); ) {
                    if ( !((Tuple)it.next()).canGetDouble(m_field) ) {
                        numbers = false;
                        break;
                    }
                }
            }
            if ( numbers ) {
                return Constants.NUMERICAL;
            } else {
                return Constants.ORDINAL;
            }
        } else {
            return m_type;
        }
    }
    
    /**
     * Set the minimum and maximum pixel values.
     */
    private void setMinMax() {
        Rectangle2D b = getLayoutBounds();
        if ( m_axis == Constants.X_AXIS ) {
            m_min = b.getMinX();
            m_range = b.getMaxX() - m_min;
        } else {
            m_min = b.getMaxY();
            m_range = b.getMinY() - m_min;
        }
    }
    
    /**
     * Set the layout position of an item.
     */
    protected void set(VisualItem item, double frac) {
        double xOrY = m_min + frac*m_range;
        if ( m_axis == Constants.X_AXIS ) {
            setX(item, null, xOrY);
        } else {
            setY(item, null, xOrY);
        }
    }
    
    /**
     * Compute a quantitative axis layout.
     */
    protected void numericalLayout(TupleSet ts) {
        if ( !m_modelSet ) {
            m_dist[0] = DataLib.min(ts, m_field).getDouble(m_field);
            m_dist[1] = DataLib.max(ts, m_field).getDouble(m_field);
            
            double lo = m_dist[0], hi = m_dist[1];
            if ( m_model == null ) {
                m_model = new NumberRangeModel(lo, hi, lo, hi);
            } else {
                ((NumberRangeModel)m_model).setValueRange(lo, hi, lo, hi);
            }
        } else {
            m_dist[0] = ((Number)m_model.getLowValue()).doubleValue();
            m_dist[1] = ((Number)m_model.getHighValue()).doubleValue();
        }
        
        Iterator iter = m_vis.items(m_group, m_filter);
        while ( iter.hasNext() ) {
            VisualItem item = (VisualItem)iter.next();
            double v = item.getDouble(m_field);
            double f = MathLib.interp(m_scale, v, m_dist);
            set(item, f);
        }
    }
    
    /**
     * Compute an ordinal axis layout.
     */
    protected void ordinalLayout(TupleSet ts) {
        if ( !m_modelSet) {
            Object[] array = DataLib.ordinalArray(ts, m_field);
            
            if ( m_model == null ) {
                m_model = new ObjectRangeModel(array);
            } else {
                ((ObjectRangeModel)m_model).setValueRange(array);
            }
        }
        
        ObjectRangeModel model = (ObjectRangeModel)m_model;
        int start = model.getValue();
        int end = start + model.getExtent();
        double total = (double)(end-start);
        
        Iterator iter = m_vis.items(m_group, m_filter);
        while ( iter.hasNext() ) {
            VisualItem item = (VisualItem)iter.next();
            int order = model.getIndex(item.get(m_field)) - start;
            set(item, order/total);
        }
    }
}
