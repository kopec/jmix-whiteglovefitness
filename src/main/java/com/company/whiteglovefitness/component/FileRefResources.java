package com.company.whiteglovefitness.component;

import io.jmix.core.FileRef;
import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageLocator;

import com.vaadin.flow.server.streams.DownloadHandler;
import org.apache.catalina.connector.ClientAbortException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public final class FileRefResources {

    private FileRefResources() {
    }

    public static DownloadHandler inlineResource(FileRef fileRef, FileStorageLocator fileStorageLocator) {
        return createResource(fileRef, fileStorageLocator, true);
    }

    public static DownloadHandler downloadResource(FileRef fileRef, FileStorageLocator fileStorageLocator) {
        return createResource(fileRef, fileStorageLocator, false);
    }

    private static DownloadHandler createResource(FileRef fileRef, FileStorageLocator fileStorageLocator,
                                                  boolean inline) {
        String fileName = resolveFileName(fileRef);
        FileStorage fileStorage = getFileStorage(fileRef, fileStorageLocator);

        return event -> {
            if (inline) {
                event.inline(fileName);
            } else {
                event.setFileName(fileName);
            }
            event.setContentType(resolveContentType(fileRef, fileName));

            try (InputStream inputStream = fileStorage.openStream(fileRef)) {
                inputStream.transferTo(event.getOutputStream());
            } catch (ClientAbortException e) {
                // Browsers can cancel image/file requests when a card is re-rendered or navigation changes.
            } catch (IOException e) {
                throw new IllegalStateException("Unable to stream file " + fileName, e);
            }
        };
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
