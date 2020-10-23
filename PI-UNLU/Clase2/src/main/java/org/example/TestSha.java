package org.example;

import org.apache.commons.codec.digest.DigestUtils;

public class TestSha {
    public static void main( String[] args )
    {
        // parametros de consola
        String originalString = "dilan,nico,angel";
        String sha256hex = DigestUtils.sha256Hex(originalString);
        System.out.println ("SHA256 es: "+sha256hex);

    }
}
