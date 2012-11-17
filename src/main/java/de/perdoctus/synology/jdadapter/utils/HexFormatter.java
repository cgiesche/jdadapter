/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.formatter
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package de.perdoctus.synology.jdadapter.utils;

public class HexFormatter {

    public static String byteArrayToHex(byte[] digest) {
        StringBuilder ret = new StringBuilder();
        String tmp;
        for (byte d : digest) {
            tmp = Integer.toHexString(d & 0xFF);
            if (tmp.length() < 2) ret.append('0');
            ret.append(tmp);
        }
        return ret.toString();
    }

    public static byte[] hexToByteArray(String s) {
        final int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

}
