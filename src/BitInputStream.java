import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class BitInputStream implements BitInputStreamInf {
    private final int MAX_BUFFER_SIZE = 50000; // bytes
    BufferedInputStream inputStream;
    //private byte buffer = 0;
    private int operationCounter = 0;
    private byte[] buffer = new byte[MAX_BUFFER_SIZE];
    private int currentBytePtr = 0;
    private int bytesRead = 0; // pointer to the last byte in the buffer

    public BitInputStream(FileInputStream fis) throws IOException {
        inputStream = new BufferedInputStream(fis);
       // buffer = inputStream.readAllBytes();
    }

    // Have to invoke this function before any reading process to fill the buffer
    // output: state integer if -1 the file has been ended

    //        if (currentBytePtr < MAX_BUFFER_SIZE && currentBytePtr > bytesRead)
    //            return -1;
    public int fetch() throws IOException {
        if (currentBytePtr >= bytesRead) {
            currentBytePtr = 0;
            bytesRead = inputStream.read(buffer);
        }
        return bytesRead; // if +ve then the buffer is not empty, continue reading
    }

    @Override
    public boolean readBit() {
        byte currentByte = buffer[currentBytePtr];
        boolean bit = (currentByte & (1 << (7 - operationCounter))) != 0;
        operationCounter++;
        if (operationCounter == 8) {
            currentBytePtr++;
            operationCounter = 0;
        }
        return bit;
    }

    @Override
    public byte readByte() {
        byte b = 0;
        for (int i = 7; i >= 0; i--) {
            b |= (byte) ((readBit() ? 1 : 0) << i);
        }
        return b;
    }

    @Override
    public byte[] readNBytes(int n) {
        if(n <= 0) return null;
        byte[] arr = new byte[n];
        for (int i = 0; i < n; i++) {
            arr[i] = readByte();
        }
        return arr;
    }

    @Override
    public int readInt() {
        byte[] Int = readNBytes(4);
        int a = 0;
        for (int i = 3; i >= 0; i--) {
            a |= (Int[3 - i] << i * 8);
        }
        return a;
    }


}
