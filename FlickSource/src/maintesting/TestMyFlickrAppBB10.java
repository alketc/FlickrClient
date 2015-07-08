package maintesting;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.aetrion.flickr.Flickr;
import com.aetrion.flickr.FlickrException;
import com.aetrion.flickr.Locations;
import com.aetrion.flickr.REST;
import com.aetrion.flickr.RequestContext;
import com.aetrion.flickr.photos.Exif;
import com.aetrion.flickr.photos.GeoData;
import com.aetrion.flickr.photos.Photo;
import com.aetrion.flickr.photos.PhotoList;
import com.aetrion.flickr.photos.PhotosInterface;
import com.aetrion.flickr.photos.SearchParameters;

public class TestMyFlickrAppBB10{

	static int picXpage = 1;

	static String apiKey = "1ac6de35ced1f6b784abb562e1713713";
    static String sharedSecret = "63960291a0599594";
    
    
    Flickr f;
    REST rest;
    RequestContext requestContext;
    
    public TestMyFlickrAppBB10()
      throws ParserConfigurationException, IOException {
        f = new Flickr(apiKey,sharedSecret,new REST());
        requestContext = RequestContext.getRequestContext();
 
    }
	
    public void showActivityBB(String city, double v1x, double v1y,double v3x, double v3y,Calendar start, Calendar end, String output_file) throws IOException {
    	
    	PrintWriter out = new PrintWriter(new FileWriter(new File(output_file)));
    	
    	PhotosInterface pi = f.getPhotosInterface();
    	SearchParameters params = new SearchParameters();
    	params.setMinTakenDate(start.getTime());
    	params.setMaxTakenDate(end.getTime());
    	System.out.println(String.valueOf(v1x));
    	params.setBBox(String.valueOf(v1x),String.valueOf (v1y),String.valueOf(v3x),String.valueOf (v3y));
    	params.setHasGeo(true);
    	
    	int counter=0;
    	
    	PhotoList list = null;
		try {
			list = pi.search(params, picXpage, 1);
		} catch (FlickrException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	System.out.println("\nRetrievind data around of "+city+"from " +""+start.getTime()+" to "+end.getTime());
    	System.out.println(list.getTotal()+" pictures being retrieved...");
    	
    	for(int k=1; k<=list.getPages();k++) {
    		try {
				list = pi.search(params, picXpage, k);
			} catch (FlickrException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	for(int i=0; i<list.getPerPage();i++) {
	    		
	    			Photo x = (Photo)list.get(i);
	    			String description,title="";
	    			try {
						x = pi.getInfo(x.getId(), x.getSecret());
					} catch (SAXException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (FlickrException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
	    			//String description=x.getDescription().replace('\n', ' ');
	    			//String title=x.getTitle().replace('\n', ' ');
		    		String date = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM,
		    				Locale.ITALY).format(x.getDateTaken());
		    		GeoData geo = x.getGeoData();
		    		String username = x.getOwner().getUsername();
		    		description=(x.getDescription());
		    		if (description!=null)
		    			description=description.replace('\n', ' ');
		    		title=(x.getTitle());
		    		if (title!=null)
		    			title=title.replace('\n', ' ');
		    		if(geo!=null){
		    			//out.println(username+"*"+geo.getLatitude()+"*"+geo.getLongitude()+"*"+geo.getAccuracy()+"*"+date);	
		    			if (description!=null)	
		    			try {
		    		        // Convert from Unicode to UTF-8
		    		        //String string = "abc\u5639\u563b";
		    		        byte[] utf8 = description.getBytes("UTF-8");
		    		    
		    		        // Convert from UTF-8 to Unicode
		    		        description = new String(utf8, "UTF-8");
		    		        System.out.println(description);
		    		    } catch (UnsupportedEncodingException e) {
		    		    	System.out.println(e);
		    		    }
		    		    
		    			out.println(username+"\t"+title+"\t"+description+"\t"+geo.getLatitude()+"\t"+geo.getLongitude()+"\t"+geo.getAccuracy()+"\t"+date);
		    			counter++;
		    		}
		    		else {
		    			try{
		    			Iterator <Exif>iter = pi.getExif(x.getId(), x.getSecret()).iterator();
		    			while(iter.hasNext()) {
		    				Exif exif = iter.next();
		    				String raw = exif.getLabel()+" "+exif.getRaw();
		    				if(raw.contains("atitude") || raw.contains("ongitude")) // avoid 1st letter for case sensitive
		    				System.out.println("!!! "+exif.getLabel()+" "+exif.getRaw());
		    			}
		    			}catch(Exception e){
		    				
		    			}
		    			System.out.println("broken picture @ page= "+k+" pic= "+i);
		    		}
	    	}
	    	System.out.println((100*k)/list.getPages()+"% Complete. Pictures "+counter+" on "+list.getTotal());
    	}
    	out.close();
    }

     public static void main (String [] args) {
	
     try
        {
    	 
         TestMyFlickrAppBB10 tfa = new TestMyFlickrAppBB10();

         Locations l = Locations.UMBRIA;
         String city = l.getDescription();
         String directoryName = l.getDescription()+"_bbox";
         File dataDir = new File(directoryName);
 		
   	  // if the directory does not exist, create it
   	     if (!dataDir.exists()){
   	       System.out.println("Creo la directory: " + directoryName);
   	          dataDir.mkdir();
   	}
   	
   	
    		try{
    		tfa.showActivityBB(city,l.getV1x() , l.getV1y() ,l.getV3x(), l.getV3y(),
    				new GregorianCalendar(2014,11,17,0,1,1),
    				new GregorianCalendar(2014,11,31,23,59,59),
    				directoryName+"/"+17+" output "+31+" "+11+"_"+2014+".csv");
    		}catch(Exception e ){
    			
    		}
    	
    
        
        }catch (Exception e) {
          e.printStackTrace();
        }
   	 
    //System.exit(0);	


}
	
}
