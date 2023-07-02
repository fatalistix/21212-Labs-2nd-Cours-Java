import java.io.IOException;
import java.io.RandomAccessFile;

public class FileSeparatorFirstPart {
    public static void main(String[] args) throws IOException {
        RandomAccessFile rAccessFile = new RandomAccessFile("/home/vyacheslav/Documents/forTorrent/Original/EndeavourOS_Cassini_Nova-03-2023_R1.iso", "rw");
        rAccessFile.setLength(256 * 1024 * 4000);
    }
} 
