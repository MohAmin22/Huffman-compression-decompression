import java.io.IOException;

public interface BitInputStreamInf {
    public boolean readBit();

    public byte readByte();

    public int readInt();

    public long readLong();

    public byte[] readNBytes(int n);

    public int fetch() throws IOException;

    public void setGetNumberOfBitsWrittenInCompressedFile(long numberOfBitsWritten);

}
