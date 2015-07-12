/**
 * 
 */
package org.duyi.svq.vast2014;

import java.awt.Insets;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;

import javax.swing.JFrame;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.layout.AxisLabelLayout;
import prefuse.action.layout.AxisLayout;
import prefuse.data.Table;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.VisiblePredicate;

/**
 * @author	duyi
 * @date	2014年6月24日
 * 在整个系统中废弃
 */
@Deprecated
public class ResultFrame extends JFrame {

//	private Table table;
	String group = "data";
	
	private Rectangle2D m_xlabB = new Rectangle2D.Double();
	private Rectangle2D m_ylabB = new Rectangle2D.Double();
	private Rectangle2D m_dataB = new Rectangle2D.Double();
	String yxlabel = "rand";//"avgg";//"stock_price_open";//"avg";//stock_price_open", 
	
	private	XAxisLayout2 			x_axis = null;
	private	XAxisLabelLayout 	xlabels = null;
	
	public ResultFrame(){
		
	}

	public ResultFrame(Table t) {
		Display d = new Display(new Visualization());
		
		Visualization m_vis = d.getVisualization();
		m_vis.addTable(group, t);
		m_vis.setRendererFactory(new CustomizedRenderFactory());
		
        // set up the actions
		//在新建AxisLayout，使得新的Layout支持多层的Fisheye		
		x_axis = new XAxisLayout2(group, "date", 
				Constants.X_AXIS, VisiblePredicate.TRUE);
		x_axis.setDataType(XAxisLayout2.TYPE_TIME);
		m_vis.putAction("x", x_axis);
		//y轴
		AxisLayout y_axis = new AxisLayout(group, yxlabel,
				Constants.Y_AXIS, VisiblePredicate.TRUE);
		y_axis.setDataType(Constants.NUMERICAL);
		m_vis.putAction("y", y_axis);

		y_axis.setLayoutBounds(m_dataB);
		x_axis.setLayoutBounds(m_dataB);

		// set up the axis labels 设置轴的label
		NumberFormat nf = NumberFormat.getIntegerInstance();
		nf.setMaximumFractionDigits(0);
		xlabels = new XAxisLabelLayout("xlabels", x_axis, m_xlabB);
		m_vis.putAction("xlabels", xlabels);
		AxisLabelLayout ylabels = new AxisLabelLayout("ylabels", y_axis, m_ylabB);
		ylabels.setNumberFormat(nf);
		m_vis.putAction("ylabels", ylabels);

		
		DataColorAction color = new DataColorAction(group, 
				"stock_symbol", Constants.NOMINAL, VisualItem.STROKECOLOR);
		m_vis.putAction("color", color);
        
		ActionList draw = new ActionList();
		draw.add(x_axis);
		draw.add(y_axis);
		
		draw.add(xlabels);  
		draw.add(ylabels);  
		draw.add(color);
		draw.add(new RepaintAction());
		m_vis.putAction("draw", draw);
		        
		d.setHighQuality(true);
		
        m_vis.run("draw");
        
        setLayoutBoundsForDisplay();
        this.add(d);
	}
	
	public void setLayoutBoundsForDisplay() {
		Insets i = getInsets();
		int w = 800;//getWidth();
		int h = 600;//getHeight();
		int insetWidth = i.left+i.right;
		int insetHeight = i.top+i.bottom;
		int yAxisWidth = 85;
		int xAxisHeight = 15;

		m_dataB.setRect(i.left, i.top, w-insetWidth-yAxisWidth, h-insetHeight-xAxisHeight); 
		m_xlabB.setRect(i.left, h-xAxisHeight-i.bottom, w-insetWidth-yAxisWidth, xAxisHeight);
		m_ylabB.setRect(i.left, i.top, w-insetWidth, h-insetHeight-xAxisHeight);

//		m_vis.run("update");
//		m_vis.run("xlabels");
	}
}
