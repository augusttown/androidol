/*
 * 
 */
package com.androidol.basetypes;

/**
 * Class: OpenLayers.Pixel
 * This class represents a screen coordinate, in x and y coordinates
 */
public class Pixel {
	
	// ===========================================================
	// Constants
	// ===========================================================
	
	// ===========================================================
	// Fields
	// ===========================================================
	protected double x = 0.0;
	protected double y = 0.0;
	
	// ===========================================================
	// Constructors
	// ===========================================================
    /**
     * Constructor: Pixel
     * Create an instance of OpenLayers.Pixel
     *
     * @param x {double} The x coordinate
     * @param y {double} The y coordinate
     * 
     */
    public Pixel(double x, double y) {
    	this.x = x;
    	this.y = y;
    }
    
    /**
     * APIMethod: toString
     * Cast this object into a string
     *
     * @return {String} The string representation of Pixel. 
     * 		   ex: "x=200.4,y=242.2"
     */
    @Override
    public String toString() {
        return "x: " + this.x + ", y: " + this.y;
    }
    
    /**
     * APIMethod: clone
     * Return a clone of this pixel object
     *
     * @return {@link Pixel} A clone pixel
     */
    public Pixel clone() {
        return new Pixel(this.x, this.y); 
    }
    
    /**
     * APIMethod: equals
     * Determine whether one pixel is equivalent to another
     *
     * @param pixel {@link Pixel}
     *
     * @return {boolean} 
     * 		   The point passed in as parameter is equal to this. Note that
     * 		   if px passed in is null, returns false.
     */
    public boolean equals(Pixel pixel) {
        boolean equals = false;
        if(pixel != null) {
            equals = ((this.x == pixel.x && this.y == pixel.y));
        }
        return equals;
    }
    
    /**
     * APIMethod: add
     *
     * @param x {double}
     * @param y {double}
     *
     * @return {@link Pixel} 
     * 		   A new Pixel with this pixel's x&y augmented by the 
     * 	 	   values passed in.
     */
    public Pixel add(double x, double y) {       
        return new Pixel(this.x + x, this.y + y);
    }

    /**
    * APIMethod: offset
    * 
    * @param pixel {@link Pixel} 
    * 
	* @return {@link Pixel} 
    * 		  A new Pixel with this pixel's x&y augmented by the 
    *         x&y values of the pixel passed in.
    */
    public Pixel offset(Pixel pixel) {
        Pixel newPixel = this.clone();        
        if(pixel != null) {
        	newPixel = this.add(pixel.x, pixel.y);
        }
        return newPixel;
    }

	/**
	 * @return the x
	 */
	public double getX() {
		return x;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public double getY() {
		return y;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(double y) {
		this.y = y;
	}	
    
    
}
