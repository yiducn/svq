/**
 * 
 */
package org.duyi.svq.models;

/**
 * @author	duyi
 * @date	2014年6月17日
 */
public class SearchModel {
	TimeModel time = null;
	
	/**
	 * 
	 */
	public SearchModel() {
		time = new TimeModel();
	}

	public TimeModel getTime() {
		return time;
	}

	public void setTime(TimeModel time) {
		this.time = time;
	}
	
	
	

}