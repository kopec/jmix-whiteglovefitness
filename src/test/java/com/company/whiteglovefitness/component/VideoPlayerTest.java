package com.company.whiteglovefitness.component;

import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinSession;
import io.jmix.awsfs.AwsFileStorage;
import io.jmix.core.FileRef;
import io.jmix.localfs.LocalFileStorage;
import org.apache.catalina.connector.ClientAbortException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

class VideoPlayerTest {

    @TempDir
    private Path tempDir;

    @Test
    void localPartialResponseReadsRequestedBytesWithoutOpeningStorageStream() throws Exception {
        Path filePath = tempDir.resolve("2026/07/22/video.mp4");
        Files.createDirectories(filePath.getParent());
        Files.writeString(filePath, "0123456789", StandardCharsets.UTF_8);

        FailingOpenStreamLocalFileStorage fileStorage =
                new FailingOpenStreamLocalFileStorage("fs", tempDir.toString());
        FileRef fileRef = FileRef.create("fs", "2026/07/22/video.mp4", "video.mp4");
        ByteArrayOutputStream responseBody = new ByteArrayOutputStream();

        handleRangeRequest(fileStorage, fileRef, "bytes=2-5", responseBody);

        Assertions.assertFalse(fileStorage.openStreamCalled);
        Assertions.assertEquals("2345", responseBody.toString(StandardCharsets.UTF_8));
    }

    @Test
    void s3PartialResponseRequestsOnlyRequestedRangeFromS3() throws Exception {
        AwsFileStorage fileStorage = new AwsFileStorage();
        AtomicReference<String> requestedRange = new AtomicReference<>();
        setField(fileStorage, AwsFileStorage.class, "bucket", "test-bucket");
        setAwsS3Client(fileStorage, createS3ClientProxy(requestedRange));

        FileRef fileRef = FileRef.create("s3", "2026/07/22/video.mp4", "video.mp4");
        ByteArrayOutputStream responseBody = new ByteArrayOutputStream();

        handleRangeRequest(fileStorage, fileRef, "bytes=2-5", responseBody);

        Assertions.assertEquals("bytes=2-5", requestedRange.get());
        Assertions.assertEquals("2345", responseBody.toString(StandardCharsets.UTF_8));
    }

    @Test
    void clientAbortDuringPartialResponseIsIgnored() throws Exception {
        Path filePath = tempDir.resolve("2026/07/22/video.mp4");
        Files.createDirectories(filePath.getParent());
        Files.writeString(filePath, "0123456789", StandardCharsets.UTF_8);

        FailingOpenStreamLocalFileStorage fileStorage =
                new FailingOpenStreamLocalFileStorage("fs", tempDir.toString());
        FileRef fileRef = FileRef.create("fs", "2026/07/22/video.mp4", "video.mp4");
        Object handler = createVideoRequestHandler(fileStorage, fileRef);
        VaadinRequest request = Mockito.mock(VaadinRequest.class);
        VaadinResponse response = Mockito.mock(VaadinResponse.class);

        Mockito.when(request.getHeader("Range")).thenReturn("bytes=2-5");
        Mockito.when(response.getOutputStream()).thenReturn(new ClientAbortOutputStream());

        Method handleRequestMethod = handler.getClass().getDeclaredMethod("handleRequest",
                VaadinRequest.class, VaadinResponse.class, VaadinSession.class, Element.class);
        handleRequestMethod.setAccessible(true);
        Assertions.assertDoesNotThrow(() -> handleRequestMethod.invoke(handler, request, response, null, null));
    }

    private static void handleRangeRequest(Object fileStorage, FileRef fileRef, String rangeHeader,
                                           OutputStream responseBody) throws Exception {
        Object handler = createVideoRequestHandler(fileStorage, fileRef);
        VaadinRequest request = Mockito.mock(VaadinRequest.class);
        VaadinResponse response = Mockito.mock(VaadinResponse.class);

        Mockito.when(request.getHeader("Range")).thenReturn(rangeHeader);
        Mockito.when(response.getOutputStream()).thenReturn(responseBody);

        Method handleRequestMethod = handler.getClass().getDeclaredMethod("handleRequest",
                VaadinRequest.class, VaadinResponse.class, VaadinSession.class, Element.class);
        handleRequestMethod.setAccessible(true);
        handleRequestMethod.invoke(handler, request, response, null, null);

        Mockito.verify(response).setStatus(206);
        Mockito.verify(response).setHeader("Accept-Ranges", "bytes");
        Mockito.verify(response).setHeader("Content-Range", "bytes 2-5/10");
        Mockito.verify(response).setContentLengthLong(4);
    }

    private static Object createVideoRequestHandler(Object fileStorage, FileRef fileRef) throws Exception {
        Class<?> handlerClass = Class.forName(VideoPlayer.class.getName() + "$VideoRequestHandler");
        Constructor<?> constructor = handlerClass.getDeclaredConstructor(
                io.jmix.core.FileStorage.class, FileRef.class, String.class, String.class);
        constructor.setAccessible(true);
        return constructor.newInstance(fileStorage, fileRef, "video.mp4", "video/mp4");
    }

    private static Object createS3ClientProxy(AtomicReference<String> requestedRange) throws Exception {
        Class<?> s3ClientClass = Class.forName("software.amazon.awssdk.services.s3.S3Client");
        return Proxy.newProxyInstance(s3ClientClass.getClassLoader(), new Class<?>[]{s3ClientClass},
                (proxy, method, args) -> {
                    if (Object.class.equals(method.getDeclaringClass())) {
                        return handleObjectMethod(proxy, method, args);
                    }
                    if ("headObject".equals(method.getName())) {
                        return buildSdkObject("software.amazon.awssdk.services.s3.model.HeadObjectResponse", 10L);
                    }
                    if ("getObject".equals(method.getName())) {
                        requestedRange.set((String) args[0].getClass().getMethod("range").invoke(args[0]));
                        Object getObjectResponse =
                                buildSdkObject("software.amazon.awssdk.services.s3.model.GetObjectResponse", null);
                        return createResponseInputStream(getObjectResponse, "2345");
                    }
                    if ("close".equals(method.getName())) {
                        return null;
                    }
                    throw new UnsupportedOperationException(method.toString());
                });
    }

    private static Object handleObjectMethod(Object proxy, Method method, Object[] args) {
        return switch (method.getName()) {
            case "equals" -> proxy == args[0];
            case "hashCode" -> System.identityHashCode(proxy);
            case "toString" -> "s3ClientProxy";
            default -> throw new UnsupportedOperationException(method.toString());
        };
    }

    private static Object buildSdkObject(String className, Long contentLength) throws Exception {
        Class<?> objectClass = Class.forName(className);
        Class<?> builderClass = Class.forName(className + "$Builder");
        Object builder = objectClass.getMethod("builder").invoke(null);
        if (contentLength != null) {
            builderClass.getMethod("contentLength", Long.class).invoke(builder, contentLength);
        }
        return builderClass.getMethod("build").invoke(builder);
    }

    private static Object createResponseInputStream(Object response, String body) throws Exception {
        Class<?> responseInputStreamClass = Class.forName("software.amazon.awssdk.core.ResponseInputStream");
        return responseInputStreamClass
                .getConstructor(Object.class, InputStream.class)
                .newInstance(response, new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
    }

    private static void setAwsS3Client(AwsFileStorage fileStorage, Object s3Client) throws Exception {
        @SuppressWarnings("unchecked")
        AtomicReference<Object> s3ClientReference =
                (AtomicReference<Object>) getField(fileStorage, AwsFileStorage.class, "s3ClientReference");
        s3ClientReference.set(s3Client);
    }

    private static Object getField(Object target, Class<?> targetClass, String fieldName) throws Exception {
        Field field = targetClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    private static void setField(Object target, Class<?> targetClass, String fieldName, Object value) throws Exception {
        Field field = targetClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static final class FailingOpenStreamLocalFileStorage extends LocalFileStorage {

        private boolean openStreamCalled;

        private FailingOpenStreamLocalFileStorage(String storageName, String storageDir) {
            super(storageName, storageDir);
        }

        @Override
        public InputStream openStream(FileRef reference) {
            openStreamCalled = true;
            throw new AssertionError("Partial local video responses should not open the storage stream");
        }
    }

    private static final class ClientAbortOutputStream extends OutputStream {

        @Override
        public void write(int b) {
        }

        @Override
        public void write(byte[] b, int off, int len) throws ClientAbortException {
            throw new ClientAbortException("Connection reset by peer");
        }
    }
}
