#!/bin/bash

#Columns to filter data by
Region=["\"Australia\"","\"Oceania\""]
Item_Type=["\"Office_Supplies\""]
Sales_Channel=["\"Offline\""]
Order_Priority=["\"Medium\""]
Country=["\"Fiji\""]
filterBy="{\"Region\":$Region,\"Item_Type\":$Item_Type,\"Sales_Channel\":$Sales_Channel,\"Order_Priority\":$Order_Priority,\"Country\":$Country}"

#Columns to perform aggregations on
max=["\"Units_Sold\""];
min=["\"Units_Sold\""];
avg=["\"Order_Processing_Time\"","\"Gross_Margin\"","\"Units_Sold\""]
sum=["\"Units_Sold\"","\"Total_Revenue\"","\"Total_Profit\""]


aggregateBy="{\"max\":$max,\"min\":$min,\"avg\":$avg,\"sum\":$sum}"
aggregateByAsString="$(echo $aggregateBy |jq tojson)"
filterByAsString="$(echo $filterBy |jq tojson)"

bucketname="testbucketohiodap";
key="562sales_small_new.csv";
json={"\"filterBy\"":$filterByAsString,"\"aggregateBy\"":$aggregateByAsString,"\"bucketname\"":"\"$bucketname\"","\"key\"":"\"$key\""}


#echo "Invoking Lambda function using API Gateway"
#time output=`curl -s -H "Content-Type: application/json" -X POST -d  $json {INSERT API GATEWAY URL HERE}`

#echo ""
#echo "CURL RESULT:"
#echo $output
#echo ""
#echo ""

echo "Invoking Lambda function using AWS CLI"
time output=`aws lambda invoke --invocation-type RequestResponse --function-name ServiceThree --region us-east-2 --payload $json /dev/stdout | head -n 1 | head -c -2 ; echo`
echo ""
echo "AWS CLI RESULT:"
echo $output
echo ""

