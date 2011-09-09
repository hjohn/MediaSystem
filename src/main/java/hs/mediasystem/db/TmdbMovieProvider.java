package hs.mediasystem.db;

import hs.mediasystem.Levenshtein;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TreeSet;

import net.sf.jtmdb.Movie;
import net.sf.jtmdb.MovieImages;
import net.sf.jtmdb.MoviePoster;

import org.json.JSONException;

public class TmdbMovieProvider implements ItemProvider {

  @Override
  public Item getItem(Item item) {
    Path path = item.getPath();
    final String fileName = item.getPath().getFileName().toString();
    String title = item.getTitle();
    String subtitle = item.getSubtitle();
    String year = extractYear(item.getReleaseDate());
    int seq = item.getSeason();
    
    try {
      String bestMatchingImdbNumber = null;
      
      if(item.getImdbId() != null) {
        bestMatchingImdbNumber = item.getImdbId();
      }
      
      if(bestMatchingImdbNumber == null) {
        TreeSet<Score> scores = new TreeSet<Score>(new Comparator<Score>() {
          @Override
          public int compare(Score o1, Score o2) {
            return Double.compare(o2.score, o1.score);
          }
        });
        
        for(Movie movie : Movie.search(title)) {
          String movieYear = extractYear(movie.getReleasedDate());
          double score = 0;

          if(movieYear.equals(year) && movieYear.length() > 0) {
            score += 100;
          }
          if(movie.getImdbID() != null) {
            score += 50;
          }

          String searchString = title;
          
          if(seq > 1) {
            searchString += " " + seq;
          }
          if(subtitle.length() > 0) {
            searchString += " " + subtitle;
          }
          
          System.out.println(movie.getName() + " -vs- " + searchString);
          double matchScore = Levenshtein.compare(movie.getName().toLowerCase(), searchString.toLowerCase());
          
          score += matchScore * 90;

          scores.add(new Score(movie, score));
          System.out.println(new Score(movie, score));
        }
        
        if(!scores.isEmpty()) {
          bestMatchingImdbNumber = scores.first().movie.getImdbID();
          System.out.println("Best was: " + scores.first());
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
        
        final byte[] cover = url != null ? Downloader.readURL(url) : null;

        return new Item(path) {{
          setImdbId(movie.getImdbID());
          setProvider("TMDB");
          setProviderId("" + movie.getID());
          setTitle(movie.getName());
          setLocalName(fileName);
          setCover(cover);
          setPlot(movie.getOverview());
          setRating((float)movie.getRating());
          setReleaseDate(movie.getReleasedDate());
          setRuntime(movie.getRuntime());
          setType("movie");
          
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
  
  private static class Score {
    private final Movie movie;
    private final double score;

    public Score(Movie movie, double score) {
      this.movie = movie;
      this.score = score;
    }
    
    @Override
    public String toString() {
      return String.format("Score[%10.2f, " + movie.getImdbID() + " : " + movie.getName() + " : " + movie.getReleasedDate() + "]", score); 
    }
  }
  

  
  private String extractYear(Date date) {
    if(date == null) {
      return "";
    }
    
    GregorianCalendar gc = new GregorianCalendar();
    gc.setTime(date);
    return "" + gc.get(Calendar.YEAR);
  }
}
