package com.androidol.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.util.Log;
import com.androidol.constants.UtilConstants;

public class Util implements UtilConstants {
	
	/**
	 * static field used to generate unique id
	 */
	private static long lastSeqID = 0;
		
	/**
	 * API Method: getInchesPerUnit
	 * 
	 * @param units
	 * 
	 * @return inches
	 * inches per given unit
	 */
	public static double getInchesPerUnit(String units) {
		double inches = 0.0;
		if(units.equalsIgnoreCase("in") || units.equalsIgnoreCase("inches")) {
			inches = 1.0;
		}
		if(units.equalsIgnoreCase("ft") || units.equalsIgnoreCase("feets")) {
			inches = 12.0;
		}
		if(units.equalsIgnoreCase("m") || units.equalsIgnoreCase("meters")) {
			inches = 39.3701;
		}
		if(units.equalsIgnoreCase("km") || units.equalsIgnoreCase("kolimeters")) {
			inches = 39370.1;
		}
		if(units.equalsIgnoreCase("dd") || units.equalsIgnoreCase("degrees")) {
			inches = 4374754.0;
		}
		return inches;
	}
	
	/**
	 * private method: normalizeScale
	 * 
	 * @param scale
	 * @return normalized scale
	 */
	private static double normalizeScale(double scale) {
		double normScale = (scale > 1.0) ? (1.0 / scale) : scale;
	    return normScale;
	}
	
	/**
	 * API Method: getResolutionFromScale
	 * 
	 * @param scale
	 * @param units
	 * 
	 * @return resolution
	 * calculate resolution based on scale and units
	 */
	public static double getResolutionFromScale(double scale, String units) {
	    if(units == null) {
	        units = "degrees";
	    }
	    double normScale = normalizeScale(scale);	
	    double resolution = 1 / (normScale * getInchesPerUnit(units) * DOTS_PER_INCH);
	    return resolution;
	}
	
	/**
	 * API Method: getScaleFromResolution
	 * 
	 * @param resolution
	 * @param units
	 * 
	 * @return scale
	 * calculate scale based on resolution and units
	 */
	public static double getScaleFromResolution(double resolution, String units) {
		if(units.equalsIgnoreCase("") || units == null) {
	        units = "degrees";
	    }	
	    double scale = resolution * getInchesPerUnit(units) * DOTS_PER_INCH;
	    return scale;
	}

	// ===========================================================
	// utilities functions for logging
	// ===========================================================	
	
	public static void printDebugMessage(String message) {
		if(ISDEBUGMODE) {
			Log.d(DEBUGTAG, message);
		}
	}
	
	public static void printErrorMessage(String message) {		
		Log.e(DEBUGTAG, message);	
	}
	
	public static void printErrorMessage(String message, Throwable e) {		
		Log.e(DEBUGTAG, message, e);	
	}
	
	public static void printWarningMessage(String message) {		
		Log.w(DEBUGTAG, message);	
	}
	
	// ===========================================================
	// other utilities functions 
	// ===========================================================
	
	/**
	 * API Method: extend
	 * merge all objects in one hash to another
	 * 
	 * @param target
	 * @param source
	 */
	public static HashMap<String, String> extend(HashMap<String, String> target, HashMap<String, String> source) {
		HashMap<String, String> results = new HashMap<String, String>();
		String name = "";
		String value = "";
		if(target!=null && target.size()>0) {
			Set<Map.Entry<String, String>> keys = target.entrySet();
			for (Map.Entry<String, String> key : keys) {
				name = key.getKey();
				value = target.get(name); 
				results.put(name, value);
			}			
		}
		if(source!=null && source.size()>0) {
			Set<Map.Entry<String, String>> keys = source.entrySet();
			for (Map.Entry<String, String> key : keys) {
				name = key.getKey();
				value = source.get(name); 				
				results.remove(name);
				results.put(name, value);				
			}		
		}
		return results;
	}
	
	/**
	 * API Method: applyDefaults
	 * 
	 * @param target
	 * @param source
	 * 
	 */
	public static HashMap<String, String> applyDefaults(HashMap<String, String> target, HashMap<String, String> defaults) {		
		HashMap<String, String> results = new HashMap<String, String>();
		String name = "";
		String value = "";
		if(target != null && target.size()>0) {
			Set<Map.Entry<String, String>> keys = target.entrySet();
			for (Map.Entry<String, String> key : keys) {
				name = key.getKey();
				value = target.get(name); 
				results.put(name, value);
			}			
		}
		if(defaults!=null && defaults.size()>0) {
			Set<Map.Entry<String, String>> keys = defaults.entrySet();
			for (Map.Entry<String, String> key : keys) {
				name = key.getKey();
				value = defaults.get(name); 
				if(results.containsKey(name) == false) {
					results.put(name, value);
				}
			}		
		}
		return results;
	}
	
	/**
	 * API Method: upperCases
	 * 
	 * @param source
	 * @return
	 */
	public static HashMap<String, String> upperCases(HashMap<String, String> source) {
		HashMap<String, String> results = new HashMap<String, String>();
		String name = "";
		String value = "";
		if(source!=null && source.size()>0) {
			Set<Map.Entry<String, String>> keys = source.entrySet();
			for (Map.Entry<String, String> key : keys) {
				name = key.getKey();
				value = source.get(name); 
				results.put(name.toUpperCase(), value);
			}		
		}
		return results;
	}
	
	
	/**
	 * API Method: getParameterString
	 * 
	 * @param params
	 * 
	 * @return query string
	 * encode a url query string	
	 */
	public static String getQueryString(HashMap<String, String> params) {
		String name = "";
		String value = "";
		String queryString = "";
		// does not enforce the order of elements
		/*
		Set<Map.Entry<String, String>> keys = params.entrySet();
		for(Map.Entry<String, String> key : keys) {			
			name = key.getKey();
			value = params.get(name); 
			queryString = queryString + "&" + name + "=" + value;
		}
		*/
		// enforce the order of elements
		Set<String> keys = params.keySet();
		List<String> keyList = new ArrayList<String>(keys);
		Collections.sort(keyList);
		for(String key : keyList) {
			name = key;
			value = params.get(name);
			queryString = queryString + "&" + name + "=" + value;
		}
		return queryString;
	}
	
	/**
	 * API Method: createUniqueID
	 * 
	 * @return unique id string
	 */
	public static String createUniqueID(String prefix) {
		if(prefix == null || prefix.equalsIgnoreCase("")==true) {
	        prefix = "id_";
	    }
	    lastSeqID = lastSeqID + 1; 
	    return prefix + String.valueOf(lastSeqID); 
	}
	
}
