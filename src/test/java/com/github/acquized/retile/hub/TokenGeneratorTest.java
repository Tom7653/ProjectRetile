package com.github.acquized.retile.hub;

import com.github.acquized.retile.reports.TokenGenerator;

import org.junit.Test;

public class TokenGeneratorTest {

    @Test
    public void testRandomGenerator() throws Exception {
        String[] array = new String[12];
        for (int i = 0; i < 10; i++) {
            array[i] = TokenGenerator.generate();
            System.out.println(array[i] + " : " + array[i].length());
        }

        /*
        if(containsDuplicates(array)) {
            throw new Exception("Array contains Duplicates!");
        }*/
    }

    public boolean containsDuplicates(Object[] array) {
        boolean duplicates = false;
        for(int j = 0; j < array.length; j++) {
            for(int k = j + 1; k < array.length; k++) {
                if (k != j && array[k] == array[j]) {
                    duplicates = true;
                }
            }
        }
        return duplicates;
    }

}