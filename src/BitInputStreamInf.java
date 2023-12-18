import java.io.IOException;

public interface BitInputStreamInf {
    public boolean readBit() throws IOException;

    public byte readByte() throws IOException;

    public int readInt() throws IOException;

    public long readLong() throws IOException;

    public byte[] readNBytes(int n) throws IOException;

    public int fetch() throws IOException;

    public void setGetNumberOfBitsWrittenInCompressedFile(long numberOfBitsWritten);

}
