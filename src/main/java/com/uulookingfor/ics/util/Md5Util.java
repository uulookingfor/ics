package com.uulookingfor.ics.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.locks.ReentrantLock;

public class Md5Util {

	public static final String ENCODE = "UTF-8";
	
	private static char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	
	private static ReentrantLock lock = new ReentrantLock();
	 
	private static MessageDigest md5Encoder = null;
	
	static{
		try {
			md5Encoder = MessageDigest.getInstance("md5");
		} catch (NoSuchAlgorithmException e) {
			//IGNORE
		}
	}
	
	public static String getMd5String(String param){
		
		return bytes2String(md5Digest(param));
				
	}
	
    public static String bytes2String(byte[] bytes) {
    	
        int bytesLen = bytes.length;

        //double it
        char[] out = new char[bytesLen << 1];

        for (int i = 0, j = 0; i < bytesLen; i++) {
            out[j++] = digits[(0xF0 & bytes[i]) >>> 4];
            out[j++] = digits[0x0F & bytes[i]];
        }
        
        return new String(out);
    }
    
    public static byte[] md5Digest(String str) {
    	 
        lock.lock();
        
        try {
            byte[] bytes = md5Encoder.digest(str.getBytes(ENCODE));
            if (null == bytes || bytes.length != 16) {
                throw new IllegalArgumentException("md5 need");
            }
            return bytes;
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException("unsupported utf-8 encoding", e);
        }
        finally {
            lock.unlock();
        }
    }
     
    public static void main(String[] args){
    	System.out.println(Md5Util.getMd5String("aabccd"));
    } 
}
