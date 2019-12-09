package main

import (
	"context"
	"database/sql"
	"encoding/csv"
	"fmt"
	"strings"

	_ "github.com/go-sql-driver/mysql"

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

	bucketname := request.BucketName
	key := request.Key
	tablename := request.TableName

	mySession, err := session.NewSession()
	if err != nil {
		return nil, err
	}
	s3client := s3.New(mySession)

	s3object, err := s3client.GetObject(&s3.GetObjectInput{Bucket: &bucketname, Key: &key})
	if err != nil {
		return nil, err
	}

	body := s3object.Body

	reader := csv.NewReader(body)

	records, err := reader.ReadAll()
	if err != nil {
		return nil, err
	}

	err = writeRecords(records, tablename)
	if err != nil {
		return nil, err
	}

	//****************END FUNCTION IMPLEMENTATION***************************

	//Collect final information such as total runtime and cpu deltas.
	inspector.InspectAllDeltas()
	return inspector.Finish(), nil
}

func writeRecords(records [][]string, tablename string) error {
	var dbinfo = struct {
		password string
		dbname   string
		username string
	}{
		password: "tcss562group2",
		dbname:   "tcp(service2rds.cluster-cwunkmk4eqtz.us-east-2.rds.amazonaws.com:3306)/service2db",
		username: "tcss562",
	}

	db, err := sql.Open("mysql", fmt.Sprintf("%s:%s@%s", dbinfo.username, dbinfo.password, dbinfo.dbname))
	if err != nil {
		return err
	}
	defer db.Close()

	err = db.Ping()
	if err != nil {
		return err
	}

	fmt.Println("Connection established")

	// Drop the table
	stmt, err := db.Prepare("DROP TABLE IF EXISTS `" + tablename + "`;")
	if err != nil {
		return err
	}
	_, err = stmt.Exec()
	if err != nil {
		return err
	}

	// Create the table
	stmt, err = db.Prepare("CREATE TABLE " + tablename + " (Region VARCHAR(40), Country VARCHAR(40), `Item Type` VARCHAR(40), `Sales Channel` VARCHAR(40),`Order Priority` VARCHAR(40), `Order Date` VARCHAR(40),`Order ID` INT PRIMARY KEY, `Ship Date` VARCHAR(40), `Units Sold` INT,`Unit Price` DOUBLE, `Unit Cost` DOUBLE, `Total Revenue` DOUBLE, `Total Cost` DOUBLE, `Total Profit` DOUBLE, `Order Processing Time` INT, `Gross Margin` FLOAT);")
	if err != nil {
		return err
	}
	_, err = stmt.Exec()
	if err != nil {
		return err
	}

	for i, record := range records {
		if i == 0 {
			continue
		}

		values := &strings.Builder{}
		for _, field := range record {
			values.WriteRune('"')
			values.WriteString(field)
			values.WriteRune('"')
			values.WriteRune(',')
		}
		valuesString := strings.TrimSuffix(values.String(), ",")

		_, err = db.Exec("insert into " + tablename + " values(" + valuesString + ");")
		if err != nil {
			return err
		}
	}

	return nil
}
