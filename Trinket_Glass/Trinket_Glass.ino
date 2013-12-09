//Get "DigisparkUSB" from https://github.com/digistump/DigisparkArduinoIntegration
#include <DigiUSB.h>


void setup() {
  DigiUSB.begin();
  pinMode(1,OUTPUT);
}

void get_input() {
  int lastRead;

  if (DigiUSB.available()) {
    // something to read
    lastRead = DigiUSB.read();
    if (lastRead==0x80){
      digitalWrite(1,!digitalRead(1)); 
    }   
    if (lastRead==0x81){
      DigiUSB.write(GetTemp());
    }
  }
  // refresh the usb port 
  DigiUSB.refresh();
}

void loop() {
  // print output
  //DigiUSB.println("Waiting for input...");
  // get input
  get_input();
}

unsigned char GetTemp(void)
{
  unsigned int wADC;

  // The internal temperature has to be used
  // with the internal reference of 1.1V.
  // Channel 8 can not be selected with
  // the analogRead function yet.

  // Set the internal reference and mux.
  ADMUX = ((0b10<<REFS0) | (0b1111<<MUX0));
  ADCSRA |= _BV(ADEN);  // enable the ADC

  delay(20);            // wait for voltages to become stable.

  ADCSRA |= _BV(ADSC);  // Start the ADC

    // Detect end-of-conversion
  while (bit_is_set(ADCSRA,ADSC));

  // Reading register "ADCW" takes care of how to read ADCL and ADCH.
  wADC = ADCW;

  // The returned temperature is in degrees Celcius.
  return (wADC - 275);
}



