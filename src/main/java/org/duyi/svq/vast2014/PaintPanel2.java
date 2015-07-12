/**
 * 
 */
package org.duyi.svq.vast2014;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;

import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.distortion.Distortion;
import prefuse.action.filter.VisibilityFilter;
import prefuse.action.layout.AxisLabelLayout;
import prefuse.action.layout.AxisLayout;
import prefuse.controls.ToolTipControl;
import prefuse.data.Table;
import prefuse.data.expression.AndPredicate;
import prefuse.data.io.DelimitedTextTableReader;
import prefuse.render.AxisRenderer;
import prefuse.render.Renderer;
import prefuse.render.RendererFactory;
import prefuse.util.ColorMap;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.VisiblePredicate;

/**
 * @author	duyi
 * @date	2014年6月20日
 */
public class PaintPanel2 extends Display  implements MouseListener, MouseMotionListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String group = "data";
	
	//用来区别鱼眼手势与检索手势
	public	static	final	int	ACTION_FISHEYE	=		0;
	public	static	final	int	ACTION_SKETCH_QUERY	=	1;
	public 	static 	final 	int ACTION_INK			=	3;
	
	private	int		actionType	=	1;
	
	
	private Rectangle2D m_xlabB = new Rectangle2D.Double();
	private Rectangle2D m_ylabB = new Rectangle2D.Double();
	private Rectangle2D m_dataB = new Rectangle2D.Double();
	
	private	ArrayList<Stroke>	strokeList4Fish	=	null;
	private	ArrayList<Stroke>	strokeList4Search	=	null;
	private	ArrayList<Stroke>	strokeList4Ink	=	null;
	
	private	Stroke				currentStroke	=	null;
	
	private	XAxisLayout2 			x_axis = null;
	private	XAxisLabelLayout 	xlabels = null;
	
	private	ColorActionWithlegend colorAction = null;
	private	Table table = null;
	
	//识别结果table
	private	ArrayList<Table> resultTables;
	//检索的数据模型
	private	SketchQuery sq;
	//绘制的颜色
	private	Color strokeColor = Color.DARK_GRAY;
	
	static	ArrayList<Double> fisheyeranges = new ArrayList<Double>();
	
	private	VisibilityFilter	filter	=	null;
	//结果panel
	private ResultPanel resultPanel;
	
	public PaintPanel2(Table t){
		super(new Visualization());
		initSketch();
		this.table = t;
		m_vis.addTable(group, t);
		m_vis.setRendererFactory(new CustomizedRenderFactory());
		
		//在新建AxisLayout，使得新的Layout支持多层的Fisheye		
		x_axis = new XAxisLayout2(group, "date", 
				Constants.X_AXIS, VisiblePredicate.TRUE);
		x_axis.setDataType(XAxisLayout2.TYPE_TIME);
		m_vis.putAction("x", x_axis);
		//y轴
		AxisLayout y_axis = new AxisLayout(group, ConstantsSVQ.XLABEL,
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
		
		colorAction = new ColorActionWithlegend(group, 
				"stock_symbol", Constants.NOMINAL, VisualItem.STROKECOLOR);
		m_vis.putAction("color", colorAction);
        
		ActionList draw = new ActionList();
		draw.add(x_axis);
		draw.add(y_axis);
		
		draw.add(xlabels);  
		draw.add(ylabels);  
		draw.add(colorAction);
		draw.add(new RepaintAction());	
        
		m_vis.putAction("draw", draw);
		
        ActionList distort = new ActionList();
        
        Distortion feye = new MultiDistortion(0.1,3,0,1.0, fisheyeranges);
        feye.setSizeDistorted(false);
        distort.add(feye);
        distort.add(colorAction);        
        distort.add(new RepaintAction());
        m_vis.putAction("distort", distort);        
        addControlListener(new MousePressedController(feye, "distort", fisheyeranges, this));
        addControlListener(new ToolTipControl("stock_symbol"));
        
//        AndPredicate filter = new AndPredicate(searchQ.getPredicate());
        
//        filter	= new VisibilityFilter(m_vis, group, );

		setHighQuality(true);
		
        m_vis.run("draw");
        
        setLayoutBoundsForDisplay();
	}
	
	public PaintPanel2(Table table, ResultPanel jpanelTop) {
		this(table);
		this.resultPanel = jpanelTop;
	}

	/**
	 * 初始化笔迹相关内容
	 */
	private void initSketch() {
		//绘制笔迹
		strokeList4Fish = new ArrayList<Stroke>();
		strokeList4Search = new ArrayList<Stroke>();
		strokeList4Ink = new ArrayList<Stroke>();
		addMouseListener(this);
		addMouseMotionListener(this);
		sq = new SketchQuery();
	}

	/**
	 * 获得用来写legend的color map
	 * @return
	 */
	public Map getColorMap(){
		Map r = new HashMap();
		Map m = colorAction.getColorMap();
		ColorMap cm = colorAction.getColorMap2();
		for(Object o:m.keySet()){
			r.put(o, cm.getColor(Double.parseDouble(m.get(o).toString())));
		}				
		return r;
	}
	
	/**
	 * 得到某一列的某一个指标的均值
	 * @param f 计算均值的列
	 * @return q 过滤的列
	 * v 过滤的列名
	 */
	public double getAvgByField(String f, String q, String v){
		double d = 0.0;
		int count = 0;
		for(int i = 0; i < table.getRowCount(); i ++){			
			if(table.getString(i, q).equals(v)){
				d += table.getDouble(i, f);
				count ++;
			}
		}
		return d/count;
	}
	
	public static void main(String[] args){		
		Table table = null;
        try {
        	table = (new DelimitedTextTableReader()).readTable(ConstantsSVQ.PARSE_DATA);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        final JFrame jf = new JFrame();
        
        jf.add(new PaintPanel2(table));
        jf.pack();
        jf.setSize(1770, 900);        
        jf.setVisible(true);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public void setLayoutBoundsForDisplay() {
		Insets i = getInsets();
		int w = 1800;//getWidth();
		int h = 700;//getHeight();
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

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2 = (Graphics2D)g;
		g2.setStroke(new BasicStroke(2));
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		if(actionType == ACTION_FISHEYE){
			for(Stroke s : strokeList4Fish){
				g2.setPaint(Color.DARK_GRAY);
				g2.setStroke(new BasicStroke(3));
				g.drawPolyline(s.getIntXs(), s.getIntYs(), s.getSize());
			}
		}else if(actionType == ACTION_SKETCH_QUERY){
			if(strokeList4Search.size() > 0){
				g2.setPaint(strokeColor);
				Stroke s = strokeList4Search.get(strokeList4Search.size()-1);
				g2.drawPolyline(s.getIntXs(), s.getIntYs(), s.getSize());	
			}
		}
		
		for(Stroke s : strokeList4Ink){
			g2.setPaint(Color.LIGHT_GRAY);
			g2.setStroke(new BasicStroke(2));
			g.drawPolyline(s.getIntXs(), s.getIntYs(), s.getSize());
		}		
	}
	
	public void mouseDragged(MouseEvent e) {
		currentStroke.addPoint(e.getPoint());
		repaint();
	}
	public void mouseMoved(MouseEvent e) {
	
	}	
	public void mouseClicked(MouseEvent e) {
		
	}
	public void mousePressed(MouseEvent e) {
		currentStroke = new Stroke();
		if(actionType == ACTION_FISHEYE){		
			strokeList4Fish.add(currentStroke);
		}else if(actionType == ACTION_SKETCH_QUERY){
			strokeList4Search.add(currentStroke);
		}else if(actionType == ACTION_INK){
			strokeList4Ink.add(currentStroke);
		}
		repaint();		
	}
	public void mouseReleased(MouseEvent e) {
		if(actionType == ACTION_FISHEYE){			
			m_vis.run("draw");
			m_vis.run("distort");
		}else if(actionType == ACTION_SKETCH_QUERY){
			sq.setQuery(strokeList4Search.get(strokeList4Search.size()-1));
			setResult(sq.run(), colorAction.getColorMap(), colorAction.getColorMap2());
		}
	}

	public void mouseEntered(MouseEvent e) {
		
	}
	public void mouseExited(MouseEvent e) {
		
	}
	
	/**
	 * 设置当前操作类型
	 * ACTION_FISHEYE
	 * ACTION_SKETCH_QUERY
	 */
	public void setActionType(int type) {
		this.actionType = type;
	}
	public int getActionType(){
		return actionType;
	}
	
	public ArrayList<Table> getResultTables(){		
		return resultTables;
	}
	private void setResult(ArrayList<Table> run, Map map, ColorMap colorMap) {
		this.resultTables = run;
		if(resultPanel != null)
			resultPanel.setTables(run, map, colorMap);
	}

	public Color getStrokeColor() {
		return strokeColor;
	}

	public void setStrokeColor(Color strokeColor) {
		this.strokeColor = strokeColor;
	}

	public SketchQuery getSq() {
		return sq;
	}

	public void setSq(SketchQuery sq) {
		this.sq = sq;
	}
	
	
}

class CustomizedRenderFactory implements RendererFactory{
	Renderer yAxisRenderer = new AxisRenderer(Constants.LEFT, Constants.TOP);
	Renderer xAxisRenderer = new XAxisRenderer(Constants.CENTER, Constants.FAR_BOTTOM);
	LineShapeRenderer dRender = new LineShapeRenderer(2);
	public CustomizedRenderFactory(){
	}
	public Renderer getRenderer(VisualItem item) {
		if(item.isInGroup("ylabels")){
			return yAxisRenderer;
		}
		if(item.isInGroup("xlabels")){
			return xAxisRenderer;
		}
		return dRender;
	}
	
}


