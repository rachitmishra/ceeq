package in.ceeq;

import java.util.ArrayList;

import android.app.Application;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import com.android.volley.cache.DiskLruBasedCache.ImageCacheParams;
import com.android.volley.cache.SimpleImageLoader;

public class Ceeq extends Application {
		
	private SimpleImageLoader imageLoader;
	private static Ceeq instance;
	
	public void onCreate() {
		super.onCreate();
		instance = this;
		//BugSenseHandler.initAndStartSession(getApplicationContext(), "5996b3d9");
		//Crashlytics.start(this);
	}
	
	public static synchronized Ceeq getInstance(){
		return instance;
	}
	
	public SimpleImageLoader getImageCache() {
		if (imageLoader == null) {
			ImageCacheParams cacheParams = new ImageCacheParams(getApplicationContext(), "data/ceeq/cache");
			cacheParams.setMemCacheSizePercent(0.5f);

			ArrayList<Drawable> placeHolderDrawables = new ArrayList<Drawable>();
			placeHolderDrawables.add(getResources().getDrawable(R.drawable.ic_user));
			placeHolderDrawables.add(new ColorDrawable(android.R.color.transparent));

			imageLoader = new SimpleImageLoader(getApplicationContext(), placeHolderDrawables, cacheParams);
			imageLoader.setMaxImageSize(200);
			imageLoader.setFadeInImage(false);
			imageLoader.setContetResolver(getContentResolver());
		}
		
		return imageLoader;
	}
}
