package net.sf.jtmdb;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import net.sf.jtmdb.Log.Verbosity;

/**
 * This class contains the options used in the Movie.browse method.
 *
 * @author Savvas Dalkitsis
 */
public class BrowseOptions implements Serializable {

	private static final long serialVersionUID = 3834662739424850032L;

	/**
	 * Enumeration of the types of order for the results.
	 *
	 * @author Savvas Dalkitsis
	 */
	public static enum ORDER_BY {
		/**
		 * Order by rating.
		 */
		RATING("rating"),
		/**
		 * Order by release date (default).
		 */
		RELEASE("release"),
		/**
		 * Order by title.
		 */
		TITLE("title");

		private String name;

		private ORDER_BY(String name) {
			this.name = name;
		}

		protected String getName() {
			return name;
		}
	}

	/**
	 * Enumeration of the order of the results.
	 *
	 * @author Savvas Dalkitsis
	 */
	public static enum ORDER {
		/**
		 * Ascending order (default).
		 */
		ASC("asc"),
		/**
		 * Descending order.
		 */
		DESC("desc");

		private String name;

		private ORDER(String name) {
			this.name = name;
		}

		protected String getName() {
			return name;
		}
	}

	/**
	 * Enumeration providing AND, OR operators.
	 *
	 * @author Savvas Dalkitsis
	 */
	public static enum BOOLEAN {
		/**
		 * Boolean AND.
		 */
		AND("and"),
		/**
		 * Boolean OR.
		 */
		OR("or");

		private String name;

		private BOOLEAN(String name) {
			this.name = name;
		}

		protected String getName() {
			return name;
		}
	}

	/**
	 * The type of ordering for the results.
	 */
	private ORDER_BY orderBy = ORDER_BY.RELEASE;
	/**
	 * The order of the results.
	 */
	private ORDER order = ORDER.ASC;
	/**
	 * The page of the results.
	 */
	private Integer page = null;
	/**
	 * The amount of results per page.
	 */
	private Integer perPage = null;
	/**
	 * The query of the search.
	 */
	private String query = null;
	/**
	 * The minimum rating of the results.
	 */
	private Float ratingMin = null;
	/**
	 * The maximum rating of the results.
	 */
	private Float ratingMax = null;
	/**
	 * The genres of the search.
	 */
	private Set<Integer> genres = null;
	/**
	 * The boolean operator to apply to the genres search.
	 */
	private BOOLEAN genreSelector = BOOLEAN.AND;
	/**
	 * The minimum release date for the search.
	 */
	private Date releaseMin = null;
	/**
	 * The maximum release date for the search.
	 */
	private Date releaseMax = null;
	/**
	 * The year in which movies where released.
	 */
	private Integer year = null;
	/**
	 * The certifications of the search.
	 */
	private Set<String> certifications = null;
	/**
	 * The companies of the search.
	 */
	private Set<String> companies = null;
	/**
	 * The countries of the search.
	 */
	private Set<String> countries = null;
	/**
	 * The minimum amount of votes a Movie must have to appear in the results.
	 */
	private Integer minVotes = null;

	/**
	 * Gets the type of the ordering of the results. Default is
	 * ORDER_BY.RELEASE.
	 *
	 * @return The type of the ordering of the results. Default is
	 *         ORDER_BY.RELEASE.
	 */
	public ORDER_BY getOrderBy() {
		return orderBy;
	}

	/**
	 * Sets the type of the ordering of the results.
	 *
	 * @param orderBy
	 *            The type of the ordering of the results.
	 */
	public void setOrderBy(ORDER_BY orderBy) {
		this.orderBy = orderBy;
	}

	/**
	 * Gets the order of the results. Default is ORDER.ASC.
	 *
	 * @return The order of the results. Default is ORDER.ASC.
	 */
	public ORDER getOrder() {
		return order;
	}

	/**
	 * Sets the order of the results.
	 *
	 * @param order
	 *            The order of the results.
	 */
	public void setOrder(ORDER order) {
		this.order = order;
	}

	/**
	 * Clears the page information from the search. Also clears the PerPage
	 * information.
	 */
	public void clearPage() {
		this.page = null;
		this.perPage = null;
	}

	/**
	 * Gets the current page of the results.
	 *
	 * @return The current page of the results. Will be null if the page is not
	 *         set or has been cleared.
	 */
	public Integer getPage() {
		return page;
	}

	/**
	 * Sets the page of the results. Must be greater than 0.
	 *
	 * @param page
	 *            The page of the results. Must be greater than 0.
	 */
	public void setPage(int page) {
		if (page > 0) {
			this.page = page;
		}
	}

	/**
	 * Clears the per page information from the search. Does NOT clear the page
	 * information as it can be used without this setting.
	 */
	public void clearPerPage() {
		this.perPage = null;
	}

	/**
	 * Gets the amount of results displayed in each page of the results. Will be
	 * null if the per page is not set or has been cleared.
	 *
	 * @return The amount of results displayed in each page of the results. Will
	 *         be null if the per page is not set or has been cleared.
	 */
	public Integer getPerPage() {
		return perPage;
	}

	/**
	 * Sets the amount of results to display per page. Must be greater than 0.
	 *
	 * @param perPage
	 *            The amount of results to display per page. Must be greater
	 *            than 0.
	 */
	public void setPerPage(int perPage) {
		if (perPage > 0) {
			this.perPage = perPage;
		}
	}

	/**
	 * Clears the query from the search.
	 */
	public void clearQuery() {
		this.query = null;
	}

	/**
	 * Gets the query for the search. Will be null if it has not been set or has
	 * been cleared.
	 *
	 * @return The query for the search. Will be null if it has not been set or
	 *         has been cleared.
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * Sets the query for the search. If you pass null it acts like
	 * {@link #clearQuery()}.
	 *
	 * @param query
	 *            The query for the search. If you pass null it acts like
	 *            {@link #clearQuery()}.
	 */
	public void setQuery(String query) {
		this.query = query;
	}

	/**
	 * Clears the ratingMin and ratingMax settings from the search.
	 */
	public void clearRatings() {
		ratingMin = null;
		ratingMax = null;
	}

	/**
	 * Gets the minimum rating a movie must have to appear in the results.
	 *
	 * @return The minimum rating a movie must have to appear in the results.
	 */
	public Float getRatingMin() {
		return ratingMin;
	}

	/**
	 * Gets the maximum rating a movie must have to appear in the results.
	 *
	 * @return The maximum rating a movie must have to appear in the results.
	 */
	public Float getRatingMax() {
		return ratingMax;
	}

	/**
	 * Sets the minimum rating a movie must have to appear in the results. Must
	 * be greater or equal to 0 and smaller than or equal to 10. If the maximum
	 * rating has not been set or if it has been set but is smaller than this
	 * value, this will set it to 10.
	 *
	 * @param ratingMin
	 *            The minimum rating a movie must have to appear in the results.
	 *            Must be greater or equal to 0 and smaller than or equal to 10.
	 *            If the maximum rating has not been set or if it has been set
	 *            but is smaller than this value, this will set it to 10.
	 */
	public void setRatingMin(float ratingMin) {
		if (ratingMin < 0 || ratingMin > 10) {
			return;
		}
		this.ratingMin = ratingMin;
		if (ratingMax == null || ratingMax < ratingMin) {
			ratingMax = 10F;
		}
	}

	/**
	 * Sets the maximum rating a movie must have to appear in the results. Must
	 * be greater or equal to 0 and smaller than or equal to 10. If the minimum
	 * rating has not been set or if it has been set but is greater than this
	 * value, this will set it to 0.
	 *
	 * @param ratingMax
	 *            The maximum rating a movie must have to appear in the results.
	 *            Must be greater or equal to 0 and smaller than or equal to 10.
	 *            If the minimum rating has not been set or if it has been set
	 *            but is greater than this value, this will set it to 0.
	 */
	public void setRatingMax(float ratingMax) {
		if (ratingMax > 10 || ratingMax < 0) {
			return;
		}
		this.ratingMax = ratingMax;
		if (ratingMin == null || ratingMin > ratingMax) {
			ratingMin = 0F;
		}
	}

	/**
	 * Clears the genres from the search.
	 */
	public void clearGenres() {
		genres = null;
	}

	/**
	 * Gets the set of genres for the search.
	 *
	 * @return The set of genres for the search.
	 */
	public Set<Integer> getGenres() {
		return genres;
	}

	/**
	 * Adds the genres to the list for the search.
	 *
	 * @param genreIDs
	 *            The IDs of the genres to add.
	 */
	public void addGenres(int... genreIDs) {
		if (genres == null) {
			genres = new LinkedHashSet<Integer>();
		}
		for (int ID : genreIDs) {
			genres.add(ID);
		}
	}

	/**
	 * Adds the genres to the list for the search.
	 *
	 * @param genres
	 *            The genres to add.
	 */
	public void addGenres(Genre... genres) {
		if (this.genres == null) {
			this.genres = new LinkedHashSet<Integer>();
		}
		for (Genre g : genres) {
			this.genres.add(g.getID());
		}
	}

	/**
	 * Attempts to add genres to the list for the search by name. <br />
	 * <br />
	 * <strong>NOTE:</strong><br/>
	 * This calls the method Genre.getList which connects to the server to
	 * request a list of all the genres. Also note that depending on the
	 * language you have specified in the GeneralSettings, the list returned may
	 * be translated to another language and therefore there may be mismatches
	 * between the names you specified and the returned names, in which case the
	 * genres will not be added.
	 *
	 * @param genreNames
	 *            The names of genres to add.
	 */
	public void addGenres(String... genreNames) {
		try {
			Set<Genre> genres = Genre.getList().getSecond();
			addGenres(genres, genreNames);
		} catch (Exception e) {
			Log.log(e, Verbosity.ERROR);
		}
	}

	/**
	 * Attempts to add genres to the list for the search by name. It assumes
	 * that the genre name matches the name of one of the genres in the
	 * specified set. This method is useful because calling the method
	 * {@link #addGenres(String...)}, a connection to the server is made making
	 * it costly to use. With this method you can cache the list of genres
	 * initially by manually calling Genre.getList() and pass it to this method.<br />
	 * <br />
	 * <strong>NOTE:</strong><br/>
	 * As with the method {@link #addGenres(String...)}, the list returned by
	 * the method Genre.getList() may be translated and thus, the names may not
	 * match.
	 *
	 * @param genreList
	 *            The set of genres in which to look for the genre by name.
	 * @param genreNames
	 *            The names of genres to add.
	 */
	public void addGenres(Set<Genre> genreList, String... genreNames) {
		for (Genre g : genreList) {
			for (String genreName : genreNames) {
				if (g.getName().equals(genreName)) {
					addGenres(g.getID());
				}
			}
		}
	}

	/**
	 * Removes the genres from the search.
	 *
	 * @param genreIDs
	 *            The IDs of the genres to remove.
	 */
	public void removeGenres(int... genreIDs) {
		if (genres != null) {
			for (int ID : genreIDs) {
				genres.remove(ID);
			}
		}
	}

	/**
	 * Removes the genres from the search.
	 *
	 * @param genres
	 *            The genres to remove.
	 */
	public void removeGenres(Genre... genres) {
		if (this.genres != null) {
			for (Genre g : genres) {
				this.genres.remove(g.getID());
			}
		}
	}

	/**
	 * Attempts to remove genres from the list for the search by name. <br />
	 * <br />
	 * <strong>NOTE:</strong><br/>
	 * This calls the method Genre.getList which connects to the server to
	 * request a list of all the genres. Also note that depending on the
	 * language you have specified in the GeneralSettings, the list returned may
	 * be translated to another language and therefore there may be mismatches
	 * between the names you specified and the returned names, in which case the
	 * genres will not be removed.
	 *
	 * @param genreNames
	 *            The names of genres to remove.
	 */
	public void removeGenres(String... genreNames) {
		try {
			Set<Genre> genres = Genre.getList().getSecond();
			removeGenres(genres, genreNames);
		} catch (Exception e) {
			Log.log(e, Verbosity.ERROR);
		}
	}

	/**
	 * Attempts to remove genres to the list for the search by name. It assumes
	 * that the genre name matches the name of one of the genres in the
	 * specified set. This method is useful because calling the method
	 * {@link #removeGenres(String...)}, a connection to the server is made
	 * making it costly to use. With this method you can cache the list of
	 * genres initially by manually calling Genre.getList() and pass it to this
	 * method.<br />
	 * <br />
	 * <strong>NOTE:</strong><br/>
	 * As with the method {@link #removeGenres(String...)}, the list returned by
	 * the method Genre.getList() may be translated and thus, the names may not
	 * match.
	 *
	 * @param genreList
	 *            The set of genres in which to look for the genre by name.
	 * @param genreNames
	 *            The names of genres to remove.
	 */
	public void removeGenres(Set<Genre> genreList, String... genreNames) {
		for (Genre g : genreList) {
			for (String genreName : genreNames) {
				if (g.getName().equals(genreName)) {
					removeGenres(g.getID());
				}
			}
		}
	}

	/**
	 * Gets the genre selector for the search. Default is BOOLEAN.AND.
	 *
	 * @return The genre selector for the search. Default is BOOLEAN.AND.
	 */
	public BOOLEAN getGenreSelector() {
		return genreSelector;
	}

	/**
	 * Sets the genre selector for the search.
	 *
	 * @param genreSelector
	 *            The genre selector for the search.
	 */
	public void setGenreSelector(BOOLEAN genreSelector) {
		this.genreSelector = genreSelector;
	}

	/**
	 * Clears the releaseMin and releaseMax from the search.
	 */
	public void clearReleases() {
		releaseMin = null;
		releaseMax = null;
	}

	/**
	 * Gets the minimum release date a movie must have to appear in the results.
	 *
	 * @return The minimum release date a movie must have to appear in the
	 *         results.
	 */
	public Date getReleaseMin() {
		return releaseMin;
	}

	/**
	 * Gets the maximum release date a movie must have to appear in the results.
	 *
	 * @return The maximum release date a movie must have to appear in the
	 *         results.
	 */
	public Date getReleaseMax() {
		return releaseMax;
	}

	/**
	 * Sets the minimum release date a movie must have to appear in the results.
	 * If the maximum release date has not been set or if it has been set but is
	 * earlier than this date, this will set it to the latest date that can be
	 * represented. If null is passed, it acts like calling
	 * {@link #clearReleases()}. If not, this method calls {@link #clearYear()}
	 * since they cannot be used side by side.
	 *
	 * @param ratingMin
	 *            The minimum release date a movie must have to appear in the
	 *            results. If the maximum release date has not been set or if it
	 *            has been set but is earlier than this date, this will set it
	 *            to the latest date that can be represented. If null is passed,
	 *            it acts like calling {@link #clearReleases()}. If not, this
	 *            method calls {@link #clearYear()} since they cannot be used
	 *            side by side.
	 */
	public void setReleaseMin(Date releaseMin) {
		if (releaseMin == null) {
			clearReleases();
			return;
		}
		clearYear();
		this.releaseMin = releaseMin;
		if (releaseMax == null || releaseMax.before(releaseMin)) {
			releaseMax = new Date(Long.MAX_VALUE);
		}
	}

	/**
	 * Sets the maximum release date a movie must have to appear in the results.
	 * If the minimum release date has not been set or if it has been set but is
	 * later than this date, this will set it to the earliest date available
	 * (epoch). If null is passed, it acts like calling {@link #clearReleases()}
	 * . If not, this method calls {@link #clearYear()} since they cannot be
	 * used side by side.
	 *
	 * @param ratingMin
	 *            The maximum release date a movie must have to appear in the
	 *            results. If the minimum release date has not been set or if it
	 *            has been set but is later than this date, this will set it to
	 *            the earliest date available (epoch). If null is passed, it
	 *            acts like calling {@link #clearReleases()} . If not, this
	 *            method calls {@link #clearYear()} since they cannot be used
	 *            side by side.
	 */
	public void setReleaseMax(Date releaseMax) {
		if (releaseMax == null) {
			clearReleases();
			return;
		}
		clearYear();
		this.releaseMax = releaseMax;
		if (releaseMin == null || releaseMin.after(releaseMax)) {
			releaseMin = new Date(0);
		}
	}

	/**
	 * Clears the year from the search.
	 */
	public void clearYear() {
		year = null;
	}

	/**
	 * Gets the year setting for the search.
	 *
	 * @return The year setting for the search.
	 */
	public Integer getYear() {
		return year;
	}

	/**
	 * Sets the year setting for the search. Must be greater or equal to 0. This
	 * will call {@link #clearReleases()} since these settings cannot exist side
	 * by side.
	 *
	 * @param year
	 *            The year setting for the search. Must be greater or equal to
	 *            0. This will call {@link #clearReleases()} since these
	 *            settings cannot exist side by side.
	 */
	public void setYear(int year) {
		if (year < 0) {
			return;
		}
		clearReleases();
		this.year = year;
	}

	/**
	 * Clears the certifications from the search.
	 */
	public void clearCertifications() {
		certifications = null;
	}

	/**
	 * Gets the certifications for the search.
	 *
	 * @return The certifications for the search.
	 */
	public Set<String> getCertifications() {
		return certifications;
	}

	/**
	 * Add certifications to the search.
	 *
	 * @param certifications
	 *            The certifications to add to the search.
	 */
	public void addCertifications(String... certifications) {
		if (this.certifications == null) {
			this.certifications = new LinkedHashSet<String>();
		}
		for (String certification : certifications) {
			this.certifications.add(certification);
		}
	}

	/**
	 * Removes certifications from the search.
	 *
	 * @param certifications
	 *            The certifications to remove from the search.
	 */
	public void removeCertifications(String... certifications) {
		if (this.certifications != null) {
			for (String certification : certifications) {
				this.certifications.remove(certification);
			}
		}
	}

	/**
	 * Clears the companies from the search.
	 */
	public void clearCompanies() {
		companies = null;
	}

	/**
	 * Gets the companies for the search.
	 *
	 * @return The companies for the search.
	 */
	public Set<String> getCompanies() {
		return companies;
	}

	/**
	 * Adds companies to the search.
	 *
	 * @param companies
	 *            The companies to add to the search.
	 */
	public void addCompanies(String... companies) {
		if (this.companies == null) {
			this.companies = new LinkedHashSet<String>();
		}
		for (String company : companies) {
			this.companies.add(company);
		}
	}

	/**
	 * Removes companies from the search.
	 *
	 * @param companies
	 *            The companies to remove from the search.
	 */
	public void removeCompanies(String... companies) {
		if (this.companies != null) {
			for (String company : companies) {
				this.companies.remove(company);
			}
		}
	}

	/**
	 * Clears the countries from the search.
	 */
	public void clearCountries() {
		countries = null;
	}

	/**
	 * Gets the countries for the search.
	 *
	 * @return The countries for the search.
	 */
	public Set<String> getCountries() {
		return countries;
	}

	/**
	 * Adds countries to the search.
	 *
	 * @param countries
	 *            The countries to add to the search.
	 */
	public void addCountries(String... countries) {
		if (this.countries == null) {
			this.countries = new LinkedHashSet<String>();
		}
		for (String country : countries) {
			this.countries.add(country);
		}
	}

	/**
	 * Removes countries from the search.
	 *
	 * @param countries
	 *            The countries to remove from the search.
	 */
	public void removeCountries(String... countries) {
		if (this.countries != null) {
			for (String country : countries) {
				this.countries.remove(country);
			}
		}
	}

	/**
	 * Clears the minimum amount of votes from the search.
	 */
	public void clearMinVotes() {
		this.minVotes = null;
	}

	/**
	 * Gets the minimum amount of votes a Movie must have to appear in the
	 * results.
	 *
	 * @return The minimum amount of votes a Movie must have to appear in the
	 *         results.
	 */
	public Integer getMinVotes() {
		return minVotes;
	}

	/**
	 * Sets the minimum amount of votes a Movie must have in order to appear in
	 * the results. Must be greater or equal to 0.
	 *
	 * @param minVotes
	 *            The minimum amount of votes a Movie must have in order to
	 *            appear in the results. Must be greater or equal to 0.
	 */
	public void setMinVotes(int minVotes) {
		if (minVotes < 0) {
			return;
		}
		this.minVotes = minVotes;
	}

	/**
	 * Builds the query to be appended to the call to the server.
	 *
	 * @return The query to be appended to the call to the server.
	 */
	protected String buildQuery() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("order_by=").append(orderBy.getName()).append("&order=")
				.append(order.getName()).append(
						(getPage() == null) ? "" : "&page=" + getPage())
				.append(
						(getPerPage() == null) ? "" : "&per_page="
								+ getPerPage());
		String query = getQuery();
		if (query != null) {
			try {
				query = URLEncoder.encode(query, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				query = query.replaceAll(" ", "%20");
			}
			buffer.append("&query=" + query);
		}
		buffer.append((getRatingMin() == null) ? "" : "&rating_min="
				+ getRatingMin() + "&rating_max=" + getRatingMax());
		if (getGenres() != null && getGenres().size() > 0) {
			buffer.append("&genres=");
			for (int ID : getGenres()) {
				buffer.append(ID).append(",");
			}
			buffer.deleteCharAt(buffer.length() - 1);
			if (getGenres().size() > 1) {
				buffer.append("&genres_selector="
						+ getGenreSelector().getName());
			}
		}
		buffer.append(
				(releaseMin == null) ? "" : "&release_min="
						+ (getReleaseMin().getTime() / 1000) + "&release_max="
						+ (getReleaseMax().getTime() / 1000)).append(
				(getYear() == null) ? "" : "&year=" + getYear());
		if (getCertifications() != null && getCertifications().size() > 0) {
			buffer.append("&certifications=");
			for (String certification : getCertifications()) {
				buffer.append(certification).append(",");
			}
			buffer.deleteCharAt(buffer.length() - 1);
		}
		if (getCompanies() != null && getCompanies().size() > 0) {
			buffer.append("&companies=");
			for (String company : getCompanies()) {
				buffer.append(company).append(",");
			}
			buffer.deleteCharAt(buffer.length() - 1);
		}
		if (getCountries() != null && getCountries().size() > 0) {
			buffer.append("&countries=");
			for (String country : getCountries()) {
				buffer.append(country).append(",");
			}
			buffer.deleteCharAt(buffer.length() - 1);
		}
		buffer.append((getMinVotes() == null) ? "" : "&min_votes="
				+ getMinVotes());
		return buffer.toString();
	}
}
