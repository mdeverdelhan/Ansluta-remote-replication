package eu.verdelhan.ansluta;

import com.pi4j.io.gpio.*;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;
import com.pi4j.io.spi.SpiMode;

import java.io.IOException;

/**
 * An Ikea Ansluta remote.
 * 
 * CC2500 : http://www.ti.com/lit/ds/swrs040c/swrs040c.pdf
 */
public class AnslutaRemote {

    public static final boolean DEBUG = true;

    /** GPIO controller */
    private static final GpioController gpio = GpioFactory.getInstance();

    /**
     * Some SPI pins.
     */
    private static Pin spiMiso = RaspiPin.GPIO_13;
    private static Pin spiCs = RaspiPin.GPIO_10;

    private static GpioPinDigitalInput misoInput = null;
    private static GpioPinDigitalOutput chipSelectOutput = null;

    /** The SPI device */
    private SpiDevice spi;

    /**
     * Inits the remote.
     */
    public void initAnsluta() throws IOException{
        chipSelectOutput = gpio.provisionDigitalOutputPin(spiCs, "CS");
        misoInput = gpio.provisionDigitalInputPin(spiMiso, "MISO");

        if(DEBUG) {
            System.out.println("Debug mode");
            System.out.print("Initialisation");
        }
        spi = SpiFactory.getInstance(SpiChannel.CS0, 6000000, SpiMode.MODE_0); //Faster SPI mode, maximal speed for the CC2500 without the need for extra delays

        chipSelectOutput.high();
        sendStrobe(CC2500.CC2500_SRES); //0x30 SRES Reset chip.
        initCC2500();
        // sendStrobe(CC2500.CC2500_SPWD); //Enter power down mode    -   Not used in the prototype
        writeReg((byte) 0x3E, (byte) 0xFF); //Maximum transmit power - write 0xFF to 0x3E (PATABLE)
        if (DEBUG) {
            System.out.println(" - Done");
        }
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down.");
            gpio.shutdown();
        }));
    }

    /**
     * Reads the address bytes from a remote (by sniffing its packets wireless).
     */
    public void readAddressBytes() throws IOException {
        byte tries = 0;
        boolean AddressFound = false;

        if (DEBUG) {
            System.out.print("Listening for an Address");
        }

        while ((tries<2000)&&(!AddressFound)) { //Try to listen for the address 2000 times
            if (DEBUG){
                System.out.print(".");
            }
            sendStrobe(CC2500.CC2500_SRX);
            writeReg(CC2500.REG_IOCFG1, (byte) 0x01);   // Switch MISO to output if a packet has been received or not
            delay(5);
            if (misoInput.isHigh()) { // TODO check condition
                //byte readStatus = readRegister(CC2500.REG_RXBYTES); // TODO: test3
                //System.out.println("read status: " + bytesToHexString(readStatus));
                byte PacketLength = readRegister(CC2500.CC2500_FIFO);
                byte[] recvPacket = new byte[PacketLength];
                if(DEBUG) {
                    System.out.print("\nPacket received: " + PacketLength + " bytes");
                }

                if (PacketLength <= 8) { // A packet from the remote cant be longer than 8 bytes
                    // Read the received data from CC2500
                    for (byte i = 0; i < PacketLength; i++) {
                        recvPacket[i] = readRegister(CC2500.CC2500_FIFO);
                    }
                    if (DEBUG) {
                        System.out.println(bytesToHexString(recvPacket));
                    }
                }

                // Search for the start of the sequence
                byte start=0;
                while ((recvPacket[start]!=0x55) && (start < PacketLength)) {
                    start++;
                }
                if (recvPacket[start+1]==0x01 && recvPacket[start+5]==0xAA) {
                    // The bytes match an Ikea remote sequence
                    AddressFound = true;
                    byte AddressByteA = recvPacket[start+2]; // Extract the addressbytes
                    byte AddressByteB = recvPacket[start+3];
                    byte RecvCommand = recvPacket[start+4];
                    if (DEBUG) {
                        System.out.println("Address Bytes found: " + bytesToHexString(AddressByteA, AddressByteB));
                        System.out.println("Hex command: " + bytesToHexString(RecvCommand));
                        if(RecvCommand==AnslutaCommand.LIGHT_OFF.command){System.out.println("Command:OFF");}
                        if(RecvCommand==AnslutaCommand.LIGHT_ON_50.command){System.out.println("Command:50%");}
                        if(RecvCommand==AnslutaCommand.LIGHT_ON_100.command){System.out.println("Command:100%");}
                        if(RecvCommand==AnslutaCommand.LIGHT_PAIR.command){System.out.println("Command:Pair");}
                    }
                }
                sendStrobe(CC2500.CC2500_SIDLE); // Needed to flush RX FIFO
                sendStrobe(CC2500.CC2500_SFRX); // Flush RX FIFO
            }
            tries++; //Another try has passed
        }
        if (DEBUG) {
            System.out.println(" - Done");
        }
    }

    /**
     * Sends a command like a real Ansluta remote.
     * @param addressByteA the first byte of the address
     * @param addressByteB the second byte of the address
     * @param command the command to be sent
     */
    public void sendCommand(byte addressByteA, byte addressByteB, byte command) throws IOException{
        if (DEBUG) {
            System.out.print("Send command " + bytesToHexString(addressByteA, addressByteB, command));
        }
        for (byte i=0;i<50;i++) { // Send 50 times
            System.out.print(".");
            sendStrobe(CC2500.CC2500_SIDLE); // 0x36 SIDLE Exit RX / TX, turn off frequency synthesizer and exit Wake-On-Radio mode if applicable.
            sendStrobe(CC2500.CC2500_SFTX); // 0x3B SFTX Flush the TX FIFO buffer. Only issue SFTX in IDLE or TXFIFO_UNDERFLOW states.
            chipSelectOutput.low();
            while (misoInput.isHigh()) { }; // Wait untill MISO low
            //Command 0x01=Light OFF 0x02=50% 0x03=100% 0xFF=Pairing
            spi.write((byte) 0x7F, (byte) 0x06, (byte) 0x55, (byte) 0x01, addressByteA, addressByteB, command, (byte) 0xAA, (byte) 0xFF);
            chipSelectOutput.high();
            sendStrobe(CC2500.CC2500_STX); //0x35 STX In IDLE state: Enable TX. Perform calibration first if MCSM0.FS_AUTOCAL=1. If in RX state and CCA is enabled: Only go to TX if channel is clear
            delay(1);
        }
        if (DEBUG) {
            System.out.println(" - Done");
        }
    }

    /**
     * Reads a CC2500 register.
     * @param addr the CC2500 register address
     * @return the read value
     */
    private byte readRegister(byte addr) throws IOException {
        chipSelectOutput.low();
        while (misoInput.isHigh()) { };
        byte[] readStatus = spi.write((byte) (addr + 0x80), (byte) 0);
        chipSelectOutput.high();
        return readStatus[0];
    }

    /**
     * Sends a strobe to the CC2500
     * @param strobe the strobe to be sent
     */
    private void sendStrobe(byte strobe) throws IOException {
        chipSelectOutput.low();
        while (misoInput.isHigh()) { };
        spi.write(strobe);
        chipSelectOutput.high();
        delay(20);
    }

    /**
     * Writes a value to a CC2500 register.
     * @param addr the CC2500 register address
     * @param value the value to be written
     */
    private void writeReg(byte addr, byte value) throws IOException {
        chipSelectOutput.low();
        while (misoInput.isHigh()) { };
        byte[] writeStatus = spi.write(addr, value);
        chipSelectOutput.high();
    }

    /**
     * Inits the TI CC2500 like in a real Ansluta remote.
     */
    private void initCC2500() throws IOException {

        writeReg(CC2500.REG_IOCFG2, (byte) 0x29);
        writeReg(CC2500.REG_IOCFG0, (byte) 0x06);
        writeReg(CC2500.REG_PKTLEN, (byte) 0xFF); // Max packet length
        writeReg(CC2500.REG_PKTCTRL1, (byte) 0x04);
        writeReg(CC2500.REG_PKTCTRL0, (byte) 0x05); // variable packet length; CRC enabled
        writeReg(CC2500.REG_ADDR, (byte) 0x01); // Device address
        writeReg(CC2500.REG_CHANNR, (byte) 0x10); // Channel number
        writeReg(CC2500.REG_FSCTRL1, (byte) 0x09);
        writeReg(CC2500.REG_FSCTRL0, (byte) 0x00);
        writeReg(CC2500.REG_FREQ2, (byte) 0x5D); // RF frequency 2433.000000 MHz 
        writeReg(CC2500.REG_FREQ1, (byte) 0x93); // RF frequency 2433.000000 MHz 
        writeReg(CC2500.REG_FREQ0, (byte) 0xB1); // RF frequency 2433.000000 MHz
        writeReg(CC2500.REG_MDMCFG4, (byte) 0x2D);
        writeReg(CC2500.REG_MDMCFG3, (byte) 0x3B); // Data rate 250.000000 kbps
        writeReg(CC2500.REG_MDMCFG2, (byte) 0x73); // MSK, (byte) No Manchester; 30/32 sync mode
        writeReg(CC2500.REG_MDMCFG1, (byte) 0xA2);
        writeReg(CC2500.REG_MDMCFG0, (byte) 0xF8); // Channel spacing 199.9500 kHz
        writeReg(CC2500.REG_DEVIATN, (byte) 0x01);
        writeReg(CC2500.REG_MCSM2, (byte) 0x07);
        writeReg(CC2500.REG_MCSM1, (byte) 0x30);
        writeReg(CC2500.REG_MCSM0, (byte) 0x18);
        writeReg(CC2500.REG_FOCCFG, (byte) 0x1D);
        writeReg(CC2500.REG_BSCFG, (byte) 0x1C);
        writeReg(CC2500.REG_AGCCTRL2, (byte) 0xC7);
        writeReg(CC2500.REG_AGCCTRL1, (byte) 0x00);
        writeReg(CC2500.REG_AGCCTRL0, (byte) 0xB2);
        writeReg(CC2500.REG_WOREVT1, (byte) 0x87);
        writeReg(CC2500.REG_WOREVT0, (byte) 0x6B);
        writeReg(CC2500.REG_WORCTRL, (byte) 0xF8);
        writeReg(CC2500.REG_FREND1, (byte) 0xB6);
        writeReg(CC2500.REG_FREND0, (byte) 0x10);
        writeReg(CC2500.REG_FSCAL3, (byte) 0xEA);
        writeReg(CC2500.REG_FSCAL2, (byte) 0x0A);
        writeReg(CC2500.REG_FSCAL1, (byte) 0x00);
        writeReg(CC2500.REG_FSCAL0, (byte) 0x11);
        writeReg(CC2500.REG_RCCTRL1, (byte) 0x41);
        writeReg(CC2500.REG_RCCTRL0, (byte) 0x00);
        writeReg(CC2500.REG_FSTEST, (byte) 0x59);
        writeReg(CC2500.REG_TEST2, (byte) 0x88);
        writeReg(CC2500.REG_TEST1, (byte) 0x31);
        writeReg(CC2500.REG_TEST0, (byte) 0x0B);
        writeReg(CC2500.REG_DAFUQ, (byte) 0xFF);
    }

    /**
     * Ugly delay method.
     * @param delay the delay (in milliseconds)
     */
    private static void delay(long delay) {
        try { Thread.sleep(delay); } catch (InterruptedException ie) { }
    }

    /** Array of all hexadecimal chars */
    private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

    /**
     * @param bytes a byte array
     * @return a hex string
     */
    private static String bytesToHexString(byte... bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_CHARS[v >>> 4];
            hexChars[i * 2 + 1] = HEX_CHARS[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Test entry point.
     */
    public static void main(String[] args) throws Exception{

        AnslutaRemote r = new AnslutaRemote();
        r.initAnsluta();
        r.readAddressBytes();

    }
}
