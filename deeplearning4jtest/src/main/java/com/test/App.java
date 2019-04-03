package com.test;

import java.io.File;
import java.util.logging.Logger;

/**
 * Hello world!
 *
 */
public class App 
{
    private static Logger log = Logger.getLogger(App.class.getName());
    public static void main( String[] args )
    {

        String basePath ="D:/test" + "mnist";
        String dataUrl = "http://github.com/myleott/mnist_png/raw/master/mnist_png.tar.gz";
        String localFilePath = basePath + "/mnist_png.tar.gz";
        try {
            DataUtilities.downloadFile(dataUrl, localFilePath);
            if (!new File(basePath + "/mnist_png").exists())
                DataUtilities.extractTarGz(localFilePath, basePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println( "Hello World!" );
    }
}
