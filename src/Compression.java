import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.io.OutputStream;

public class Compression {
    private String inputPath;
    private int numberOfBytesPerWord;
    private long fileSizeInBytes;

    public void compress(String inputPath, int numberOfBytesPerWord) {
        try {
            this.setInput(inputPath, numberOfBytesPerWord);
            this.compressFile();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // Don't forget to close streams
    private void setInput(String inputPath, int numberOfBytesPerWord) throws Exception {
        this.inputPath = inputPath;

        this.numberOfBytesPerWord = numberOfBytesPerWord;

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
        byte[] buffer = new byte[numberOfBytesPerWord];
        int bytesRead = 0;
        while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
            //put byte[] in frequency table ------------------------------- numberOfBytesPerWord instead of bytesRead in case of errors
            byte[] currentBuffer = Arrays.copyOf(buffer, bytesRead); // To enforce mutability of map keys
            frequencyTable.put(new ByteArrayWrapper(currentBuffer),
                    frequencyTable.getOrDefault(new ByteArrayWrapper(currentBuffer), 0L) + 1L);
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

    public PriorityQueue<Node> deepCopy(PriorityQueue<Node> queue) {
        PriorityQueue<Node> queueCopy = new PriorityQueue<>(Comparator.comparingLong(Node::getFrequency));
        for (Node node : queue) {
            queueCopy.add(new Node(node.getWord(), node.getFrequency(), node.getLeft(), node.getRight()));
        }
        return queueCopy;
    }

    // input : queue of word-frequency pairs
    private Node constructHuffmanTree(final PriorityQueue<Node> queue) {
        // to enforce mutability performs a deep copy of the queue
        PriorityQueue<Node> queueCopy = deepCopy(queue);

        while (queueCopy.size() > 1) {
            Node left = queueCopy.poll();
            Node right = queueCopy.poll();
            assert right != null; // Throws an Exception when the file is so small that fits in 1 or less byte[n]
            Node parent = new Node(null, left.getFrequency() + right.getFrequency(), left, right);
            queueCopy.add(parent);
        }
        return queueCopy.poll();
    }

    /*
     *  input : root of huffman tree
     *  output : huffman table in ArrayList form
     */
    private List<WordCodePair> constructHuffmanTable(Node root) {
        List<WordCodePair> huffmanTable = new ArrayList<>();
        buildHuffmanTable(huffmanTable, "", root);
        return huffmanTable;
    }

    private void buildHuffmanTable(List<WordCodePair> huffmanTable, String code, Node node) {
        if (node.getLeft() == null && node.getRight() == null) {
            huffmanTable.add(new WordCodePair(node.getWord(), code));
        }
        if (node.getLeft() != null) {
            buildHuffmanTable(huffmanTable, code + "0", node.getLeft());
        }
        if (node.getRight() != null) {
            buildHuffmanTable(huffmanTable, code + "1", node.getRight());
        }
    }

    // Don't forget to close streams
    private FileOutputStream storeHuffmanTable(List<WordCodePair> huffmanTable) throws IOException {
        // Generate output file path
        String inputFileName = this.inputPath.substring(this.inputPath.lastIndexOf(File.separatorChar) + 1);
        String outputFileName = "20011502" + "." + this.numberOfBytesPerWord + "." + inputFileName + "." + "hc";
        String outputPath = this.inputPath.substring(0, this.inputPath.lastIndexOf('/') + 1) + outputFileName;
        // Save the arrayList in the output file
        FileOutputStream fos = new FileOutputStream(outputPath);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(huffmanTable);
        // Flush the ObjectOutputStream to ensure all buffered data is sent to the OS to write into the file
        oos.flush();
        return fos;
    }
    private String binarySearch(List<WordCodePair> huffmanTable, ByteArrayWrapper word) {
        huffmanTable.sort((o1, o2) -> Arrays.compare(o1.getWord(), o2.getWord()));
        int wordCodeIndex = Collections.binarySearch(huffmanTable, new WordCodePair(word.getBuffer(), ""),
                (o1, o2) -> Arrays.compare(o1.getWord(), o2.getWord()));
        assert wordCodeIndex >= 0;
        return huffmanTable.get(wordCodeIndex).getCode();
    }
    private void encodeAndCompress(FileOutputStream fos, List<WordCodePair> huffmanTable) throws FileNotFoundException {
        BufferedInputStream bufferedInputStream = createInputStream();
        try {
            //read byte[numberOfBytesPerWord] from input
            byte[] buffer = new byte[numberOfBytesPerWord];
            int bytesRead = 0;
            long ByteCountWithoutLastByte = (this.fileSizeInBytes / this.numberOfBytesPerWord) * this.numberOfBytesPerWord;
            long currentByteCount = 0;
            // Set bitOutputStream for bit manipulation
            //BitOutputStream bos = new BitOutputStream(fos);
            while ((bytesRead = bufferedInputStream.read(buffer)) != -1) { // bytesRead in the last byte group may be n or less
                currentByteCount += bytesRead;
                // Find the code of the current word
                String code = this.binarySearch(huffmanTable, new ByteArrayWrapper(buffer));
                // Write the code to the output file
                for(char c : code.toCharArray()){
                    fos.write(c);
                }
                if (currentByteCount >= ByteCountWithoutLastByte) break;
            }
            if (currentByteCount >= ByteCountWithoutLastByte) {

            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void closeStreams(FileOutputStream fos) {
        try {
            fos.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void compressFile() throws IOException {
        // Collect statistics
        Map<ByteArrayWrapper, Long> frequencyTable = constructFrequencyMap();
        // Construct huffman tree
        PriorityQueue<Node> queue = convertFrequencyTableMapToPriorityQueue(frequencyTable);
        Node root = constructHuffmanTree(queue);
        // Extract huffman table
        List<WordCodePair> huffmanTable = constructHuffmanTable(root);
        // Write huffman table to file
        FileOutputStream fos = storeHuffmanTable(huffmanTable);
        // Encode and compress the file
        encodeAndCompress(fos, huffmanTable);
        // Close the open streams
        closeStreams(fos);
    }


}
