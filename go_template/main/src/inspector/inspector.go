package inspector

import (
	"fmt"
	"io/ioutil"
	"time"
)

type Inspector struct {
	startTime  int64
	attributes map[string]interface{}

	inspectedCPU       bool
	inspectedMemory    bool
	inspectedContainer bool
	inspectedPlatform  bool
	inspectedLinux     bool
}

func New() Inspector {
	inspector := Inspector{}

	inspector.startTime = time.Now().UnixNano()
	inspector.attributes = make(map[string]interface{})

	inspector.attributes["version"] = 0.4
	inspector.attributes["lang"] = "go"

	return inspector
}

func (inspector *Inspector) AddAttribute(key string, value interface{}) {
	inspector.attributes[key] = value
}

func (inspector *Inspector) GetAttribute(key string) interface{} {
	return inspector.attributes[key]
}

func (inspector *Inspector) InspectAll() {
	inspector.InspectContainer()
	inspector.InspectPlatform()
	inspector.InspectLinux()
	inspector.InspectMemory()
	inspector.InspectCPU()
	inspector.AddTimeStamp("frameworkRuntime")
}

func (inspector *Inspector) InspectContainer() {
	// TODO
}

func (inspector *Inspector) InspectPlatform() {
	// TODO
}

func (inspector *Inspector) InspectLinux() {
	// TODO
}

func (inspector *Inspector) InspectMemory() {
	// TODO
}

func (inspector *Inspector) InspectCPU() {
	inspector.inspectedCPU = true

	// var text string
	// var start int
	// var end int

	f, err := ioutil.ReadFile("/proc/cpuinfo")
	if err != nil {
		fmt.Println(err)
		return
	}

	f2, err := ioutil.ReadFile("/proc/stat")
	if err != nil {
		fmt.Println(err)
		return
	}
	// fileMap := parseFile(f)
	// text = string(f)

	inspector.AddAttribute("cpuinfo file", string(f))
	inspector.AddAttribute("stat file", string(f2))
	// start = strings.Index(text, "name") + 7
	// end = start + text[]

	//     //Get CPU Type
	//     text = getFileAsString("/proc/cpuinfo");
	//     start = text.indexOf("name") + 7;
	//     end = start + text.substring(start).indexOf(":");
	//     String cpuType = text.substring(start, end - 9).trim();
	//     attributes.put("cpuType", cpuType);

	//     //Get CPU Model
	//     start = text.indexOf("model") + 9;
	//     end = start + text.substring(start).indexOf(":");
	//     String cpuModel = text.substring(start, end - 11).trim();
	//     attributes.put("cpuModel", cpuModel);

	//     //Get CPU Core Count
	//     start = text.indexOf("cpu cores") + 12;
	//     end = start + text.substring(start).indexOf(":");
	//     String cpuCores = text.substring(start, end - 9).trim();
	//     attributes.put("cpuCores", cpuCores);

	//     //Get CPU Metrics
	//     String filename = "/proc/stat";
	//     File f = new File(filename);
	//     Path p = Paths.get(filename);
	//     if (f.exists()) {
	//         try (BufferedReader br = Files.newBufferedReader(p)) {
	//             text = br.readLine();
	//             String params[] = text.split(" ");

	//             String[] metricNames = {"cpuUsr", "cpuNice", "cpuKrn", "cpuIdle",
	//                 "cpuIowait", "cpuIrq", "cpuSoftIrq", "vmcpusteal"};

	//             for (int i = 0; i < metricNames.length; i++) {
	//                 attributes.put(metricNames[i], Long.parseLong(params[i + 2]));
	//             }

	//             while ((text = br.readLine()) != null && text.length() != 0) {
	//                 if (text.contains("ctxt")) {
	//                     String prms[] = text.split(" ");
	//                     attributes.put("contextSwitches", Long.parseLong(prms[1]));
	//                 }
	//             }

	//             br.close();
	//         } catch (IOException ioe) {
	//             //sb.append("Error reading file=" + filename);
	//         }
	//     }
	// TODO
}

// func parseFile(b []byte) map[string]interface{} {
// 	fileMap := map[string]interface{}

// 	bytes.Spli
// }

func (inspector *Inspector) InspectAllDeltas() {

	if val, ok := inspector.attributes["frameworkRuntime"]; ok {
		currentTime := time.Now().UnixNano()
		codeRuntime := (currentTime - inspector.startTime) - val.(int64)
		inspector.attributes["userRuntime"] = codeRuntime
	}

	inspector.InspectCPUDelta()
	inspector.InspectMemoryDelta()
}

func (inspector *Inspector) InspectCPUDelta() {
	// TODO
}

func (inspector *Inspector) InspectMemoryDelta() {
	// TODO
}

// func (inspector *Inspector) ConsumeResponse(response Response) {
// 	// TODO
// }

func (inspector *Inspector) AddTimeStamp(key string) {
	currentTime := time.Now().UnixNano()
	runtime := (currentTime - inspector.startTime)
	inspector.attributes[key] = runtime
}

func (inspector *Inspector) Finish() map[string]interface{} {
	endTime := time.Now().UnixNano()
	runtime := (endTime - inspector.startTime)
	inspector.attributes["runtime"] = runtime
	return inspector.attributes
}
