package eu.verdelhan.ansluta;


import com.pi4j.io.gpio.*;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;
import com.pi4j.io.spi.SpiMode;

import java.io.IOException;

public class AnslutaRemote {

    public static final boolean DEBUG = true;

    private static final GpioController gpio = GpioFactory.getInstance();

    private static Pin spiMiso = RaspiPin.GPIO_13;
    private static Pin spiCs = RaspiPin.GPIO_10;

    private static GpioPinDigitalInput misoInput = null;
    private static GpioPinDigitalOutput chipSelectOutput = null;


    SpiDevice spi;

    void initAnsluta() throws IOException{
        chipSelectOutput = gpio.provisionDigitalOutputPin(spiCs, "CS");
        misoInput = gpio.provisionDigitalInputPin(spiMiso, "MISO");

        if(DEBUG) {
            System.out.println("Debug mode");
            System.out.print("Initialisation");
        }
        spi = SpiFactory.getInstance(SpiChannel.CS0, 6000000, SpiMode.MODE_0); //Faster SPI mode, maximal speed for the CC2500 without the need for extra delays

        chipSelectOutput.high();
        sendStrobe(CC2500.CC2500_SRES); //0x30 SRES Reset chip.
        init_CC2500();
        //  sendStrobe(CC2500.CC2500_SPWD); //Enter power down mode    -   Not used in the prototype
        WriteReg((byte) 0x3E, (byte) 0xFF);  //Maximum transmit power - write 0xFF to 0x3E (PATABLE)
        if(DEBUG) {
            System.out.println(" - Done");
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down.");
            gpio.shutdown();
        }));
    }

    void readAddressBytes() throws IOException{     //Read Address Bytes From a remote by sniffing its packets wireless
        byte tries=0;
        boolean AddressFound = false;

        if(DEBUG) {
            System.out.print("Listening for an Address");
        }

        while((tries<2000)&&(!AddressFound)) { //Try to listen for the address 2000 times
            if(DEBUG){
                System.out.print(".");
            }
            sendStrobe(CC2500.CC2500_SRX);
            WriteReg(CC2500.REG_IOCFG1,(byte) 0x01);   // Switch MISO to output if a packet has been received or not
            delay(5);
            if (misoInput.isHigh()) { // TODO check condition
                byte PacketLength = readRegister(CC2500.CC2500_FIFO);
                byte[] recvPacket = new byte[PacketLength];
                if(DEBUG) {
                    System.out.print("\nPacket received: " + PacketLength + " bytes");
                }

                if(PacketLength <= 8) {                       //A packet from the remote cant be longer than 8 bytes
                    for(byte i = 1; i <= PacketLength; i++) {   //Read the received data from CC2500
                        recvPacket[i] = readRegister(CC2500.CC2500_FIFO);
                    }
                    if(DEBUG) {
                        System.out.println(bytesToHexString(recvPacket));
                    }
                }

                byte start=0;
                while((recvPacket[start]!=0x55) && (start < PacketLength)) {   //Search for the start of the sequence
                    start++;
                }
                if(recvPacket[start+1]==0x01 && recvPacket[start+5]==0xAA) {   //If the bytes match an Ikea remote sequence
                    AddressFound = true;
                    byte AddressByteA = recvPacket[start+2];                // Extract the addressbytes
                    byte AddressByteB = recvPacket[start+3];
                    byte RecvCommand = recvPacket[start+4];
                    if(DEBUG) {
                        System.out.println("Address Bytes found: " + bytesToHexString(AddressByteA, AddressByteB));
                        System.out.println("Hex command: " + bytesToHexString(RecvCommand));
                        if(RecvCommand==AnslutaCommand.LIGHT_OFF.command){System.out.println("Command:OFF");}
                        if(RecvCommand==AnslutaCommand.LIGHT_ON_50.command){System.out.println("Command:50%");}
                        if(RecvCommand==AnslutaCommand.LIGHT_ON_100.command){System.out.println("Command:100%");}
                        if(RecvCommand==AnslutaCommand.LIGHT_PAIR.command){System.out.println("Command:Pair");}
                    }
                }
                sendStrobe(CC2500.CC2500_SIDLE);      // Needed to flush RX FIFO
                sendStrobe(CC2500.CC2500_SFRX);       // Flush RX FIFO
            }
            tries++;  //Another try has passed
        }
        if(DEBUG) {
            System.out.println(" - Done");
        }
    }

    private byte readRegister(byte addr) throws IOException {
        chipSelectOutput.low();
        while (misoInput.isHigh()) { };
        byte[] x = spi.write((byte) (addr + 0x80));
        delay(10);
        byte[] y = spi.write((byte) 0);
        chipSelectOutput.high();
        return y[0];
    }

    private void sendStrobe(byte strobe) throws IOException {
        chipSelectOutput.low();
        while (misoInput.isHigh()) { };
        spi.write(strobe);
        chipSelectOutput.high();
        delay(20);
    }

    void sendCommand(byte AddressByteA, byte AddressByteB, byte Command) throws IOException{
        if(DEBUG) {
            System.out.print("Send command " + bytesToHexString(Command) + " to " + bytesToHexString(AddressByteA, AddressByteB));
        }
        for(byte i=0;i<50;i++) {       //Send 50 times
            System.out.print(".");
            sendStrobe(CC2500.CC2500_SIDLE);   //0x36 SIDLE Exit RX / TX, turn off frequency synthesizer and exit Wake-On-Radio mode if applicable.
            sendStrobe(CC2500.CC2500_SFTX);    //0x3B SFTX Flush the TX FIFO buffer. Only issue SFTX in IDLE or TXFIFO_UNDERFLOW states.
            chipSelectOutput.low();
            while (misoInput.isHigh()) { };  //Wait untill MISO high (NdMarc: LOW!!!)
            spi.write((byte) 0x7F, (byte) 0x06, (byte) 0x55, (byte) 0x01, AddressByteA, AddressByteB, Command, (byte) 0xAA, (byte) 0xFF);
            //SPI.transfer(0x7F);
            //delayMicroseconds(delayA);
            //SPI.transfer(0x06);
            //delayMicroseconds(delayA);
            //SPI.transfer(0x55);
            //delayMicroseconds(delayA);
            //SPI.transfer(0x01);
            //delayMicroseconds(delayA);
            //SPI.transfer(AddressByteA);                 //Address Byte A
            //delayMicroseconds(delayA);
            //SPI.transfer(AddressByteB);                 //Address Byte B
            //delayMicroseconds(delayA);
            //SPI.transfer(Command);                      //Command 0x01=Light OFF 0x02=50% 0x03=100% 0xFF=Pairing
            //delayMicroseconds(delayA);
            //SPI.transfer(0xAA);
            //delayMicroseconds(delayA);
            //SPI.transfer(0xFF);
            chipSelectOutput.high();
            sendStrobe(CC2500.CC2500_STX);                 //0x35 STX In IDLE state: Enable TX. Perform calibration first if MCSM0.FS_AUTOCAL=1. If in RX state and CCA is enabled: Only go to TX if channel is clear
            delay(1); //delayMicroseconds(10);      //Longer delay for transmitting
        }
        if(DEBUG) {
            System.out.println(" - Done");
        }
    }

    private void WriteReg(byte addr, byte value) throws IOException {
        chipSelectOutput.low();
        while (misoInput.isHigh()) { };
        spi.write(addr, value);
        //SPI.transfer(addr);
        //delay(1); //delayMicroseconds(200);
        //SPI.transfer(value);
        chipSelectOutput.high();

    }

    private static void delay(long delay) {
        try { Thread.sleep(delay); } catch (InterruptedException ie) { }
    }

    /** Array of all hexadecimal chars */
    private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

    /**
     * @param bytes a byte array
     * @return a hex string
     */
    public static String bytesToHexString(byte... bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_CHARS[v >>> 4];
            hexChars[i * 2 + 1] = HEX_CHARS[v & 0x0F];
        }
        return new String(hexChars);
    }

    private void init_CC2500() throws IOException {
        WriteReg(CC2500.REG_IOCFG2, (byte) 0x29);
        WriteReg(CC2500.REG_IOCFG0, (byte) 0x06);
        WriteReg(CC2500.REG_PKTLEN, (byte) 0xFF); // Max packet length
        WriteReg(CC2500.REG_PKTCTRL1, (byte) 0x04);
        WriteReg(CC2500.REG_PKTCTRL0, (byte) 0x05); // variable packet length; CRC enabled
        WriteReg(CC2500.REG_ADDR, (byte) 0x01); // Device address
        WriteReg(CC2500.REG_CHANNR, (byte) 0x10); // Channel number
        WriteReg(CC2500.REG_FSCTRL1, (byte) 0x09);
        WriteReg(CC2500.REG_FSCTRL0, (byte) 0x00);
        WriteReg(CC2500.REG_FREQ2, (byte) 0x5D); // RF frequency 2433.000000 MHz 
        WriteReg(CC2500.REG_FREQ1, (byte) 0x93); // RF frequency 2433.000000 MHz 
        WriteReg(CC2500.REG_FREQ0, (byte) 0xB1); // RF frequency 2433.000000 MHz // RF frequency 2433.000000 MHz 
        WriteReg(CC2500.REG_MDMCFG4, (byte) 0x2D);
        WriteReg(CC2500.REG_MDMCFG3, (byte) 0x3B); // Data rate 250.000000 kbps
        WriteReg(CC2500.REG_MDMCFG2, (byte) 0x73); //MSK, (byte) No Manchester; 30/32 sync mode
        WriteReg(CC2500.REG_MDMCFG1, (byte) 0xA2);
        WriteReg(CC2500.REG_MDMCFG0, (byte) 0xF8); // Channel spacing 199.9500 kHz
        WriteReg(CC2500.REG_DEVIATN, (byte) 0x01);
        WriteReg(CC2500.REG_MCSM2, (byte) 0x07);
        WriteReg(CC2500.REG_MCSM1, (byte) 0x30);
        WriteReg(CC2500.REG_MCSM0, (byte) 0x18);
        WriteReg(CC2500.REG_FOCCFG, (byte) 0x1D);
        WriteReg(CC2500.REG_BSCFG, (byte) 0x1C);
        WriteReg(CC2500.REG_AGCCTRL2, (byte) 0xC7);
        WriteReg(CC2500.REG_AGCCTRL1, (byte) 0x00);
        WriteReg(CC2500.REG_AGCCTRL0, (byte) 0xB2);
        WriteReg(CC2500.REG_WOREVT1, (byte) 0x87);
        WriteReg(CC2500.REG_WOREVT0, (byte) 0x6B);
        WriteReg(CC2500.REG_WORCTRL, (byte) 0xF8);
        WriteReg(CC2500.REG_FREND1, (byte) 0xB6);
        WriteReg(CC2500.REG_FREND0, (byte) 0x10);
        WriteReg(CC2500.REG_FSCAL3, (byte) 0xEA);
        WriteReg(CC2500.REG_FSCAL2, (byte) 0x0A);
        WriteReg(CC2500.REG_FSCAL1, (byte) 0x00);
        WriteReg(CC2500.REG_FSCAL0, (byte) 0x11);
        WriteReg(CC2500.REG_RCCTRL1, (byte) 0x41);
        WriteReg(CC2500.REG_RCCTRL0, (byte) 0x00);
        WriteReg(CC2500.REG_FSTEST, (byte) 0x59);
        WriteReg(CC2500.REG_TEST2, (byte) 0x88);
        WriteReg(CC2500.REG_TEST1, (byte) 0x31);
        WriteReg(CC2500.REG_TEST0, (byte) 0x0B);
        WriteReg(CC2500.REG_DAFUQ, (byte) 0xFF);

    }

    public static void main(String[] args) throws Exception{

        AnslutaRemote r = new AnslutaRemote();
        r.initAnsluta();
        r.readAddressBytes();

    }
}
