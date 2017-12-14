# raspi-cc2500

Pi4J / Wiring Pi | Raspberry Pi 3 Model B | Texas Instruments CC2500 RF Transceiver 
---------------- | -----------------------|----------------------------------------
|| Pin 17: Power 3V3 | VCC 
12 | Pin 19: SPI0\_MOSI (GPIO10) | MOSI 
14 | Pin 23: SPI0\_SCLK (GPIO11) | SCLK 
13 | Pin 21: SPI0\_MISO (GPIO9) | MISO 
10 | Pin 24: SPI0\_CE0\_N (GPIO8) | CSN 
|| Pin 25: Ground | GND 

See http://pinout.xyz/ & http://pi4j.com/pins/model-3b-rev1.html

## Commands

```bash
git clone https://github.com/RGassmann/rpiCC2500.git
cd rpiCC2500
make
sudo ./rpiCC2500
```

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
