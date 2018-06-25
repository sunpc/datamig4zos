/**
 *  Class: util.DesEncrypter
 *  Description: Password Encrypter
 *  From http://topic.csdn.net/t/20050607/22/4066781.html
 *  Created for DataMig4zOS V5.1. (password encrypter)
 * 
 * 	Author: Peng Cheng Sun
 *  
 *  Modification History
 *  1. 05/06/2009: Code baseline. (V5.1)
 *  2. 06/18/2010: Use org.eclipse.ecf.core.util.Base64 to replace sun.misc.BASE64Decoder. (V6.0 phase 1)
 *  3. 03/05/2011: Change Class to static. (V6.0 phase 1)
 *  4. 11/06/2012: Security patch by using the local HD serial number in password phrase. (V6.1)
 */
package net.sourceforge.datamig4zos.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.eclipse.ecf.core.util.Base64;

public class DesEncrypter {
	private static Cipher ecipher;
	private static Cipher dcipher;

	// passPhrase
	private static String passPhrase;

	// 8-byte Salt
	private static byte[] salt = { (byte) 0xA9, (byte) 0x9B, (byte) 0xC8,
			(byte) 0x32, (byte) 0xA6, (byte) 0x35, (byte) 0xE3, (byte) 0x03 };

	// Iteration count
	private static int iterationCount = 19;

	// initiate the des encrypter
	private static void initDesEncrypter() {
		try {
			// initialize the pass phrase - v6.1
			passPhrase = "Dmg" + getHDSerial("C") + "Crypto";
			
			// Create the key
			KeySpec keySpec = new PBEKeySpec(passPhrase.toCharArray(), salt,
					iterationCount);
			SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES")
					.generateSecret(keySpec);
			ecipher = Cipher.getInstance(key.getAlgorithm());
			dcipher = Cipher.getInstance(key.getAlgorithm());

			// Prepare the parameter to the ciphers
			AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt,
					iterationCount);

			// Create the ciphers
			ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
			dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
		} catch (java.security.InvalidAlgorithmParameterException e) {
		} catch (java.security.spec.InvalidKeySpecException e) {
		} catch (javax.crypto.NoSuchPaddingException e) {
		} catch (java.security.NoSuchAlgorithmException e) {
		} catch (java.security.InvalidKeyException e) {
		}
	}
	
	// get serial number of the local HD - v6.1
	// sourced from http://blog.csdn.net/njzdl/article/details/4676302
	private static String getHDSerial(String drive) {
		String result = "";

		try {
			File file = File.createTempFile("gethdserial", ".vbs");

			file.deleteOnExit();

			FileWriter fw = new java.io.FileWriter(file);

			String vbs = "Set objFSO = CreateObject(\"Scripting.FileSystemObject\")\n"
					+ "Set colDrives = objFSO.Drives\n"
					+ "Set objDrive = colDrives.item(\"" + drive + "\")\n"
					+ "Wscript.Echo objDrive.SerialNumber";

			fw.write(vbs);
			fw.close();

			Process p = Runtime.getRuntime().exec("cscript //NoLogo " + file.getPath());

			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line;

			while ((line = input.readLine()) != null) {
				result += line;
			}

			input.close();
		}

		catch (Exception e) {
			e.printStackTrace();
		}

		return result.trim();
	}

	// encrypt a string
	public static String encrypt(String str) {
		initDesEncrypter();

		// encrypt
		try {
			// Encode the string into bytes using utf-8
			byte[] utf8 = str.getBytes("UTF8");

			// Encrypt
			byte[] enc = ecipher.doFinal(utf8);

			// Encode bytes to base64 to get a string
			return Base64.encode(enc); // v6.0 phase 1
		} catch (javax.crypto.BadPaddingException e) {
		} catch (IllegalBlockSizeException e) {
		} catch (UnsupportedEncodingException e) {
		}
		return "";		// v6.1
	}

	// decrypt a string
	public static String decrypt(String str) {
		initDesEncrypter();

		// decrypt process
		try {
			// Decode base64 to get bytes
			byte[] dec = Base64.decode(str); 	// v6.0 phase 1

			// Decrypt
			byte[] utf8 = dcipher.doFinal(dec);

			// Decode using utf-8
			return new String(utf8, "UTF8");
		} catch (javax.crypto.BadPaddingException e) {
		} catch (IllegalBlockSizeException e) {
		} catch (UnsupportedEncodingException e) {
		}
		return "";		// v6.1
	}

}