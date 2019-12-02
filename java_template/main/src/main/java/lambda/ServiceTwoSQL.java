/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lambda;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.Properties;
import java.io.InputStreamReader;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import saaf.Inspector;
import saaf.Response;
import java.util.HashMap;
import java.util.Random;
import java.io.StringWriter;
import java.io.InputStream;
import java.io.*;
import java.util.*;
import com.amazonaws.services.s3.model.ObjectMetadata;
import java.nio.charset.StandardCharsets;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import java.io.FileReader;
import java.util.Arrays;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.amazonaws.services.s3.model.Region;
import com.amazonaws.services.s3.model.*;


/**
 * uwt.lambda_test::handleRequest
 * @author wlloyd
 */
public class ServiceTwoSQL implements RequestHandler<Request, HashMap<String, Object>>
{
    static String CONTAINER_ID = "/tmp/container-id";
    static Charset CHARSET = Charset.forName("US-ASCII");
   
    public List<String[]> readcsv(InputStream input) {
        List<String[]> records = new ArrayList<String[]>();
        try {
            CSVReader csvReader = new CSVReader(new InputStreamReader(input));
            records = csvReader.readAll();
        } catch (Exception e) {
            System.out.println("error");
        }
        
        return records;
    }




    public void write_csv(List<String[]> info, String url,String username,String password, LambdaLogger logger) {
        try 
        { 
            logger.log("checkcon: " + url + ", " +username +", " + password  );
            Connection con = DriverManager.getConnection(url,username,password);
            logger.log("checkcon2");

            //String query = "Insert into mytable values(?,?,?,?, ?,?,?,?,?,?,?,?,?,?,?,?)";

            for (int i =1; i <info.size(); i++) {
                int col = info.get(i).length;

                String currString ="";
                for (int j = 0; j < col; j++) {
                  //  System.out.println("i = " + i + ", j= " + j +"info size= " + info.size());
                    currString += "\"" +(info.get(i)[j]) +"\"";

                    if ((j+1)!=col)
                        currString += ",";

                }
                PreparedStatement ps = con.prepareStatement("insert into mytable values("+ currString + ");");
                ps.execute();

            }
            PreparedStatement ps = con.prepareStatement("ALTER TABLE mytable ORDER BY `Order Id`");
            ps.execute();
            con.close();
        }
        catch (Exception e) 
        {
            logger.log("Got an exception working with MySQL! ");
            logger.log(e.getMessage());
        }
    }

    // Lambda Function Handler
    public HashMap<String, Object> handleRequest(Request request, Context context) {
        // Create logger
        LambdaLogger logger = context.getLogger();
        
        //Collect inital data.
        Inspector inspector = new Inspector();
        inspector.inspectAll();
        inspector.addTimeStamp("frameworkRuntime");
        
        //****************START FUNCTION IMPLEMENTATION*************************

        //Create and populate a separate response object for function output. (OPTIONAL)
        Response r = new Response();
        logger.log("chpa ");
        String bucketname = request.getBucketName();
        String key = request.getKey();
        logger.log(bucketname);
        logger.log(key);

        //AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();  
        //S3Object s3Object = s3Client.getObject(new GetObjectRequest(bucketname, key));
        //InputStream objectData = s3Object.getObjectContent();


        AmazonS3Client s3 = new AmazonS3Client();
        s3.setEndpoint("s3.amazonaws.com");
        S3Object obj = s3.getObject(new GetObjectRequest(bucketname, key));
        logger.log(obj.getBucketName());
        InputStream objectData = obj.getObjectContent();

        logger.log("chpb ");

        try 
        {
            Properties properties = new Properties();
            properties.load(new FileInputStream("db.properties"));
            
            String url = properties.getProperty("url");
            String username = properties.getProperty("username");
            String password = properties.getProperty("password");
            String driver = properties.getProperty("driver");
            
            // Manually loading the JDBC Driver is commented out
            // No longer required since JDBC 4
            //Class.forName(driver);
            

            List<String[]> records = readcsv(objectData);
            logger.log("chp1 ");

            write_csv(records, url, username, password, logger);
            logger.log("chp2 ");

         
        } 
        catch (Exception e) 
        {
            logger.log("Got an exception working with MySQL! ");
            logger.log(e.getMessage());
        }
        

        //Print log information to the Lambda log as needed
        //logger.log("log message...");
        
        // Set return result in Response class, class is marshalled into JSON
        r.setValue("temp");

        
        //****************END FUNCTION IMPLEMENTATION***************************
        inspector.consumeResponse(r);
        
        //Collect final information such as total runtime and cpu deltas.
        inspector.inspectAllDeltas();
        return inspector.finish();
    }
    
    
}
