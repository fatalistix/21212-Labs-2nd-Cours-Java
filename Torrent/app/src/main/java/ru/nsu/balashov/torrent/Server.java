package ru.nsu.balashov.torrent;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardProtocolFamily;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final Selector selector;
    private final static int DEFAULT_LISTENING_PORT = 6969;
    private final static int DEFAULT_BUFFER_SIZE = 2 * 1024 * 1024;
    private final ByteBuffer sharedBuffer;

    public Server() throws IOException {
        this.selector = Selector.open();
        sharedBuffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
    }

    public Server(int byteBufferSize) throws IOException {
        this.selector = Selector.open();
        sharedBuffer = ByteBuffer.allocate(byteBufferSize);
    }

//    public void upload(SavedFilesManager savedFilesManager) {
//        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open(StandardProtocolFamily.INET)) {
//            serverSocketChannel.configureBlocking(false);
//            InetSocketAddress inetSocketAddress = new InetSocketAddress(DEFAULT_LISTENING_PORT);
//            serverSocketChannel.bind(inetSocketAddress);
//            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
//
//            while (true) {
//                int count = selector.select();
//                if (count == 0) {
//                    continue;
//                }
//                for (SelectionKey key : selector.selectedKeys()) {
//                    if (key.isAcceptable()) {
//                        try (SocketChannel socketChannel = ((ServerSocketChannel) key.channel()).accept()) {
//                            socketChannel.configureBlocking(false);
//                            socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
//                        } catch (IOException e) {
//                            //? Do nothing
//                        }
//                    }
//                    if (key.isReadable() && key.isWritable()) {
//                        try (SocketChannel socketChannel = (SocketChannel) key.channel()) {
//                            socketChannel.read()
//                        } catch (IOException e) {
//                            key.cancel();
//                        }
//                    }
//                }
//            }
//        }
//        } catch (IOException e) {
//            for (SelectionKey key : selector.keys()) {
//                key.channel().close();
//            }
//        }
//    }
}
