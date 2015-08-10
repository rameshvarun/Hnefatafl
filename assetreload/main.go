package main

import (
	"log"
    "github.com/howeyc/fsnotify"
	"flag"
    "net/http"
    "path/filepath"
    "io/ioutil"
    "bytes"
)


func main() {
	server := flag.String("server", "", "The asset server to connect to.")
    flag.Parse()

    if *server == "" {
        log.Fatal("Please provide a server in the form 'hostname:port' with the -server option.")
    }
    log.Println("Server: " + *server)

	watcher, err := fsnotify.NewWatcher()
    if err != nil {
        log.Fatal(err)
    }

    done := make(chan bool)

    client := &http.Client{}

    go func() {
        for {
            select {
            case ev := <-watcher.Event:
                log.Println("event:", ev)
				if ev.IsModify() {
                    // Read file data
                    data, err := ioutil.ReadFile(ev.Name)
                    if err != nil {
                        log.Println(err)
                        break
                    }

                    normalized := filepath.Clean(ev.Name)
                    url := "http://" + *server + "/" + normalized;
                    log.Println("PUT " + url)
                    req, err := http.NewRequest("PUT", url, bytes.NewBuffer(data))
                    if err != nil {
                        log.Println(err)
                        break
                    }
                    resp, err := client.Do(req)
                    if err != nil {
                        log.Println(err)
                        break
                    }
                    defer resp.Body.Close()

                    body, err := ioutil.ReadAll(resp.Body)
                    if err != nil {
                        log.Println(err)
                        break
                    }
                    log.Println("Response Body: " + string(body))

				}
            case err := <-watcher.Error:
                log.Println("error:", err)
            }
        }
    }()

    err = watcher.Watch(".")
    if err != nil {
        log.Fatal(err)
    }

    <-done

    watcher.Close()
}
