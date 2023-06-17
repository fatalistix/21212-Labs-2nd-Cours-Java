package ru.nsu.balashov.torrent;

import java.nio.ByteBuffer;

public record NotCompleteDownloaded(String name, double downloadPercent, ByteBuffer infoHash) { }
