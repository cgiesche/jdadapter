package de.perdoctus.synology.jdadapter.controller;

import de.perdoctus.synolib.DownloadRedirectorClient;
import de.perdoctus.synolib.exceptions.LoginException;
import de.perdoctus.synolib.exceptions.SynoException;
import de.perdoctus.synology.jdadapter.utils.Decrypter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.FormParam;
import javax.ws.rs.PathParam;

import org.apache.log4j.Logger;
import org.apache.log4j.lf5.util.StreamUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * @author Christoph Giesche <christoph.giesche@gmx.net>
 */
@Controller
public class JdAdapter {
	
	private final Logger log = Logger.getLogger(getClass());

    @Autowired
    private DownloadRedirectorClient drClient;

    @RequestMapping(value = "/jdcheck.js", method = RequestMethod.GET)
    public void returnJdScript(HttpServletResponse resp) throws IOException {
    	log.info("Got Request from 'classic' click'n'load button.");
        resp.setStatus(200);
        resp.getWriter().println("jdownloader=true");
    }
    
    @RequestMapping(value = "/crossdomain.xml", method = RequestMethod.GET)
    public void allowCrossdomain(HttpServletResponse resp) throws IOException {
    	OutputStream response = resp.getOutputStream();
    	InputStream input = getClass().getResourceAsStream("/crossdomain.xml");
    	StreamUtils.copy(input, response);
    }
    
    @RequestMapping(value = "/flash", method = RequestMethod.GET)
    public void enableFlashButton(HttpServletResponse resp) throws IOException {
    	log.info("Got Request from flash click'n'load button.");
    	resp.setStatus(HttpServletResponse.SC_OK);
		resp.getWriter().print("JDownloader");
    }

    @RequestMapping(value = "/flash/addcrypted2", method = RequestMethod.POST)
    public void addDownloads(@FormParam("jk") String jk, @FormParam("crypted") String crypted, @PathParam("source") String source, @PathParam("jd") String jd, HttpServletResponse resp) throws IOException {
    	log.info("Got download request! (Assuming NON-DLC.)");
    	handleClassicRequest(jk, crypted, source, resp);
        log.info("Finished.");
    }
    
    public void handleDlcRequest(@FormParam("jk") String jk, @FormParam("crypted") String crypted, @PathParam("source") String source, @PathParam("jd") String jd, HttpServletResponse resp) throws IOException {
    	resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Das gibts noch nicht!");
    }
    
    public void handleClassicRequest(String jk, String crypted, String source, HttpServletResponse resp) throws IOException {
    	try {
            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine engine = mgr.getEngineByMimeType("text/javascript");
            engine.eval(jk + "\nvar result = f();");
            String key = engine.get("result").toString();

            List<URI> targets;
            try {
            	log.debug("Decrypting URLs.");
                targets = Decrypter.decryptDownloadUri(crypted, key);
                log.debug("Finished. Number of URIs: " + targets.size());
            } catch (URISyntaxException ex) {
            	log.error("Decryped URL seems to be corrupt.", ex);
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
                return;
            }

            try {
                for (URI target : targets) {
                	log.debug("Adding URLs to Synology Download Station.");
                    drClient.addDownloadUrl(target);
                    log.debug("Done.");
                }
            } catch (SynoException ex) {
            	log.error(ex.getMessage(), ex);
                if (ex instanceof LoginException) {
                    resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage());
                    return;
                } else {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
                    return;
                }
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().print("Sucessfully added " + targets.size() + " downloads to Synology NAS.");
            
        } catch (ScriptException ex) {
        	log.error(ex.getMessage(), ex);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Failed to evaluate script:\n" + ex.getMessage());
        } catch (MalformedURLException ex) {
        	log.error(ex.getMessage(), ex);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Encrypted data contains invalid URL.\n" + ex.getMessage());
        }
    }


    public DownloadRedirectorClient getClient() {
        return drClient;
    }

    public void setClient(DownloadRedirectorClient client) {
        this.drClient = client;
    }
}
