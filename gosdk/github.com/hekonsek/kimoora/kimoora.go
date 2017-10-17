package kimoora

import (
	"net/http"
	"os"
	"bytes"
	"fmt"
	"encoding/json"
	"io/ioutil"
	"errors"
)

type Kimoora struct {

	FrontDoorEndpoint string

	Token string

	HttpClient* http.Client

}

func KimmoraClient(frontDoorEndpoint string, token string) Kimoora {
	return Kimoora{FrontDoorEndpoint: frontDoorEndpoint, Token: token, HttpClient: &http.Client{}}
}

func DiscoverKimmoraClient(token string) Kimoora {
	frontDoorEndpoint := os.Getenv("KIMOORA_FRONT_DOOR_ENDPOINT")
	return Kimoora{FrontDoorEndpoint: frontDoorEndpoint, Token:token, HttpClient: &http.Client{}}
}

func (thisKimoora Kimoora) CachePut(cacheName string, key string, value map[string]interface{}) error  {
	url := fmt.Sprintf("%s/cachePut/%s/%s", thisKimoora.FrontDoorEndpoint, cacheName, key)
	valueJson, err := json.Marshal(value)
	if err != nil {
		return err
	}

	req, err := http.NewRequest("POST", url, bytes.NewBuffer(valueJson))
	req.Header.Set("Authentication", "Bearer " + thisKimoora.Token)
	req.Header.Set("Content-Type", "application/json")
	resp, err := thisKimoora.HttpClient.Do(req)
	if err != nil {
		return err
	}

	body := resp.Body
	defer body.Close()
	bodyBytes, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		return err
	}
	var responseJson map[string]interface{}
	json.Unmarshal(bodyBytes, &responseJson)
	if responseJson["status"] != "OK" {
		return errors.New("invalid response from Kimmora server")
	}

	return nil
}