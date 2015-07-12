/**
 * 
 */
package org.duyi.svq.vast2014;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import prefuse.action.distortion.Distortion;

/**
 * 目前实现了两部distortion
 * @author	duyi
 * @date	2014年6月22日
 */
public class MultiDistortion extends Distortion {

    
    private double rx, ry; // magnification ranges
    private double mx, my; // magnification factor
	private ArrayList<Double> fisheyeranges;
    
    
    /**
     * Create a new BifocalDistortion with default range and magnification.
     */
    public MultiDistortion() {
        this(0.1,3);
    }
    
    /**
     * <p>Create a new BifocalDistortion with the specified range and
     * magnification. The same range and magnification is used for both
     * axes.</p>
     * 
     * <p><strong>NOTE:</strong>if the range value times the magnification
     * value is greater than 1, the resulting distortion can exceed the
     * display bounds.</p>
     * 
     * @param range the range around the focus that should be magnified. This
     *  specifies the size of the magnified focus region, and should be in the
     *  range of 0 to 1, 0 being no magnification range and 1 being the whole
     *  display.
     * @param mag how much magnification should be used in the focal area
     */
    public MultiDistortion(double range, double mag) {
        this(range,mag,range,mag);
    } //
    
    /**
     * <p>Create a new BifocalDistortion with the specified range and 
     * magnification along both axes.</p>
     * 
     * <p><strong>NOTE:</strong>if the range value times the magnification
     * value is greater than 1, the resulting distortion can exceed the
     * display bounds.</p>
     * 
     * @param xrange the range around the focus that should be magnified along
     *  the x direction. This specifies the horizontal size of the magnified 
     *  focus region, and should be a value between 0 and 1, 0 indicating no
     *  focus region and 1 indicating the whole display.
     * @param xmag how much magnification along the x direction should be used
     *  in the focal area
     * @param yrange the range around the focus that should be magnified along
     *  the y direction. This specifies the vertical size of the magnified 
     *  focus region, and should be a value between 0 and 1, 0 indicating no
     *  focus region and 1 indicating the whole display.
     * @param ymag how much magnification along the y direction should be used
     *  in the focal area
     */
    public MultiDistortion(double xrange, double xmag, 
                             double yrange, double ymag)
    {
        rx = xrange;
        mx = xmag;
        ry = yrange;
        my = ymag;
        m_distortX = !(rx == 0 || mx == 1.0);
        m_distortY = !(ry == 0 || my == 1.0);
    }
    
    public MultiDistortion(double xrange, double xmag, 
            double yrange, double ymag,
			ArrayList<Double> fisheyeranges) {
        rx = xrange;
        mx = xmag;
        ry = yrange;
        my = ymag;
        m_distortX = !(rx == 0 || mx == 1.0);
        m_distortY = !(ry == 0 || my == 1.0);
        this.fisheyeranges = fisheyeranges;
	}

	/**
     * @see prefuse.action.distortion.Distortion#distortX(double, java.awt.geom.Point2D, java.awt.geom.Rectangle2D)
     */
    protected double distortX(double x, Point2D a, Rectangle2D b) {
        return bifocal(x, a.getX(), rx, mx, b.getMinX(), b.getMaxX());
    }
    
    /**
     * @see prefuse.action.distortion.Distortion#distortY(double, java.awt.geom.Point2D, java.awt.geom.Rectangle2D)
     */
    protected double distortY(double y, Point2D a, Rectangle2D b) {
        return bifocal(y, a.getY(), ry, my, b.getMinY(), b.getMaxY());
    }
    
    /**
     * @see prefuse.action.distortion.Distortion#distortSize(java.awt.geom.Rectangle2D, double, double, java.awt.geom.Point2D, java.awt.geom.Rectangle2D)
     */
    protected double distortSize(Rectangle2D bbox, double x, double y, 
            Point2D anchor, Rectangle2D bounds)
    {
        boolean xmag = false, ymag = false;
        double m;
        
        if ( m_distortX ) {
            double cx = bbox.getCenterX(), ax = anchor.getX();
            double minX = bounds.getMinX(), maxX = bounds.getMaxX();
            m = (cx<ax ? ax-minX : maxX-ax);
            if ( m == 0 ) m = maxX-minX;
            if ( Math.abs(cx-ax) <= rx*m )
                xmag = true;
        }
        
        if ( m_distortY ) {
            double cy = bbox.getCenterY(), ay = anchor.getY();
            double minY = bounds.getMinY(), maxY = bounds.getMaxY();
            m = (cy<ay ? ay-minY : maxY-ay);
            if ( m == 0 ) m = maxY-minY;
            if ( Math.abs(cy-ay) <= ry*m )
                ymag = true;
        }
        
        if ( xmag && !m_distortY ) {
            return mx;
        } else if ( ymag && !m_distortX ) {
            return my;
        } else if ( xmag && ymag ) {
            return Math.min(mx,my);
        } else {
            return Math.min((1-rx*mx)/(1-rx), (1-ry*my)/(1-ry));
        }
    }
    
    private double bifocal(double x, double a, double r, 
                           double mag, double min, double max)
    {
    	if(fisheyeranges.size() == 2){
	        double m = (x<a ? a-min : max-a);
	        if ( m == 0 ) m = max-min;
	        double v = x - a, s = m*r;
	        if ( Math.abs(v) <= s ) {  // in focus
//	        	System.out.println("1:"+x+":"+(v*mag + a));
	            return x = v*mag + a;
	        } else {                   // out of focus
	            double bx = r*mag;
//	            System.out.println("1:"+x+":"+((v<0?-1:1)*m*(((Math.abs(v)-s) / m) * ((1-bx)/(1-r)) + bx) + a));
	            x = ((Math.abs(v)-s) / m) * ((1-bx)/(1-r));
	           
	            return (v<0?-1:1)*m*(x + bx) + a;
	        }
    	}else if(fisheyeranges.size() == 4){
    		double k1,k2;
    		if(fisheyeranges.get(0) < fisheyeranges.get(2)){
	    		k1 = fisheyeranges.get(0);
	    		k2 = fisheyeranges.get(2);
    		}else{
	    		k2 = fisheyeranges.get(0);
	    		k1 = fisheyeranges.get(2);
    		}
//    		System.out.println("max:"+max+":"+k1+":"+k2);
    		double m1 = (k1+min)/2;
    		double m2 = k1+(k2-k1)/3;
    		double m3 = k1+(k2-k1)*2/3;
    		double m4 = (k2+max)/2;
    		double m5 = m2+(m3-m2)/3;
    		double m6 = m2+(m3-m2)*2/3;
    		if(x < k1){
//    			System.out.println("1:"+x+":"+x/2);
    			return min+(x-min)/2;
    		}else if(x >= k2){
//    			System.out.println("2:"+x+":"+(max - (max-x)/2));
    			return m4+((x-k2)/(max-k2))*(max-m4);
    		}else if(x >= k1 && x < m2){
//    			System.out.println("3:"+x+":"+(m1+((x-k1)/(m2-k1))*(m5-m1)));
    			return m1+((x-k1)/(m2-k1))*(m5-m1);
    		}else if(x >=m2 && x < m3){
//    			System.out.println("4:"+x+":"+(m5+((x-m2)/(m3-m2))*(m6-m5)));
    			return m5+((x-m2)/(m3-m2))*(m6-m5);
    		}else if(x >=m3 && x < k2){
//    			System.out.println("5:"+x+":"+(m6+((x-m3)/(k2-m6))*(m4-m6)));
    			return m6+((x-m3)/(k2-m3))*(m4-m6);
    		}
    		return 0;
    	}else{
    		return 0;
    	}
    }

    /**
     * Returns the magnification factor for the x-axis.
     * @return Returns the magnification factor for the x-axis.
     */
    public double getXMagnification() {
        return mx;
    }

    /**
     * Sets the magnification factor for the x-axis.
     * @param mx The magnification factor for the x-axis.
     */
    public void setXMagnification(double mx) {
        this.mx = mx;
    }

    /**
     * Returns the magnification factor for the y-axis.
     * @return Returns the magnification factor for the y-axis.
     */
    public double getYMagnification() {
        return my;
    }

    /**
     * Sets the magnification factor for the y-axis.
     * @param my The magnification factor for the y-axis.
     */
    public void setYMagnification(double my) {
        this.my = my;
    }

    /**
     * Returns the range of the focal area along the x-axis.
     * @return Returns the range of the focal area along the x-axis.
     */
    public double getXRange() {
        return rx;
    }

    /**
     * Sets the range of the focal area along the x-axis.
     * @param rx The focal range for the x-axis, a value between 0 and 1.
     */
    public void setXRange(double rx) {
        this.rx = rx;
    }

    /**
     * Returns the range of the focal area along the y-axis.
     * @return Returns the range of the focal area along the y-axis.
     */
    public double getYRange() {
        return ry;
    }

    /**
     * Sets the range of the focal area along the y-axis.
     * @param ry The focal range for the y-axis, a value between 0 and 1.
     */
    public void setYRange(double ry) {
        this.ry = ry;
    }
}
