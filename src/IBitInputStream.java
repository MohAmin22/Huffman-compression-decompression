import java.io.IOException;

public interface IBitInputStream {
    boolean readBit() throws IOException;

    byte readByte() throws IOException;

    int readInt() throws IOException;

    long readLong() throws IOException;

    byte[] readNBytes(int n) throws IOException;

    int fetch() throws IOException;

    void setGetNumberOfBitsWrittenInCompressedFile(long numberOfBitsWritten);

}
