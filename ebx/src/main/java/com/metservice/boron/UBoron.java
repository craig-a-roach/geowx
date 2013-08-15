/*
 * Copyright 2010 Meteorological Service of New Zealand Limited all rights reserved. No part of this work may be stored
 * in a retrievable system, transmitted or reproduced in any way without the prior written permission of the
 * Meteorological Service of New Zealand
 */
package com.metservice.boron;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import com.metservice.argon.ArgonText;
import com.metservice.argon.Ds;
import com.metservice.argon.Elapsed;
import com.metservice.argon.net.ArgonPlatform;

/**
 * @author roach
 */
class UBoron {

	public static final Comparator<File> FileByLastModified = new Comparator<File>() {

		@Override
		public int compare(File lhs, File rhs) {
			final long lhsD = lhs.lastModified();
			final long rhsD = rhs.lastModified();
			if (lhsD < rhsD) return -1;
			if (lhsD > rhsD) return +1;
			final String lhsN = lhs.getName();
			final String rhsN = rhs.getName();
			return lhsN.compareTo(rhsN);
		}
	};

	private static final String[] ZLINES = new String[0];
	private static final Pattern OSNAMEPATT_LINUX = Pattern.compile(".*linux.*");
	private static final Pattern OSNAMEPATT_WIN = Pattern.compile(".*windows.*");
	private static final Pattern OSVERPATT_5X = Pattern.compile("5[.][\\d]+");
	private static final Pattern OSVERPATT_6X = Pattern.compile("6[.][\\d]+");
	private static final Pattern OSVERPATT_7X = Pattern.compile("7[.][\\d]+");

	private static final String CsqCaller = "Contained by library caller";
	private static final int CH_CR = ArgonText.CH_ASCII_CR;
	private static final int CH_LF = ArgonText.CH_ASCII_LF;

	public static final byte[] CRLF = { CH_CR, CH_LF };
	public static final byte[] LF = { CH_LF };

	public static final String OSFAMILY_LINUX = "linux";
	public static final String OSFAMILY_WIN = "win";
	public static final String OSVER_LINUX = "linux";
	public static final String OSVER_WIN5x = "win5x";
	public static final String OSVER_WIN6x = "win6x";
	public static final String OSVER_WIN7x = "win7x";
	public static final String OSVER_WINxx = "winxx";

	private static ByteBuffer newEncoded(ISpaceProbe probe, Charset charset, String zTerm, List<String> zlLines)
			throws DiskException {
		if (probe == null) throw new IllegalArgumentException("object is null");
		if (charset == null) throw new IllegalArgumentException("object is null");
		if (zTerm == null) throw new IllegalArgumentException("object is null");
		if (zlLines == null) throw new IllegalArgumentException("object is null");
		final int lineCount = zlLines.size();
		final int estCharCount = lineCount * CBoron.AvgCharsPerTextLine;
		final StringBuilder sb = new StringBuilder(estCharCount);
		for (int i = 0; i < lineCount; i++) {
			sb.append(zlLines.get(i));
			sb.append(zTerm);
		}
		final CharBuffer cb = CharBuffer.wrap(sb);
		final CharsetEncoder encoder = charset.newEncoder();
		try {
			return encoder.encode(cb);
		} catch (final CharacterCodingException ex) {
			final Ds ds = Ds.triedTo("Encode text content", ex);
			ds.a("charset", charset);
			ds.a("content", sb);
			probe.failSoftware(ds);
			throw new DiskException();
		}
	}

	public static void close(ISpaceProbe oprobe, File file, FileInputStream ofis) {
		if (ofis != null) {
			try {
				ofis.close();
			} catch (final IOException exIO) {
				if (oprobe != null) {
					final Ds ds = Ds.triedTo("Close file input stream", exIO, "Potential resource leak");
					ds.a("file", file);
					oprobe.warnFile(ds, file);
				}
			}
		}
	}

	public static void close(ISpaceProbe oprobe, File file, FileOutputStream ofos) {
		if (ofos != null) {
			try {
				ofos.close();
			} catch (final IOException exIO) {
				if (oprobe != null) {
					final Ds ds = Ds.triedTo("Close file output stream", exIO, "Potential resource leak");
					ds.a("file", file);
					oprobe.warnFile(ds, file);
				}
			}
		}
	}

	public static String consume(BoronProductIterator biterator, Elapsed oTimeout)
			throws InterruptedException {
		if (biterator == null) throw new IllegalArgumentException("object is null");
		final String Attn = "!";
		final StringBuilder sb = new StringBuilder();
		try {
			while (biterator.hasNext()) {
				final IBoronProduct product = biterator.next(oTimeout);
				if (product instanceof BoronProductStreamLine) {
					final BoronProductStreamLine streamLine = (BoronProductStreamLine) product;
					sb.append(streamLine.zLine()).append("\n");
					continue;
				}
				if (product instanceof BoronProductCancellation) {
					sb.append(Attn).append(product).append("\n");
					continue;
				}
				if (product instanceof BoronProductManagementFailure) {
					sb.append(Attn).append(product).append("\n");
					continue;
				}
				if (product instanceof BoronProductInterpreterFailure) {
					sb.append(Attn).append(product).append("\n");
					continue;
				}
				if (product instanceof BoronProductExitCode) {
					final BoronExitCode exitCode = ((BoronProductExitCode) product).exitCode();
					if (exitCode.value() != 0) {
						sb.append(Attn).append(product).append("\n");
					}
					continue;
				}
			}
		} catch (final BoronApiException ex) {
			sb.append(diagnostic(ex)).append('\n');
		}
		return sb.toString();
	}

	public static FileInputStream createFileInputStream(File srcFile) {
		if (srcFile == null) throw new IllegalArgumentException("object is null");
		try {
			return new FileInputStream(srcFile);
		} catch (final FileNotFoundException exFNF) {
			return null;
		}
	}

	public static void destroyProcess(Process oprocess) {
		if (oprocess != null) {
			oprocess.destroy();
		}
	}

	public static BoronInterpreterId detectDiagnosticInterpreterId() {
		final String idOsVer = detectOsNameVersion();
		final boolean isWinOS = idOsVer.startsWith(OSFAMILY_WIN);
		return isWinOS ? BoronInterpreterId.IntrinsicWinCmd : BoronInterpreterId.IntrinsicBash;
	}

	public static byte[] detectLineTerminator() {
		final String zLineSeparator = ArgonPlatform.zccLineSeparator();
		if (zLineSeparator.length() == 0) return LF;
		return zLineSeparator.getBytes();
	}

	public static String detectOsNameVersion() {
		final String zlctwOsName = ArgonPlatform.qcctwOsName().toLowerCase();
		final String zlctwOsVersion = ArgonPlatform.qcctwOsVersion().toLowerCase();
		if (OSNAMEPATT_LINUX.matcher(zlctwOsName).matches()) return OSVER_LINUX;

		if (OSNAMEPATT_WIN.matcher(zlctwOsName).matches()) {
			if (OSVERPATT_5X.matcher(zlctwOsVersion).matches()) return OSVER_WIN5x;
			if (OSVERPATT_6X.matcher(zlctwOsVersion).matches()) return OSVER_WIN6x;
			if (OSVERPATT_7X.matcher(zlctwOsVersion).matches()) return OSVER_WIN7x;
			return OSVER_WINxx;
		}

		return OSVER_LINUX;
	}

	public static String diagnostic(BoronException ex) {
		return "!!" + ex.getMessage();
	}

	public static String[] loadText(ISpaceProbe probe, File srcFile, Charset charset)
			throws DiskException {
		if (probe == null) throw new IllegalArgumentException("object is null");
		if (srcFile == null) throw new IllegalArgumentException("object is null");
		if (charset == null) throw new IllegalArgumentException("object is null");
		final long bcFile = srcFile.length();
		if (bcFile > CBoron.MaxTextFileBufferBc) {
			final Ds ds = Ds.invalidBecause("Text file exceeds load limit", CsqCaller);
			ds.a("bcFile", bcFile);
			probe.failFile(ds, srcFile);
			throw new DiskException();
		}
		final int ibcFile = (int) bcFile;

		final ByteBuffer bufferFile = ByteBuffer.allocate(ibcFile);
		final FileInputStream ofis = createFileInputStream(srcFile);
		if (ofis == null) return null;
		final FileChannel fch = ofis.getChannel();
		try {
			while (bufferFile.hasRemaining()) {
				final int rc = fch.read(bufferFile);
				if (rc == -1) {
					final Ds ds = Ds.invalidBecause("Unexpected end-of-file", CsqCaller);
					ds.a("bcFile", bcFile);
					ds.a("bufferFile", bufferFile);
					probe.failFile(ds, srcFile);
					throw new DiskException();
				}
			}
			bufferFile.flip();
			final String zText = charset.decode(bufferFile).toString();
			final int charCount = zText.length();
			if (charCount == 0) return ZLINES;

			final int estLines = (charCount / CBoron.AvgCharsPerTextLine) + 1;
			final List<String> zlLines = new ArrayList<String>(estLines);
			int startPos = 0;
			int pos = 0;
			while (pos < charCount) {
				final char ch = zText.charAt(pos);
				if (ch == CH_CR) {
					zlLines.add(zText.substring(startPos, pos));
					pos += 2;
					startPos = pos;
				} else if (ch == CH_LF) {
					zlLines.add(zText.substring(startPos, pos));
					pos += 1;
					startPos = pos;
				} else {
					pos++;
				}
			}
			if (pos > startPos) {
				zlLines.add(zText.substring(startPos, pos));
			}
			final int lineCount = zlLines.size();
			return zlLines.toArray(new String[lineCount]);
		} catch (final IOException ex) {
			final Ds ds = Ds.triedTo("Read encoded text from file channel", ex);
			ds.a("buffer", bufferFile);
			probe.failFile(ds, srcFile);
			throw new DiskException();
		} finally {
			close(probe, srcFile, ofis);
		}
	}

	public static FileOutputStream newFileOutputStream(ISpaceProbe probe, File destFile)
			throws DiskException {
		if (probe == null) throw new IllegalArgumentException("object is null");
		if (destFile == null) throw new IllegalArgumentException("object is null");
		try {
			return new FileOutputStream(destFile, false);
		} catch (final FileNotFoundException ex) {
			final Ds ds = Ds.triedTo("Create output file", ex);
			probe.failFile(ds, destFile);
			throw new DiskException();
		}
	}

	public static void saveBinary(ISpaceProbe probe, File destFile, byte[] zptContent)
			throws DiskException {
		if (probe == null) throw new IllegalArgumentException("object is null");
		if (destFile == null) throw new IllegalArgumentException("object is null");
		if (zptContent == null) throw new IllegalArgumentException("array is null");
		final ByteBuffer bb = ByteBuffer.wrap(zptContent);
		final FileOutputStream fos = newFileOutputStream(probe, destFile);
		final FileChannel fch = fos.getChannel();
		try {
			while (bb.hasRemaining()) {
				fch.write(bb);
			}
		} catch (final IOException ex) {
			final Ds ds = Ds.triedTo("Write bytes to file channel", ex, CsqCaller);
			ds.a("buffer", bb);
			probe.failFile(ds, destFile);
			throw new DiskException();
		} finally {
			close(probe, destFile, fos);
		}
	}

	public static void saveText(ISpaceProbe probe, File destFile, Charset charset, String zTerm, List<String> zlLines)
			throws DiskException {
		if (probe == null) throw new IllegalArgumentException("object is null");
		if (destFile == null) throw new IllegalArgumentException("object is null");
		if (charset == null) throw new IllegalArgumentException("object is null");
		if (zTerm == null) throw new IllegalArgumentException("object is null");
		if (zlLines == null) throw new IllegalArgumentException("object is null");
		final ByteBuffer bb = newEncoded(probe, charset, zTerm, zlLines);
		final FileOutputStream fos = newFileOutputStream(probe, destFile);
		final FileChannel fch = fos.getChannel();
		try {
			while (bb.hasRemaining()) {
				fch.write(bb);
			}
		} catch (final IOException ex) {
			final Ds ds = Ds.triedTo("Write encoded text to file channel", ex);
			ds.a("buffer", bb);
			probe.failFile(ds, destFile);
			throw new DiskException();
		} finally {
			close(probe, destFile, fos);
		}
	}

	public static void unlock(ISpaceProbe oprobe, File file, FileLock ofileLock) {
		if (ofileLock != null) {
			try {
				ofileLock.release();
			} catch (final IOException exIO) {
				if (oprobe != null) {
					final Ds ds = Ds.triedTo("Unlock file", exIO, "Potential contention problems");
					ds.a("file", file);
					oprobe.warnFile(ds, file);
				}
			}
		}
	}
}
