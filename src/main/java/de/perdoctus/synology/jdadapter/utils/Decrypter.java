package de.perdoctus.synology.jdadapter.utils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;

import sun.misc.BASE64Decoder;

/**
 *
 * @author Christoph Giesche <christoph.giesche@gmx.net>
 */
public class Decrypter {
    
	public static Logger LOG = Logger.getLogger(Decrypter.class);
	
    public static List<URI> decryptDownloadUri(final String hexContent, final String hexKey) throws URISyntaxException, IOException {
        List<URI> resultURLs = new ArrayList<URI>();
        
        BASE64Decoder dec = new BASE64Decoder();
        byte[] content = dec.decodeBuffer(hexContent);
        byte[] key = HexFormatter.hexToByteArray(hexKey);
        
        String[] results = decrypt(content, key).split("\n");
        
        for (String result : results) {
            result = result.trim();
            if (result.trim().length() > 0) {
                URI resultURL = new URI(result);
                resultURLs.add(resultURL);
            }
        }
        
        return resultURLs;
    }

    public static String decrypt(byte[] b, byte[] key) {
        String result = null;
        
        try {
            Cipher cipher;
            IvParameterSpec ivSpec = new IvParameterSpec(key);
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
            cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec);
            result = new String(cipher.doFinal(b));
        } catch (Exception e) {
            LOG.error(e);
        }
        return result;
    }
}
