package main

import (
	"context"
	"fmt"

	"github.com/aws/aws-lambda-go/lambda"
	inspector "github.com/daperez18/562_group2_project/golang/inspector/src/saaf"
)

func main() {
	lambda.Start(HandleRequest)
}

func HandleRequest(ctx context.Context, request map[string]interface{}) (map[string]interface{}, error) {

	inspector := inspector.NewInspector()
	inspector.InspectAll()

	//****************START FUNCTION IMPLEMENTATION*************************

	inspector.AddAttribute("message", fmt.Sprintf("Hello %s!", request["name"]))
	inspector.AddAttribute("request", request)

	//****************END FUNCTION IMPLEMENTATION***************************

	//Collect final information such as total runtime and cpu deltas.
	inspector.InspectAllDeltas()
	return inspector.Finish(), nil
}
