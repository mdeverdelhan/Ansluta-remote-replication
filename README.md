# raspi-cc2500

Here is an attempt to replicate an Ikea Ansluta remote using a Raspberry Pi, a TI CC2500, Java an Pi4J.


## Hardware

There are at least two different versions of the Ikea Ansluta remote. This project is about the 2.4GHz one.

The original remote uses a [TI CC2500 2.4GHz RF controller](http://www.ti.com/lit/ds/swrs040c/swrs040c.pdf). We choose to use the same component. We bought this one: https://www.ebay.com/itm/2PCS-1-8-3-6V-CC2500-IC-Wireless-RF-2400MHZ-Transceiver-Module-SPI-ISM-Demo-Code/401239287968

We use the Raspberry Pi 3 model B.

### Linking

Pi4J / Wiring Pi | Raspberry Pi 3 Model B | Texas Instruments CC2500 RF Transceiver 
---------------- | -----------------------|----------------------------------------
|| Pin 17: Power 3V3 | VCC 
12 | Pin 19: SPI0\_MOSI (GPIO10) | MOSI 
14 | Pin 23: SPI0\_SCLK (GPIO11) | SCLK 
13 | Pin 21: SPI0\_MISO (GPIO9) | MISO 
10 | Pin 24: SPI0\_CE0\_N (GPIO8) | CSN 
|| Pin 25: Ground | GND 

See http://pinout.xyz/ & http://pi4j.com/pins/model-3b-rev1.html

## Software

The project is divided into 3 classes. The main class is [AnslutaRemote](https://github.com/mdeverdelhan/raspi-cc2500/blob/master/src/main/java/eu/verdelhan/ansluta/AnslutaRemote.java). It has a `main()` so you can run it in order to get the address bytes of the remote you want to copy.

## Resources

  * https://github.com/doctor64/sensors-code
  * https://github.com/msloth/contiki-launchpad/commit/a951b28808d0eea5b8e069c0db9ff30271e9b6ba
  * https://github.com/RGassmann/rpiCC2500
  * https://github.com/yasiralijaved/Arduino-CC2500-Library
  * https://github.com/tlalexander/Arduino-CC2500-Library
  * https://github.com/RoXXoR/CC2500
  * https://github.com/Zohan/ArduinoCC2500Demo
  * https://github.com/alexer/cc2500-tools
  * https://www.youtube.com/watch?v=AGlIB6O0iOA
