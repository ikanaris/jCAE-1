/* jCAE stand for Java Computer Aided Engineering. Features are : Small CAD
   modeler, Finite element mesher, Plugin architecture.
 
    Copyright (C) 2003,2004,2005,2006, by EADS CRC
 
    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.
 
    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.
 
    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.jcae.mesh.amibe.patch;

import org.jcae.mesh.amibe.metrics.Metric2D;
import java.util.Stack;

/**
 * Distance computations in 2D parameter space by using 3D metrics.
 */
public class Calculus3D implements Calculus
{
	//  The Mesh2D instance on which methods are applied
	private Mesh2D mesh;
	
	private static final int level_max = 10;
	private static final double delta_max = 0.5;
	private static Integer [] intArray = new Integer[level_max+1];
	private static boolean accurateDistance = false;

	static {
		String accurateDistanceProp = System.getProperty("org.jcae.mesh.amibe.ds.tools.Calculus3D.accurateDistance");
		if (accurateDistanceProp == null)
		{
			accurateDistanceProp = "false";
			System.setProperty("org.jcae.mesh.amibe.ds.tools.Calculus3D.accurateDistance", accurateDistanceProp);
		}
		accurateDistance = accurateDistanceProp.equals("true");
		for (int i = 0; i <= level_max; i++)
			intArray[i] = new Integer(i);
	};
	
	/**
	 * Constructor.
	 *
	 * @param  m   the <code>Mesh2D</code> being modified.
	 */
	public Calculus3D(Mesh2D m)
	{
		mesh = m;
	}

	/**
	 * Returns the Riemannian distance between nodes.
	 * This distance is computed with metrics on start and end points,
	 * and the maximal distance is returned.
	 *
	 * @param start  the start node
	 * @param end  the end node
	 * @return the distance between nodes
	 **/
	public double distance(Vertex2D start, Vertex2D end)
	{
		double l1 = distance(start, end, start);
		double l2 = distance(start, end, end);
		double lmax = Math.max(l1, l2);
		if (!accurateDistance || Math.abs(l1 - l2) < delta_max * lmax)
			return lmax;
		
		Stack v = new Stack();
		Vertex2D mid = Vertex2D.middle(start, end);
		v.push(intArray[level_max]);
		v.push(end);
		v.push(mid);
		v.push(intArray[level_max]);
		v.push(mid);
		v.push(start);
		double ret = 0.0;
		int level = level_max;
		while (v.size() > 0)
		{
			Vertex2D pt1 = (Vertex2D) v.pop();
			Vertex2D pt2 = (Vertex2D) v.pop();
			level = ((Integer) v.pop()).intValue();
			l1 = distance(pt1, pt2, pt1);
			l2 = distance(pt1, pt2, pt2);
			lmax = Math.max(l1, l2);
			if (Math.abs(l1 - l2) < delta_max * lmax || level == 0)
				ret += lmax;
			else
			{
				level--;
				mid = Vertex2D.middle(pt1, pt2);
				v.push(intArray[level]);
				v.push(pt2);
				v.push(mid);
				v.push(intArray[level]);
				v.push(mid);
				v.push(pt1);
			}
		}
		return ret;
	}
	
	/**
	 * Returns the Riemannian distance between nodes.
	 *
	 * @param start  the start node
	 * @param end  the end node
	 * @param vm  the vertex on which metrics is evaluated
	 * @return the distance between nodes
	 **/
	public double distance(Vertex2D start, Vertex2D end, Vertex2D vm)
	{
		double ret;
		Metric2D m = vm.getMetrics(mesh);
		ret = m.distance(start.getUV(), end.getUV());
		return ret;
	}
	
	/**
	 * Returns the 2D radius of the 3D unit ball centered at a point.
	 * This routine returns a radius such that the 2D circle centered
	 * at a given vertex will have a distance lower than 1 in 3D.
	 * This method is used by {@link QuadTree#getNearestVertex}
	 *
	 * @param vm  the vertex on which metrics is evaluated
	 * @return the radius in 2D space.
	 */
	public double radius2d(Vertex2D vm)
	{
		Metric2D m = vm.getMetrics(mesh);
		return 1.0 / Math.sqrt(m.minEV());
	}
	
	/**
	 * Returns the length of an edge.
	 *
	 * @param ot  the edge being evaluated
	 * @return the distance between its two endpoints.
	 */
	public double length(OTriangle2D ot)
	{
		return distance((Vertex2D) ot.origin(), (Vertex2D) ot.destination());
	}
	
}
