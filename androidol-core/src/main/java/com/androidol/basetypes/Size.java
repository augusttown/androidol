/*
 * 
 */
package com.androidol.basetypes;

/**
 * Class: com.esri.android.openlayers.basetypes.Size
 * Instances of this class represent a width/height pair
 */
public class Size {
	
	// ===========================================================
	// Constants
	// ===========================================================
	
	// ===========================================================
	// Fields
	// ===========================================================
    private double width = 0.0;  
    private double height = 0.0;

    // ===========================================================
	// Constructors
	// ===========================================================
    /**
     * Constructor: Size
     * Create an instance of com.esri.android.openlayers.basetypes.Size
     *
     * @param width {double}
     * @param height {double}
     * 
     */
    public Size(double width, double height) {
    	this.width = width;
    	this.height = height;
    }
    
    /**
     * APIMethod: toString
     * Return the string representation of a size object
     *
     * @returns {String} 
     *  		The string representation of OpenLayers.Size object. 
     *  		(ex. <i>"w=55,h=66"</i>)
     */
    @Override
    public String toString() {
        return "width:" + this.width + ", height:" + this.height;
    }

    /**
     * APIMethod: clone
     * Create a clone of this size object
     *
     * @returns {@link Size} 
     * 			A new OpenLayers.Size object with the same width and height values
     */
    public Size clone() {
        return new Size(this.width, this.height);
    }

    /**
     * APIMethod: equals
     * Determine where this size is equal to another
     *
     * @param size {@link Size}
     *
     * @returns {boolean} The passed in size has the same h and w properties as this one.
     * 			sNote that if sz passed in is null, returns false.
     */
    public boolean equals(Size size) {
    	boolean equals = false;
        if(size != null) {
            equals = ((this.width == size.width && this.height == size.height));
        }
        return equals;
    }

	/**
	 * @return the width
	 */
	public double getWidth() {
		return width;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(double width) {
		this.width = width;
	}

	/**
	 * @return the height
	 */
	public double getHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(double height) {
		this.height = height;
	}    
}
