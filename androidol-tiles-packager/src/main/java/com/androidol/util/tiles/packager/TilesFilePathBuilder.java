package com.androidol.util.tiles.packager;

import java.io.File;

public class TilesFilePathBuilder {
	
	protected String rootPath = "";
	
	public TilesFilePathBuilder(String rootPath) {
		this.rootPath = rootPath;
		if(this.rootPath == null || "".equalsIgnoreCase(this.rootPath)==true) {
			this.rootPath = "packages" + File.separator + "tiles";
		}
		// escape white space
		this.rootPath = this.rootPath.replace(' ', '_');
	}
	
	/**
	 * 
	 * @param tileUrl
	 * @return
	 */
	public String createTileFilePath(String tileUrl) {
		
		String fileSeparator = File.separator;		
		String httpStripped = tileUrl.substring(7);
		String folderName = httpStripped.substring(0, httpStripped.indexOf("/"));
		String fileName = httpStripped.substring(httpStripped.indexOf("/")+1, httpStripped.length()).replace('/', '_');
		String tileFilePath = this.rootPath + fileSeparator + folderName + fileSeparator + fileName; 		
		return tileFilePath;
	}

	public String getRootPath() {
		return rootPath;
	}

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}
	
}
