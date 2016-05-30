package ch.sourcepond.maven.plugins.testproject.singlepom;

import org.junit.Assert;

public class MainTest {
    @org.junit.Test
    public void aTestThatFails() throws Exception {
        Assert.assertEquals("wave", "particle");
    }
}
