package com.company.whiteglovefitness.component;

import io.jmix.core.FileRef;
import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageLocator;

import com.vaadin.flow.server.streams.DownloadEvent;
import com.vaadin.flow.server.streams.DownloadHandler;
import org.apache.catalina.connector.ClientAbortException;

import javax.imageio.IIOImage;
import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Locale;

public final class FileRefResources {

    private static final int DEFAULT_THUMBNAIL_MAX_WIDTH = 480;
    private static final int DEFAULT_THUMBNAIL_MAX_HEIGHT = 360;
    private static final float THUMBNAIL_JPEG_QUALITY = 0.82f;

    private FileRefResources() {
    }

    public static DownloadHandler inlineResource(FileRef fileRef, FileStorageLocator fileStorageLocator) {
        return createResource(fileRef, fileStorageLocator, true);
    }

    public static DownloadHandler downloadResource(FileRef fileRef, FileStorageLocator fileStorageLocator) {
        return createResource(fileRef, fileStorageLocator, false);
    }

    public static DownloadHandler thumbnailResource(FileRef fileRef, FileStorageLocator fileStorageLocator) {
        return thumbnailResource(fileRef, fileStorageLocator, DEFAULT_THUMBNAIL_MAX_WIDTH, DEFAULT_THUMBNAIL_MAX_HEIGHT);
    }

    public static DownloadHandler thumbnailResource(FileRef fileRef, FileStorageLocator fileStorageLocator,
                                                    int maxWidth, int maxHeight) {
        if (maxWidth < 1 || maxHeight < 1) {
            throw new IllegalArgumentException("Thumbnail dimensions must be positive");
        }

        String fileName = resolveFileName(fileRef);
        FileStorage fileStorage = getFileStorage(fileRef, fileStorageLocator);

        return event -> {
            try {
                byte[] thumbnail = createThumbnail(fileStorage, fileRef, maxWidth, maxHeight);
                if (thumbnail != null) {
                    event.inline(resolveThumbnailFileName(fileName));
                    event.setContentType("image/jpeg");
                    event.getOutputStream().write(thumbnail);
                    return;
                }

                streamOriginal(event, fileStorage, fileRef, fileName, true);
            } catch (ClientAbortException e) {
                // Browsers can cancel image/file requests when a card is re-rendered or navigation changes.
            } catch (IOException e) {
                throw new IllegalStateException("Unable to stream thumbnail for file " + fileName, e);
            }
        };
    }

    private static DownloadHandler createResource(FileRef fileRef, FileStorageLocator fileStorageLocator,
                                                  boolean inline) {
        String fileName = resolveFileName(fileRef);
        FileStorage fileStorage = getFileStorage(fileRef, fileStorageLocator);

        return event -> {
            try {
                streamOriginal(event, fileStorage, fileRef, fileName, inline);
            } catch (ClientAbortException e) {
                // Browsers can cancel image/file requests when a card is re-rendered or navigation changes.
            } catch (IOException e) {
                throw new IllegalStateException("Unable to stream file " + fileName, e);
            }
        };
    }

    private static void streamOriginal(DownloadEvent event, FileStorage fileStorage, FileRef fileRef, String fileName,
                                       boolean inline) throws IOException {
        if (inline) {
            event.inline(fileName);
        } else {
            event.setFileName(fileName);
        }
        event.setContentType(resolveContentType(fileRef, fileName));

        try (InputStream inputStream = fileStorage.openStream(fileRef)) {
            inputStream.transferTo(event.getOutputStream());
        }
    }

    private static byte[] createThumbnail(FileStorage fileStorage, FileRef fileRef, int maxWidth, int maxHeight)
            throws IOException {
        try (InputStream inputStream = fileStorage.openStream(fileRef)) {
            BufferedImage sourceImage;
            try {
                sourceImage = readSourceImage(inputStream, maxWidth, maxHeight);
            } catch (IIOException e) {
                return null;
            }
            if (sourceImage == null) {
                return null;
            }

            BufferedImage thumbnailImage = scaleToFit(sourceImage, maxWidth, maxHeight);
            return writeJpeg(thumbnailImage);
        }
    }

    private static BufferedImage readSourceImage(InputStream inputStream, int maxWidth, int maxHeight)
            throws IOException {
        try (ImageInputStream imageInputStream = new MemoryCacheImageInputStream(inputStream)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInputStream);
            if (!readers.hasNext()) {
                return null;
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(imageInputStream, true, true);
                ImageReadParam readParam = reader.getDefaultReadParam();
                int subsampling = calculateSubsampling(reader.getWidth(0), reader.getHeight(0), maxWidth, maxHeight);
                if (subsampling > 1) {
                    readParam.setSourceSubsampling(subsampling, subsampling, 0, 0);
                }
                return reader.read(0, readParam);
            } finally {
                reader.dispose();
            }
        }
    }

    private static int calculateSubsampling(int sourceWidth, int sourceHeight, int maxWidth, int maxHeight) {
        return Math.max(1, Math.min(sourceWidth / maxWidth, sourceHeight / maxHeight));
    }

    private static BufferedImage scaleToFit(BufferedImage sourceImage, int maxWidth, int maxHeight) {
        double scale = Math.min(1d, Math.min(
                (double) maxWidth / sourceImage.getWidth(),
                (double) maxHeight / sourceImage.getHeight()));
        int thumbnailWidth = Math.max(1, (int) Math.round(sourceImage.getWidth() * scale));
        int thumbnailHeight = Math.max(1, (int) Math.round(sourceImage.getHeight() * scale));

        BufferedImage thumbnailImage = new BufferedImage(thumbnailWidth, thumbnailHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = thumbnailImage.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, thumbnailWidth, thumbnailHeight);
            graphics.drawImage(sourceImage, 0, 0, thumbnailWidth, thumbnailHeight, null);
        } finally {
            graphics.dispose();
        }

        return thumbnailImage;
    }

    private static byte[] writeJpeg(BufferedImage image) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            throw new IllegalStateException("No JPEG ImageIO writer is available");
        }

        ImageWriter writer = writers.next();
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ImageOutputStream imageOutputStream = new MemoryCacheImageOutputStream(outputStream)) {
            writer.setOutput(imageOutputStream);
            ImageWriteParam writeParam = writer.getDefaultWriteParam();
            if (writeParam.canWriteCompressed()) {
                writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                writeParam.setCompressionQuality(THUMBNAIL_JPEG_QUALITY);
            }
            writer.write(null, new IIOImage(image, null, null), writeParam);
            imageOutputStream.flush();
            return outputStream.toByteArray();
        } finally {
            writer.dispose();
        }
    }

    private static String resolveThumbnailFileName(String fileName) {
        int extensionStart = fileName.lastIndexOf('.');
        String baseName = extensionStart > 0 ? fileName.substring(0, extensionStart) : fileName;
        return baseName + "-thumbnail.jpg";
    }

    private static FileStorage getFileStorage(FileRef fileRef, FileStorageLocator fileStorageLocator) {
        String storageName = fileRef.getStorageName();
        return storageName == null || storageName.isBlank()
                ? fileStorageLocator.getDefault()
                : fileStorageLocator.getByName(storageName);
    }

    private static String resolveFileName(FileRef fileRef) {
        String fileName = fileRef.getFileName();
        return fileName == null || fileName.isBlank() ? "file" : fileName;
    }

    private static String resolveContentType(FileRef fileRef, String fileName) {
        String contentType = fileRef.getContentType();
        if (contentType != null && !contentType.isBlank()) {
            return contentType;
        }

        String normalizedFileName = fileName.toLowerCase(Locale.ROOT);
        if (normalizedFileName.endsWith(".pdf")) {
            return "application/pdf";
        }
        if (normalizedFileName.endsWith(".png")) {
            return "image/png";
        }
        if (normalizedFileName.endsWith(".gif")) {
            return "image/gif";
        }
        if (normalizedFileName.endsWith(".webp")) {
            return "image/webp";
        }
        if (normalizedFileName.endsWith(".jpg") || normalizedFileName.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (normalizedFileName.endsWith(".webm")) {
            return "video/webm";
        }
        if (normalizedFileName.endsWith(".ogv") || normalizedFileName.endsWith(".ogg")) {
            return "video/ogg";
        }
        if (normalizedFileName.endsWith(".mov")) {
            return "video/quicktime";
        }
        if (normalizedFileName.endsWith(".mp4") || normalizedFileName.endsWith(".m4v")) {
            return "video/mp4";
        }

        return "application/octet-stream";
    }
}
