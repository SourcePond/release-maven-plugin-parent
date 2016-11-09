package ch.sourcepond.maven.plugins.testprojects.versioninheritor;

import ch.sourcepond.maven.plugins.testprojects.versioninheritor.Calculator;

public class App {
    public static void main(String[] args) {
        Calculator calculator = new Calculator();
        System.out.println("1 + 2 = " + calculator.add(1, 2));
    }
}
