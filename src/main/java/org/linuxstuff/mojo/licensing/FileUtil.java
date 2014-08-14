/*
 * #%L
 * License Maven Plugin
 *
 * $Id: FileUtil.java 14409 2011-08-10 15:30:41Z tchemit $
 * $HeadURL: http://svn.codehaus.org/mojo/tags/license-maven-plugin-1.0/src/main/java/org/codehaus/mojo/license/FileUtil.java $
 * %%
 * Copyright (C) 2010 - 2011 Codehaus
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
package org.linuxstuff.mojo.licensing;

import java.io.File;
import java.io.IOException;

/**
 * Some basic file io utilities
 * 
 * @author pgier
 * @author tchemit <chemit@codelutin.com>
 * @since 1.0
 */
public class FileUtil {

    /**
     * Creates the directory (and his parents) if necessary.
     * 
     * @param dir
     *            the directory to create if not exisiting
     * @return {@code true} if directory was created, {@code false} if was no
     *         need to create it
     * @throws IOException
     *             if could not create directory
     */
    public static boolean createDirectoryIfNecessary(File dir) throws IOException {
        if (!dir.exists()) {
            boolean b = dir.mkdirs();
            if (!b) {
                throw new IOException("Could not create directory " + dir);
            }
            return true;
        }
        return false;
    }

    public static boolean createNewFile(File file) throws IOException {
        createDirectoryIfNecessary(file.getParentFile());
        if (!file.exists()) {
            boolean b = file.createNewFile();
            if (!b) {
                throw new IOException("Could not create new file " + file);
            }
            return true;
        }
        return false;
    }

}
