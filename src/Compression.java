import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Compression {
    private String inputPath;
    private final int  numberOfBytesPerWord;
    private long fileSizeInBytes;
    private byte[] lastByteArray;
    //
    private final int MAX_CAPACITY; // Size of input buffer

    public Compression(int numberOfBytesPerWord) {
        this.numberOfBytesPerWord = numberOfBytesPerWord;
        this.MAX_CAPACITY = numberOfBytesPerWord * 50000; // MAX_CAPACITY has to be multiplier of numberOfBytesPerWord
    }

    public void compress(String inputPath) {
        try {
            this.setInput(inputPath);
            this.compressFile();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // Don't forget to close streams
    private void setInput(String inputPath) throws Exception {
        this.inputPath = inputPath;

        Path path = Paths.get(inputPath);
        this.fileSizeInBytes = Files.size(path);
    }

    private BufferedInputStream createInputStream() throws FileNotFoundException {
        FileInputStream inputStream = new FileInputStream(inputPath);
        return new BufferedInputStream(inputStream);
    }

    private Map<ByteArrayWrapper, Long> constructFrequencyMap() throws IOException {
        // Create input stream
        BufferedInputStream bufferedInputStream = createInputStream();
        Map<ByteArrayWrapper, Long> frequencyTable = new HashMap<>();
        //read byte[numberOfBytesPerWord] from input
        byte[] buffer = new byte[MAX_CAPACITY];
        //byte[] buffer = new byte[numberOfBytesPerWord];
        int bytesRead;
        while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
            int start = 0, end = numberOfBytesPerWord - 1;
            while (end < bytesRead) {
                byte[] currentBuffer = Arrays.copyOfRange(buffer, start, end + 1); // To enforce mutability of map keys
                frequencyTable.put(new ByteArrayWrapper(currentBuffer),
                        frequencyTable.getOrDefault(new ByteArrayWrapper(currentBuffer), 0L) + 1L);
                start = end + 1;
                end += numberOfBytesPerWord;
            }
            if (end < MAX_CAPACITY) {
                this.lastByteArray = Arrays.copyOfRange(buffer, start, bytesRead); // lastIndexOfInCompleteWord = bytesRead - 1;
            }
        }
        bufferedInputStream.close();
        //return frequency table
        return frequencyTable;
    }

    private PriorityQueue<Node> convertFrequencyTableMapToPriorityQueue(Map<ByteArrayWrapper, Long> frequencyTable) {
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingLong(Node::getFrequency));
        for (Map.Entry<ByteArrayWrapper, Long> entry : frequencyTable.entrySet()) {
            queue.add(new Node(entry.getKey().getBuffer(), entry.getValue()));
        }
        return queue;
    }

    // input : queue of word-frequency pairs
    private Node constructHuffmanTree(PriorityQueue<Node> queue) {
        while (queue.size() > 1) {
            Node left = queue.poll();
            Node right = queue.poll();
            assert right != null; // Throws an Exception when the file is so small that fits in 1 or less byte[n]
            Node parent = new Node(null, left.getFrequency() + right.getFrequency(), left, right);
            queue.add(parent);
        }
        return queue.poll();
    }

    /*
     *  input : root of huffman tree
     *  output : huffman table in ArrayList form
     */
    private Map<ByteArrayWrapper, String> constructHuffmanTable(Node root) {
        Map<ByteArrayWrapper, String> huffmanTable = new HashMap<>();
        buildHuffmanTable(huffmanTable, "", root);
        return huffmanTable;
    }

    private void buildHuffmanTable(Map<ByteArrayWrapper, String> huffmanTable, String code, Node node) {
        if (node.getLeft() == null && node.getRight() == null) {
            huffmanTable.put(new ByteArrayWrapper(node.getWord()), code);
        }
        if (node.getLeft() != null) {
            buildHuffmanTable(huffmanTable, code + "0", node.getLeft());
        }
        if (node.getRight() != null) {
            buildHuffmanTable(huffmanTable, code + "1", node.getRight());
        }
    }

    private void storeLastByteArray(BitOutputStream bitOutputStream) throws IOException {
        if (this.lastByteArray != null) {
            bitOutputStream.writeInt(this.lastByteArray.length);
            bitOutputStream.writeByteArray(this.lastByteArray);
        } else {
            bitOutputStream.writeInt(0);
        }
    }

    private String getOutputPath() {
        String inputFileName = this.inputPath.substring(this.inputPath.lastIndexOf(File.separatorChar) + 1);
        String outputFileName = "20011502" + "." + this.numberOfBytesPerWord + "." + inputFileName + "." + "hc";
        return this.inputPath.substring(0, this.inputPath.lastIndexOf('/') + 1) + outputFileName;
    }

    private BitOutputStream createBitOutputStream() throws FileNotFoundException {
        String outputPath = getOutputPath();
        FileOutputStream fos = new FileOutputStream(outputPath);
        return new BitOutputStream(fos);
    }

    private void storeHuffmanTree(Node root, BitOutputStream bitOutputStream) throws IOException {
        if (root.isLeaf()) {
            bitOutputStream.writeBit(false); // left indicator
            bitOutputStream.writeBit(false); // right indicator
            bitOutputStream.writeByteArray(root.getWord());
        } else if (root.hasLeftChild() && root.hasRightChild()) {
            bitOutputStream.writeBit(true);
            bitOutputStream.writeBit(true);
            storeHuffmanTree(root.getLeft(), bitOutputStream);
            storeHuffmanTree(root.getRight(), bitOutputStream);
        } else if (root.hasLeftChild() && !root.hasRightChild()) {
            bitOutputStream.writeBit(true);
            bitOutputStream.writeBit(false);
            storeHuffmanTree(root.getLeft(), bitOutputStream);
        } else if (!root.hasLeftChild() && root.hasRightChild()) {
            bitOutputStream.writeBit(false);
            bitOutputStream.writeBit(true);
            storeHuffmanTree(root.getRight(), bitOutputStream);
        }
    }
    private void storeNumberOfBytesPerWord(BitOutputStream bitOutputStream) throws IOException {
        bitOutputStream.writeInt(this.numberOfBytesPerWord);
    }

    private void encodeAndCompress(BitOutputStream bitOutputStream, Map<ByteArrayWrapper, String> huffmanTable) throws IOException {
        BufferedInputStream bufferedInputStream = createInputStream();
        try {
            //read byte[numberOfBytesPerWord] from input
            byte[] buffer = new byte[numberOfBytesPerWord];
            int bytesRead;
            // Set bitOutputStream for bit manipulation
            //BitOutputStream bos = new BitOutputStream(fos);
            while ((bytesRead = bufferedInputStream.read(buffer)) != -1) { // bytesRead in the last byte group may be n or less
                if (bytesRead < numberOfBytesPerWord) break; // Last byte[] was saved in the file
                // Find the code of the current word
                String code = huffmanTable.get(new ByteArrayWrapper(buffer));
                // Write the code to the output file
                for (char c : code.toCharArray()) {
                    if (c == '0') {
                        bitOutputStream.writeBit(false);
                    } else if (c == '1') {
                        bitOutputStream.writeBit(true);
                    } else {
                        throw new IllegalArgumentException();
                    }
                }
            }
            bitOutputStream.endWriting();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            bufferedInputStream.close();
            bitOutputStream.close();
        }
    }

    private long huffmanTreeSize(Node root) {
        if (root == null) return 0;
        if (root.getLeft() == null && root.getRight() == null) return 1;
        return 1 + huffmanTreeSize(root.getLeft()) + huffmanTreeSize(root.getRight());
    }

    private void compressFile() throws IOException {
        // Collect statistics
        Map<ByteArrayWrapper, Long> frequencyTable = constructFrequencyMap();
        System.out.println("Table size: " + frequencyTable.size());
        // Construct huffman tree
        PriorityQueue<Node> queue = convertFrequencyTableMapToPriorityQueue(frequencyTable);
        Node root = constructHuffmanTree(queue);
        System.out.println("Huffman Tree size : " + huffmanTreeSize(root));
        // Write huffman tree to file
        BitOutputStream bitOutputStream = createBitOutputStream();
        // Store number of bytes per word (n)
        storeNumberOfBytesPerWord(bitOutputStream);
        // Store last byte[] information
        storeLastByteArray(bitOutputStream);
        // Store huffman tree in the file
        storeHuffmanTree(root, bitOutputStream);
        // to del when enable encodeAndCompress(bitOutputStream, huffmanTable);
        bitOutputStream.endWriting();
        bitOutputStream.close();
        //

        // Extract huffman table ( word -> code )
        Map<ByteArrayWrapper, String> huffmanTable = constructHuffmanTable(root);
        // Encode and compress the file
        //encodeAndCompress(bitOutputStream, huffmanTable);
    }




}
