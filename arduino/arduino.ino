#include <math.h>
#define ThermistorPIN 2
#include <SoftwareSerial.h>
//SoftwareSerial ESPserial(0, 1); // RX | TX

int inputPirPin = 3; // choose the input pin (PIR sensor)
int pirState = LOW; // we start, assuming no motion detected
int val = 0; // variable for reading the pin status


const int rgbrPin = 5;
const int rgbgPin = 6;
const int rgbbPin = 11;
const int sensorPin = 0;
const int redPin = 9;
const int greenPin = 8;
int lightLevel, high = 0, low = 1023;
int blinds = 0;

float pad = 10000;

float Thermistor(int RawADC) {
  long Resistance;
  float Temp;

  Resistance = pad * ((1024.0 / RawADC) - 1);
  Temp = log(Resistance);
  Temp = 1 / (0.001129148 + (0.000234125 * Temp) + (0.0000000876741 * Temp * Temp * Temp));
  Temp = Temp - 273.15;
  return Temp;
}

// the setup routine runs once when you press reset:
void setup() {
  // initialize the digital pin as an output.
  pinMode(redPin, OUTPUT);
  pinMode(greenPin, OUTPUT);
  pinMode(rgbrPin, OUTPUT);
  pinMode(rgbgPin, OUTPUT);
  pinMode(rgbbPin, OUTPUT);
  pinMode(inputPirPin, INPUT);
  Serial.begin(115200);
  //ESPserial.begin(115200);
}

// the loop routine runs over and over again forever:
void loop() {
  if ( Serial.available() )   {
    int i,a,b,counta1=0,counta0=0,countb1=0, countb0=0;
    for(i = 0; i<5; i++){
      a = Serial.parseInt();
      b = Serial.parseInt();
      /*Serial.print(a,1);
      Serial.print(" ");
      Serial.print(b,1);
      Serial.println();*/
      if(a == 0)
        counta0++;
      if(a == 1)
        counta1++;
      if(b == 0)
        countb0++;
      if(b == 1)
        countb1++;
    }
    if(counta0>counta1)
      a = 0;
    else
      a = 1;

    if(countb0>countb1)
      b = 0;
    else
      b = 1;
    
    if(b == 1 && blinds == 0)
      openBlinds();
    if(b == 0 && blinds == 1)
      closeBlinds();
    if(a == 1)
      acCold();
    if(a == 0)
      acOff();

    
    while(Serial.available())
       {
           Serial.read();
       }
    //delay(5000);
    lightLevel = analogRead(sensorPin);
    manualTune();
    Serial.print(lightLevel, 1);
    Serial.print(" ");
    Serial.print(lightLevel, 1);
    Serial.print(" ");
    Serial.print(lightLevel, 1);
    Serial.print(" ");
    Serial.print(lightLevel, 1);
    Serial.print(" ");
    Serial.print(lightLevel, 1);
    Serial.print(" ");
    if (pirState == HIGH)
      Serial.println("1 1 1 1 1");
    else
      Serial.println("0 0 0 0 0");
    
    pirState = LOW;
  }
  pirSensor();
  delay(2000);
  
}

void acCold() {
  digitalWrite(rgbbPin, HIGH);
  digitalWrite(rgbrPin, LOW);
  digitalWrite(rgbgPin, LOW);
}

void acHot() {
  digitalWrite(rgbbPin, LOW);
  digitalWrite(rgbrPin, HIGH);
  digitalWrite(rgbgPin, LOW);
}

void acOff() {
  digitalWrite(rgbbPin, LOW);
  digitalWrite(rgbrPin, LOW);
  digitalWrite(rgbgPin, LOW);
}

void openBlinds() {
  blinds = 1;
  digitalWrite(greenPin, HIGH);
  delay(10000);
  digitalWrite(greenPin, LOW);
}

void closeBlinds() {
  blinds = 0;
  digitalWrite(redPin, HIGH);
  delay(10000);
  digitalWrite(redPin, LOW);
}

void pirSensor() {
  val = digitalRead(inputPirPin); // read input value
  if (val == HIGH) {
    // check if the input is HIGH
    //digitalWrite(ledPin, HIGH); // turn LED ON
    if (pirState == LOW) {
      // we have just turned on
      //Serial.println("Motion detected!");
      // We only want to print on the output change, not state
      pirState = HIGH;
    }
  }
}

void manualTune() {
  lightLevel = map(lightLevel, 0, 1023, 0, 255);
  lightLevel = constrain(lightLevel, 0, 255);

  float temp;
  temp = Thermistor(analogRead(ThermistorPIN));
  Serial.print(temp, 1);
  Serial.print(" ");
  Serial.print(temp, 1);
  Serial.print(" ");
  Serial.print(temp, 1);
  Serial.print(" ");
  Serial.print(temp, 1);
  Serial.print(" ");
  Serial.print(temp, 1);
  Serial.print(" ");
}
