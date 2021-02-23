package com.ruize.tabu;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException {
        System.out.println("Start....");
        Tabu tabu = new Tabu(48, 1000, 200, 20);
        tabu.init("data.txt");
        tabu.solve();

    }
}
