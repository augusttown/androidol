package com.androidol.style;

import android.graphics.Paint;

public class Style {
	
	// ===========================================================
	// Fields
	// ===========================================================	
		
	protected int 		fillColor;
	protected double 	fillOpacity;
	protected int		strokeColor;
	protected double 	strokeOpacity;
	protected int 		strokeWidth;
	protected String 	strokeLinecap;
	protected String 	strokeLinejoin;
	protected String	strokeDashstyle;
	protected double	pointRadius;
	protected int		hoverFillColor;
	protected double 	hoverFillOpacity;
	protected int 		hoverStrokeColor;
	protected double 	hoverStrokeOpacity;
	protected int 		hoverStrokeWidth;
	protected double	hoverPointRadius;

	protected String 	externalGraphic;
	protected int		graphicWidth;
	protected int		graphicHeight;
	protected double	graphicOpacity;
	protected int		graphicXOffset;
	protected int	    graphicYOffset;
	protected String	graphicName;
	
	protected boolean	isAntiAlias						= true;
	
	/**
	 * 
	 * @param style
	 * @return
	 */
	public static Paint createFillPaint(Style style) {
		Paint paint = new Paint();		
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(style.getFillColor());
		paint.setAlpha((int)(style.getFillOpacity()*255));		
		return paint;
	}
	
	/**
	 * 
	 * @param style
	 * @return
	 */
	public static Paint createStrokePaint(Style style) {
		Paint paint = new Paint();	
		paint.setStyle(Paint.Style.STROKE);
		paint.setAntiAlias(style.isAntiAlias());
		paint.setColor(style.getStrokeColor());
		paint.setStrokeWidth(style.getStrokeWidth());
		// TODO: paint.setStrokeCap();
		// TODO: paint.setStrokeJoin();
		paint.setAlpha((int)(style.getStrokeOpacity()*255));	
		return paint;
	}	
	
	/**
	 * Constructor
	 */
	public Style() {
		// 'default' style
		this.fillColor = 0xee9900;
		this.fillOpacity = 0.4; 
		this.hoverFillColor = 0xffffff;
		this.hoverFillOpacity = 0.8;
		this.strokeColor = 0xee9900;
		this.strokeOpacity = 1.0;
		this.strokeWidth = 1;
		this.strokeLinecap = "round";
		this.strokeDashstyle = "solid";
		this.hoverStrokeColor = 0xff0000;
		this.hoverStrokeOpacity = 1.0;
		this.hoverStrokeWidth = 2;
		this.pointRadius = 6;
	}
	
	/**
	 * 
	 */
	public void applySelectedStyle() {
		this.fillColor = 0x0000ff;
		this.fillOpacity = 0.4; 
		this.hoverFillColor = 0xffffff;
		this.hoverFillOpacity = 0.8;
		this.strokeColor = 0x0000ff;
		this.strokeOpacity = 1.0;
		this.strokeWidth = 2;
		this.strokeLinecap = "round";
		this.strokeDashstyle = "solid";
		this.hoverStrokeColor = 0xff0000;
		this.hoverStrokeOpacity = 1.0;
		this.hoverStrokeWidth = 2;
		this.pointRadius = 6;
	}
	
	/**
	 * 
	 */
	public void applyTemporaryStyle() {
		this.fillColor = 0xffff00;
		this.fillOpacity = 0.4; 
		this.hoverFillColor = 0xffffff;
		this.hoverFillOpacity = 0.8;
		this.strokeColor = 0xffff00;
		this.strokeOpacity = 1.0;
		this.strokeWidth = 1;
		this.strokeLinecap = "round";
		this.strokeDashstyle = "solid";
		this.hoverStrokeColor = 0xff0000;
		this.hoverStrokeOpacity = 1.0;
		this.hoverStrokeWidth = 2;
		this.pointRadius = 6;
	}
	
	/**
	 * @return the fillColor
	 */
	public int getFillColor() {
		return fillColor;
	}

	/**
	 * @param fillColor the fillColor to set
	 */
	public void setFillColor(int fillColor) {
		this.fillColor = fillColor;
	}

	/**
	 * @return the fillOpacity
	 */
	public double getFillOpacity() {
		return fillOpacity;
	}

	/**
	 * @param fillOpacity the fillOpacity to set
	 */
	public void setFillOpacity(double fillOpacity) {
		this.fillOpacity = fillOpacity;
	}

	/**
	 * @return the strokeColor
	 */
	public int getStrokeColor() {
		return strokeColor;
	}

	/**
	 * @param strokeColor the strokeColor to set
	 */
	public void setStrokeColor(int strokeColor) {
		this.strokeColor = strokeColor;
	}

	/**
	 * @return the strokeOpacity
	 */
	public double getStrokeOpacity() {
		return strokeOpacity;
	}

	/**
	 * @param strokeOpacity the strokeOpacity to set
	 */
	public void setStrokeOpacity(double strokeOpacity) {
		this.strokeOpacity = strokeOpacity;
	}

	/**
	 * @return the strokeWidth
	 */
	public int getStrokeWidth() {
		return strokeWidth;
	}

	/**
	 * @param strokeWidth the strokeWidth to set
	 */
	public void setStrokeWidth(int strokeWidth) {
		this.strokeWidth = strokeWidth;
	}

	/**
	 * @return the strokeLinecap
	 */
	public String getStrokeLinecap() {
		return strokeLinecap;
	}

	/**
	 * @param strokeLinecap the strokeLinecap to set
	 */
	public void setStrokeLinecap(String strokeLinecap) {
		this.strokeLinecap = strokeLinecap;
	}

	/**
	 * @return the strokeDashstyle
	 */
	public String getStrokeDashstyle() {
		return strokeDashstyle;
	}

	/**
	 * @param strokeDashstyle the strokeDashstyle to set
	 */
	public void setStrokeDashstyle(String strokeDashstyle) {
		this.strokeDashstyle = strokeDashstyle;
	}

	/**
	 * @return the pointRadius
	 */
	public double getPointRadius() {
		return pointRadius;
	}

	/**
	 * @param pointRadius the pointRadius to set
	 */
	public void setPointRadius(double pointRadius) {
		this.pointRadius = pointRadius;
	}

	/**
	 * @return the hoverFillColor
	 */
	public int getHoverFillColor() {
		return hoverFillColor;
	}

	/**
	 * @param hoverFillColor the hoverFillColor to set
	 */
	public void setHoverFillColor(int hoverFillColor) {
		this.hoverFillColor = hoverFillColor;
	}

	/**
	 * @return the hoverFillOpacity
	 */
	public double getHoverFillOpacity() {
		return hoverFillOpacity;
	}

	/**
	 * @param hoverFillOpacity the hoverFillOpacity to set
	 */
	public void setHoverFillOpacity(double hoverFillOpacity) {
		this.hoverFillOpacity = hoverFillOpacity;
	}

	/**
	 * @return the hoverStrokeColor
	 */
	public int getHoverStrokeColor() {
		return hoverStrokeColor;
	}

	/**
	 * @param hoverStrokeColor the hoverStrokeColor to set
	 */
	public void setHoverStrokeColor(int hoverStrokeColor) {
		this.hoverStrokeColor = hoverStrokeColor;
	}

	/**
	 * @return the hoverStrokeOpacity
	 */
	public double getHoverStrokeOpacity() {
		return hoverStrokeOpacity;
	}

	/**
	 * @param hoverStrokeOpacity the hoverStrokeOpacity to set
	 */
	public void setHoverStrokeOpacity(double hoverStrokeOpacity) {
		this.hoverStrokeOpacity = hoverStrokeOpacity;
	}

	/**
	 * @return the hoverStrokeWidth
	 */
	public int getHoverStrokeWidth() {
		return hoverStrokeWidth;
	}

	/**
	 * @param hoverStrokeWidth the hoverStrokeWidth to set
	 */
	public void setHoverStrokeWidth(int hoverStrokeWidth) {
		this.hoverStrokeWidth = hoverStrokeWidth;
	}

	/**
	 * @return the hoverPointRadius
	 */
	public double getHoverPointRadius() {
		return hoverPointRadius;
	}

	/**
	 * @param hoverPointRadius the hoverPointRadius to set
	 */
	public void setHoverPointRadius(double hoverPointRadius) {
		this.hoverPointRadius = hoverPointRadius;
	}

	/**
	 * @return the externalGraphic
	 */
	public String getExternalGraphic() {
		return externalGraphic;
	}

	/**
	 * @param externalGraphic the externalGraphic to set
	 */
	public void setExternalGraphic(String externalGraphic) {
		this.externalGraphic = externalGraphic;
	}

	/**
	 * @return the graphicWidth
	 */
	public int getGraphicWidth() {
		return graphicWidth;
	}

	/**
	 * @param graphicWidth the graphicWidth to set
	 */
	public void setGraphicWidth(int graphicWidth) {
		this.graphicWidth = graphicWidth;
	}

	/**
	 * @return the graphicHeight
	 */
	public int getGraphicHeight() {
		return graphicHeight;
	}

	/**
	 * @param graphicHeight the graphicHeight to set
	 */
	public void setGraphicHeight(int graphicHeight) {
		this.graphicHeight = graphicHeight;
	}

	/**
	 * @return the graphicOpacity
	 */
	public double getGraphicOpacity() {
		return graphicOpacity;
	}

	/**
	 * @param graphicOpacity the graphicOpacity to set
	 */
	public void setGraphicOpacity(double graphicOpacity) {
		this.graphicOpacity = graphicOpacity;
	}

	/**
	 * @return the graphicXOffset
	 */
	public int getGraphicXOffset() {
		return graphicXOffset;
	}

	/**
	 * @param graphicXOffset the graphicXOffset to set
	 */
	public void setGraphicXOffset(int graphicXOffset) {
		this.graphicXOffset = graphicXOffset;
	}

	/**
	 * @return the graphicYOffset
	 */
	public int getGraphicYOffset() {
		return graphicYOffset;
	}

	/**
	 * @param graphicYOffset the graphicYOffset to set
	 */
	public void setGraphicYOffset(int graphicYOffset) {
		this.graphicYOffset = graphicYOffset;
	}

	/**
	 * @return the graphicName
	 */
	public String getGraphicName() {
		return graphicName;
	}

	/**
	 * @param graphicName the graphicName to set
	 */
	public void setGraphicName(String graphicName) {
		this.graphicName = graphicName;
	}

	/**
	 * @return the isAntiAlias
	 */
	public boolean isAntiAlias() {
		return isAntiAlias;
	}

	/**
	 * @param isAntiAlias the isAntiAlias to set
	 */
	public void setAntiAlias(boolean isAntiAlias) {
		this.isAntiAlias = isAntiAlias;
	}

	/**
	 * @return the strokeLinejoin
	 */
	public String getStrokeLinejoin() {
		return strokeLinejoin;
	}

	/**
	 * @param strokeLinejoin the strokeLinejoin to set
	 */
	public void setStrokeLinejoin(String strokeLinejoin) {
		this.strokeLinejoin = strokeLinejoin;
	}

}
