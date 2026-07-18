package ir.ac.pvz.model.support;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class DataFileLocator {

    private DataFileLocator() {
    }

    public static InputStream open(String fileName) throws IOException {
        for (Path path : candidates(fileName)) {
            if (Files.isRegularFile(path)) {
                return Files.newInputStream(path);
            }
        }
        InputStream resource = DataFileLocator.class.getClassLoader()
                .getResourceAsStream("assets/Data/" + fileName);
        if (resource != null) {
            return resource;
        }
        throw new IOException("Data file not found: " + fileName);
    }

    public static boolean exists(String fileName) {
        try {
            InputStream input = open(fileName);
            input.close();
            return true;
        } catch (IOException exception) {
            return false;
        }
    }

    private static List<Path> candidates(String fileName) {
        List<Path> paths = new ArrayList<>();
        String configured = System.getProperty("pvz.data.dir");
        if (configured == null || configured.isBlank()) {
            configured = System.getenv("PVZ_DATA_DIR");
        }
        if (configured != null && !configured.isBlank()) {
            paths.add(Path.of(configured, fileName));
        }
        paths.add(Path.of("assets", "Data", fileName));
        paths.add(Path.of("pvz_src", "assets", "Data", fileName));
        paths.add(Path.of("src", "assets", "Data", fileName));
        return paths;
    }
}
