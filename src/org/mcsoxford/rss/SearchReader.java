package org.mcsoxford.rss;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * HTTP client to retrieve and parse RSS 2.0 feeds. Callers must call
 * {@link SearchReader#close()} to release all resources.
 * 
 * @author Mr Horn
 */
public class SearchReader implements java.io.Closeable {

  /**
   * Thread-safe {@link HttpClient} implementation.
   */
  private final HttpClient httpclient;

  /**
   * Thread-safe RSS parser SPI.
   */
  private final RSSParserSPI parser;

  /**
   * Instantiate a thread-safe HTTP client to retrieve RSS feeds. The injected
   * {@link HttpClient} implementation must be thread-safe.
   * 
   * @param httpclient thread-safe HTTP client implementation
   * @param parser thread-safe RSS parser SPI implementation
   */
  public SearchReader(HttpClient httpclient, RSSParserSPI parser) {
    this.httpclient = httpclient;
    this.parser = parser;
  }

  /**
   * Instantiate a thread-safe HTTP client to retrieve and parse RSS feeds.
   * Default RSS configuration capacity values are used.
   */
  public SearchReader() {
    this(new DefaultHttpClient(), new SeachParser(new RSSConfig()));
  }

  /**
   * Instantiate a thread-safe HTTP client to retrieve and parse RSS feeds.
   * Internal memory consumption and load performance can be tweaked with
   * {@link RSSConfig}.
   */
  public SearchReader(RSSConfig config) {
    this(new DefaultHttpClient(), new SeachParser(config));
  }

  /**
   * Send HTTP GET request and parse the XML response to construct an in-memory
   * representation of an RSS 2.0 feed.
   * 
   * @param uri RSS 2.0 feed URI
   * @return in-memory representation of downloaded RSS feed
   * @throws RSSReaderException if RSS feed could not be retrieved because of
   *           HTTP error
   * @throws RSSFault if an unrecoverable IO error has occurred
   */
  public RSSFeed load(String uri) throws RSSReaderException {
    final HttpGet httpget = new HttpGet(uri);

    InputStream feedStream = null;
    try {
      // Send GET request to URI
      final HttpResponse response = httpclient.execute(httpget);

      // Check if server response is valid
      final StatusLine status = response.getStatusLine();
      if (status.getStatusCode() != HttpStatus.SC_OK) {
        throw new RSSReaderException(status.getStatusCode(),
            status.getReasonPhrase());
      }

      // Extract content stream from HTTP response
      HttpEntity entity = response.getEntity();
      feedStream = entity.getContent();

      RSSFeed feed = parser.parse(feedStream);

      if (feed.getLink() == null) {
        feed.setLink(android.net.Uri.parse(uri));
      }

      return feed;
    } catch (ClientProtocolException e) {
      throw new RSSFault(e);
    } catch (IOException e) {
      throw new RSSFault(e);
    } finally {
      Resources.closeQuietly(feedStream);
    }
  }

  /**
   * Release all HTTP client resources.
   */
  public void close() {
    httpclient.getConnectionManager().shutdown();
  }

}
