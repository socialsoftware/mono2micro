package pt.ist.socialsoftware.edition.ldod.compressors;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import kieker.common.configuration.Configuration;
import kieker.common.util.filesystem.FSUtil;
import kieker.monitoring.writer.compression.ICompressionFilter;

/**
 * Zip compression filter for the writer pool.
 *
 * @author Reiner Jung
 *
 * @since 1.14
 */
public class ZipCompressor implements ICompressionFilter {

	/**
	 * Initialize ZipCompression with parameter to adhere Kieker configuration system.
	 *
	 * @param configuration
	 *            Kieker configuration object
	 */
	public ZipCompressor(final Configuration configuration) { // NOPMD block warning of unused configuration parameter
		// Empty constructor. No initialization necessary.
	}

	@Override
	public OutputStream chainOutputStream(final OutputStream outputStream, final Path fileName) throws IOException {
		final ZipOutputStream compressedOutputStream = new ZipOutputStream(outputStream);
		final ZipEntry newZipEntry = new ZipEntry(fileName.toString() + FSUtil.DAT_FILE_EXTENSION);
		compressedOutputStream.putNextEntry(newZipEntry);

		return compressedOutputStream;
	}

	@Override
	public String getExtension() {
		return FSUtil.ZIP_FILE_EXTENSION;
	}

}