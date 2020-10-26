package pl.waw.staszic.w.admin1.symphoniae_core.models

import java.io.File
import java.io.IOException
import java.lang.Exception
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipInputStream

private fun dirContains(dir : Path, other : Path) : Boolean {
    val dirStr = dir.normalize().toAbsolutePath().toString() + File.separator
    val otherStr = other.normalize().toAbsolutePath().toString()
    return otherStr.startsWith(dirStr)
}

fun download(downloadURL : URL, outputDir : Path, listener : ProgressListener) {
    try {
        val urlHash = Integer.toHexString(downloadURL.hashCode())
        var buffer = ByteArray(1024)
        Files.createDirectories(outputDir)
        val unzippedDir = Files.createTempDirectory(outputDir, "$urlHash-unzip")
        val zippedFile = outputDir.resolve("$urlHash.zip")
        if(!Files.exists(zippedFile)) {
            val connection = downloadURL.openConnection();
            listener.initProgress(connection.getContentLengthLong());
            try {
                Files.newOutputStream(zippedFile).use { zipOutput ->
                    connection.getInputStream().use { downloadInput ->
                        listener.onDownloadStart();
                        var progress = 0L
                        var readSize = downloadInput.read(buffer)
                        while (readSize > 0) {
                            if(Thread.interrupted()) throw InterruptedException()
                            zipOutput.write(buffer, 0, readSize)
                            progress += readSize
                            listener.updateProgress(progress)
                            readSize = downloadInput.read(buffer)
                        }
                    }
                }
            } catch (e: Exception) {
                if(e is IOException || e is InterruptedException) {
                    Files.delete(zippedFile)
                }
                throw e
            }
        }
        ZipInputStream(Files.newInputStream(zippedFile)).use { zipInput ->
            listener.onUnzipStart()
            var progress = 0L
            var entry = zipInput.getNextEntry()
            while (entry != null) {
                progress += entry.compressedSize
                listener.updateProgress(progress)
                val unzippedFile = unzippedDir.resolve(entry.name)
                if (!dirContains(
                        unzippedDir,
                        unzippedFile
                    )
                ) throw IOException("Illegal zipped file path.")
                if (entry.isDirectory()) Files.createDirectories(unzippedFile)
                else {
                    Files.newOutputStream(unzippedFile).use { unzippedOutput ->
                        var readSize = zipInput.read(buffer)
                        while (readSize > 0) {
                            if(Thread.interrupted()) throw InterruptedException()
                            unzippedOutput.write(buffer, 0, readSize)
                            readSize = zipInput.read(buffer)
                        }
                    }
                }
                entry = zipInput.getNextEntry()
            }
        }
        Files.newDirectoryStream(unzippedDir).use { dirStream ->
            for(path in dirStream) {
                Files.move(path, outputDir.resolve(unzippedDir.relativize(path)))
            }
        }
        Files.delete(zippedFile)
        if(Thread.interrupted()) throw InterruptedException()
        listener.onComplete()
    } catch(e : Exception) {
        listener.onException(e)
    }
}

interface ProgressListener {
    fun initProgress(totalSize : Long)
    fun updateProgress(progress : Long)
    fun onDownloadStart()
    fun onUnzipStart()
    fun onComplete()
    fun onException(exception : Exception)
}