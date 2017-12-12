package Tools;
 
 
import java.io.IOException;
import java.io.InputStream;

 

public class CAVLCReader extends BitstreamReader {

    public CAVLCReader(InputStream is) throws IOException {
        super(is);
    }

    public long readNBit(int n, String message) throws IOException {
        long val = readNBit(n);

        trace(message, String.valueOf(val));

        return val;
    }

    /**
     * Read unsigned exp-golomb code
     *
     * @return
     * @throws java.io.IOException
     * @throws java.io.IOException
     */
    private int readUE() throws IOException {
        int cnt = 0;
        while (read1Bit() == 0)
            cnt++;

        int res = 0;
        if (cnt > 0) {
            long val = readNBit(cnt);

            res = (int) ((1 << cnt) - 1 + val);
        }

        return res;
    }

    /*
      * (non-Javadoc)
      *
      * @see
      * ua.org.jplayer.javcodec.h264.H264BitInputStream#readUE(java.lang.String)
      */
    public int readUE(String message) throws IOException {
        int res = readUE();

        trace(message, String.valueOf(res));

        return res;
    }

    public int readSE(String message) throws IOException {
        int val = readUE();

        int sign = ((val & 0x1) << 1) - 1;
        val = ((val >> 1) + (val & 0x1)) * sign;

        trace(message, String.valueOf(val));

        return val;
    }

    public boolean readBool(String message) throws IOException {

        boolean res = read1Bit() == 0 ? false : true;

        trace(message, res ? "1" : "0");

        return res;
    }

    public int readU(int i, String string) throws IOException {
        return (int) readNBit(i, string);
    }

    public byte[] read(int payloadSize) throws IOException {
        byte[] result = new byte[payloadSize];
        for (int i = 0; i < payloadSize; i++) {
            result[i] = (byte) readByte();
        }
        return result;
    }

    public boolean readAE() {
        // TODO: do it!!
        throw new UnsupportedOperationException("Stan");
    }

    public int readTE(int max) throws IOException {
        if (max > 1)
            return readUE();
        return ~read1Bit() & 0x1;
    }

    public int readAEI() {
        // TODO: do it!!
        throw new UnsupportedOperationException("Stan");
    }

    public int readME(String string) throws IOException {
        return readUE(string);
    }

  

    public void readTrailingBits() throws IOException {
        read1Bit();
        readRemainingByte();
    }

    private void trace(String message, String val) {
     
      
    }
}