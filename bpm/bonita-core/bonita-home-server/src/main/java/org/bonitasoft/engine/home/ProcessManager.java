/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/

package org.bonitasoft.engine.home;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.io.IOUtil;

/**
 * @author Baptiste Mesta
 */
public class ProcessManager {


    private final BonitaHomeServer bonitaHomeServer;

    public ProcessManager(BonitaHomeServer bonitaHomeServer) {

        this.bonitaHomeServer = bonitaHomeServer;
    }

    public Map<String, byte[]> getProcessClasspath(final long tenantId, final long processId) throws BonitaHomeNotSetException, IOException {
        final Map<String, byte[]> resources = new HashMap<>();
        final Folder processClasspathFolder = getProcessClasspathFolder(tenantId, processId);
        final File[] listFiles = processClasspathFolder.listFiles();
        for (final File jarFile : listFiles) {
            final String name = jarFile.getName();
            final byte[] jarContent = FileUtils.readFileToByteArray(jarFile);
            resources.put(name, jarContent);
        }
        return resources;
    }
    private Folder getProcessClasspathFolder(final long tenantId, final long processId) throws BonitaHomeNotSetException, IOException {
        return FolderMgr.getTenantWorkProcessClasspathFolder(getBonitaHomeFolder(), tenantId, processId);
    }

    private File getBonitaHomeFolder() throws BonitaHomeNotSetException {
        return bonitaHomeServer.getBonitaHomeFolder();
    }

    public void storeClasspathFile(long tenantId, final long processId, String resourceName, byte[] resourceContent) throws BonitaHomeNotSetException,
            IOException {
        final File newFile = getProcessClasspathFolder(tenantId, processId).getFile(resourceName);
        IOUtil.write(newFile, resourceContent);
    }

    public void deleteClasspathFiles(long tenantId, long processId, String... resourceNames) throws BonitaHomeNotSetException, IOException {
        if (resourceNames != null) {
            final Folder classpathFolder = getProcessClasspathFolder(tenantId, processId);
            for (String resourceName : resourceNames) {
                classpathFolder.deleteFile(resourceName);
            }
        }
    }

    public Map<String, byte[]> getConnectorFiles(long tenantId, long processId) throws BonitaHomeNotSetException, IOException {
        return getConnectorsFolder(tenantId, processId).listFilesAsResources();
    }

    private Folder getConnectorsFolder(long tenantId, long processId) throws BonitaHomeNotSetException, IOException {
        return FolderMgr.getTenantWorkProcessConnectorsFolder(getBonitaHomeFolder(), tenantId, processId);
    }

    public void deleteConnectorFile(long tenantId, long processId, String fileName) throws BonitaHomeNotSetException, IOException {
        final Folder connectorsFolder = getConnectorsFolder(tenantId, processId);
        connectorsFolder.deleteFile(fileName);
    }
    public void storeConnectorFile(long tenantId, final long processId, String resourceName, byte[] resourceContent) throws BonitaHomeNotSetException,
            IOException {
        final File newFile = getConnectorsFolder(tenantId, processId).getFile(resourceName);
        IOUtil.write(newFile, resourceContent);
    }


    Folder getProcessFolder(long tenantId, long processId) throws BonitaHomeNotSetException, IOException {
        return FolderMgr.getTenantWorkProcessFolder(getBonitaHomeFolder(), tenantId, processId);
    }

    public List<BonitaResource> getUserFiltersFiles(long tenantId, long processId) throws BonitaHomeNotSetException, IOException {
        final Map<String, byte[]> resourceMap = getUserFiltersFolder(tenantId, processId).listFilesAsResources();
        final List<BonitaResource> bonitaResources = new ArrayList<>(resourceMap.size());
        for (Map.Entry<String, byte[]> resource : resourceMap.entrySet()) {
            bonitaResources.add(new BonitaResource(resource.getKey(), resource.getValue()));
        }
        return bonitaResources;
    }

    private Folder getUserFiltersFolder(long tenantId, long processId) throws BonitaHomeNotSetException, IOException {
        return FolderMgr.getTenantWorkProcessUserFiltersFolder(getBonitaHomeFolder(), tenantId, processId);
    }
    public byte[] getInitialProcessDocument(long tenantId, long processId, String documentName) throws BonitaHomeNotSetException, IOException {
        final Folder documentsFolder = FolderMgr.getTenantWorkProcessDocumentFolder(getBonitaHomeFolder(), tenantId, processId);
        return FileUtils.readFileToByteArray(documentsFolder.getFile(documentName));
    }

    public byte[] exportBarProcessContentUnderHome(long tenantId, long processId, final String actorMappingContent) throws IOException,
            BonitaHomeNotSetException {
        final FileOutputStream actorMappingOS = getProcessDefinitionFileOutputstream(tenantId, processId, "actorMapping.xml");
        IOUtil.writeContentToFileOutputStream(actorMappingContent, actorMappingOS);
        final Folder processFolder = getProcessFolder(tenantId, processId);
        return processFolder.zip(processFolder);
    }

    public void writeBusinessArchive(long tenantId, long processId, BusinessArchive businessArchive) throws BonitaHomeNotSetException, IOException {
        final Folder processFolder = getProcessFolder(tenantId, processId);
        processFolder.create();
        processFolder.writeBusinessArchive(businessArchive);
    }


    private FileOutputStream getProcessDefinitionFileOutputstream(long tenantId, long processId, String fileName) throws BonitaHomeNotSetException, IOException {
        final Folder processFolder = getProcessFolder(tenantId, processId);
        final File file = processFolder.getFile(fileName);
        return new FileOutputStream(file);
    }


    public void deleteProcess(long tenantId, long processId) throws BonitaHomeNotSetException, IOException {
        FolderMgr.deleteTenantWorkProcessFolder(getBonitaHomeFolder(), tenantId, processId);
        FolderMgr.deleteTenantTempProcessFolder(getBonitaHomeFolder(), tenantId, processId);
    }

    public Map<String, byte[]> getProcessResources(long tenantId, long processId, String filenamesPattern) throws BonitaHomeNotSetException, IOException {
        final Folder processFolder = getProcessFolder(tenantId, processId);
        return processFolder.getResources(filenamesPattern);
    }


}
