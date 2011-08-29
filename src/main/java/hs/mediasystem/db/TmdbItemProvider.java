package hs.mediasystem.db;

import hs.mediasystem.screens.movie.Element;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import net.sf.jtmdb.Movie;
import net.sf.jtmdb.MovieImages;
import net.sf.jtmdb.MoviePoster;

import org.json.JSONException;

public class TmdbItemProvider implements ItemProvider {

  @Override
  public Item getItem(Element element) throws ItemNotFoundException {
    return getItem(element.getPath().toString(), element.getTitle(), element.getYear(), element.getImdbNumber());
  }

  @Override
  public Item getItem(final String fileName, String title, String year, String imdbNumber) throws ItemNotFoundException {
    try {
      String bestMatchingImdbNumber = null;
      
      if(imdbNumber != null) {
        bestMatchingImdbNumber = imdbNumber;
      }
      
      if(bestMatchingImdbNumber == null) {
        for(Movie movie : Movie.search(title)) {
          String possibleImdbNumber = movie.getImdbID();
          
          System.out.println("Checking possible number : " + movie.getImdbID() + " : " + movie.getName());
  //        db.search(possibleImdbNumber);
          
          if(bestMatchingImdbNumber == null) {
            bestMatchingImdbNumber = possibleImdbNumber;
          }
          
          Date date = movie.getReleasedDate();
          GregorianCalendar gc = new GregorianCalendar();
          gc.setTime(date);
          
          String imdbYear = "" + gc.get(Calendar.YEAR);
          
          if(year == null || imdbYear.equals(year)) {
            bestMatchingImdbNumber = possibleImdbNumber;
            break;
          }
        }
      }
      
      if(bestMatchingImdbNumber != null) {
        System.out.println("best mathcing imdb number: " + bestMatchingImdbNumber);
        final Movie movie = Movie.imdbLookup(bestMatchingImdbNumber);
  
        System.out.println("Found movie:");
        System.out.println("name: " + movie.getName());
        System.out.println("released date: " + movie.getReleasedDate());
        System.out.println("type: " + movie.getMovieType());
        System.out.println("overview: " + movie.getOverview());
        System.out.println("runtime: " + movie.getRuntime());

        final MovieImages images = movie.getImages();
        URL url = null;

        if(images.posters.size() > 0) {
          MoviePoster poster = images.posters.iterator().next();
          
          url = poster.getLargestImage();
        }
        
        final byte[] cover = url != null ? readURL(url) : null;

        return new Item() {{
          setImdbId(movie.getImdbID());
          setTmdbId("" + movie.getID());
          setTitle(movie.getName());
          setLocalName(fileName);
          setCover(cover);
          setPlot(movie.getOverview());
          setRating((float)movie.getRating());
          setReleaseDate(movie.getReleasedDate());
          setRuntime(movie.getRuntime());
          
//          for(CastInfo castInfo : movie.getCast()) {
//            castInfo.getCharacterName();
//            castInfo.getID();
//            castInfo.getName();
//            castInfo.getThumb();
//            
//            addCastMember(castInfo.getCastID(), castInfo.getName(), castInfo.getCharacterName());
//          }
        }};
      }
      else {
        throw new RuntimeException("cannot get movie details");
      }
    }
    catch(IOException e) {
      throw new RuntimeException(e);
    }
    catch(JSONException e) {
      throw new RuntimeException(e);
    }
  }
  
  private static final byte[] readURL(URL url) {
    ByteArrayOutputStream bais = new ByteArrayOutputStream();
    InputStream is = null;

    try {
      is = url.openStream();
      byte[] byteChunk = new byte[4096];
      int n;

      while((n = is.read(byteChunk)) > 0) {
        bais.write(byteChunk, 0, n);
      }
      
      return bais.toByteArray();
    }
    catch(IOException e) {
      System.err.println("Error reading url: " + url + ": " + e);
      return null;
    }
    finally {
      if (is != null) { 
        try {
          is.close();
        }
        catch(IOException e) {
          // don't care
        } 
      }
    }
  }
}
