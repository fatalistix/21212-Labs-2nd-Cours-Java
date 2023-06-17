package ru.nsu.balashov.torrent;

import ru.nsu.balashov.torrent.TorrentBBMessagesParser.Server.PieceData;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardProtocolFamily;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;




public class Server {
    private final Selector selector;
    private final static int DEFAULT_LISTENING_PORT = 6967;
    private final static int DEFAULT_BUFFER_SIZE = 2 * 1024 * 1024;
    private int byteBufferSize = DEFAULT_BUFFER_SIZE;
    private int listeningPort = DEFAULT_LISTENING_PORT;

    public Server() throws IOException {
        this.selector = Selector.open();
    }

    public Server(int byteBufferSize) throws IOException {
        this.selector = Selector.open();
        this.byteBufferSize = byteBufferSize;
    }

    public Server(int byteBufferSize, short portNumber) throws IOException {
        this.selector = Selector.open();
        this.byteBufferSize = byteBufferSize;
        this.listeningPort = portNumber;
    }

    public void startUploading(SavedFilesManager savedFilesManager) throws IOException {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open(StandardProtocolFamily.INET)) {
            serverSocketChannel.configureBlocking(false);
            InetSocketAddress inetSocketAddress = new InetSocketAddress(listeningPort);
            serverSocketChannel.bind(inetSocketAddress);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                int count = selector.select();
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                if (count == 0) {
                    continue;
                }
                Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    iter.remove();
                    if (key.isAcceptable()) {
                        SocketChannel socketChannel = ((ServerSocketChannel) key.channel()).accept();
                        try {
                            socketChannel.configureBlocking(false);
                            ByteBuffer channelByteBuffer = ByteBuffer.allocate(byteBufferSize);
                            socketChannel.register(selector, SelectionKey.OP_READ, channelByteBuffer);
                            TorrentBBMessagesParser.writeHandshake(channelByteBuffer);
                            socketChannel.write(channelByteBuffer);
                            System.out.println("WROTE HANDSHAKE");
                        } catch (IOException ignore) {
                            //? Do nothing because if channel closed it shouldn't be in selector
                        }
                    }
                    if (key.isReadable()) {
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        ByteBuffer byteBuffer = (ByteBuffer) key.attachment();
                        byteBuffer.clear();
                        try {
                            int readBytes = TorrentBBMessagesParser.readAllMessageBytes(byteBuffer, socketChannel);
                            if (readBytes == -1) {
                                key.cancel();
                                socketChannel.close();
                                break;
                            }
                            switch (TorrentBBMessagesParser.readMessageType(byteBuffer)) {
                                case REQUEST_AVAILABLE -> {
                                    ByteBuffer infoHash = TorrentBBMessagesParser.Server.readRequestAvailable(byteBuffer);
                                    byte[] bitmask = savedFilesManager.getExistingParts(infoHash);
                                    if (bitmask == null) {
                                        TorrentBBMessagesParser.writeUnknown(byteBuffer);
                                    } else {
                                        TorrentBBMessagesParser.Server.writeResponseAvailable(byteBuffer, infoHash, bitmask);
                                    }
                                    socketChannel.write(byteBuffer);
                                }
                                case REQUEST_PIECE -> {
                                    PieceData pieceData = TorrentBBMessagesParser.Server.readRequestPiece(byteBuffer);
                                    byte[] piece = savedFilesManager.getPiece(pieceData.infoHash(), pieceData.index());

                                    if (piece == null) {
                                        TorrentBBMessagesParser.writeUnknown(byteBuffer);
                                    } else {
                                        TorrentBBMessagesParser.Server.writeResponsePiece(byteBuffer, pieceData.infoHash(), piece, pieceData.index());
                                    }
                                    socketChannel.write(byteBuffer);
                                }
                                case REQUEST_CANCEL -> {
                                    socketChannel.close();
                                    key.cancel();
                                }
                                case UNKNOWN -> {}
                            }
                        } catch (IOException e) {
                            System.out.println("KEY CANCELED");
                            key.cancel();
                            socketChannel.close();
                        }
                    }
                }
            }
        } catch (InterruptedException ignore) {
        }
    }

    public void kill() {
        try {
            for (SelectionKey key : selector.keys()) {
                key.channel().close();
            }
            selector.close();
        } catch (IOException ignored) {
        }
    }
}
