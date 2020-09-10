package com.safety.android.tools;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.myapplication.R;
import com.safety.android.MainActivity;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;

/**
 * @author Mr.Zheng
 * @date 2014年8月22日 下午1:44:23
 */
public final class RSAUtils
{

    /**
     * 通过公钥文件进行加密数据
     *
     * @return 加密后经过base64处理的字符串
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String encrypt(String source) throws Exception {
        InputStream fis = null;
        try {
            fis = MainActivity.getContext().getResources().openRawResource(R.raw.test);
            CertificateFactory cf = CertificateFactory.getInstance("x509");
            Certificate cerCert = cf.generateCertificate(fis);
            PublicKey pubKey = cerCert.getPublicKey();
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);
            byte[] sbt = source.getBytes("UTF-8");
           // byte[] epByte = cipher.doFinal(sbt);

            int inputLen = sbt.length;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int offSet = 0;
            byte[] cache;
            int i = 0;
            // 对数据分段加密
            while (inputLen - offSet > 0) {
                if (inputLen - offSet > 117) {
                    cache = cipher.doFinal(sbt, offSet, 117);
                } else {
                    cache = cipher.doFinal(sbt, offSet, inputLen - offSet);
                }
                out.write(cache, 0, cache.length);
                i++;
                offSet = i * 117;
            }
            byte[] encryptData = out.toByteArray();
            out.close();

            String encodeToString = Base64.getEncoder().encodeToString(encryptData);

            return toUtf8String(encodeToString);

        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String toUtf8String(String s) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 0 && c <= 255) {
                sb.append(c);
            } else {
                byte[] b;
                try {
                    b = String.valueOf(c).getBytes("utf-8");
                } catch (Exception ex) {
                    System.out.println(ex);
                    b = new byte[0];
                }
                for (int j = 0; j < b.length; j++) {
                    int k = b[j];
                    if (k < 0)
                        k += 256;
                    sb.append("%" + Integer.toHexString(k).toUpperCase());
                }
            }
        }
        return sb.toString();
    }


}