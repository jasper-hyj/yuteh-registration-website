package com.yuteh.register.file;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

public class FileUtil {

    public static void decompressGzip(Path source, HttpServletResponse response) throws IOException {
        try (GZIPInputStream gis = new GZIPInputStream(new FileInputStream(source.toFile()));
                ServletOutputStream stream = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gis.read(buffer)) > 0) {
                stream.write(buffer, 0, len);
            }
        }
    }
}
