package com.androidol.util.tiles.packager;

public class TileTicket {
		
	protected String tileUrl;
	protected String tileFilePath;
	
	public TileTicket(String tileUrl, String tileFilePath) {
		this.tileUrl = tileUrl;
		this.tileFilePath = tileFilePath;
	}
	
	public String getTileUrl() {
		return tileUrl;
	}
	/*
	public void setTileUrl(String tileUrl) {
		this.tileUrl = tileUrl;
	}
	*/
	public String getTileFilePath() {
		return tileFilePath;
	}
	/*
	public void setTileFilePath(String tileFilePath) {
		this.tileFilePath = tileFilePath;
	}
	*/	
}
