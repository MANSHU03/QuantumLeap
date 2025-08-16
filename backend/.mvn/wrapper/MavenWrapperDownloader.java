/*
 * Copyright 2007-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.*;
import java.net.URL;
import java.net.URI;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Properties;

public class MavenWrapperDownloader {

    private static final String WRAPPER_VERSION = "3.2.0";
    private static final String DEFAULT_DOWNLOAD_URL = "https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/" + WRAPPER_VERSION + "/maven-wrapper-" + WRAPPER_VERSION + ".jar";

    public static void main(String args[]) {
        try {
            System.out.println("- Downloader started");
            File wrapperJar = wrapperJar();
            if (wrapperJar.exists()) {
                System.out.println("- Downloader skipped everything, already exists");
                return;
            }

            File wrapperDir = wrapperJar.getParentFile();
            if (!wrapperDir.exists()) {
                wrapperDir.mkdirs();
            }

            if (args.length > 0) {
                System.out.println("- Downloading from: " + args[0]);
                downloadFileFromURL(args[0], wrapperJar);
                System.out.println("Done");
            } else {
                File propertiesFile = new File(".mvn/wrapper/maven-wrapper.properties");
                if (propertiesFile.exists()) {
                    FileInputStream mavenWrapperPropertyFileInputStream = null;
                    try {
                        mavenWrapperPropertyFileInputStream = new FileInputStream(propertiesFile);
                        Properties mavenWrapperProperties = new Properties();
                        mavenWrapperProperties.load(mavenWrapperPropertyFileInputStream);
                        System.out.println("- Downloading from: " + mavenWrapperProperties.getProperty("wrapperUrl", "https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/" + WRAPPER_VERSION + "/maven-wrapper-" + WRAPPER_VERSION + ".jar"));
                        downloadFileFromURL(mavenWrapperProperties.getProperty("wrapperUrl", "https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/" + WRAPPER_VERSION + "/maven-wrapper-" + WRAPPER_VERSION + ".jar"), wrapperJar);
                    } catch (IOException e) {
                        System.out.println("- Error loading " + propertiesFile.getAbsolutePath());
                    } finally {
                        if (mavenWrapperPropertyFileInputStream != null) {
                            try {
                                mavenWrapperPropertyFileInputStream.close();
                            } catch (IOException e) {
                                // Ignore close exception
                            }
                        }
                    }
                } else {
                    System.out.println("- Downloading from: " + DEFAULT_DOWNLOAD_URL);
                    downloadFileFromURL(DEFAULT_DOWNLOAD_URL, wrapperJar);
                }
                System.out.println("Done");
            }
        } catch (Exception e) {
            System.out.println("- Error downloading");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void downloadFileFromURL(String urlString, File destination) throws Exception {
        if (urlString.startsWith("file:")) {
            File file = new File(urlString.substring("file:".length()));
            if (file.exists()) {
                FileInputStream fis = null;
                FileOutputStream fos = null;
                try {
                    fis = new FileInputStream(file);
                    fos = new FileOutputStream(destination);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) != -1) {
                        fos.write(buffer, 0, length);
                    }
                } finally {
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                            // Ignore close exception
                        }
                    }
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            // Ignore close exception
                        }
                    }
                }
            }
        } else {
            URL website = new URL(urlString);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(destination);
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        // Ignore close exception
                    }
                }
            }
        }
    }

    private static File wrapperJar() {
        return new File(".mvn/wrapper/maven-wrapper.jar");
    }
}
