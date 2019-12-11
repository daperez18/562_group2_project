# This is just to support Azure.
# If you are not deploying there this can be removed.
import os
import sys
import boto3
import csv
import pymysql

sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__))))

import json
import logging
#import pandas as pd
from Inspector import *

import time

#
# Define your FaaS F    unction here.
# Each platform handler will call and pass parameters to this function.
# 
# @param request A JSON object provided by the platform handler.
# @param context A platform specific object used to communicate with the cloud platform.
# @returns A JSON object to use as a response.
#
def yourFunction(request, context):
    # Import the module and collect data
    inspector = Inspector()
    inspector.inspectAll()
    inspector.addTimeStamp("frameworkRuntime")
    bucketname = str(request['bucketname'])
    key = str(request['key'])

    s3 = boto3.client('s3')
    csvfile = s3.get_object(Bucket=bucketname, Key=key)
    csvcontent = csvfile['Body'].read().split(b'\n')
    i = 0
    for line in csvcontent:
        csvcontent[i] = line.decode("utf-8")
        i = i+1
    csv_data = csv.DictReader(csvcontent)

    #csv_input = pd.read_csv('input.csv')
    #csv_input['Berries'] = csv_input['Name']
    #csv_input.to_csv('output.csv', index=False)

    #test_str=csv_data["Region"]
    test_val=""
    content = read_content(csvcontent)
    output = write_output(content)
    # Add custom message and finish the function
    if ('key' in request):
        inspector.addAttribute("bucketname", "bucketname " + str(request['bucketname']) + "!")
        inspector.addAttribute("key", str(request['key']))
        inspector.addAttribute("test val", csvcontent[0])


    
    inspector.inspectCPUDelta()
    return inspector.finish()

def read_content(content):
    list = []
    try:
        temp = csv.reader(content, delimiter=',')
        for row in temp:
            list.append(row)
    
    except Exception as ex:
        print(ex.__str__())

    return list

def write_output(content):
    try:
        connection = mysql.connector.connect(url="url", databasename="dbname", username="tcss562", password="password")
        cursor = connection.cursor();
        
        for i in range(1, len(content)):
            col = len(content[i])
            curr_string = ""
            for j in range(col):
                curr_string += "\"" + content[i][j] + "\""

                if (j+1)!=col:
                    curr_string += ","
            cursor.execute("insert into mytable values("+ curr_string + ");")
        cursor("ALTER TABLE mytable ORDER BY `Order Id`")    
    except Exception as ex:
        print(ex.__str___())
    finally:
        connection.close()
        cursor.close()
