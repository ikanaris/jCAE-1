/*
 * Project Info:  http://jcae.sourceforge.net
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 *
 * (C) Copyright 2008, by EADS France
 */

package org.jcae.vtk.test;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import org.jcae.vtk.Canvas;
import javax.swing.JFrame;
import org.jcae.mesh.oemm.OEMM;
import org.jcae.mesh.oemm.Storage;
import org.jcae.vtk.SelectionListener;
import org.jcae.vtk.Utils;
import org.jcae.vtk.View;
import org.jcae.vtk.Viewable;
import org.jcae.vtk.ViewableOEMM;
import vtk.vtkInteractorStyleTrackballCamera;
import vtk.vtkRenderer;

/**
 *
 * @author ibarz
 */
public class TestOEMMVisu extends ViewableOEMM implements SelectionListener, KeyListener {
	public Canvas canvas;

	public TestOEMMVisu(OEMM oemm)
	{
		super(oemm);
	}
	
	public void selectionChanged(Viewable viewable)
	{
		viewable.highlight();
	}

	public void keyReleased(KeyEvent e)
	{
		// DO Nothing
	}

	public void keyTyped(KeyEvent e)
	{
		// DO Nothing
	}

	public void keyPressed(KeyEvent e)
	{
		switch (e.getKeyCode())
		{
			case KeyEvent.VK_A:
				System.out.println("SWITCH APPEND SELECTION");
				setAppendSelection(!getAppendSelection());
				break;
			case KeyEvent.VK_E:
				System.out.println("SWITCH AUTOMATIC SELECTION");
				setAutomaticSelection(!isAutomaticSelection());
				break;
			default:
			/*case KeyEvent.VK_V:
				setSelectionType(SelectionType.POINT);
				canvas.lock();
				System.out.println("Capabilities : " +canvas.GetRenderWindow().ReportCapabilities());
				canvas.unlock();
				break;*/
		}
	}
	
	public static void main(String[] args)
	{
		Utils.loadVTKLibraries();

		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		View canvas = new View();
		frame.add(canvas, BorderLayout.CENTER);
		frame.setSize(800, 600);
		vtkRenderer renderer = canvas.GetRenderer();

		final OEMM oemm = Storage.readOEMMStructure(args[0]);
		TestOEMMVisu test = new TestOEMMVisu(oemm);
		canvas.add(test);
		canvas.addKeyListener(test);
		test.addSelectionListener(test);

		vtkInteractorStyleTrackballCamera style = new vtkInteractorStyleTrackballCamera();
		style.AutoAdjustCameraClippingRangeOn();
		canvas.getIren().SetInteractorStyle(style);

		canvas.lock();
		renderer.ResetCamera();
		canvas.unlock();

	}
}
