package demo.qualcomm.spi.spi;


public class spiConfigs {
    public static final String[] devicePath    = {"/sys/class/spidev1"};
    public static final int    speed         = 5000000;
    public static final int    SPI_CPHA      = 0x01;
    public static final int    SPI_CPOL      = 0x02;
    public static final int    SPI_MODE_0    = (0 | 0);
    public static final int    SPI_MODE_1    = (0 | SPI_CPHA);
    public static final int    SPI_MODE_2    = (SPI_CPOL | 0);
    public static final int    SPI_MODE_3    = (SPI_CPOL | SPI_CPHA);
    public static final int    SPI_CS_HIGH   = 0x04;
    public static final int    SPI_MODE_CS_H = (SPI_MODE_3 | SPI_CS_HIGH);
    public static final int    SPI_MODE_CS_L = (SPI_MODE_3 | 0);
    public static final int    SPI_LSB_FIRST = 0x08;
    public static final int    SPI_3WIRE     = 0x10;
    public static final int    SPI_LOOP      = 0x20;
    public static final int    SPI_NO_CS     = 0x40;
    public static final int    SPI_READY     = 0x80;
    public static final int    SPI_TX_DUAL   = 0x100;
    public static final int    SPI_TX_QUAD   = 0x200;
    public static final int    SPI_RX_DUAL   = 0x400;
    public static final int    SPI_RX_QUAD   = 0x800;
}
