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
 * (C) Copyright 2005, by EADS CRC
 */

package org.jcae.netbeans.mesh;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import org.jcae.mesh.bora.ds.BModel;
import org.jcae.mesh.bora.xmldata.BModelReader;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Node;

public class MeshDataObject extends MultiDataObject implements SaveCookie, PropertyChangeListener
{
	private static final Logger LOGGER = Logger.getLogger(MeshDataObject.class.getName());

	public MeshDataObject(FileObject arg0, MultiFileLoader arg1) throws DataObjectExistsException
	{
		super(arg0, arg1);
		dir = arg0;
	}

	@Override
	protected Node createNodeDelegate()
	{
		MeshNode toReturn = new MeshNode(this);
		return toReturn;
	}
	
	private BModel bModel;
	private FileObject dir;

	public BModel getBModel() {
		return bModel;
	}

	public BModel getBModel(String geomFile) {
		bModel = new BModel(geomFile,  getDirectory());
		bModel.cleanWorkDirectory();
		return bModel;
	}

	public void save() throws IOException
	{
		if (bModel != null)
			bModel.save();
	}

	/**
	 * If the "model" file exists in the directory of the Bora mesh
	 * this method loads the BModel
	 */
	public void load() {
		String tmpDir =  getDirectory();
		File f = new File(tmpDir, "model");
		if (f.exists()) {
			LOGGER.info("Loading Bora model at : " + getDirectory());
			bModel = BModelReader.readObject(getDirectory(), "model", false);
		}

	}

	@Override
	public String getName() {
		String directory = getDirectory();
		return directory.substring(directory.lastIndexOf(File.separator) + 1, directory.lastIndexOf("."));
	}

	public void propertyChange(PropertyChangeEvent evt)
	{
		setModified(true);
	}

	public void updateBModelDir() {
		if (bModel != null)
			bModel.setOutputDir(getDirectory());
	}

	public String getDirectory() {
		return dir.toString();
	}
}
