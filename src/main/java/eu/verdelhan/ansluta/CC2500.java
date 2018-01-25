package eu.verdelhan.ansluta;

/**
 * 
 */
public class CC2500 {

    /**
     * Command Strobe registers
     */
    public static final byte CC2500_SRES        = 0x30;	// Reset chip
    public static final byte CC2500_SFSTXON     = 0x31;	// Enable and calibrate frequency synthesizer (if MCSM0.FS_AUTOCAL=1).
    public static final byte CC2500_SXOFF       = 0x32;	// Turn off crystal oscillator.
    public static final byte CC2500_SCAL        = 0x33;	// Calibrate frequency synthesizer and turn it off.
    public static final byte CC2500_SRX         = 0x34; // Enable RX. Perform calibration if enabled
    public static final byte CC2500_STX         = 0x35; // Enable TX. If in RX state, only enable TX if CCA passes
    public static final byte CC2500_SIDLE       = 0x36; // Exit RX / TX
    public static final byte CC2500_SWOR        = 0x38;	// Start automatic RX polling sequence (Wake-on-Radio)
    public static final byte CC2500_SPWD        = 0x39;	// Enter power down mode when CSn goes high.
    public static final byte CC2500_SFRX        = 0x3A; // Flush the RX FIFO buffer. Only issue SFRX in IDLE or RXFIFO_OVERFLOW states
    public static final byte CC2500_SFTX        = 0x3B; // Flush the TX FIFO buffer. Only issue SFTX in IDLE or TXFIFO_UNDERFLOW states
    public static final byte CC2500_SWORRST     = 0x3C;	// Reset real time clock to Event1 value.
    public static final byte CC2500_SNOP        = 0x3D;	// No operation. May be used to get access to the chip status byte.
    public static final byte CC2500_FIFO        = 0x3F; // TX and RX FIFO

    /**
     * Radio register.
     */
    public static final byte REG_IOCFG2         = 0x0000;
    public static final byte REG_IOCFG1         = 0x0001;
    public static final byte REG_IOCFG0         = 0x0002;
    public static final byte REG_FIFOTHR        = 0x0003;
    public static final byte REG_SYNC1          = 0x0004;
    public static final byte REG_SYNC0          = 0x0005;
    public static final byte REG_PKTLEN         = 0x0006;
    public static final byte REG_PKTCTRL1       = 0x0007;
    public static final byte REG_PKTCTRL0       = 0x0008;
    public static final byte REG_ADDR           = 0x0009;
    public static final byte REG_CHANNR         = 0x000A;
    public static final byte REG_FSCTRL1        = 0x000B;
    public static final byte REG_FSCTRL0        = 0x000C;
    public static final byte REG_FREQ2          = 0x000D;
    public static final byte REG_FREQ1          = 0x000E;
    public static final byte REG_FREQ0          = 0x000F;
    public static final byte REG_MDMCFG4        = 0x0010;
    public static final byte REG_MDMCFG3        = 0x0011;
    public static final byte REG_MDMCFG2        = 0x0012;
    public static final byte REG_MDMCFG1        = 0x0013;
    public static final byte REG_MDMCFG0        = 0x0014;
    public static final byte REG_DEVIATN        = 0x0015;
    public static final byte REG_MCSM2          = 0x0016;
    public static final byte REG_MCSM1          = 0x0017;
    public static final byte REG_MCSM0          = 0x0018;
    public static final byte REG_FOCCFG         = 0x0019;
    public static final byte REG_BSCFG          = 0x001A;
    public static final byte REG_AGCCTRL2       = 0x001B;
    public static final byte REG_AGCCTRL1       = 0x001C;
    public static final byte REG_AGCCTRL0       = 0x001D;
    public static final byte REG_WOREVT1        = 0x001E;
    public static final byte REG_WOREVT0        = 0x001F;
    public static final byte REG_WORCTRL        = 0x0020;
    public static final byte REG_FREND1         = 0x0021;
    public static final byte REG_FREND0         = 0x0022;
    public static final byte REG_FSCAL3         = 0x0023;
    public static final byte REG_FSCAL2         = 0x0024;
    public static final byte REG_FSCAL1         = 0x0025;
    public static final byte REG_FSCAL0         = 0x0026;
    public static final byte REG_RCCTRL1        = 0x0027;
    public static final byte REG_RCCTRL0        = 0x0028;
    public static final byte REG_FSTEST         = 0x0029;
    public static final byte REG_PTEST          = 0x002A;
    public static final byte REG_AGCTEST        = 0x002B;
    public static final byte REG_TEST2          = 0x002C;
    public static final byte REG_TEST1          = 0x002D;
    public static final byte REG_TEST0          = 0x002E;
    public static final byte REG_PARTNUM        = 0x0030;
    public static final byte REG_VERSION        = 0x0031;
    public static final byte REG_FREQEST        = 0x0032;
    public static final byte REG_LQI            = 0x0033;
    public static final byte REG_RSSI           = 0x0034;
    public static final byte REG_MARCSTATE      = 0x0035;
    public static final byte REG_WORTIME1       = 0x0036;
    public static final byte REG_WORTIME0       = 0x0037;
    public static final byte REG_PKTSTATUS      = 0x0038;
    public static final byte REG_VCO_VC_DAC     = 0x0039;
    public static final byte REG_TXBYTES        = 0x003A;
    public static final byte REG_RXBYTES        = 0x003B;
    public static final byte REG_RCCTRL1_STATUS = 0x003C;
    public static final byte REG_RCCTRL0_STATUS = 0x003D;
    public static final byte REG_DAFUQ          = 0x007E;
}
