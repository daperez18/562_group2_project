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
        CSVReader csvReader = null;
        try {
            csvReader = new CSVReader(new InputStreamReader(input));
            csvReader.readNext();
            records = csvReader.readAll();

        } catch (Exception e) {
            System.out.println("error");
        }
        return records;
    }





    public void write_csv(List<String[]> QueryList, String url,String username,String password, String mytable, LambdaLogger logger) {
        try 
        { 
            logger.log("checkcon: " + url + ", " +username +", " + password  );
            Connection con = DriverManager.getConnection(url,username,password);
            logger.log("checkcon2");

            //String query = "Insert into mytable values(?,?,?,?, ?,?,?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement ps = con.prepareStatement("DROP TABLE IF EXISTS `" + mytable + "`;");
            ps.execute();
            ps = con.prepareStatement("CREATE TABLE "+ mytable + " (Region VARCHAR(40), Country VARCHAR(40), `Item Type` VARCHAR(40), `Sales Channel` VARCHAR(40),`Order Priority` VARCHAR(40), `Order Date` VARCHAR(40),`Order ID` INT PRIMARY KEY, `Ship Date` VARCHAR(40), `Units Sold` INT,`Unit Price` DOUBLE, `Unit Cost` DOUBLE, `Total Revenue` DOUBLE, `Total Cost` DOUBLE, `Total Profit` DOUBLE, `Order Processing Time` INT, `Gross Margin` FLOAT) ENGINE = MyISAM;");
            ps.execute();
	    logger.log("before insertion");
            String mySql = "insert into "+ mytable +" (Region, Country, `Item Type`, `Sales Channel`, `Order Priority`, `Order Date`, `Order ID`, `Ship Date`, `Units Sold`, `Unit Price`, `Unit Cost`, `Total Revenue`, `Total Cost`, `Total Profit`, `Order Processing Time`, `Gross Margin`) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ,? ,? ,?)";
            PreparedStatement statement = con.prepareStatement(mySql);

            try {
                int i = 0;
                for (String[] query: QueryList) {
                    statement.setString(1, query[0]);
                    statement.setString(2, query[1]);
                    statement.setString(3, query[2]);
                    statement.setString(4, query[3]);
                    statement.setString(5, query[4]);
                    statement.setString(6, query[5]);
                    statement.setString(7, query[6]);
                    statement.setString(8, query[7]);
                    statement.setString(9, query[8]);
                    statement.setString(10, query[9]);
                    statement.setString(11, query[10]);
                    statement.setString(12, query[11]);
                    statement.setString(13, query[12]);
                    statement.setString(14, query[13]);
                    statement.setString(15, query[14]);
                    statement.setString(16, query[15]);

                    statement.addBatch();
                 

                    if (++i % 10000 == 0) {
                        logger.log("finished: " + i);
                        statement.executeBatch();
                    }
                }
                statement.executeBatch();
                statement.close();
                con.close();

            } catch (Exception e) {
                logger.log("Exection using csvreader: " + e);
            }

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
	String mytable=request.getTableName();
	logger.log(mytable);
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

            write_csv(records, url, username, password, mytable, logger);
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
