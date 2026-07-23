package com.company.whiteglovefitness.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.streams.ElementRequestHandler;
import io.jmix.awsfs.AwsFileStorage;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageLocator;
import io.jmix.localfs.LocalFileStorage;
import org.apache.catalina.connector.ClientAbortException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

@Tag("video")
public class VideoPlayer extends Component implements HasSize, HasStyle {

    public VideoPlayer() {
        setControls(true);
        setPreload("metadata");
        setPlaysInline(true);

        getStyle().set("display", "block");
        getStyle().set("position", "relative");
        getStyle().set("z-index", "1");
    }

    public VideoPlayer(String source) {
        this();
        setSource(source);
    }

    public void setSource(String source) {
        if (source == null || source.isBlank()) {
            clearSource();
        } else {
            getElement().setAttribute("src", source);
            reload();
        }
    }

    private void setSource(ElementRequestHandler source) {
        if (source == null) {
            clearSource();
        } else {
            getElement().setAttribute("src", source);
            reload();
        }
    }

    public void setFileRef(FileRef fileRef, FileStorageLocator fileStorageLocator) {
        if (fileRef == null) {
            clearSource();
            return;
        }

        String fileName = fileRef.getFileName();
        if (fileName == null || fileName.isBlank()) {
            clearSource();
            return;
        }

        FileStorage fileStorage = getFileStorage(fileRef, fileStorageLocator);
        setSource(new VideoRequestHandler(fileStorage, fileRef, fileName, resolveContentType(fileRef, fileName)));
    }

    public String getSource() {
        return getElement().getAttribute("src");
    }

    public void setControls(boolean controls) {
        getElement().setProperty("controls", controls);
    }

    public void setAutoplay(boolean autoplay) {
        getElement().setProperty("autoplay", autoplay);
    }

    public void setMuted(boolean muted) {
        getElement().setProperty("muted", muted);
    }

    public void setLoop(boolean loop) {
        getElement().setProperty("loop", loop);
    }

    public void setPlaysInline(boolean playsInline) {
        getElement().setProperty("playsInline", playsInline);

        if (playsInline) {
            getElement().setAttribute("playsinline", "");
        } else {
            getElement().removeAttribute("playsinline");
        }
    }

    public void setPreload(String preload) {
        getElement().setAttribute("preload", preload);
    }

    public void setPoster(String posterUrl) {
        if (posterUrl == null || posterUrl.isBlank()) {
            getElement().removeAttribute("poster");
        } else {
            getElement().setAttribute("poster", posterUrl);
        }
    }

    public void play() {
        getElement().callJsFunction("play");
    }

    public void pause() {
        getElement().callJsFunction("pause");
    }

    public void clearSource() {
        getElement().removeAttribute("src");
        reload();
    }

    private void reload() {
        // Tells the browser to reload after changing src.
        getElement().callJsFunction("load");
    }

    private FileStorage getFileStorage(FileRef fileRef, FileStorageLocator fileStorageLocator) {
        String storageName = fileRef.getStorageName();
        return storageName == null || storageName.isBlank()
                ? fileStorageLocator.getDefault()
                : fileStorageLocator.getByName(storageName);
    }

    private String resolveContentType(FileRef fileRef, String fileName) {
        String contentType = fileRef.getContentType();
        if (contentType != null && !contentType.isBlank()) {
            return contentType;
        }

        String normalizedFileName = fileName.toLowerCase(Locale.ROOT);
        if (normalizedFileName.endsWith(".webm")) {
            return "video/webm";
        }
        if (normalizedFileName.endsWith(".ogv") || normalizedFileName.endsWith(".ogg")) {
            return "video/ogg";
        }
        if (normalizedFileName.endsWith(".mov")) {
            return "video/quicktime";
        }
        return "video/mp4";
    }

    private static final class VideoRequestHandler implements ElementRequestHandler {

        private static final int BUFFER_SIZE = 64 * 1024;

        private final FileStorage fileStorage;
        private final FileRef fileRef;
        private final String fileName;
        private final String contentType;
        private final long contentLength;

        private VideoRequestHandler(FileStorage fileStorage, FileRef fileRef, String fileName, String contentType) {
            this.fileStorage = fileStorage;
            this.fileRef = fileRef;
            this.fileName = fileName;
            this.contentType = contentType;
            this.contentLength = resolveContentLength(fileStorage, fileRef);
        }

        @Override
        public void handleRequest(VaadinRequest request, VaadinResponse response, VaadinSession session, Element owner)
                throws IOException {
            try {
                String rangeHeader = request.getHeader("Range");
                if (rangeHeader == null || rangeHeader.isBlank()) {
                    writeFullResponse(response);
                    return;
                }

                ByteRange range = parseRange(rangeHeader, contentLength);
                if (range == null) {
                    response.setStatus(416);
                    response.setHeader("Content-Range", "bytes */" + contentLength);
                    response.setHeader("Accept-Ranges", "bytes");
                    return;
                }

                writePartialResponse(response, range);
            } catch (ClientAbortException e) {
                // Browsers routinely cancel older video range requests after seeks or reloads.
            }
        }

        @Override
        public String getUrlPostfix() {
            return fileName;
        }

        private void writeFullResponse(VaadinResponse response) throws IOException {
            response.setStatus(200);
            setCommonHeaders(response);
            response.setContentLengthLong(contentLength);

            try (InputStream inputStream = fileStorage.openStream(fileRef);
                 OutputStream outputStream = response.getOutputStream()) {
                inputStream.transferTo(outputStream);
            }
        }

        private void writePartialResponse(VaadinResponse response, ByteRange range) throws IOException {
            long rangeLength = range.end() - range.start() + 1;

            response.setStatus(206);
            setCommonHeaders(response);
            response.setHeader("Content-Range", "bytes " + range.start() + "-" + range.end() + "/" + contentLength);
            response.setContentLengthLong(rangeLength);

            try (OutputStream outputStream = response.getOutputStream()) {
                writePartialContent(outputStream, range, rangeLength);
            }
        }

        private void writePartialContent(OutputStream outputStream, ByteRange range, long rangeLength)
                throws IOException {
            if (fileStorage instanceof LocalFileStorage localFileStorage) {
                writeLocalPartialContent(localFileStorage, outputStream, range, rangeLength);
                return;
            }
            if (fileStorage instanceof AwsFileStorage awsFileStorage) {
                writeAwsPartialContent(awsFileStorage, outputStream, range, rangeLength);
                return;
            }

            throw new IOException("Partial video responses are not supported for "
                    + fileStorage.getClass().getName());
        }

        private void writeLocalPartialContent(LocalFileStorage fileStorage, OutputStream outputStream,
                                              ByteRange range, long rangeLength) throws IOException {
            Path path;
            try {
                path = resolveLocalPath(fileStorage, fileRef);
            } catch (ReflectiveOperationException e) {
                throw new IOException("Unable to open local video range stream", e);
            }

            try (FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ);
                 InputStream inputStream = Channels.newInputStream(fileChannel)) {
                fileChannel.position(range.start());
                copy(inputStream, outputStream, rangeLength);
            }
        }

        private void writeAwsPartialContent(AwsFileStorage fileStorage, OutputStream outputStream,
                                            ByteRange range, long rangeLength) throws IOException {
            try (InputStream inputStream = openAwsRangeStream(fileStorage, fileRef, range)) {
                copy(inputStream, outputStream, rangeLength);
            }
        }

        private void setCommonHeaders(VaadinResponse response) {
            response.setContentType(contentType);
            response.setHeader("Accept-Ranges", "bytes");
            response.setHeader("Content-Disposition", "inline; filename=\"" + fileName.replace("\"", "\\\"") + "\"");
        }

        private static long resolveContentLength(FileStorage fileStorage, FileRef fileRef) {
            if (fileStorage instanceof LocalFileStorage localFileStorage) {
                return resolveLocalContentLength(localFileStorage, fileRef);
            }
            if (fileStorage instanceof AwsFileStorage awsFileStorage) {
                return resolveAwsContentLength(awsFileStorage, fileRef);
            }
            throw new IllegalStateException("Unable to resolve video file length without reading file stream for "
                    + fileStorage.getClass().getName());
        }

        private static long resolveLocalContentLength(LocalFileStorage fileStorage, FileRef fileRef) {
            try {
                return Files.size(resolveLocalPath(fileStorage, fileRef));
            } catch (ReflectiveOperationException | IOException e) {
                throw new IllegalStateException("Unable to resolve local video file length", e);
            }
        }

        private static Path resolveLocalPath(LocalFileStorage fileStorage, FileRef fileRef)
                throws ReflectiveOperationException {
            Path relativePath = getLocalRelativePath(fileStorage, fileRef);
            Path[] storageRoots = getLocalStorageRoots(fileStorage);
            Method isInvalidPathMethod =
                    LocalFileStorage.class.getDeclaredMethod("isInvalidPath", Path.class, Path.class);
            isInvalidPathMethod.setAccessible(true);

            Path resolvedPath = null;
            for (Path root : storageRoots) {
                Path path = root.resolve(relativePath);
                boolean invalidPath = (boolean) isInvalidPathMethod.invoke(fileStorage, path, root);
                if (!invalidPath && Files.exists(path)) {
                    resolvedPath = path;
                }
            }

            if (resolvedPath != null) {
                return resolvedPath;
            }
            throw new IllegalStateException("Unable to resolve local video file path");
        }

        private static Path getLocalRelativePath(LocalFileStorage fileStorage, FileRef fileRef)
                throws ReflectiveOperationException {
            Method getRelativePathMethod = LocalFileStorage.class.getDeclaredMethod("getRelativePath", String.class);
            getRelativePathMethod.setAccessible(true);
            return (Path) getRelativePathMethod.invoke(fileStorage, fileRef.getPath());
        }

        private static Path[] getLocalStorageRoots(LocalFileStorage fileStorage) throws ReflectiveOperationException {
            Method getStorageRootsMethod = LocalFileStorage.class.getDeclaredMethod("getStorageRoots");
            getStorageRootsMethod.setAccessible(true);
            return (Path[]) getStorageRootsMethod.invoke(fileStorage);
        }

        private static long resolveAwsContentLength(AwsFileStorage fileStorage, FileRef fileRef) {
            try {
                Object s3Client = getAwsS3Client(fileStorage);
                String bucket = getAwsBucket(fileStorage);
                Object headObjectRequest = buildS3ObjectRequest(
                        "software.amazon.awssdk.services.s3.model.HeadObjectRequest",
                        bucket,
                        fileRef.getPath(),
                        null);
                Class<?> s3ClientClass = Class.forName("software.amazon.awssdk.services.s3.S3Client");
                Class<?> headObjectRequestClass =
                        Class.forName("software.amazon.awssdk.services.s3.model.HeadObjectRequest");
                Object headObjectResponse =
                        s3ClientClass.getMethod("headObject", headObjectRequestClass).invoke(s3Client, headObjectRequest);
                Object contentLength = headObjectResponse.getClass().getMethod("contentLength").invoke(headObjectResponse);

                if (contentLength instanceof Long length) {
                    return length;
                }
                throw new IllegalStateException("Unable to resolve S3 video file length");
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Unable to resolve S3 video file length", e);
            }
        }

        private static InputStream openAwsRangeStream(AwsFileStorage fileStorage, FileRef fileRef, ByteRange range)
                throws IOException {
            try {
                Object s3Client = getAwsS3Client(fileStorage);
                String bucket = getAwsBucket(fileStorage);
                Object getObjectRequest = buildS3ObjectRequest(
                        "software.amazon.awssdk.services.s3.model.GetObjectRequest",
                        bucket,
                        fileRef.getPath(),
                        "bytes=" + range.start() + "-" + range.end());
                Class<?> s3ClientClass = Class.forName("software.amazon.awssdk.services.s3.S3Client");
                Class<?> getObjectRequestClass =
                        Class.forName("software.amazon.awssdk.services.s3.model.GetObjectRequest");
                Object responseStream = s3ClientClass.getMethod("getObject", getObjectRequestClass)
                        .invoke(s3Client, getObjectRequest);

                if (responseStream instanceof InputStream inputStream) {
                    return inputStream;
                }
                throw new IllegalStateException("Unable to open S3 video range stream");
            } catch (ReflectiveOperationException e) {
                throw new IOException("Unable to open S3 video range stream", e);
            }
        }

        private static Object getAwsS3Client(AwsFileStorage fileStorage) throws ReflectiveOperationException {
            AtomicReference<?> s3ClientReference =
                    (AtomicReference<?>) getFieldValue(fileStorage, AwsFileStorage.class, "s3ClientReference");
            Object s3Client = s3ClientReference.get();
            if (s3Client != null) {
                return s3Client;
            }
            throw new IllegalStateException("S3 file storage client is not initialized");
        }

        private static String getAwsBucket(AwsFileStorage fileStorage) throws ReflectiveOperationException {
            String bucket = (String) getFieldValue(fileStorage, AwsFileStorage.class, "bucket");
            if (bucket == null || bucket.isBlank()) {
                throw new IllegalStateException("S3 file storage bucket is not configured");
            }
            return bucket;
        }

        private static Object buildS3ObjectRequest(String requestClassName, String bucket, String key,
                                                   String range) throws ReflectiveOperationException {
            Class<?> requestClass = Class.forName(requestClassName);
            Class<?> builderClass = Class.forName(requestClassName + "$Builder");
            Object builder = requestClass.getMethod("builder").invoke(null);
            builderClass.getMethod("bucket", String.class).invoke(builder, bucket);
            builderClass.getMethod("key", String.class).invoke(builder, key);
            if (range != null) {
                builderClass.getMethod("range", String.class).invoke(builder, range);
            }
            return builderClass.getMethod("build").invoke(builder);
        }

        private static Object getFieldValue(Object target, Class<?> targetType, String fieldName)
                throws ReflectiveOperationException {
            Field field = targetType.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        }

        private static ByteRange parseRange(String rangeHeader, long contentLength) {
            if (!rangeHeader.startsWith("bytes=") || contentLength < 1) {
                return null;
            }

            String rangeSpec = rangeHeader.substring("bytes=".length()).trim();
            int commaIndex = rangeSpec.indexOf(',');
            if (commaIndex >= 0) {
                rangeSpec = rangeSpec.substring(0, commaIndex).trim();
            }

            int dashIndex = rangeSpec.indexOf('-');
            if (dashIndex < 0) {
                return null;
            }

            String startValue = rangeSpec.substring(0, dashIndex).trim();
            String endValue = rangeSpec.substring(dashIndex + 1).trim();

            try {
                long start;
                long end;
                if (startValue.isEmpty()) {
                    long suffixLength = Long.parseLong(endValue);
                    if (suffixLength < 1) {
                        return null;
                    }
                    start = Math.max(contentLength - suffixLength, 0);
                    end = contentLength - 1;
                } else {
                    start = Long.parseLong(startValue);
                    end = endValue.isEmpty() ? contentLength - 1 : Long.parseLong(endValue);
                }

                if (start < 0 || end < start || start >= contentLength) {
                    return null;
                }

                return new ByteRange(start, Math.min(end, contentLength - 1));
            } catch (NumberFormatException e) {
                return null;
            }
        }

        private static void copy(InputStream inputStream, OutputStream outputStream, long bytesToCopy) throws IOException {
            byte[] buffer = new byte[BUFFER_SIZE];
            long remaining = bytesToCopy;
            while (remaining > 0) {
                int read = inputStream.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                if (read < 0) {
                    break;
                }
                outputStream.write(buffer, 0, read);
                remaining -= read;
            }
        }

        private record ByteRange(long start, long end) {
        }
    }
}
