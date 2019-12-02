package lambda;

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

/**
 * uwt.lambda_test::handleRequest
 *
 * @author Wes Lloyd
 * @author Robert Cordingly
 */
public class ServiceOne implements RequestHandler<Request, HashMap<String, Object>> {
    private static final String COMMA_DELIMITER=", ";

    /**
     * Lambda Function Handler
     * 
     * @param request Request POJO with defined variables from Request.java
     * @param context 
     * @return HashMap that Lambda will automatically convert into JSON.
     */

    public void addColumn(String path,String fileName) throws IOException{
        BufferedReader br=null;
        BufferedWriter bw=null;
        final String lineSep=System.getProperty("line.separator");

        try {
            File file = new File(path, fileName);
            File file2 = new File(path, fileName+".1");//so the
                        //names don't conflict or just use different folders

            br = new BufferedReader(new InputStreamReader(new FileInputStream(file))) ;
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file2)));
            String line = null;
                        int i=0;
            for ( line = br.readLine(); line != null; line = br.readLine(),i++)
            {               

                String addedColumn = "a";//String.valueOf(data.get(i));
                bw.write(line+addedColumn+lineSep);
        }

        }catch(Exception e){
            System.out.println(e);
        }finally  {
            if(br!=null)
                br.close();
            if(bw!=null)
                bw.close();
        }

    }
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

    public StringWriter write_csv(List<String[]> info) {
        StringWriter sw = new StringWriter();
        for (int i =0; i < info.get(0).length; i++) {
            sw.append(info.get(0)[i]);

            if (i+1 !=info.get(0).length) {
                    sw.append(",");
            }
            else {
                
                sw.append(",Order Processing Time, Gross Margin");
                sw.append("\n");
            }
        }

        //order id = 6
        Set<Integer> unique_ids = new HashSet<Integer>();        
        //info.length is amount of rows
        //info.get(i).length is values in row  i
        for (int i =1; i <info.size(); i++) {
            int col = info.get(i).length;
            if (!unique_ids.contains(info.get(i)[6])) {
            
                for (int j = 0; j < col; j++) {
                  //  System.out.println("i = " + i + ", j= " + j +"info size= " + info.size());

                    if (j == 4) {
                        String val = info.get(i)[4];
                        if (val.equals("C")) {
                            sw.append("Critical");
                        }
                        else if (val.equals("L")){
                            sw.append("Low");
                        }
                        else if (val.equals("M")){
                            sw.append("Medium");
                        }
                        else if (val.equals("H")) {
                            sw.append("High");
                        }
                    } else {
                        sw.append(info.get(i)[j]);
                    }

                    if ((j+1)!=col)
                        sw.append(",");
                    else {
                        String date1=info.get(i)[5];
                        String date2=info.get(i)[7];
                        String[] date1_values=date1.split("/");
                        String[] date2_values=date2.split("/");
                        int month = Integer.parseInt(date1_values[0]);
                        int day = Integer.parseInt(date1_values[1]);
                        int year = Integer.parseInt(date1_values[2]);

                        int month2 = Integer.parseInt(date2_values[0]);
                        int day2 = Integer.parseInt(date2_values[1]);
                        int year2 = Integer.parseInt(date2_values[2]);

                        int order_time= ((year2 - year) * 365) + ((month2 - month) * 30) + (day2 - day);

                        float gross_margin = Float.parseFloat(info.get(i)[13]) / Float.parseFloat(info.get(i)[11]);
                        sw.append("," + order_time + "," + gross_margin);
                        sw.append("\n");

                    }

                }
            } else {
                unique_ids.add(Integer.parseInt(info.get(i)[6]));
            }
        }
        return sw;
    }


    public HashMap<String, Object> handleRequest(Request request, Context context) {
       
        //Collect inital data.
        Inspector inspector = new Inspector();
        inspector.inspectAll();
        
        //****************START FUNCTION IMPLEMENTATION*************************
        //Add custom key/value attribute to SAAF's output. (OPTIONAL)
        inspector.addAttribute("message", "bucketname is = " + request.getBucketName()
                + "! This is an attributed added to the Inspector!");

		//consumes request

        String bucketname = request.getBucketName();
        String key = request.getKey();

        AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();         
        //get object file using source bucket and srcKey name
        S3Object s3Object = s3Client.getObject(new GetObjectRequest(bucketname, key));
        //get content of the file

        InputStream objectData = s3Object.getObjectContent();
        //scanning data line by line
        
        List<String[]> records = readcsv(objectData);
        StringWriter sw = write_csv(records);
        
        byte[] bytes = sw.toString().getBytes(StandardCharsets.UTF_8);
        InputStream is = new ByteArrayInputStream(bytes);
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(bytes.length);
        meta.setContentType("text/plain");
        // Create new file on S3
        AmazonS3 s3Client2 = AmazonS3ClientBuilder.standard().build();
        s3Client2.putObject(bucketname, key.substring(0, key.lastIndexOf('.')) +"_new.csv", is, meta);

        Response response = new Response();

        response.setValue("Bucket: " + bucketname + " key:" + key + " processed. record 0 = ");

        inspector.consumeResponse(response);
        
        //****************END FUNCTION IMPLEMENTATION***************************
        
        //Collect final information such as total runtime and cpu deltas.
        inspector.inspectAllDeltas();
        return inspector.finish();
    }
}
