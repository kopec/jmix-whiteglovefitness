package com.company.whiteglovefitness.component;

import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.streams.DownloadEvent;
import com.vaadin.flow.server.streams.DownloadHandler;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageLocator;
import org.apache.catalina.connector.ClientAbortException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

class FileRefResourcesTest {

    @Test
    void thumbnailResourceStreamsResizedJpeg() throws IOException {
        FileRef fileRef = FileRef.create("fs", "test/image.png", "image.png");
        byte[] sourceImage = createPng(200, 100);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        FileStorage fileStorage = Mockito.mock(FileStorage.class);
        FileStorageLocator fileStorageLocator = Mockito.mock(FileStorageLocator.class);
        VaadinResponse response = Mockito.mock(VaadinResponse.class);

        Mockito.when(fileStorageLocator.getByName("fs")).thenReturn(fileStorage);
        Mockito.when(fileStorage.openStream(fileRef)).thenReturn(new ByteArrayInputStream(sourceImage));
        Mockito.when(response.getOutputStream()).thenReturn(outputStream);

        DownloadHandler handler = FileRefResources.thumbnailResource(fileRef, fileStorageLocator, 64, 48);
        DownloadEvent event = new DownloadEvent(null, response, null, null);

        handler.handleDownloadRequest(event);

        BufferedImage thumbnail = ImageIO.read(new ByteArrayInputStream(outputStream.toByteArray()));
        Assertions.assertNotNull(thumbnail);
        Assertions.assertEquals(64, thumbnail.getWidth());
        Assertions.assertEquals(32, thumbnail.getHeight());
        Mockito.verify(response).setContentType("image/jpeg");
    }

    @Test
    void thumbnailResourceFallsBackToOriginalWhenImageCannotBeDecoded() throws IOException {
        FileRef fileRef = FileRef.create("fs", "test/image.heic", "image.heic");
        byte[] sourceImage = "original-image-data".getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        FileStorage fileStorage = Mockito.mock(FileStorage.class);
        FileStorageLocator fileStorageLocator = Mockito.mock(FileStorageLocator.class);
        VaadinResponse response = Mockito.mock(VaadinResponse.class);

        Mockito.when(fileStorageLocator.getByName("fs")).thenReturn(fileStorage);
        Mockito.when(fileStorage.openStream(fileRef))
                .thenReturn(new ByteArrayInputStream(sourceImage), new ByteArrayInputStream(sourceImage));
        Mockito.when(response.getOutputStream()).thenReturn(outputStream);

        DownloadHandler handler = FileRefResources.thumbnailResource(fileRef, fileStorageLocator, 64, 48);
        DownloadEvent event = new DownloadEvent(null, response, null, null);

        handler.handleDownloadRequest(event);

        Assertions.assertArrayEquals(sourceImage, outputStream.toByteArray());
    }

    @Test
    void clientAbortWhileStreamingInlineResourceIsIgnored() throws IOException {
        FileRef fileRef = FileRef.create("fs", "test/image.png", "image.png");
        FileStorage fileStorage = Mockito.mock(FileStorage.class);
        FileStorageLocator fileStorageLocator = Mockito.mock(FileStorageLocator.class);
        VaadinResponse response = Mockito.mock(VaadinResponse.class);

        Mockito.when(fileStorageLocator.getByName("fs")).thenReturn(fileStorage);
        Mockito.when(fileStorage.openStream(fileRef))
                .thenReturn(new ByteArrayInputStream("image-data".getBytes(StandardCharsets.UTF_8)));
        Mockito.when(response.getOutputStream()).thenReturn(new ClientAbortOutputStream());

        DownloadHandler handler = FileRefResources.inlineResource(fileRef, fileStorageLocator);
        DownloadEvent event = new DownloadEvent(null, response, null, null);

        Assertions.assertDoesNotThrow(() -> handler.handleDownloadRequest(event));
    }

    private byte[] createPng(int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(Color.BLUE);
            graphics.fillRect(0, 0, width, height);
        } finally {
            graphics.dispose();
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Assertions.assertTrue(ImageIO.write(image, "png", outputStream));
        return outputStream.toByteArray();
    }

    private static final class ClientAbortOutputStream extends OutputStream {

        @Override
        public void write(int b) {
        }

        @Override
        public void write(byte[] b, int off, int len) throws ClientAbortException {
            throw new ClientAbortException("Broken pipe");
        }
    }
}
