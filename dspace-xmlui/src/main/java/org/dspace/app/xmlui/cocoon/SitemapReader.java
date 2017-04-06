/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.xmlui.cocoon;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.environment.http.HttpEnvironment;
import org.apache.cocoon.reading.AbstractReader;
import org.dspace.core.ConfigurationManager;
import org.xml.sax.SAXException;

import org.apache.log4j.Logger;
import org.dspace.core.Utils;

/**
 * Class will read a generated Sitemap (www.sitemaps.org or HTML sitemap)
 * from [dspace]/sitemaps/ and serve it up to the requesting Search Engine.
 *
 * Sitemaps are generated by running the [dspace]/bin/generate-sitemaps script.
 *
 * There are essentially two types of Sitemaps:
 *
 * (1) Basic HTML Sitemaps
 *
 * path = "/htmlmap"
 *
 *  &lt;map:read type="SitemapReader">
 *    &lt;map:parameter name="type" value="html"/&gt;
 *  &lt;/map:read&gt;
 *
 * (2) Sitemaps.org XML Sitemaps
 *
 * path = "/sitemap"
 *
 *  &lt;map:read type="SitemapReader">
 *    &lt;map:parameter name="type" value="sitemaps.org"/&gt;
 *  &lt;/map:read&gt;
 *
 * @author Tim Donohue
 */
public class SitemapReader extends AbstractReader implements Recyclable
{
    private static Logger log = Logger.getLogger(SitemapReader.class);
    
    /** The Cocoon response */
    protected Response response;

    /** The Cocoon request */
    protected Request request;

    /** The sitemap's mime-type */
    protected String sitemapMimeType;

    /** true if we are for serving sitemap.org sitemaps, false otherwise */
    private boolean forSitemapsOrg = false;

    /**
     * Set up the bitstream reader.
     *
     * See the class description for information on configuration options.
     */
    public void setup(SourceResolver resolver, Map objectModel, String src,
            Parameters par) throws ProcessingException, SAXException,
            IOException
    {
        super.setup(resolver, objectModel, src, par);

        this.request = ObjectModelHelper.getRequest(objectModel);
        this.response = ObjectModelHelper.getResponse(objectModel);
        this.forSitemapsOrg = false;
           
        // Get our parameter that identifies type of sitemap (default to HTML sitemap)
        String type = par.getParameter("type", "html");

        if (type != null && type.equalsIgnoreCase("sitemaps.org"))
        {
            this.forSitemapsOrg = true;
        }
        else if (type == null || !type.equalsIgnoreCase("html"))
        {
            log.warn("Invalid initialization parameter for sitemapReader: assuming basic HTML");
        }
    }

    /**
	 * Generate the output.  Determine which type of sitemap is being
     * requested and setup the main request parameters
	 */
    public void generate() throws IOException, ProcessingException
    {
        String param = this.request.getParameter("map");

        String ext = (this.forSitemapsOrg ? ".xml.gz" : ".html");
        this.sitemapMimeType = (this.forSitemapsOrg ? "text/xml" : "text/html");
        String fileStem = (param == null ? "sitemap_index" : "sitemap" + param);

        sendFile(fileStem + ext, this.forSitemapsOrg);

        out.close();
    }

    /**
	 * Write the actual pre-generated Sitemap data out to the response.
     *
     * @param file  the actual file to send
     * @param compressed  true if file should be compressed
     */
    private void sendFile(String file, 
            boolean compressed) throws IOException, ResourceNotFoundException
    {
        File f = new File(ConfigurationManager.getProperty("sitemap.dir"), file);

        HttpServletResponse httpResponse = (HttpServletResponse) objectModel.get(HttpEnvironment.HTTP_RESPONSE_OBJECT);

        if (!f.exists())
        {
            httpResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
            throw new ResourceNotFoundException("Unable to locate sitemap");
        }

        long lastMod = f.lastModified();
        this.response.setDateHeader("Last-Modified", lastMod);

        // Check for if-modified-since header
        long modSince = this.request.getDateHeader("If-Modified-Since");

        if (modSince != -1 && lastMod < modSince)
        {
            // Sitemap file has not been modified since requested date,
            // hence bitstream has not; return 304
            httpResponse.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }

        if (compressed)
        {
            this.response.setHeader("Content-Encoding", "gzip");
        }

        // Pipe the bits
        InputStream is = new FileInputStream(f);

        // Set the response MIME type
        response.setHeader("Content-Type", this.sitemapMimeType);

        // Response length
        this.response.setHeader("Content-Length", String.valueOf(f.length()));

        Utils.bufferedCopy(is, this.out);
        is.close();
        this.out.flush();
    }

    
    /**
     * Returns the mime-type of the sitemap
     */
    public String getMimeType()
    {
        return this.sitemapMimeType;
    }

    /**
	 * Recycle
	 */
    public void recycle() {
        this.response = null;
        this.request = null;
        this.sitemapMimeType = null;
        this.forSitemapsOrg = false;
        super.recycle();
    }
}
