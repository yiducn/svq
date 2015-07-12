package org.duyi.svq.vast2014.thirdpartysamples;


import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.JPopupMenu;

import prefuse.controls.ControlAdapter;
import prefuse.controls.Control;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

public class FinalControlListener extends ControlAdapter implements Control {

	public void itemClicked(VisualItem item, MouseEvent e) 
	{
		if(item instanceof NodeItem)
		{
			String occupation = ((String) item.get("job"));
			int age = (Integer) item.get("age");
			
			JPopupMenu jpub = new JPopupMenu();
			jpub.add("Job: " + occupation);
			jpub.add("Age: " + age);
			jpub.show(e.getComponent(),(int) item.getX(), (int) item.getY());
		}
	}

}
