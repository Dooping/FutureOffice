/*
 *  This sketch sends a message to a TCP server
 *
 */

#include <ESP8266WiFi.h>
#include <ESP8266WiFiMulti.h>

ESP8266WiFiMulti WiFiMulti;

void setup() {
    Serial.begin(115200);
    delay(10);

    // We start by connecting to a WiFi network
    WiFiMulti.addAP("scmu", "abcd1234");

    //Serial.println();
    //Serial.println();
    //Serial.print("Wait for WiFi... ");

    while(WiFiMulti.run() != WL_CONNECTED) {
        //Serial.print(".");
        delay(500);
    }

    //Serial.println("");
    //Serial.println("WiFi connected");
    //Serial.println("IP address: ");
    //Serial.println(WiFi.localIP());

    delay(500);
}


void loop() {
    const uint16_t port = 8181;
    const char * host = "192.168.137.1"; // ip or dns

    
    
    //.print("connecting to ");
    //Serial.println(host);

    // Use WiFiClient class to create TCP connections
    WiFiClient client;

    if (!client.connect(host, port)) {
        //Serial.println("connection failed");
        //Serial.println("wait 5 sec...");
        delay(5000);
        return;
    }

    while(Serial.available())
       {
           Serial.read();
       }


    //read back one line from server
    String line = client.readStringUntil('\r');
    Serial.print(line);
    Serial.print(' ');
    Serial.print(line);
    Serial.print(' ');
    Serial.print(line);
    Serial.print(' ');
    Serial.print(line);
    Serial.print(' ');
    Serial.print(line);
    Serial.print(' ');

    //Serial.println("closing connection");
    while(client.available())
     {
         client.read();
     }

    delay(100);
    float temp = 0;
    int i;
    for(i = 0; i<5; i++)
      temp = temp + Serial.parseFloat();
    temp = temp/5;

    int lum = 0;
    for(i = 0; i<5; i++)
      lum = lum +Serial.parseInt();
    lum = lum/5;

    int a,counta1=0,counta0=0;
    for(i = 0; i<5; i++){
      a = Serial.parseInt();
      //Serial.println(a);
      if(a == 0)
        counta0++;
      if(a == 1)
        counta1++;
    }
    if(counta0>=counta1)
      a = 0;
    else
      a = 1;

    client.println(temp);
    client.println(lum);
    client.println(a);
    client.flush();

     
    client.stop();
    
    
    delay(5000);
}

