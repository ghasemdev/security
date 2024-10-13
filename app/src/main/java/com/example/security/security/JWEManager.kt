package com.example.security.security

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProperties.PURPOSE_DECRYPT
import android.security.keystore.KeyProperties.PURPOSE_ENCRYPT
import android.util.Base64
import android.util.Log
import androidx.annotation.RequiresApi
import com.nimbusds.jose.EncryptionMethod
import com.nimbusds.jose.JWEAlgorithm
import com.nimbusds.jose.JWEHeader
import com.nimbusds.jose.JWEObject
import com.nimbusds.jose.Payload
import com.nimbusds.jose.crypto.DirectDecrypter
import com.nimbusds.jose.crypto.RSAEncrypter
import com.nimbusds.jose.jwk.RSAKey
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPublicKey
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class JWEManager {
    private val keyStore by lazy {
        KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun getAesKey(encoder: EncryptionMethod): SecretKey {
        return KeyGenerator
            .getInstance(KeyProperties.KEY_ALGORITHM_AES)
            .apply {
                init(encoder.cekBitLength())
            }
            .generateKey()
    }

    private fun getKeyPair(alias: String): KeyPair? {
        val key: PrivateKey? = keyStore.getKey(alias, null) as? PrivateKey
        val cert: Certificate? = keyStore.getCertificate(alias)
        if (key != null && cert != null) {
            return KeyPair(cert.publicKey, key)
        }
        return null
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun getRSAKey(): KeyPair {
        return getKeyPair("rsa-oaep") ?: run {
            KeyPairGenerator
                .getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore")
                .apply {
                    initialize(
                        KeyGenParameterSpec
                            .Builder("rsa-oaep", ENCRYPTION_PURPOSE)
                            .setKeySize(2048)
                            .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                            .setDigests(KeyProperties.DIGEST_SHA256)
                            .setUserAuthenticationRequired(false)
                            .build()
                    )
                }
                .generateKeyPair()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun jweTest() {
        // The JWE alg and enc
        val alg = JWEAlgorithm.RSA_OAEP_256
        val enc = EncryptionMethod.A256GCM

        val publicKey = loadRSAPublicKeyFromString(CERTIFICATE)

        // Generate an RSA key pair
        val rsaKey = RSAKey
            .Builder(publicKey as RSAPublicKey)
            .build()
        val rsaPublicKey = rsaKey.toRSAPublicKey()

        // Generate the Content Encryption Key (CEK)
        val cek = getAesKey(enc)
        Log.d("aaa", "cek: ${cek.encoded.toList()}")

        // Encrypt the JWE with the RSA public key + specified AES CEK
        var jwe = JWEObject(
            JWEHeader.Builder(alg, enc)
                .customParam("server_kid", rsaKey.computeThumbprint().toString())
                .build(),
            Payload("Hello, world!")
        )
        jwe.encrypt(RSAEncrypter(rsaPublicKey, cek))
        val jweString = jwe.serialize()
        Log.d("aaa", "jwe: $jweString")

        // Decrypt JWE with CEK directly, with the DirectDecrypter in promiscuous mode
        jwe = JWEObject.parse(jweString)
        jwe.decrypt(DirectDecrypter(cek, true))
        Log.d("aaa", "payload: ${jwe.payload}")
        Log.d("aaa", "header: ${jwe.header}")
        Log.d("aaa", "iv: ${jwe.iv}")
        Log.d("aaa", "tag: ${jwe.authTag}")
        Log.d("aaa", "key: ${jwe.encryptedKey}")
        Log.d("aaa", "cipherText: ${jwe.cipherText}")
    }

    private fun loadRSAPublicKeyFromString(pemString: String): PublicKey? {
        // Decode the Base64 string (the PEM content without header and footer)
        val decodedBytes = Base64.decode(pemString, Base64.DEFAULT)

        // Create a certificate factory for X.509 certificates
        val certificateFactory = CertificateFactory.getInstance("X.509")

        // Generate the X509Certificate from the decoded bytes
        val certificate =
            certificateFactory.generateCertificate(decodedBytes.inputStream()) as X509Certificate

        // Return the RSA public key
        return certificate.publicKey
    }

    companion object {
        @RequiresApi(Build.VERSION_CODES.M)
        private const val ENCRYPTION_PURPOSE = PURPOSE_ENCRYPT or PURPOSE_DECRYPT
    }
}


private const val CERTIFICATE = "MIIGdzCCBV+gAwIBAgIQfbbIoZDR2nVM2eSSpCLJjzANBgkqhkiG9w0BAQsFADCB" +
        "hTELMAkGA1UEBhMCUEwxIjAgBgNVBAoTGVVuaXpldG8gVGVjaG5vbG9naWVzIFMu" +
        "QS4xJzAlBgNVBAsTHkNlcnR1bSBDZXJ0aWZpY2F0aW9uIEF1dGhvcml0eTEpMCcG" +
        "A1UEAxMgQ2VydHVtIERvbWFpbiBWYWxpZGF0aW9uIENBIFNIQTIwHhcNMjQxMDA5" +
        "MDczNjA3WhcNMjUxMDA5MDczNjA2WjAdMRswGQYDVQQDDBIqLnBhcnRzb2Z0d2Fy" +
        "ZS5jb20wggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQC3lMgLIrn2wmCA" +
        "IHxhsanS5NgxOvdXh8ki9pmC6OoQAS0YHpAMJLog6eq1nN61WykNUfW0hX/CU64X" +
        "OCBreQqExrBvZD/MotO0r5Cc2eBo9w0Pw8KWdj+ujUEHL2zGM7cWAIcqtGRECfOJ" +
        "/eoYtAeC8Y2TUxYn+q92DwXasKoBtyYLF1E5Rws0SNt5bWlF5IURU4lG+tAOYMa8" +
        "lFkCtLHwIII9Ek+rOMHJXelKqocKohDY/1p5dbwvWW/M67AUIakW6mOhzm/cbwzy" +
        "G2HEZ+55WMbPqLDSRrr5X9l6bpeQQkVcdZOe+Z6ZjOIwOzAUTQbGl6LO5Vpj+Mtn" +
        "fSAIb/QJAgMBAAGjggNIMIIDRDAMBgNVHRMBAf8EAjAAMDIGA1UdHwQrMCkwJ6Al" +
        "oCOGIWh0dHA6Ly9jcmwuY2VydHVtLnBsL2R2Y2FzaGEyLmNybDBxBggrBgEFBQcB" +
        "AQRlMGMwKwYIKwYBBQUHMAGGH2h0dHA6Ly9kdmNhc2hhMi5vY3NwLWNlcnR1bS5j" +
        "b20wNAYIKwYBBQUHMAKGKGh0dHA6Ly9yZXBvc2l0b3J5LmNlcnR1bS5wbC9kdmNh" +
        "c2hhMi5jZXIwHwYDVR0jBBgwFoAU5TGtvzoRlvSDvFA81LeQm5Du3iUwHQYDVR0O" +
        "BBYEFOQOBS+nBIw0gZef22x1Pbt+LO7KMB0GA1UdEgQWMBSBEmR2Y2FzaGEyQGNl" +
        "cnR1bS5wbDBLBgNVHSAERDBCMAgGBmeBDAECATA2BgsqhGgBhvZ3AgUBAzAnMCUG" +
        "CCsGAQUFBwIBFhlodHRwczovL3d3dy5jZXJ0dW0ucGwvQ1BTMB0GA1UdJQQWMBQG" +
        "CCsGAQUFBwMBBggrBgEFBQcDAjAOBgNVHQ8BAf8EBAMCBaAwLwYDVR0RBCgwJoIS" +
        "Ki5wYXJ0c29mdHdhcmUuY29tghBwYXJ0c29mdHdhcmUuY29tMIIBfwYKKwYBBAHW" +
        "eQIEAgSCAW8EggFrAWkAdwDd3Mo0ldfhFgXnlTL6x5/4PRxQ39sAOhQSdgosrLvI" +
        "KgAAAZJwNOi5AAAEAwBIMEYCIQCZ6gAdeGJpBO3VSV0pD+jpUOSTdJiF5CqAWLWa" +
        "gL7/DgIhAPiCF2WPqWcDSfm8fFidlCtgBfJrl5IjmMTnHe8G1LE1AHYADeHyMCvT" +
        "DcFAYhIJ6lUu/Ed0fLHX6TDvDkIetH5OqjQAAAGScDTo6QAABAMARzBFAiASAncB" +
        "HWEsnuyohOzwxV0BQeoYLpqMPfzLzYcFyNuahwIhAP4yyfwJwP/kEUJeem/dlJm+" +
        "xHlUb4YxG8w+tfl4zXxCAHYAfVkeEuF4KnscYWd8Xv340IdcFKBOlZ65Ay/ZDowu" +
        "ebgAAAGScDTpHgAABAMARzBFAiEA6x8AnV8CqAtmZkCH8ZhkxDBXnGAfUKHUxwsy" +
        "+yjFZ3wCIG5ahpDzIBH4QmW4TqYu0Rflfs/djl8hZ3xH/eF+PDw5MA0GCSqGSIb3" +
        "DQEBCwUAA4IBAQBTe4R8FCLfPURlWpeefUQDeXk6ZRFEgVWXj1IVRd2okuGlonWw" +
        "LUIL95FuKLx4y7HOr3frDDdgMNfAQ/evw2I2eWuwyoPljalM95ulJar2pfy/2V/h" +
        "hRYi3T96jJGEVX1UFBnE6vMIH3KVnn5Fys3MkrnrXKNJ+sljHgABqc+LILOsF9Ty" +
        "nNuyvh3nUbg9qyAimf7RANliNghBqahqerXsEa9rWUJ8cHeax7J0OO0QcdzA1i5m" +
        "3E7vKUxv4yPTJcM6co8PRO5gq7zPe0npHvvdnmXshwTF9AztF+Ikn1KHYtQRYgYY" +
        "GxEr1ulYyQMs4GH1Ap8n4/iP4Ba9UT3uK031"