package main

import (
	"context"
	"fmt"

	"github.com/wlloyduw/SAAF/go_template/src/inspector"

	"github.com/aws/aws-lambda-go/lambda"
)

func HandleRequest(ctx context.Context, request Request) (map[string]interface{}, error) {

	inspector := inspector.New()
	inspector.InspectAll()

	//****************START FUNCTION IMPLEMENTATION*************************

	inspector.AddAttribute("message", fmt.Sprintf("Hello %s!", request.Name))
	inspector.AddAttribute("request", request)

	// sleeptime, _ := time.ParseDuration("1s")
	// time.Sleep(sleeptime)

	//****************END FUNCTION IMPLEMENTATION***************************

	//Collect final information such as total runtime and cpu deltas.
	inspector.InspectAllDeltas()
	return inspector.Finish(), nil
}

func main() {
	lambda.Start(HandleRequest)
}
