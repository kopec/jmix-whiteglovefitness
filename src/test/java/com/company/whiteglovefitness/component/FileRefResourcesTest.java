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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

class FileRefResourcesTest {

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
