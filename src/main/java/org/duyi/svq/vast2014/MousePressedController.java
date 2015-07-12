/**
 * 
 */
package org.duyi.svq.vast2014;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import prefuse.Display;
import prefuse.action.distortion.Distortion;
import prefuse.action.layout.Layout;
import prefuse.controls.ControlAdapter;
import prefuse.visual.VisualItem;

/**
 * @author	duyi
 * @date	2014年6月22日
 * 在点击的同时添加一个当前点数值
 */
public class MousePressedController extends ControlAdapter {
	   
    private boolean m_anchorOverItem;
    private Layout[] m_layouts;
    private String m_action;
    private Point2D  m_tmp = new Point2D.Double();
	private ArrayList<Double> fisheyeranges;
	private PaintPanel2 paintPanel;
    
    /**
     * Create a new AnchorUpdateControl.
     * @param layout the layout for which to update the anchor point
     */
    public MousePressedController(Layout layout) {
        this(layout,null);
    }

    /**
     * Create a new AnchorUpdateControl.
     * @param layout the layout for which to update the anchor point
     * @param action the name of an action to run upon anchor updates
     */
    public MousePressedController(Layout layout, String action) {
        this(new Layout[] {layout}, action);
    }

    /**
     * Create a new AnchorUpdateControl.
     * @param layout the layout for which to update the anchor point
     * @param action the name of an action to run upon anchor updates
     * @param overItem indicates if anchor update events should be processed
     * while the mouse cursor is hovered over a VisualItem.
     */
    public MousePressedController(Layout layout, String action, boolean overItem)
    {
        this(new Layout[] {layout}, action, overItem);
    }
    
    /**
     * Create a new AnchorUpdateControl.
     * @param layout the layouts for which to update the anchor point
     * @param action the name of an action to run upon anchor updates
     */
    public MousePressedController(Layout[] layout, String action) {
        this(layout, action, true);
    }
    
    /**
     * Create a new AnchorUpdateControl.
     * @param layout the layouts for which to update the anchor point
     * @param action the name of an action to run upon anchor updates
     * @param overItem indicates if anchor update events should be processed
     * while the mouse cursor is hovered over a VisualItem.
     */
    public MousePressedController(Layout[] layout, String action, boolean overItem)
    {
        m_layouts = (Layout[])layout.clone();
        m_action = action;
        m_anchorOverItem = overItem;
    }
    
    

    public MousePressedController(Layout layout, String action,
			ArrayList<Double> fisheyeranges) {
    	 this(new Layout[] {layout}, action);
    	 this.fisheyeranges = fisheyeranges;
	}

	public MousePressedController(Distortion layout, String action,
			ArrayList<Double> fisheyeranges, PaintPanel2 paintPanel2) {
   	 this(new Layout[] {layout}, action);
   	 	this.fisheyeranges = fisheyeranges;
   	 	this.paintPanel = paintPanel2;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if(paintPanel.getActionType() == PaintPanel2.ACTION_FISHEYE){
			fisheyeranges.add(e.getPoint().getX());
			fisheyeranges.add(e.getPoint().getY());
	    	moveEvent(e);
		}
	}

    /**
     * Registers a mouse move event, updating the anchor point for all
     * registered layout instances.
     * @param e the MouseEvent
     */
    public void moveEvent(MouseEvent e) {
        Display d = (Display)e.getSource();
        d.getAbsoluteCoordinate(e.getPoint(), m_tmp);
        for ( int i=0; i<m_layouts.length; i++ ) 
            m_layouts[i].setLayoutAnchor(m_tmp);
        runAction(e);
    }
    
	/**
     * Runs an optional action upon anchor update.
     * @param e MouseEvent
     */
    private void runAction(MouseEvent e) {
//        if ( m_action != null ) {
//            Display d = (Display)e.getSource();
//            d.getVisualization().run(m_action);
//        }
    }
}
