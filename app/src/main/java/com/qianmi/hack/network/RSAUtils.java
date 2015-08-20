package com.qianmi.hack.network;

import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

/** */

/**
 * <p>
 * RSA公钥/私钥/签名工具包
 * </p>
 * <p>
 * 罗纳德·李维斯特（Ron [R]ivest）、阿迪·萨莫尔（Adi [S]hamir）和伦纳德·阿德曼（Leonard [A]dleman）
 * </p>
 * <p>
 * 字符串格式的密钥在未在特殊说明情况下都为BASE64编码格式<br/>
 * 由于非对称加密速度极其缓慢，一般文件不使用它来加密而是使用对称加密，<br/>
 * 非对称加密算法可以用来对对称加密的密钥加密，这样保证密钥的安全也就保证了数据的安全
 * </p>
 *
 * @author IceWee
 * @date 2012-4-26
 * @version 1.0
 */
public class RSAUtils {

    static String privateKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCRKuQZVGq+e7rc2PvU4WvBiyv7n54uqnYSmWJgnQOhEvLUbxNrAbuPvZysiv5YL+UKlodfcoKFoQz5p2b6DxnG3hnB2zGeGJVxshIDo5iEd9zahvQa6osTnDT1hH5nuyZ+X9+Yi9qZJjHhmXfGnhmHcQ1I6SFIC5HLa3cr7dzVYQIDAQAB";


    /** *//**
     * 加密算法RSA
     */
    public static final String KEY_ALGORITHM = "RSA";

    public static final String KEY_TYPE="RSA/ECB/PKCS1Padding";

    /** *//**
     * RSA最大加密明文大小
     */
    private static final int MAX_ENCRYPT_BLOCK = 117;

    /** *//**
     * <p>
     * 公钥加密
     * </p>
     *
     * @param data 源数据
     * @param publicKey 公钥(BASE64编码)
     * @return
     * @throws Exception
     */
    private static byte[] encryptByPublicKey(byte[] data, String publicKey)
            throws Exception {
        byte[] keyBytes = Base64Util.decodeString(publicKey);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key publicK = keyFactory.generatePublic(x509KeySpec);
        // 对数据加密
        Cipher cipher = Cipher.getInstance(KEY_TYPE);
        cipher.init(Cipher.ENCRYPT_MODE, publicK);
        int inputLen = data.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段加密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(data, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_ENCRYPT_BLOCK;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();
        return encryptedData;
    }

    /**
     * 使用公钥加密并返回base64字符串
     * @param str
     * @return
     */
    private static String encryptByPublicKeyBase64(String str,String publicKey) throws Exception {

        return Base64Util.encodeByte(encryptByPublicKey(str.getBytes(), publicKey));
    }

    public static String getEncryptPwd(String source) {
//        L.d("原文字：" + source);
        byte[] data = source.getBytes();
        try {
            String base64EncodedData = encryptByPublicKeyBase64(source, privateKey);
//            L.d("加密后Base64：" + base64EncodedData);
            return base64EncodedData;
        } catch (Exception e) {
            e.printStackTrace();
//            L.e("encrypt password failed : " + e.toString());
        }
        return null;
    }
}