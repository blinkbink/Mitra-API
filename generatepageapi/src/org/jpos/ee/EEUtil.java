/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2007 Alejandro P. Revilla
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.ee;

import org.jpos.iso.ISOUtil;
import java.util.Random;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

/**
 * @author Alejandro P. Revilla
 * @version $Revision: 1.5 $ $Date: 2004/12/09 19:45:33 $
 *
 * Assorted helpers
 */
public class EEUtil {
    public static String getHash (String userName, String pass) {
        String hash = null;
        try {
            MessageDigest md = MessageDigest.getInstance ("MD5");
//            MessageDigest md = MessageDigest.getInstance ("SHA512","nCipherKM");
            md.update (userName.getBytes());
            hash = ISOUtil.hexString (
                md.digest (pass.getBytes())
            ).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            // should never happen
        	e.printStackTrace();
        }   
        return hash;
    }
    public static String getHash (String s) {
        String hash = null;
        try {
            MessageDigest md = MessageDigest.getInstance ("MD5");
            hash = ISOUtil.hexString (md.digest (s.getBytes())).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            // should never happen
        	e.printStackTrace();

        }   
        return hash;
    }
    public static String getRandomHash () {
        return getHash (
            Double.toString (Math.random()),
            Double.toString (Math.random())
        );
    }
}

