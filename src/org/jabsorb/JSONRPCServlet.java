/*
 * jabsorb - a Java to JavaScript Advanced Object Request Broker
 * http://www.jabsorb.org
 *
 * Copyright 2007 Arthur Blake and William Becker
 *
 * based on original code from
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * Copyright Metaparadigm Pte. Ltd. 2004.
 * Michael Clark <michael@metaparadigm.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.jabsorb;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * This servlet handles JSON-RPC requests over HTTP and hands them to a
 * JSONRPCBridge instance (either a global instance or one in the user's
 * HttpSession).
 * </p>
 * <p>
 * The following can be added to your web.xml to export the servlet under the
 * URI &quot;<code>/JSON-RPC</code>&quot;
 * </p>
 * 
 * <pre>
 * &lt;servlet&gt;
 *   &lt;servlet-name&gt;com.metaparadigm.jsonrpc.JSONRPCServlet&lt;/servlet-name&gt;
 *   &lt;servlet-class&gt;com.metaparadigm.jsonrpc.JSONRPCServlet&lt;/servlet-class&gt;
 * &lt;/servlet&gt;
 * &lt;servlet-mapping&gt;
 *   &lt;servlet-name&gt;com.metaparadigm.jsonrpc.JSONRPCServlet&lt;/servlet-name&gt;
 *   &lt;url-pattern&gt;/JSON-RPC&lt;/url-pattern&gt;
 * &lt;/servlet-mapping&gt;
 * </pre>
 * 
 * </p>
 * The JSONRPCServlet looks for a session specific bridge object under the
 * attribute <code>"JSONRPCBridge"</code> in the HttpSession associated with
 * the request (without creating a session if one does not already exist). If it
 * can't find a session specific bridge instance, it will default to invoking
 * against the global bridge.
 * </p>
 * <p>
 * Using a session specific bridge allows you to export certain object instances
 * or classes only to specific users, and of course these instances could be
 * stateful and contain data specific to the user's session.
 * </p>
 * <p>
 * An example or creating a session specific bridge in JSP is as follows:
 * </p>
 * <code>
 * &lt;jsp:useBean id="JSONRPCBridge" scope="session"
 *   class="com.metaparadigm.jsonrpc.JSONRPCBridge"/&gt;
 * </code>
 * <p>
 * An example in Java (i.e. in another Servlet):
 * </p>
 * <code>
 * HttpSession session = request.getSession();<br />
 * JSONRPCBridge bridge = (JSONRPCBridge) session.getAttribute("JSONRPCBridge");<br>
 * if(bridge == null) {<br />
 * &nbsp;&nbsp;&nbsp;&nbsp;bridge = new JSONRPCBridge();<br />
 * &nbsp;&nbsp;&nbsp;&nbsp;session.setAttribute("JSONRPCBridge", bridge);<br />
 * }<br />
 * </code>
 */

public class JSONRPCServlet extends HttpServlet
{
  /**
   * Unique serialisation id.
   */
  private final static long serialVersionUID = 2;

  /**
   * The logger for this class
   */
  private final static Logger log = LoggerFactory
      .getLogger(JSONRPCServlet.class);

  /**
   * The size of the buffer used for reading requests
   */
  private final static int buf_size = 4096;

  /**
   * The GZIP_THRESHOLD indicates the response size at which the servlet will attempt to gzip the response
   * if it can.  Gzipping smaller responses is counter productive for 2 reasons:
   *
   * 1.  if the response is really small, the gzipped output can actually be larger than the non-compressed original.
   * because of the gzip header and the general overhead of the gzipping.
   * This is a lose-lose situation, so the original should always be sent in this case.
   *
   * 2.  gzipping imposes a small performance penality in that it takes a little more time to gzip the content.
   * There is also a corresponding penality on the browser side when the content has to be uncompressed.
   *
   * This penalty is really small, and is normally more than outweighed by the bandwidth savings provided
   * by gzip (the response is typically 1/10th the size when gzipped!  Especially for json data which tends to
   * have a lot of repetition.
   *
   * So, the GZIP_THRESHOLD should be tuned to a size that is optimal for your application.  If your application is
   * always served from a high speed network, you might want to set this to a very high number--
   * (or even -1 to turn it off) for slower networks where it's more important to conserve bandwidth, 
   * set this to a lower number (but not too low!)
   *
   * Set this to zero if you want to always attempt to gzip the output when the browser can accept gzip encoded responses.
   * This is useful for analyzing what a good gzip setting should be for potential responses from your application.
   *
   * You can set this to -1 if you want to turn off gzip encoding for some reason.
   *
   * todo: make this parameter settable through a servlet parameter
   * todo: -1 to turn gzip off, 0 to attempt to gzip everything, higher number for a threshold
   */
  private static int GZIP_THRESHOLD = 200;

  public void service(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ClassCastException
  {

    // Use protected method in case someone wants to override it
    JSONRPCBridge json_bridge = findBridge(request);

    // Encode using UTF-8, although We are actually ASCII clean as
    // all unicode data is JSON escaped using backslash u. This is
    // less data efficient for foreign character sets but it is
    // needed to support naughty browsers such as Konqueror and Safari
    // which do not honour the charset set in the response
    response.setContentType("text/plain;charset=utf-8");
    OutputStream out = response.getOutputStream();

    // Decode using the charset in the request if it exists otherwise
    // use UTF-8 as this is what all browser implementations use.
    // The JSON-RPC-Java JavaScript client is ASCII clean so it
    // although here we can correctly handle data from other clients
    // that do not escape non ASCII data
    String charset = request.getCharacterEncoding();
    if (charset == null)
    {
      charset = "UTF-8";
    }
    BufferedReader in = new BufferedReader(new InputStreamReader(request
        .getInputStream(), charset));

    // Read the request
    CharArrayWriter data = new CharArrayWriter();
    char buf[] = new char[buf_size];
    int ret;
    while ((ret = in.read(buf, 0, buf_size)) != -1)
    {
      data.write(buf, 0, ret);
    }
    if (log.isDebugEnabled())
    {
      log.debug("receive: " + prettyPrintJson(data.toString()));
    }

    // Process the request
    JSONObject json_req;
    JSONRPCResult json_res;
    try
    {
      json_req = new JSONObject(data.toString());
      json_res = json_bridge.call(new Object[] { request, response }, json_req);
    }
    catch (JSONException e)
    {
      log.error("can't parse call: " + data);
      json_res = new JSONRPCResult(JSONRPCResult.CODE_ERR_PARSE, null,
          JSONRPCResult.MSG_ERR_PARSE);
    }

    // Write the response
    if (log.isDebugEnabled())
    {
      log.debug("send: " + prettyPrintJson(json_res.toString()));
    }

    byte[] bout = json_res.toString().getBytes("UTF-8");

    // handle gzipping of the response if it is turned on
    if (JSONRPCServlet.GZIP_THRESHOLD != -1)
    {
      // if the request header says that the browser can take gzip compressed output, then gzip the output
      // but only if the response is large enough to warrant it and if the resultant compressed output is
      // actually smaller.
      if (acceptsGzip(request))
      {
        if (bout.length > JSONRPCServlet.GZIP_THRESHOLD)
        {
          byte[] gzippedOut = gzip(bout);
          log.debug("gzipping! original size =  " + bout.length + "  gzipped size = " + gzippedOut.length);

          // if gzip didn't actually help, abort
          if (bout.length <= gzippedOut.length)
          {
            log.warn("gzipping resulted in a larger output size!  " +
              "aborting (sending non-gzipped response)... " +
              "you may want to increase the gzip threshold if this happens a lot!" +
              " original size = " + bout.length + "  gzipped size = " + gzippedOut.length);
          }
          else
          {
            // go with the gzipped output
            bout = gzippedOut;
            response.addHeader("Content-Encoding", "gzip");
          }
        }
        else
        {
          log.debug("not gzipping because size is " + bout.length +
            " (less than the GZIP_THRESHOLD of " + JSONRPCServlet.GZIP_THRESHOLD + " bytes)");
        }
      }
      else
      {
        // this should be rare with modern user agents
        log.debug("not gzipping because user agent doesn't accept gzip encoding...");
      }
    }

    response.setIntHeader("Content-Length", bout.length);

    out.write(bout);
    out.flush();
    out.close();
  }

  /**
   * Find the JSONRPCBridge from the servlet request.
   * 
   * @param request The message received
   * @return the JSONRPCBridge to use for this request
   */
  protected JSONRPCBridge findBridge(HttpServletRequest request)
  {
    // Find the JSONRPCBridge for this session or create one
    // if it doesn't exist
    HttpSession session = request.getSession(false);
    JSONRPCBridge json_bridge = null;
    if (session != null)
    {
      json_bridge = (JSONRPCBridge) session.getAttribute("JSONRPCBridge");
    }
    if (json_bridge == null)
    {
      // Use the global bridge if we can't find a bridge in the session.
      json_bridge = JSONRPCBridge.getGlobalBridge();
      if (log.isDebugEnabled())
      {
        log.debug("Using global bridge.");
      }
    }
    return json_bridge;
  }

  /**
   * Format (pretty print) json nicely for debugging output.
   * If the pretty printing fails for any reason (this is not expected)
   * then the original, unformatted json will be returned.
   *
   * @param unformattedJSON a json string.
   *
   * @return a String containing the formatted json text for the passed in json object.
   */
  private String prettyPrintJson(String unformattedJSON)
  {
    if (unformattedJSON == null || "".equals(unformattedJSON))
    {
      return unformattedJSON;
    }
    try
    {
      return new JSONObject(unformattedJSON).toString(2);
    }
    catch (JSONException je)
    {
      return unformattedJSON; // fall back to unformatted json, if pretty print fails...
    }
  }

  /**
   * Can browser accept gzip encoding?
   *
   * @param request browser request object.
   * @return true if gzip encoding accepted.
   */
  private boolean acceptsGzip(HttpServletRequest request)
  {
    // can browser accept gzip encoding?
    String ae = request.getHeader("accept-encoding");
    return ae != null && ae.indexOf("gzip") != -1;
  }

  /**
   * Gzip something.
   *
   * @param in original content
   * @return size gzipped content
   */
  private byte[] gzip(byte[] in)
  {
    if (in != null && in.length > 0)
    {
      long tstart = System.currentTimeMillis();
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      try
      {
        GZIPOutputStream gout = new GZIPOutputStream(bout);
        gout.write(in);
        gout.flush();
        gout.close();
        if (log.isDebugEnabled())
        {
          log.debug("gzipping took " + (System.currentTimeMillis() - tstart) + " msec");
        }
        return bout.toByteArray();
      }
      catch (IOException io)
      {
        log.error("io exception gzipping byte array", io);
      }
    }
    return new byte[0];
  }
}
