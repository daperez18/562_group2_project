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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;

/**
 * uwt.lambda_test::handleRequest
 * @author wlloyd
 */
public class ServiceThree implements RequestHandler<Request, HashMap<String, Object>>
{
    static String CONTAINER_ID = "/tmp/container-id";
    static Charset CHARSET = Charset.forName("US-ASCII");
    static String[] FILTER_BY_VALUES= {"Region", "Item_Type", "Sales_Channel", "Order_Priority", "Country"};
    static String[] AGGREGATE_BY_VALUES= {"max", "min", "avg", "sum"};

    public JSONArray filterByArr(JSONObject Arr, String Key) {
            return Arr.getJSONArray(Key);
    }


    public LinkedList<String> createWhereString(JSONObject JSONObj) {
            LinkedList<String> newList = new LinkedList<String>();
            for (int i = 0; i < FILTER_BY_VALUES.length; i++) {
                    JSONArray arrJson=JSONObj.getJSONArray(FILTER_BY_VALUES[i]);

                    for(int j=0;j<arrJson.length();j++) {
                            String whereString="WHERE ";

                            String filterVal = "`" +FILTER_BY_VALUES[i].replace('_', ' ') +"`"; // WHERE `Region`="Australia"
                            //if (i == FILTER_BY_VALUES.length-1 && j ==arrJson.length()-1) {

                            whereString += filterVal +"=\"" +arrJson.getString(j).replace('_', ' ') +"\"" ;
                            //}
                            //else {
                            //	whereString += filterVal +"=\"" +arrJson.getString(j).replace('_', ' ')  + "\" AND ";
                            //}
                            newList.add(whereString);
                    }

            }
            //return whereString;
            return newList;
    }

    public LinkedList<String> createAggFunctionStrings(JSONObject JSONObj, String mytable, LinkedList<String> filterBy) {
            LinkedList<String> newList = new LinkedList<String>();

            for (int q = 0; q < filterBy.size(); q++ ) {

                String filterByString = filterBy.get(q);
                String aggString="SELECT ";

                for (int i =0; i < AGGREGATE_BY_VALUES.length; i++) {
                        JSONArray arrJson=JSONObj.getJSONArray(AGGREGATE_BY_VALUES[i]);

                        for(int j=0;j<arrJson.length();j++) {
                                aggString+= AGGREGATE_BY_VALUES[i].toUpperCase() + "(`";
                                aggString+=arrJson.getString(j).replace('_', ' ') +"`)";				
                                //if (!(j == arrJson.length() -1 && i == AGGREGATE_BY_VALUES.length - 1)) {
                                aggString+=", ";
                                //} 
                        }	

                }
                aggString+=" \"" + filterByString.replace('"', ' ')  + "\" ";
                aggString+="AS `Filtered By` ";
                aggString+="FROM " + mytable + " ";
                aggString+=filterByString +";";
                newList.add(aggString);
            }
            return newList;
    }

    public String Union_Queries(LinkedList<String> queries) {
        String fullQuery = "";
        for (int i = 0; i < queries.size(); i++) {
            fullQuery += queries.get(i).replace(';', ' ');
            if ( i != queries.size() -1) {
                fullQuery += " UNION ";
            }
        }
        fullQuery +=";";
        return fullQuery;
    }

    public String convertRsToJSON(ResultSet rs) {
        JSONArray json = new JSONArray();
        try { 
            ResultSetMetaData rsmd = rs.getMetaData();
            while(rs.next()) {
              int numColumns = rsmd.getColumnCount();
              JSONObject obj = new JSONObject();
              for (int i=1; i<=numColumns; i++) {
                String column_name = rsmd.getColumnName(i);
                obj.put(column_name, rs.getObject(column_name));
              }
              json.put(obj);
            }
        } catch (SQLException e) {
            System.out.println("Sql exception while converting ResultSet to String");
        }
        return json.toString();
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

        String bucketname = request.getBucketName();
        String key = request.getKey();
        Response r = new Response();
        String mytable = request.getTableName();
        JSONObject filterByJSON = request.getFilterByAsJSONOBJ(); 
        JSONObject aggregateByJSON = request.getAggregateByAsJSONOBJ();
        LinkedList<String> whereStrings= createWhereString(filterByJSON);
        LinkedList<String> queryStrings = createAggFunctionStrings(aggregateByJSON, mytable, whereStrings);
        String fullQuery = Union_Queries(queryStrings);
		
        logger.log(filterByJSON.toString());
        logger.log(aggregateByJSON.toString());

        for (int i = 0; i < queryStrings.size(); i++) {
            logger.log(queryStrings.get(i).toString());
        }
        logger.log(fullQuery);

        try 
        { 
            Properties properties = new Properties();
            properties.load(new FileInputStream("db.properties"));
            
            String url = properties.getProperty("url");
            String username = properties.getProperty("username");
            String password = properties.getProperty("password");
            String driver = properties.getProperty("driver");
            Connection con = DriverManager.getConnection(url,username,password);
            //String query = "Insert into mytable values(?,?,?,?, ?,?,?,?,?,?,?,?,?,?,?,?)";

            PreparedStatement ps = con.prepareStatement(fullQuery);
            ResultSet rs = ps.executeQuery();
         
            String queryResults = convertRsToJSON(rs);

            AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();  

            s3Client.putObject(bucketname, "QueryResults.txt", queryResults);

               
        }
        catch (Exception e) 
        {
            logger.log("Got an exception working with MySQL! ");
            logger.log(e.getMessage());
        }

        
        // Set return result in Response class, class is marshalled into JSON
        r.setValue("temp");

        
        //****************END FUNCTION IMPLEMENTATION***************************
        inspector.consumeResponse(r);
        
        //Collect final information such as total runtime and cpu deltas.
        inspector.inspectAllDeltas();
        return inspector.finish();
    }
    
    
}
