package me.leedi.papyrus.utils;

import android.content.Context;
import android.util.Base64;
import me.leedi.papyrus.BuildConfig;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

public class SecurityUtils {
    // 아래 Byte 는 타 언어 이식시 Padding 조절을 위해 사용된다
    public static byte[] ivBytes = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

    /**
     * AES256 암호화
     *
     * Reference : http://www.imcore.net/encrypt-decrypt-aes256-c-objective-ios-iphone-ipad-php-java-android-perl-javascript/ 
     *  
     * @param str (평문)
     * @param context (Context)
     * @return AES256로 암호화 된 암호문
     */
    
    public static String AESEncode(String str, Context context) throws java.io.UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException {
        byte[] textBytes = str.getBytes("UTF-8"); // UTF-8 문자셋으로 String 을 Byte 로 인코딩한다.
        String key = context.getSharedPreferences("security", Context.MODE_PRIVATE).getString("keyhash", null); // AES 키로 쓰일 해시를 가져온다.
        AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        SecretKeySpec newKey = new SecretKeySpec(key.getBytes("UTF-8"), "AES"); // 암호화 키 설정
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); // AES-256 Cipher 설정
        cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec); // 암호화로 설정
        
        return Base64.encodeToString(cipher.doFinal(textBytes), 0); // 암호화 데이터 반환
    }

    /**
     * AES256 복호화
     * 
     * Reference : http://www.imcore.net/encrypt-decrypt-aes256-c-objective-ios-iphone-ipad-php-java-android-perl-javascript/
     *
     * @param str (암호문)
     * @param context (Context)
     * @return AES256로 복호화 된 평문
     */

    public static String AESDecode(String str, Context context)	throws java.io.UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        byte[] textBytes = Base64.decode(str, 0); // Byte 를 String 으로 디코딩한다.
        String key = context.getSharedPreferences("security", Context.MODE_PRIVATE).getString("keyhash", null); // AES 키로 쓰일 해시를 가져온다.
        AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        SecretKeySpec newKey = new SecretKeySpec(key.getBytes("UTF-8"), "AES"); // 암호화 키 설정
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); // AES-256 Cipher 설정
        cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec); // 복호화로 설정
        
        return new String(cipher.doFinal(textBytes), "UTF-8"); // 복호화 데이터 반환
    }

    /**
     * AES256 암호화 키 해시 저장
     *
     * @param context (Context)
     */
    
    public static void setKeyHash(Context context) {
        String token = context.getSharedPreferences("common", Context.MODE_PRIVATE).getString("userToken", null); // 유저 토큰 가져오기
        String keyhash = getMD5Hash(BuildConfig.Salt1 + token + BuildConfig.Salt2); // BuildConfig 에서 Salt 가져오기
        context.getSharedPreferences("security", Context.MODE_PRIVATE).edit().putString("keyhash", keyhash).apply(); // AES 암/복호화용 키로 쓰일 해시 저장
    }

    /**
     * MD5 해시 가져오기 (이 메소드는 AES 암호화 키를 위한 해시 이외에는 보안성이 떨어져 사용을 권장하지 않는다.)
     *
     * @param data (데이터)
     */

    public static String getMD5Hash(String data) {
        String hash = null;
        if(data != null) { // 입력데이터가 비어있지 않다면
            try {
                MessageDigest md5 = MessageDigest.getInstance("MD5"); // MD5로
                md5.update(data.getBytes()); // 암호화하고
                BigInteger IntHash = new BigInteger(1, md5.digest()); // 다른 언어와 호환을 위해
                hash = IntHash.toString(16); // 뭐 이런
                while(hash.length() < 32) { // 과정을
                    hash = "0" + hash; // 거친다.
                } // 사실 자세히 몰라서...
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        return hash; // 해시를 반환한다.
    }
}
