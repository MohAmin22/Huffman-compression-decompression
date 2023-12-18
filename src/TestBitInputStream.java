import java.io.FileInputStream;
import java.io.IOException;

public class TestBitInputStream {
    BitInputStream bitInputStream = new BitInputStream(new FileInputStream("/home/mohamed/CSED_25/Year_3/Algo/Huffman Compression/20011502.1.input.txt.hc"));

    public TestBitInputStream() throws IOException {
    }

    void readNumberOfBytesPerWord() throws IOException {
        bitInputStream.fetch();
        System.out.println( "Number of bytes per word: " + bitInputStream.readInt());
        System.out.println("size of byte array: " + bitInputStream.readInt());
    }
    void readBitSequence(BitInputStream bitInputStream) throws IOException {
        while(bitInputStream.fetch() != -1) {
            for (int i = 0; i < 8; i++) {
                System.out.print((bitInputStream.readBit()?'1' : '0'));
            }
            System.out.println();
        }
    }
}
