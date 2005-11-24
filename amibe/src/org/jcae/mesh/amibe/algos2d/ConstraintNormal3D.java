/* jCAE stand for Java Computer Aided Engineering. Features are : Small CAD
   modeler, Finit element mesher, Plugin architecture.

    Copyright (C) 2005
                  Jerome Robert <jeromerobert@users.sourceforge.net>

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

package org.jcae.mesh.amibe.algos2d;

import org.jcae.mesh.amibe.ds.Mesh;
import org.jcae.mesh.amibe.ds.Triangle;
import org.jcae.mesh.amibe.ds.OTriangle;
import org.jcae.mesh.amibe.ds.OTriangle2D;
import org.jcae.mesh.amibe.metrics.Metric3D;
import java.util.Iterator;
import org.apache.log4j.Logger;

/**
 * Performs an initial surface triangulation.
 * The value of discretisation is provided by the constraint hypothesis.
 */

public class ConstraintNormal3D
{
	private static Logger logger=Logger.getLogger(ConstraintNormal3D.class);
	private Mesh mesh = null;
	
	/**
	 * Creates a <code>ConstraintNormal3D</code> instance.
	 *
	 * @param m  the <code>ConstraintNormal3D</code> instance to check.
	 */
	public ConstraintNormal3D(Mesh m)
	{
		mesh = m;
	}
	
	/**
	 * Launch method to mesh a surface.
	 */
	public void compute()
	{
		Triangle t;
		OTriangle2D ot, sym;
		int cnt = 0;
		mesh.pushCompGeom(3);
		logger.debug(" Checking inverted triangles");
		ot = new OTriangle2D();
		sym = new OTriangle2D();
		double [] vect1 = new double[3];
		double [] vect2 = new double[3];
		double [] vect3 = new double[3];
		double [] vect4 = new double[3];

		boolean redo = false;
		int niter = mesh.getTriangles().size();
		do {
			redo = false;
			cnt = 0;
			niter--;
			for (Iterator it = mesh.getTriangles().iterator(); it.hasNext(); )
			{
				t = (Triangle) it.next();
				ot.bind(t);
				for (int i = 0; i < 3; i++)
				{
					ot.nextOTri();
					ot.clearAttributes(OTriangle.SWAPPED);
				}
			}
			
			for (Iterator it = mesh.getTriangles().iterator(); it.hasNext(); )
			{
				t = (Triangle) it.next();
				ot.bind(t);
				int l = -1;
				double best = 0.0;
				for (int i = 0; i < 3; i++)
				{
					ot.nextOTri();
					if (!ot.isMutable())
						continue;
					OTriangle.symOTri(ot, sym);
					if (ot.hasAttributes(OTriangle.SWAPPED) || sym.hasAttributes(OTriangle.SWAPPED))
						continue;
					// Make sure that triangles are not
					// inverted in 2D space
					if (sym.apex().onLeft(ot.destination(), ot.apex()) <= 0L || ot.apex().onLeft(ot.origin(), sym.apex()) <= 0L)
						continue;
					// 3D coordinates of vertices
					double p1[] = ot.origin().getUV();
					double p2[] = ot.destination().getUV();
					double apex1[] = ot.apex().getUV();
					double apex2[] = sym.apex().getUV();
					double [] xo = mesh.getGeomSurface().value(p1[0], p1[1]);
					double [] xd = mesh.getGeomSurface().value(p2[0], p2[1]);
					double [] xa = mesh.getGeomSurface().value(apex1[0], apex1[1]);
					double [] xn = mesh.getGeomSurface().value(apex2[0], apex2[1]);
					mesh.getGeomSurface().setParameter(0.5*(p1[0]+p2[0]), 0.5*(p1[1]+p2[1]));
					double [] normal = mesh.getGeomSurface().normal();
					for (int k = 0; k < 3; k++)
					{
						vect1[k] = xd[k] - xo[k];
						vect2[k] = xa[k] - xo[k];
						vect3[k] = xn[k] - xo[k];
					}
					Metric3D.prodVect3D(vect1, vect2, vect4);
					double norm = Metric3D.norm(vect4);
					if (norm < 1.e-20)
						norm = 1.0;
					double scal1 = Metric3D.prodSca(normal, vect4) / norm;
					Metric3D.prodVect3D(vect3, vect1, vect4);
					norm = Metric3D.norm(vect4);
					if (norm < 1.e-20)
						norm = 1.0;
					double scal2 = Metric3D.prodSca(normal, vect4) / norm;
					// No need to check further if triangles are good enough
					if (scal1 > 0.4 && scal2 > 0.4)
						continue;
					// Check if the swapped triangle is better
					for (int k = 0; k < 3; k++)
					{
						vect1[k] = xa[k] - xn[k];
						vect2[k] = xo[k] - xn[k];
						vect3[k] = xd[k] - xn[k];
					}
					Metric3D.prodVect3D(vect1, vect2, vect4);
					norm = Metric3D.norm(vect4);
					if (norm < 1.e-20)
						norm = 1.0;
					double scal3 = Metric3D.prodSca(normal, vect4) / norm;
					Metric3D.prodVect3D(vect3, vect1, vect4);
					norm = Metric3D.norm(vect4);
					if (norm < 1.e-20)
						norm = 1.0;
					double scal4 = Metric3D.prodSca(normal, vect4) / norm;
					double res = Math.min(scal3, scal4) - Math.min(scal1, scal2);
					if (res > best)
					{
						best = res;
						l = i;
					}
				}
				if (l >= 0)
				{
					ot.bind(t);
					for (int i = 0; i <= l; i++)
						ot.nextOTri();
					ot.swap();
					cnt++;
				}
			}
			logger.debug(" Found "+cnt+" inverted triangles");
			//  The niter variable is introduced to prevent loops.
			//  With large meshes. its initial value may be too large,
			//  so we lower it now.
			if (niter > cnt)
				niter = cnt;
			if (cnt > 0)
				redo = true;
		} while (redo && niter > 0);
		mesh.popCompGeom(3);
	}
	
}
