import java.util.BitSet;

public class HuffmanCode {
    BitSet code;
   int codeLength;

    public HuffmanCode(BitSet code, int codeLength) {
        this.code = code;
        this.codeLength = codeLength;
    }
    public boolean getBit(int index) {
        return code.get(index);
    }
    public int getCodeLength() {
        return codeLength;
    }
}
