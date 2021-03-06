/** *****************************************************************************
 * Copyright 2018 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************** */
package com.ddoriya.sigar.utill;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Files;
import java.util.logging.Logger;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarLoader;

/**
 *
 * @author jun
 */
public class SigarUtil {

    private static final Logger LOGGER = Logger.getLogger(SigarUtil.class.getName());

    // Note: this is required to load the sigar native lib.
    static {
        SigarLoader loader = new SigarLoader(Sigar.class);
        try {
            String libName = loader.getLibraryName();

            final URL url = SigarUtil.class.getResource("/" + libName);
            if (url != null) {
                final File tmpDir = Files.createTempDirectory("sigar").toFile();
                tmpDir.deleteOnExit();
                final File nativeLibTmpFile = new File(tmpDir, libName);
                nativeLibTmpFile.deleteOnExit();
                try (InputStream openStream = url.openStream()) {
                    try (OutputStream output = new FileOutputStream(nativeLibTmpFile)) {
                        byte[] buffer = new byte[32 * 1024];
                        int readed;
                        while ((readed = openStream.read(buffer)) != -1) {
                            output.write(buffer, 0, readed);
                        }
                        output.flush();
                    }
                }

                System.setProperty("java.library.path", System.getProperty("java.library.path") + File.pathSeparatorChar + nativeLibTmpFile.getParent());

                Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
                fieldSysPath.setAccessible(true);
                fieldSysPath.set(null, null);

                loader.load(nativeLibTmpFile.getParent());

            } else {
                LOGGER.info("No native libs found in jar, letting the normal load mechanisms figger it out.");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Sigar getSigar() {
        return InstanceHolder.INSTANCE;
    }

    public static int getCpuCount() throws SigarException {
        return InstanceHolder.INSTANCE.getCpuList().length;
    }
    
    public static long getCurrentPid() throws SigarException {
        return InstanceHolder.INSTANCE.getPid();
    }

    private static class InstanceHolder {

        private static final Sigar INSTANCE = new Sigar();
    }
}
