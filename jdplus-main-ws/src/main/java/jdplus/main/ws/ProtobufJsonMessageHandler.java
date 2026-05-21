package jdplus.main.ws;

import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Provider
public final class ProtobufJsonMessageHandler implements MessageBodyReader<Message>, MessageBodyWriter<Message> {

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return Message.class.isAssignableFrom(type);
    }

    @Override
    public Message readFrom(Class<Message> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        try {
            Message.Builder result = (Message.Builder) type.getMethod("newBuilder").invoke(null);
            JsonFormat.parser().merge(new InputStreamReader(entityStream, charsetFromMediaType(mediaType)), result);
            return result.build();
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return Message.class.isAssignableFrom(type);
    }

    @Override
    public void writeTo(Message message, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        try (Writer writer = new OutputStreamWriter(entityStream, charsetFromMediaType(mediaType))) {
            JsonFormat.printer().appendTo(message, writer);
        }
    }

    private static Charset charsetFromMediaType(MediaType mediaType) {
        return Optional.ofNullable(mediaType)
                .map(o -> o.getParameters().get(MediaType.CHARSET_PARAMETER))
                .map(Charset::forName)
                .orElse(StandardCharsets.UTF_8);
    }
}
