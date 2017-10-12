package main

import "fmt"
import "os"

func main() {
	fmt.Printf(os.Args[1:][0])
}