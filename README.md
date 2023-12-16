# Huffman-compression-decompression
Implemented Huffman's algorithm. The implementation allow compressing and decompressing arbitrary files, collecting statistics from the input file first, 
then applying the compression algorithm, storing a representation of the codewords in the compressed file, so that you can decompress the file back. 
The program should have the capability of considering more than one byte. For example, instead of just collecting the frequencies and finding codewords for single bytes. 
The same can be done assuming the basic unit is n bytes, where n is an integer.

## How to Run

In case of compression
```javascript
java -jar huffman_20011502.jar c absolute_path_to_input_file n 
```
where n : the number of bytes per word


In case of decompression
```java
java -jar huffman_20011502.jar d absolute_path_to_input_file
```
