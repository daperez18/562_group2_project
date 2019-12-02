#!/bin/bash

# JSON object to pass to Lambda Function
bucketname="testbucketohiodap";
key="562sales_small_new.csv";

json2={"\"bucketname\"":"\"$bucketname\"","\"key\"":"\"$key\""}

#echo "Invoking Lambda function using API Gateway"
#time output=`curl -s -H "Content-Type: application/json" -X POST -d  $json {INSERT API GATEWAY URL HERE}`

#echo ""
#echo "CURL RESULT:"
#echo $output
#echo ""
#echo ""
echo $json2 | jq
echo "Invoking Lambda function using AWS CLI"
#time output=`aws lambda invoke --invocation-type RequestResponse --function-name  ServiceTwo --region us-east-2 --payload $json2 /dev/stdout | head -n 1 | head -c -2 ; echo`
echo ""
echo "AWS CLI RESULT:"
echo $output
echo ""

