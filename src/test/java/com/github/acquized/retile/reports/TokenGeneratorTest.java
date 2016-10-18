package com.github.acquized.retile.reports;

import com.github.acquized.retile.test.TestFailException;

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

public class TokenGeneratorTest {

    public static final File DIRECTORY = new File("build" + File.separator + "test-output");
    public static final File FILE = new File(DIRECTORY, "unique-ids.txt");
    public String[] array = new String[1337];

    @Before
    public void createDir() throws TestFailException {
        if((!DIRECTORY.exists()) && (!DIRECTORY.mkdirs())) {
            System.err.println("< TEST FAILED! >");
            System.err.println("Couldn't create Test Output Directory. Are you running this Test in the Main Repository Tree?");
            throw new TestFailException();
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
    public void testDuplicates() throws TestFailException {
        Set<String> set = new HashSet<>();
        for(String s : array) {
            if(set.contains(s)) {
                System.err.println("< TEST FAILED! >");
                System.err.println("The generated random UniqueID Array contains DUPLICATES");
                System.err.println("Duplicate ID: " + s + " (" + s.hashCode() + ")");
                throw new TestFailException();
            } else {
                set.add(s);
            }
        }
        System.out.println("< TEST PASSED! >");
        System.out.println("No duplicates in UniqueID Hashes have been found.");
        System.out.println(Arrays.toString(array));
    }

    @After
    public void createFile() throws IOException, TestFailException {
        if((!FILE.exists()) && (!FILE.createNewFile())) {
            System.err.println("< TEST FAILED! >");
            System.err.println("Couldn't create Unique ID's File");
            throw new TestFailException();
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