package ch.sourcepond.maven.plugins.testprojects.versioninheritor;


import org.junit.Assert;
import org.junit.Test;

import ch.sourcepond.maven.plugins.testprojects.versioninheritor.Calculator;

public class CalculatorTest {

    @Test
    public void testAdd() throws Exception {
        Assert.assertEquals(3, new Calculator().add(1, 2));
        System.out.println("The Calculator Test has run"); // used in a test to assert this has run
    }
}