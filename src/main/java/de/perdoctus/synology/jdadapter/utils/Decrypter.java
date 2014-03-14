/*
 * Copyright 2013 Christoph Giesche
 *
 * This file is part of jdadapter.
 *
 * jdadapter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jdadapter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jdadapter.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.perdoctus.synology.jdadapter.utils;

import org.apache.log4j.Logger;
import sun.misc.BASE64Decoder;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christoph Giesche
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
