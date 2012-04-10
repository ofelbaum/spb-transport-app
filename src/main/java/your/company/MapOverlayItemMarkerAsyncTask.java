package your.company;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.concurrent.ExecutionException;

/**
 * User: alexey.emelyanenko@gmail.com
 * Date: 4/10/12 2:02 AM
 */
public class MapOverlayItemMarkerAsyncTask extends AsyncTask<String, Void, Bitmap> {

	private OverlayItem overlayitem;
	private MapView mapView;
    private String[] params;

	public MapOverlayItemMarkerAsyncTask(OverlayItem overlayitem, MapView mapView) {
		this.overlayitem = overlayitem;
		this.mapView = mapView;
	}

	@Override
	protected Bitmap doInBackground(String... params) {
        System.out.println("START " + Thread.currentThread().getName());
        this.params = params;
		Bitmap bitmap = null;
		InputStream in = null;
		try {
			in = fetch(params[0]);
			bitmap = BitmapFactory.decodeStream(in, null, null);
			in.close();
			return bitmap;
		} catch (IOException e1) {
			return null;
		}
	}

	private static InputStream fetch(String address) throws MalformedURLException,IOException {
	    HttpGet httpRequest = new HttpGet(URI.create(address) );
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpResponse response = (HttpResponse) httpclient.execute(httpRequest);
	    HttpEntity entity = response.getEntity();
	    BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
	    InputStream instream = bufHttpEntity.getContent();
	    return instream;
	}

	@Override
	protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);
		if (result != null) {
			Drawable drawable = new BitmapDrawable(Resources.getSystem(), result);
            int zl = 2;
			drawable.setBounds(-drawable.getIntrinsicWidth() / zl, -drawable.getIntrinsicHeight() / zl, drawable.getIntrinsicWidth() / zl, drawable.getIntrinsicHeight() / zl);
			overlayitem.setMarker(drawable);
			mapView.invalidate();
            //result.recycle();
		}
        System.out.println("STOP " + Thread.currentThread().getName());

        final MapOverlayItemMarkerAsyncTask task = new MapOverlayItemMarkerAsyncTask(overlayitem, mapView);
//        AsyncTask<String, Void, Bitmap> asyncTask = task.execute("http://transport.orgp.spb.ru/cgi-bin/mapserv?TRANSPARENT=TRUE&FORMAT=image%2Fpng&LAYERS=vehicle_bus&MAP=vehicle_typed.map&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&STYLES=&SRS=EPSG%3A900913&_OLSALT=0.1508798657450825&BBOX=3321583.9050831,8354817.803403,3433590.4458589,8415634.772084&WIDTH=480&HEIGHT=800");
        AsyncTask<String, Void, Bitmap> asyncTask = task.execute("http://transport.orgp.spb.ru/cgi-bin/mapserv?TRANSPARENT=TRUE&FORMAT=image%2Fpng&LAYERS=vehicle_bus&MAP=vehicle_typed.map&SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&STYLES=&SRS=EPSG%3A900913&_OLSALT=0.5802055234089494&BBOX=3365039.2729075,8377526.3272622,3393040.9081015,8392730.5694324&WIDTH=1431&HEIGHT=777");
        //result.recycle();

    }



}

