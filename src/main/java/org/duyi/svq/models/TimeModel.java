package org.duyi.svq.models;

import java.util.ArrayList;

/**
 * 横轴控制模型，时间
 * @author	duyi
 * @date	2014年6月17日
 */
public class TimeModel{
	int currentUnit = 0;//当前单位，年、月、日、时、分、秒
	int currentInterval = 1;
	int groupOperation = GroupOperationModel.GROUP_AVG;//当前数据如不符合要求，如何处理
	
	public	static	final	String[]	UNITS	=	{"","year","month","day","hour","minute","second"};
	
	public TimeModel(){
		
	}
	public TimeModel(int unit, int interval,int groupOperation){
		currentUnit = unit;
		currentInterval = interval;
		this.groupOperation = groupOperation;
	}
	public int getCurrentUnit() {
		return currentUnit;
	}
	public void setCurrentUnit(int currentUnit) {
		this.currentUnit = currentUnit;
	}
	public int getCurrentInterval() {
		return currentInterval;
	}
	public void setCurrentInterval(int currentInterval) {
		this.currentInterval = currentInterval;
	}
	public int getGroupOperation() {
		return groupOperation;
	}
	public void setGroupOperation(int groupOperation) {
		this.groupOperation = groupOperation;
	}
	
	/**
	 * 返回所有使用的单元
	 * @return
	 */
	public String[] getUsingUnits(){
		ArrayList<String> r = new ArrayList<String>();
		for(int i = currentUnit; i <= UNITS.length; i ++){
			r.add(UNITS[i]);
		}
		return (String[])r.toArray();
	}
	
	
}