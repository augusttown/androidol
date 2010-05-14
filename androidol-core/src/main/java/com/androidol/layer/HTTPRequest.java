package com.androidol.layer;

import java.util.HashMap;

import android.content.Context;
import android.util.AttributeSet;

import com.androidol.util.Util;

public class HTTPRequest extends Layer {

	protected 		double 						URL_HASH_FACTOR 	= (Math.sqrt(5) - 1)/2;
	protected 		String 						url 				= null;

	protected 		String[] 					altUrls				= null;
	protected 		HashMap<String, String>		params				= null;
	
	// ===========================================================
	// Constructors
	// ===========================================================
	
	public HTTPRequest(Context context) {
		super(context);
		// TODO: set default url and params			
	}
	
	public HTTPRequest(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.url = attrs.getAttributeValue(ANDROIDOL_NAMESPACE, "url");
		// TODO: properly handle case when "url" is empty or an invalid url
		// TODO: initialize other request parameters
	}
	
	public void setParams(HashMap<String, String> params) {
		this.params = Util.upperCases(params); 
	}
	
	/**
	 * Old Constructor HTTPRequest
	 * 
	 * @param name
	 * @param url
	 * @param params
	 * @param options
	 */
	/*
	public HTTPRequest(String name, String url, HashMap<String, String> params, HashMap<String, Object> options) {
		super(name, options);
		this.url = url;
		// upper case all parameters
		this.params = Util.upperCases(params);	//extra HTTP request parameters you want to append		
	}
	*/
	
	/**
	 * API Method: destroy
	 * 
	 * @param setNewBaseLayer
	 */
	@Override
	public void destroy(boolean setNewBaseLayer) {
		this.url = null;
		this.params = null;
		super.destroy(setNewBaseLayer);
	}
	
	/**
	 * API Method: clone()
	 * 
	 * @return 
	 * a clone of the layer
	 */
	@Override
	public HTTPRequest clone() {
		// TODO: to be implemented
		return null;
	}
	
	/**
	 * API Method: mergeNewParams
	 * 
	 * @param newParams
	 * merge additional HTTP request parameters into existing list
	 */
	public void mergeNewParams(HashMap<String, String> newParams) {		
		this.params = Util.extend(this.params, newParams);
		//this.redraw();	// should redraw after new request parameters being merged?
	}
	
	/**
	 * API Method: selectUrl
	 * 
	 * @param paramString
	 * @param urls
	 * @return
	 * return a url from alternative urls
	 */
	public String selectUrl(String paramString, String[] urls) {
		// TODO: to be understood
		/*
		double product = 1;
        for(int i=0,len=paramString.length(); i<len; i++) { 
            product *= paramString.charAt(i) * this.URL_HASH_FACTOR; 
            product -= Math.floor(product); 
        }        
        return urls[(int)Math.round(Math.floor(product * urls.length))];
        */
		return urls[0]; // always return the first one now
	}
	
	/**
	 * API Method: getFullRequestString
	 * 
	 * @param newParams
	 * @param altUrl
	 * @return
	 * construct the full url string for the layer
	 */
	public String getFullRequestString(HashMap<String, String> newParams, String altUrl) {
        String url = (altUrl!=null) ? altUrl : this.url;
        
        HashMap<String, String> allParams = Util.extend(null, this.params);
        allParams = Util.extend(allParams, newParams);
        String paramsString = Util.getQueryString(allParams);
        
	    if(this.altUrls != null) {
            url = this.selectUrl(paramsString, this.getUrls());
        }  
        // TODO: upper case all query string parameter key to avoid duplicate       
	    //paramsString = Util.getQueryString(allParams);
        
        String requestString = url;        
        // add '?' or '&' in front of query string, right after base url
        if(paramsString != "") {
        	String lastServerChar = url.substring(url.length() - 1);
            if((lastServerChar == "&") || (lastServerChar == "?")) {
                requestString += paramsString;
            } else {
                if(url.indexOf('?') == -1) {
                    requestString += '?' + paramsString;
                } else {
                    requestString += '&' + paramsString;
                }
            }
        }
        return requestString;	
	}
	
	/**
	 * API Method: getUrls
	 * 
	 * @return
	 * return an array of urls which is a union of this.url and this.altUrls 
	 */
	public String[] getUrls() {
		String[] urls = null;		
		if((altUrls == null) || (altUrls.length == 0)) {
			urls = new String[1];
			urls[0] = this.url;
		} else {
			urls = new String[altUrls.length+1];
			urls[0] = this.url;
			for(int i=1; i<=altUrls.length; i++) {
				urls[i] = altUrls[i-1];
			}
		}		
		return urls;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return this.url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

}
