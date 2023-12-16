import java.io.Serializable;
import java.util.Arrays;

public class WordCodePair implements Serializable {
    private byte[] word;
    private String code;

    public WordCodePair(byte[] word, String code) {
        this.word = word;
        this.code = code;
    }

    public byte[] getWord() {
        return word;
    }

    public void setWord(byte[] word) {
        this.word = word;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof WordCodePair wordCodePair)) return false;
        return Arrays.equals(word, wordCodePair.word);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(word);
    }
}
