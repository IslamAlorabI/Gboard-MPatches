package dev.jason.gboardpatches.extension.webclipboard;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Creates the Web Clipboard HTTPS identity: a fresh P-256 key and a self-signed certificate.
 * Android has no public certificate builder, so the certificate is DER-encoded here.
 */
final class ClipboardSyncSelfSignedCertificate {
    static final String COMMON_NAME = "GBoard Web Clipboard";
    private static final long ONE_DAY_MS = 24L * 60L * 60L * 1000L;
    private static final long VALIDITY_MS = 3650L * ONE_DAY_MS;
    // 1.2.840.10045.4.3.2 ecdsa-with-SHA256
    private static final byte[] OID_ECDSA_WITH_SHA256 = new byte[] {
            0x2A, (byte) 0x86, 0x48, (byte) 0xCE, 0x3D, 0x04, 0x03, 0x02
    };
    // 2.5.4.3 commonName
    private static final byte[] OID_COMMON_NAME = new byte[] { 0x55, 0x04, 0x03 };

    private ClipboardSyncSelfSignedCertificate() {
    }

    static KeyStore createKeyStore(String alias, char[] password) throws Exception {
        SecureRandom random = new SecureRandom();
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(new ECGenParameterSpec("secp256r1"), random);
        KeyPair keyPair = generator.generateKeyPair();
        X509Certificate certificate = selfSign(keyPair, random, System.currentTimeMillis());
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null);
        keyStore.setKeyEntry(alias, keyPair.getPrivate(), password,
                new Certificate[] { certificate });
        return keyStore;
    }

    static X509Certificate selfSign(KeyPair keyPair, SecureRandom random, long nowMs)
            throws Exception {
        byte[] algorithm = sequence(tlv(0x06, OID_ECDSA_WITH_SHA256));
        byte[] name = sequence(tlv(0x31, sequence(
                tlv(0x06, OID_COMMON_NAME),
                tlv(0x0C, COMMON_NAME.getBytes(StandardCharsets.UTF_8)))));
        byte[] serialBytes = new byte[16];
        random.nextBytes(serialBytes);
        serialBytes[0] = (byte) ((serialBytes[0] & 0x7F) | 0x01);
        byte[] tbsCertificate = sequence(
                tlv(0xA0, tlv(0x02, new byte[] { 0x02 })),
                tlv(0x02, new BigInteger(1, serialBytes).toByteArray()),
                algorithm,
                name,
                sequence(utcTime(nowMs - ONE_DAY_MS), utcTime(nowMs + VALIDITY_MS)),
                name,
                keyPair.getPublic().getEncoded());

        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(keyPair.getPrivate(), random);
        signature.update(tbsCertificate);
        byte[] signatureBits = signature.sign();
        byte[] bitString = new byte[signatureBits.length + 1];
        System.arraycopy(signatureBits, 0, bitString, 1, signatureBits.length);

        byte[] encoded = sequence(tbsCertificate, algorithm, tlv(0x03, bitString));
        X509Certificate certificate = (X509Certificate) CertificateFactory.getInstance("X.509")
                .generateCertificate(new ByteArrayInputStream(encoded));
        certificate.verify(keyPair.getPublic());
        return certificate;
    }

    private static byte[] utcTime(long timeMs) {
        SimpleDateFormat format = new SimpleDateFormat("yyMMddHHmmss'Z'", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return tlv(0x17, format.format(new Date(timeMs)).getBytes(StandardCharsets.US_ASCII));
    }

    private static byte[] sequence(byte[]... parts) {
        ByteArrayOutputStream content = new ByteArrayOutputStream();
        for (byte[] part : parts) {
            content.write(part, 0, part.length);
        }
        return tlv(0x30, content.toByteArray());
    }

    private static byte[] tlv(int tag, byte[] value) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(tag);
        int length = value.length;
        if (length < 0x80) {
            output.write(length);
        } else if (length <= 0xFF) {
            output.write(0x81);
            output.write(length);
        } else {
            output.write(0x82);
            output.write(length >>> 8);
            output.write(length & 0xFF);
        }
        output.write(value, 0, value.length);
        return output.toByteArray();
    }
}
