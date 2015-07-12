/**
 * 
 */
package org.duyi.svq.vast2014;

import java.awt.Shape;
import java.awt.geom.GeneralPath;

import prefuse.render.ShapeRenderer;
import prefuse.visual.VisualItem;

/**
 * @author	duyi
 * @date	2014年6月23日
 * 画折线图的Renderer
 */
public class LineShapeRenderer extends ShapeRenderer {
	private GeneralPath m_path = new GeneralPath();
	
	VisualItem sourceItem;
	VisualItem targetItem;
	static int index = 0;
	
	public LineShapeRenderer(int size){
		setBaseSize(size);
	}
    
    protected Shape getRawShape(VisualItem item) {    	
    	String symbol = item.getString("stock_symbol");
    	
    	int index = item.getRow();
    	//如果是同一支股票，则一直画线;否则画新线
    	if(index >1 && symbol.equals(item.getTable().getTuple(index-1).getString("stock_symbol"))){
	    	if( item.getTable().getRowCount() <= (index+1))
	    		return null;
	    	else if(index <= 1){
	    		return null;
	    	}else{
		    	targetItem = item;
		    	sourceItem = (VisualItem)item.getTable().getTuple(index-1);
		    	
		        m_path.reset();
		        m_path.moveTo(sourceItem.getX(),sourceItem.getY());
		        m_path.lineTo(targetItem.getX(), targetItem.getY());
		        m_path.closePath();
		        return m_path;
	    	}
    	}else{
    		index++;
    		return null;
    	}
    }
}
