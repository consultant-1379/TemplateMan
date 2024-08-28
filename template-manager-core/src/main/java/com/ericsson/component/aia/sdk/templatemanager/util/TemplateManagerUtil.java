/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.component.aia.sdk.templatemanager.util;

import static com.ericsson.component.aia.sdk.templatemanager.TemplateManagerConstants.ZIP_FILE_EXTENSION;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.component.aia.sdk.pba.model.Pba;
import com.ericsson.component.aia.sdk.templatemanager.exception.TemplateManagerException;
import com.ericsson.component.aia.sdk.templatemanager.exception.code.TemplateManagerExceptionCodes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * This class provides utility methods for template manager
 *
 * @author echchik
 *
 */
public class TemplateManagerUtil {

    private static final String TAG_SEPARATOR = ":";
    private static final String TEMPLATE_TAG = "template";
    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateManagerUtil.class);
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final JsonParser jsonParser = new JsonParser();

    private TemplateManagerUtil() {

    }

    /**
     * Creates template ID based on PBA information.
     *
     * @param pba
     *            the pba file
     * @return the created ID
     */
    public static String createTemplateId(final Pba pba) {

        return new StringBuilder(TEMPLATE_TAG).append(TAG_SEPARATOR).append(pba.getAuthorInfo().getAuthor().toLowerCase().replaceAll("\\s+", ""))
                .append(TAG_SEPARATOR).append(pba.getTemplateInfo().getName().toLowerCase()).append(TAG_SEPARATOR)
                .append(pba.getTemplateInfo().getVersion().toLowerCase()).toString();
    }

    /**
     * This method will find the first instance of a file type within a path
     *
     * @param searchPath
     *            The path to search for files
     * @param fileExtension
     *            The file extension to search for.
     * @return Optional Path to the file if found.
     * @throws IOException
     *             Thrown if an exception occurs when searching through the specified path.
     */
    public static Optional<Path> findFileTypeInPath(final Path searchPath, final String fileExtension) throws IOException {
        try (final Stream<Path> repoStream = Files.walk(searchPath);) {
            return repoStream.filter(path -> (path.toString().toLowerCase().endsWith(fileExtension))).findFirst();
        }
    }

    /**
     * This method will replace the contents of pba.json file with pbaAsString contents in the ZIP file
     *
     * @param pbaAsString
     *            PBA information as ZIP file
     * @param zipFilePath
     *            ZIP file where the pba.json contents needs to be replaced.
     */
    public static void updatePbaInZipFile(final String pbaAsString, final Path zipFilePath) {
        LOGGER.debug("Updating pba file {} in path {}", pbaAsString, zipFilePath);
        try (FileSystem zipfs = FileSystems.newFileSystem(zipFilePath, TemplateManagerUtil.class.getClassLoader())) {
            final JsonElement jsonElement = jsonParser.parse(pbaAsString);
            final String prettyJsonString = gson.toJson(jsonElement);
            final Path pathInZipfile = zipfs.getPath("/pba.json");
            Files.copy(IOUtils.toInputStream(prettyJsonString), pathInZipfile, StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException exp) {
            throw new TemplateManagerException(TemplateManagerExceptionCodes.ERROR_UPDATING_PBA, exp);
        }
    }

    /**
     * This method will rename the zip file to the name provided as renameTo parameter.
     *
     * @param zipFileToRename
     *            to rename
     * @param renameTo
     *            name of the new zip file.
     * @return path to the renamed zip file.
     */
    public static Path renameZipFile(final Path zipFileToRename, final String renameTo) {
        try {
            LOGGER.debug("renameZipFile from {} to {}", zipFileToRename, renameTo);
            final Path newZipFilePath = zipFileToRename.getParent().resolve(renameTo);
            Files.copy(zipFileToRename, newZipFilePath, StandardCopyOption.REPLACE_EXISTING);
            return newZipFilePath;
        } catch (final IOException exp) {
            throw new TemplateManagerException(TemplateManagerExceptionCodes.ERROR_UPDATING_PBA, exp);
        }
    }

    /**
     * Check the application zip file for the presence of a README.md, if this file is present it will replace the existing README.md within the
     * applications Git repository with the new README.md.
     *
     * @param applicationPath
     *            The path to the application zip file.
     * @param gitRepo
     *            The path to the applications Git Repository.
     */
    public static void updateTemplateReadMe(final Path applicationPath, final Path gitRepo) {
        try (final FileSystem zipfs = FileSystems.newFileSystem(applicationPath, TemplateManagerUtil.class.getClassLoader())) {

            Optional<Path> readMe = Optional.empty();
            for (final Path root : zipfs.getRootDirectories()) {
                readMe = findFileInPath("README.md", root);
                if (readMe.isPresent()) {
                    Files.copy(readMe.get(), gitRepo.resolve("README.md"), StandardCopyOption.REPLACE_EXISTING);
                    break;
                }
            }

        } catch (final IOException exp) {
            throw new TemplateManagerException(TemplateManagerExceptionCodes.ERROR_EXTRACTING_APPLICATION_DATA,
                    "Exception occurred, when trying to extract README.md file from application being published", exp);
        }
    }

    /**
     * Gets the zip file name.
     *
     * @param name
     *            the name
     * @param version
     *            the version
     * @return the zip file name
     */
    public static String getZipFileName(final String name, final String version) {
        return new StringBuilder(name).append("-").append(version).append(".").append(ZIP_FILE_EXTENSION).toString();
    }

    private static Optional<Path> findFileInPath(final String fileName, final Path root) throws IOException {
        try (final Stream<Path> repoStream = Files.walk(root);) {
            return repoStream.filter(path -> (path.getFileName() != null && fileName.equalsIgnoreCase(path.getFileName().toString()))).findFirst();

        }
    }
}
