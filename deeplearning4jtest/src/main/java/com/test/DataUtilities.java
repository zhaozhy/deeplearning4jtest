package com.test;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.*;
import java.util.zip.GZIPInputStream;

public class DataUtilities {


    public static boolean downloadFile(String remoteUrl, String localPath) throws  Exception{
        boolean  downloadState=false;

        if(remoteUrl==null || localPath==null){
            return  downloadState;
        }
        File  file=new File(localPath);
          if(!file .exists()){
              file.getParentFile().mkdirs();
              HttpClientBuilder builder=HttpClientBuilder.create();
              CloseableHttpClient client =builder.build();

              try (CloseableHttpResponse response =client.execute(new HttpGet(remoteUrl))){

                  HttpEntity entity =response.getEntity();

                  if(entity !=null ){
                     try(FileOutputStream outputStream =new FileOutputStream(file)){
                         entity.writeTo(outputStream);
                         outputStream.flush();
                         outputStream.close();
                     }
                  }
              }
              downloadState=true;
          }
          if(!file.exists())
              throw  new IOException("File doesn't exist:"+localPath);
          return  downloadState;
    }
   public static  void extractTarGz(String inputPath, String outoutPath)throws IOException{
        if(inputPath==null || outoutPath==null )
            return;
        final  int bufferSize=4096;
        if (!outoutPath.endsWith(""+File.separatorChar))
            outoutPath=outoutPath+File.separatorChar;
        try(TarArchiveInputStream inputStream =new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(inputPath))))){
            TarArchiveEntry entry;
            while ((entry=(TarArchiveEntry) inputStream .getNextEntry())!=null ){
                if(entry.isDirectory()){
                    new File(outoutPath+entry.getName()).mkdirs();
                }
                else{
                  int count;
                  byte data[]=new byte[bufferSize];
                  FileOutputStream fos=new FileOutputStream(outoutPath+entry.getName());
                  BufferedOutputStream dest=new BufferedOutputStream(fos,bufferSize);
                  while ((count=inputStream.read(data,0,bufferSize))!=-1){
                      dest.write(data,0,count);
                  }
                  dest.close();
                }
            }
        }
   }
}
