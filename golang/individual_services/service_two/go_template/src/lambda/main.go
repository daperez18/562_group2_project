package main

import (
	"context"
	"encoding/csv"
	"fmt"

	"github.com/aws/aws-lambda-go/lambda"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/aws/aws-sdk-go/service/s3"
	"github.com/daperez18/562_group2_project/golang/individual_services/service_two/go_template/src/saaf"
)

func main() {
	lambda.Start(HandleRequest)
}

func HandleRequest(ctx context.Context, request saaf.Request) (map[string]interface{}, error) {
	inspector := saaf.NewInspector()
	inspector.InspectAll()

	//****************START FUNCTION IMPLEMENTATION*************************

	inspector.AddAttribute("message", "bucketname is = "+request.BucketName+"! This is an attributed added to the Inspector!")
	fmt.Println("Hello")
	bucketname := request.BucketName
	key := request.Key

	mySession, err := session.NewSession()
	if err != nil {
		return nil, err
	}
	s3client := s3.New(mySession)

	fmt.Println("Hello")

	s3object, err := s3client.GetObject(&s3.GetObjectInput{Bucket: &bucketname, Key: &key})
	if err != nil {
		return nil, err
	}

	fmt.Println("Hello2")

	body := s3object.Body

	reader := csv.NewReader(body)

	records, err := reader.ReadAll()
	if err != nil {
		return nil, err
	}
	fmt.Println(records[0])

	// for _, record := range records {
	// 	fmt.Println(record)
	// }

	// writeRecords(records)

	//****************END FUNCTION IMPLEMENTATION***************************

	//Collect final information such as total runtime and cpu deltas.
	inspector.InspectAllDeltas()
	return inspector.Finish(), nil
}

// func writeRecords(records [][]string) error {
// 	// var dbinfo = struct {
// 	// 	password string
// 	// 	url      string
// 	// 	driver   string
// 	// 	username string
// 	// }{
// 	// 	password: "562group2",
// 	// 	url:      "jdbc:mysql://service2rds.cluster-cjpsmuknzd8o.us-east-2.rds.amazonaws.com:3306/562group2DB",
// 	// 	driver:   "com.mysql.cj.jdbc.Driver",
// 	// 	username: "tcss562",
// 	// }

// 	db, err := gorm.Open("postgres", "wow")
// 	if err != nil {
// 		return err
// 	}

// 	tx := db.Begin()

// 	tx.Commit()

// 	return nil
// }
