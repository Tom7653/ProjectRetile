package com.github.acquized.retile.reports;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.fail;

public class TokenGeneratorTest {

    public static final File DIRECTORY = new File("build" + File.separator + "test-output");
    public static final File FILE = new File(DIRECTORY, "unique-ids.txt");
    public String[] array = new String[1337];

    @Before
    public void createDir() {
        if((!DIRECTORY.exists()) && (!DIRECTORY.mkdirs())) {
            fail("Could not create Directory");
        }
        System.out.println("< TESTING GENERATOR... >");
        System.out.println("Testing random UniqueID Generation with Size " + array.length + " (" + Arrays.hashCode(array) + ")");
    }

    @Test
    @Before
    public void testGenerator() {
        for(int i = 0; i < array.length; i++) {
            array[i] = TokenGenerator.generate();
        }
    }

    @Test
    public void testDuplicates() {
        Set<String> set = new HashSet<>();
        for(String s : array) {
            if(set.contains(s)) {
                fail("Duplicate UniqueID found: " + s + " (" + s.hashCode() + ")");
            } else {
                set.add(s);
            }
        }
        System.out.println("< TEST PASSED! >");
        System.out.println("No duplicates in UniqueID Hashes have been found.");
        System.out.println(Arrays.toString(array));
    }

    @After
    public void createFile() throws IOException {
        if((!FILE.exists()) && (!FILE.createNewFile())) {
            fail("Could not create File");
        }
    }

    @After
    public void saveGenerated() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(FILE));
        for(String s : array) {
            writer.write(s + "\n");
        }
        writer.flush();
        writer.close();
    }

}