package com.androidol.test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.androidol.Map;
import com.androidol.R;
import com.androidol.events.Event;
import com.androidol.events.TileEvents;
import com.androidol.layer.Layer;
import com.androidol.layer.osm.Mapnik;
import com.androidol.util.Util;
import com.androidol.util.tiles.StreamUtils;
import com.androidol.util.tiles.TileHttpLoader;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.ZoomControls;

public class MapActivity extends Activity {    

	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);
		
		final Map map = (Map)findViewById(R.id.map);
		ZoomControls zoomControls = (ZoomControls)findViewById(R.id.zoomcontrols);
		zoomControls.setOnZoomInClickListener(new OnClickListener() {
			public void onClick(View view) {
				map.zoomIn();
			}
	    });
		zoomControls.setOnZoomOutClickListener(new OnClickListener() {
			public void onClick(View view) {
				map.zoomOut();
			}
	    });		
    }
	
	@Override 
	protected void onStart() {
		super.onStart();		
	}	
}

