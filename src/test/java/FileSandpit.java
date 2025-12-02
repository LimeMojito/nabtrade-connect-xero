/*
 * Copyright 2011-2025 Lime Mojito Pty Ltd
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.assertj.core.util.Files;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * A small utility to create and manage a temporary sandbox directory ("sandpit") for tests.
 * <p>
 * The sandpit is created as a real temporary directory on the filesystem and is scheduled for
 * deletion on JVM exit. Test resources can be copied from the classpath into the sandpit while
 * preserving their relative paths, enabling tests to work with realistic file structures without
 * writing into the project tree. The directory can be explicitly cleared, and it is also cleaned
 * up automatically when this instance is closed (it implements {@link AutoCloseable}).
 * </p>
 */
@Slf4j
public class FileSandpit implements AutoCloseable {

    private final File sandpitDir;

    /**
     * Creates a new sandpit by creating a fresh temporary directory and scheduling it for
     * deletion when the JVM exits.
     */
    @SneakyThrows
    public FileSandpit() {
        sandpitDir = Files.newTemporaryFolder();
        FileUtils.forceDeleteOnExit(sandpitDir);
    }

    /**
     * Copies the given classpath resources into the sandpit, preserving their relative paths,
     * and returns the sandpit directory for convenience.
     * <p>
     * Each path is resolved via {@code getResourceAsStream(path)} relative to this class. If any
     * resource cannot be found on the classpath, the operation will fail.
     * </p>
     *
     * @param resourcePaths one or more classpath resource paths (e.g. {@code "/data/file.csv"})
     * @return the sandpit directory containing the copied resources
     * @throws IOException if an I/O error occurs while copying resources
     */
    public File prepareWithResources(String... resourcePaths) throws IOException {
        log.info("Preparing sandpit for {}", List.of(resourcePaths));
        for (String resourcePath : resourcePaths) {
            resourceToSandpit(resourcePath);
        }
        return sandpitDir;
    }

    /**
     * Deletes the sandpit directory and its contents, ignoring any errors.
     */
    public void clear() {
        log.info("Clearing sandpit");
        FileUtils.deleteQuietly(sandpitDir);
    }

    /**
     * Closes this sandpit by clearing the underlying temporary directory.
     * Equivalent to calling {@link #clear()}.
     */
    @Override
    public void close() {
        clear();
    }

    /**
     * Loads the specified classpath resource as UTF-8 text and returns its contents as a list of lines.
     * <p>
     * The resource is resolved via {@code getResourceAsStream(resourcePath)} relative to this class.
     * The method fails if the resource does not exist on the classpath.
     * </p>
     *
     * @param resourcePath the classpath resource path (e.g. {@code "/data/file.csv"})
     * @return the resource contents as a list of lines (UTF-8)
     * @throws IOException if an I/O error occurs while reading the resource
     */
    public List<String> loadResourceAsTextLines(String resourcePath) throws IOException {
        log.info("Reading {} from classpath as text lines", resourcePath);
        try (InputStream input = getClass().getResourceAsStream(resourcePath)) {
            assertThat(input).withFailMessage("Resource %s not on classpath", resourcePath).isNotNull();
            return IOUtils.readLines(input, UTF_8);
        }
    }

    /**
     * Reads a file from the sandpit directory as UTF-8 text and returns its contents as a list of lines.
     *
     * @param filePathInSandpit the path to the file inside the sandpit (relative to the sandpit root)
     * @return the file contents as a list of lines (UTF-8)
     * @throws IOException if an I/O error occurs while reading the file
     */
    public List<String> loadFileContentsAsTextLines(String filePathInSandpit) throws IOException {
        log.info("Reading {} from sandpit as text lines", filePathInSandpit);
        return FileUtils.readLines(new File(sandpitDir, filePathInSandpit), UTF_8);
    }

    private void resourceToSandpit(String resourcePath) throws IOException {
        try (InputStream input = getClass().getResourceAsStream(resourcePath)) {
            assertThat(input).withFailMessage("Resource %s not on classpath", resourcePath).isNotNull();
            File destination = new File(sandpitDir, resourcePath);
            FileUtils.forceMkdir(destination.getParentFile());
            FileUtils.copyInputStreamToFile(input, destination);
        }
    }

}
